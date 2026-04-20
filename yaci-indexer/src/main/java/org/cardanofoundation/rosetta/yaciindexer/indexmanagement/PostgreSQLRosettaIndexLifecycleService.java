package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import com.bloxbean.cardano.yaci.store.adminui.service.SyncStatusService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("!h2 & !test-integration")
@RequiredArgsConstructor
@Slf4j
public class PostgreSQLRosettaIndexLifecycleService implements RosettaIndexLifecycleService {

    private final JdbcTemplate jdbcTemplate;
    private final RosettaIndexConfig indexConfig;
    private final SyncStatusService syncStatusService;

    private final AtomicReference<IndexLifecycleState> state = new AtomicReference<>(IndexLifecycleState.PENDING);
    private final Map<String, IndexItemStatus> itemStatusMap = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastProgressAt = new AtomicReference<>(null);

    @PostConstruct
    public void init() {
        if (indexConfig.getDbIndexes() == null || indexConfig.getDbIndexes().isEmpty()) {
            state.set(IndexLifecycleState.READY);
            return;
        }

        boolean allReady = true;
        for (RosettaIndexConfig.DbIndex dbIndex : indexConfig.getDbIndexes()) {
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
        return new ArrayList<>(itemStatusMap.values());
    }

    @Override
    public Instant getLastProgressAt() {
        return lastProgressAt.get();
    }

    @Scheduled(fixedDelay = 30000)
    public void checkSyncAndTrigger() {
        if (state.get() == IndexLifecycleState.PENDING) {
            if (syncStatusService.getSyncStatus().isSynced()) {
                log.info("Node reached tip. Triggering rosetta index creation.");
                triggerIndexing();
            }
        }
    }

    @Override
    public void triggerIndexing() {
        if (!state.compareAndSet(IndexLifecycleState.PENDING, IndexLifecycleState.APPLYING)) {
            return;
        }

        lastProgressAt.set(Instant.now());

        Thread indexingThread = new Thread(() -> {
            try {
                for (RosettaIndexConfig.DbIndex dbIndex : indexConfig.getDbIndexes()) {
                    IndexItemState currentState = queryIndexState(dbIndex.name());
                    
                    if (currentState == IndexItemState.INVALID) {
                        log.info("Index {} is INVALID. Dropping before rebuild.", dbIndex.name());
                        jdbcTemplate.execute("DROP INDEX IF EXISTS " + dbIndex.name());
                        currentState = IndexItemState.MISSING;
                    }

                    if (currentState == IndexItemState.MISSING) {
                        log.info("Building index: {}", dbIndex.name());
                        updateItemState(dbIndex.name(), IndexItemState.BUILDING, null);
                        
                        try {
                            jdbcTemplate.execute(dbIndex.command());
                            updateItemState(dbIndex.name(), IndexItemState.READY, null);
                            log.info("Successfully built index: {}", dbIndex.name());
                        } catch (Exception e) {
                            log.error("Failed to build index: {}", dbIndex.name(), e);
                            updateItemState(dbIndex.name(), IndexItemState.FAILED, e.getMessage());
                            state.set(IndexLifecycleState.FAILED);
                            return;
                        }
                    }
                    lastProgressAt.set(Instant.now());
                }
                
                // Final check
                boolean allReady = true;
                for (RosettaIndexConfig.DbIndex dbIndex : indexConfig.getDbIndexes()) {
                    if (queryIndexState(dbIndex.name()) != IndexItemState.READY) {
                        allReady = false;
                        break;
                    }
                }
                
                if (allReady) {
                    state.set(IndexLifecycleState.READY);
                    log.info("All rosetta indexes are READY.");
                } else {
                    state.set(IndexLifecycleState.FAILED);
                    log.error("Failed to verify all indexes are READY after building.");
                }

            } catch (Exception e) {
                log.error("Unexpected error during index creation", e);
                state.set(IndexLifecycleState.FAILED);
            }
        }, "RosettaIndexBuilder");

        indexingThread.setDaemon(true);
        indexingThread.start();
    }

    private IndexItemState queryIndexState(String indexName) {
        String sql = "SELECT indisready, indisvalid FROM pg_index i " +
                "JOIN pg_class c ON c.oid = i.indexrelid " +
                "WHERE c.relname = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, indexName);
        
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
