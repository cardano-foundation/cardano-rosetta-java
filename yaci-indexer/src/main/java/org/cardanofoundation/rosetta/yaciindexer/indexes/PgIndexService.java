package org.cardanofoundation.rosetta.yaciindexer.indexes;

import com.bloxbean.cardano.yaci.store.adminui.service.SyncStatusService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PostgreSQL implementation of the Rosetta index lifecycle service.
 *
 * <p>After blockchain sync reaches tip, this service automatically creates required
 * Rosetta indexes using {@code CREATE INDEX CONCURRENTLY}. It gates readiness on
 * index completion and exposes per-index lifecycle state via the actuator endpoint.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Uses raw JDBC connections with explicit {@code autoCommit=true} because
 *       {@code CREATE INDEX CONCURRENTLY} cannot run inside a transaction block.</li>
 *   <li>Uses a single-thread executor (instead of raw {@code Thread}) to integrate
 *       with Spring's lifecycle and allow graceful shutdown.</li>
 *   <li>Filters pg_index queries by {@code current_schema()} to avoid cross-schema matches.</li>
 *   <li>Treats {@code BUILDING} state (from a prior JVM crash) the same as {@code INVALID}:
 *       drop and rebuild.</li>
 * </ul>
 */
@Service
@Profile("!h2 & !test-integration")
@Slf4j
public class PgIndexService implements IndexService {

    private final JdbcTemplate jdbcTemplate;
    private final IndexCatalog indexConfig;
    private final SyncStatusService syncStatusService;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private String currentSchema;

    private final ExecutorService indexExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "RosettaIndexBuilder");
        t.setDaemon(true);
        return t;
    });

    private final AtomicReference<IndexLifecycleState> state = new AtomicReference<>(IndexLifecycleState.PENDING);
    private final Map<String, IndexItemStatus> itemStatusMap = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastProgressAt = new AtomicReference<>(null);

    private static final String PG_INDEX_STATE_SQL =
            "SELECT i.indisready, i.indisvalid FROM pg_index i " +
            "JOIN pg_class c ON c.oid = i.indexrelid " +
            "JOIN pg_namespace n ON n.oid = c.relnamespace " +
            "WHERE c.relname = ? AND n.nspname = current_schema()";

    public PgIndexService(JdbcTemplate jdbcTemplate,
                          IndexCatalog indexConfig,
                          SyncStatusService syncStatusService) {
        this.jdbcTemplate = jdbcTemplate;
        this.indexConfig = indexConfig;
        this.syncStatusService = syncStatusService;
    }

    @PostConstruct
    public void init() {
        String schema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        this.currentSchema = (schema != null) ? schema : "public";

        if (indexConfig.getDbIndexes() == null || indexConfig.getDbIndexes().isEmpty()) {
            state.set(IndexLifecycleState.READY);
            return;
        }

        boolean allReady = true;
        for (IndexCatalog.DbIndex dbIndex : indexConfig.getDbIndexes()) {
            IndexItemState itemState = queryIndexState(dbIndex.name());
            itemStatusMap.put(dbIndex.name(), new IndexItemStatus(dbIndex.name(), itemState, null));
            if (itemState != IndexItemState.READY) {
                allReady = false;
            }
        }

        if (allReady) {
            state.set(IndexLifecycleState.READY);
        } else {
            state.set(IndexLifecycleState.PENDING);
        }
    }

    @Override
    public IndexLifecycleState getState() {
        return state.get();
    }

    @Override
    public List<IndexItemStatus> getIndexStatus() {
        if (indexConfig.getDbIndexes() == null || indexConfig.getDbIndexes().isEmpty()) {
            return new ArrayList<>(itemStatusMap.values());
        }

        List<IndexItemStatus> liveStatuses = new ArrayList<>();
        for (IndexCatalog.DbIndex dbIndex : indexConfig.getDbIndexes()) {
            IndexItemState liveState = queryIndexState(dbIndex.name());
            // Preserve error messages from the in-memory map
            IndexItemStatus cached = itemStatusMap.get(dbIndex.name());
            String errorMessage = (cached != null) ? cached.errorMessage() : null;
            IndexItemStatus liveStatus = new IndexItemStatus(dbIndex.name(), liveState, errorMessage);
            itemStatusMap.put(dbIndex.name(), liveStatus);
            liveStatuses.add(liveStatus);
        }
        return liveStatuses;
    }

    @Override
    public Instant getLastProgressAt() {
        return lastProgressAt.get();
    }

    @Scheduled(fixedDelay = 30000)
    public void checkSyncAndTrigger() {
        IndexLifecycleState currentState = state.get();
        if (currentState != IndexLifecycleState.PENDING) {
            log.trace("Skipping sync check — lifecycle already in terminal state: {}", currentState);
            return;
        }

        if (syncStatusService.getSyncStatus().isSynced()) {
            log.info("Node reached tip. Triggering rosetta index creation.");
            triggerIndexing();
        }
    }

    @Override
    public void triggerIndexing() {
        if (!state.compareAndSet(IndexLifecycleState.PENDING, IndexLifecycleState.APPLYING)) {
            return;
        }

        lastProgressAt.set(Instant.now());

        indexExecutor.submit(this::executeIndexCreation);
    }

    private void executeIndexCreation() {
        try {
            int failedCount = 0;
            for (IndexCatalog.DbIndex dbIndex : indexConfig.getDbIndexes()) {
                // Re-query pg_index per index so that any future retry path (e.g. manual re-trigger)
                // always detects and drops invalid/building rows before re-attempting — never rely on
                // cached state from a previous run.
                IndexItemState currentItemState = queryIndexState(dbIndex.name());

                // Treat BUILDING (from prior JVM crash) the same as INVALID — drop and rebuild.
                // Schema-qualified drop (Tier 1.3) avoids matching wrong index in multi-schema DBs.
                if (currentItemState == IndexItemState.INVALID || currentItemState == IndexItemState.BUILDING) {
                    log.info("Index {} is {}. Dropping before rebuild.", dbIndex.name(), currentItemState);
                    jdbcTemplate.execute("DROP INDEX IF EXISTS " + currentSchema + "." + dbIndex.name());
                    currentItemState = IndexItemState.MISSING;
                }

                if (currentItemState == IndexItemState.MISSING) {
                    log.info("Building index: {}", dbIndex.name());
                    updateItemState(dbIndex.name(), IndexItemState.BUILDING, null);

                    try {
                        executeWithAutoCommit(dbIndex.command());
                        updateItemState(dbIndex.name(), IndexItemState.READY, null);
                        log.info("Successfully built index: {}", dbIndex.name());
                    } catch (Exception e) {
                        log.error("Failed to build index: {}", dbIndex.name(), e);
                        updateItemState(dbIndex.name(), IndexItemState.FAILED, e.getMessage());
                        failedCount++;
                        // Continue — remaining indexes are independent of this failure.
                    }
                }
                lastProgressAt.set(Instant.now());
            }

            if (failedCount == 0) {
                state.set(IndexLifecycleState.READY);
                log.info("All rosetta indexes are READY.");
            } else {
                state.set(IndexLifecycleState.FAILED);
                log.warn("{} rosetta index(es) failed. See per-index status via /actuator/rosetta-indexes.", failedCount);
            }

        } catch (Exception e) {
            log.error("Unexpected error during index creation", e);
            state.set(IndexLifecycleState.FAILED);
        }
    }

    /**
     * Execute SQL using a raw JDBC connection with explicit {@code autoCommit=true}.
     * Required because {@code CREATE INDEX CONCURRENTLY} cannot run inside a transaction block.
     */
    private void executeWithAutoCommit(String sql) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(0); // Infinite timeout for long-running index creations
                stmt.execute(sql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL with autoCommit: " + sql, e);
        }
    }

    private IndexItemState queryIndexState(String indexName) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(PG_INDEX_STATE_SQL, indexName);

        if (results.isEmpty()) {
            return IndexItemState.MISSING;
        }

        Map<String, Object> row = results.get(0);
        Boolean isValid = (Boolean) row.get("indisvalid");
        Boolean isReady = (Boolean) row.get("indisready");

        if (Boolean.TRUE.equals(isValid) && Boolean.TRUE.equals(isReady)) {
            return IndexItemState.READY;
        } else if (Boolean.FALSE.equals(isValid)) {
            return IndexItemState.INVALID;
        } else {
            return IndexItemState.BUILDING;
        }
    }

    private void updateItemState(String name, IndexItemState itemState, String errorMessage) {
        itemStatusMap.put(name, new IndexItemStatus(name, itemState, errorMessage));
    }
}
