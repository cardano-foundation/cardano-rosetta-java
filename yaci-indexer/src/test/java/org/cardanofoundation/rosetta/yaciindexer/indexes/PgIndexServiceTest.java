package org.cardanofoundation.rosetta.yaciindexer.indexes;

import com.bloxbean.cardano.yaci.store.common.domain.SyncStatus;
import com.bloxbean.cardano.yaci.store.core.service.SyncStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgIndexServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;


    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private IndexCatalog indexConfig;

    @Mock
    private SyncStatusService syncStatusService;

    private PgIndexService service;

    // Updated to match the new schema-filtered SQL (Issue #4)
    private static final String PG_INDEX_SQL =
            "SELECT i.indisready, i.indisvalid FROM pg_index i " +
            "JOIN pg_class c ON c.oid = i.indexrelid " +
            "JOIN pg_namespace n ON n.oid = c.relnamespace " +
            "WHERE c.relname = ? AND n.nspname = current_schema()";

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private IndexCatalog.DbIndex dbIndex(String name) {
        return new IndexCatalog.DbIndex(name, "CREATE INDEX CONCURRENTLY IF NOT EXISTS " + name + " ON test_table (col)");
    }

    private Map<String, Object> pgIndexRow(boolean isReady, boolean isValid) {
        return Map.of("indisready", isReady, "indisvalid", isValid);
    }

    private void waitForState(IndexLifecycleState expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (service.getState() == expected) return;
            Thread.sleep(50);
        }
        assertEquals(expected, service.getState(), "Timed out waiting for state " + expected);
    }

    /**
     * Sets up the DataSource mock chain so that executeWithAutoCommit() works in tests.
     * Must be called before triggerIndexing() in any test that expects index creation.
     * Uses lenient() because the index executor runs asynchronously — Mockito's strict
     * stubbing check may fire before the background thread consumes these stubs.
     */
    private void setupDataSourceMock() throws Exception {
        lenient().when(connection.createStatement()).thenReturn(statement);
    }

    private PgIndexService createService() {
        when(jdbcTemplate.queryForObject("SELECT current_schema()", String.class)).thenReturn("public");
        return new PgIndexService(jdbcTemplate, indexConfig, syncStatusService) {
            @Override
            Connection getConnection() throws java.sql.SQLException {
                return connection;
            }
        };
    }

    // ---------------------------------------------------------------------------
    // Init Tests
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("init()")
    class Init {

        @Test
        @DisplayName("should set READY when no indexes are configured")
        void readyWhenNoIndexes() {
            when(indexConfig.getDbIndexes()).thenReturn(null);

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should set READY when configured list is empty")
        void readyWhenEmptyList() {
            when(indexConfig.getDbIndexes()).thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should set READY when all indexes already exist and are valid")
        void readyWhenAllIndexesExist() {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(List.of(pgIndexRow(true, true)));

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
            assertEquals(2, service.getIndexStatus().size());
        }

        @Test
        @DisplayName("should set PENDING when some indexes are missing")
        void pendingWhenSomeMissing() {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList()); // MISSING

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should detect INVALID index state from pg_index")
        void detectsInvalidIndex() {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, false))); // indisvalid=false

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
            IndexItemStatus status = service.getIndexStatus().get(0);
            assertEquals(IndexItemState.INVALID, status.state());
        }

        @Test
        @DisplayName("should detect BUILDING index state from pg_index")
        void detectsBuildingIndex() {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            // BUILDING: indisready=false, indisvalid=true (concurrent build finishing)
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(false, true)));

            service = createService();
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
            IndexItemStatus status = service.getIndexStatus().get(0);
            assertEquals(IndexItemState.BUILDING, status.state());
        }
    }

    // ---------------------------------------------------------------------------
    // checkSyncAndTrigger Tests
    // Issue #14: Removed @MockitoSettings(strictness = LENIENT) — each test now
    // sets up only the stubs it needs.
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("checkSyncAndTrigger()")
    class CheckSyncAndTrigger {

        @Test
        @DisplayName("should not trigger when sync is not complete")
        void doesNotTriggerWhenNotSynced() {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            SyncStatus dto = SyncStatus.builder().synced(false).build();
            when(syncStatusService.getSyncStatus()).thenReturn(dto);

            service.checkSyncAndTrigger();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should trigger indexing when sync is complete")
        void triggersWhenSynced() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            SyncStatus dto = SyncStatus.builder().synced(true).build();
            when(syncStatusService.getSyncStatus()).thenReturn(dto);

            setupDataSourceMock();

            service.checkSyncAndTrigger();

            // State should transition from PENDING to APPLYING (at minimum)
            assertNotEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should skip check when state is READY")
        void skipsWhenReady() {
            when(indexConfig.getDbIndexes()).thenReturn(null);

            service = createService();
            service.init(); // Sets READY because no indexes

            service.checkSyncAndTrigger();

            // syncStatusService should never be called
            verifyNoInteractions(syncStatusService);
        }
    }

    // ---------------------------------------------------------------------------
    // triggerIndexing Tests
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("triggerIndexing()")
    class TriggerIndexing {

        @Test
        @DisplayName("should create missing indexes and transition to READY")
        void createsIndexesAndBecomesReady() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: both MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing: MISSING -> executeWithAutoCommit -> READY
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList());

            setupDataSourceMock();

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            assertEquals(IndexLifecycleState.READY, service.getState());
            assertNotNull(service.getLastProgressAt());
        }

        @Test
        @DisplayName("should drop INVALID index before rebuilding")
        void dropsInvalidIndexBeforeRebuild() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_bad"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: INVALID
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_bad")))
                    .thenReturn(List.of(pgIndexRow(true, false)));

            service = createService();
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing: INVALID -> DROP -> MISSING -> CREATE -> READY
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_bad")))
                    .thenReturn(List.of(pgIndexRow(true, false)));

            setupDataSourceMock();

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            verify(jdbcTemplate).execute("DROP INDEX IF EXISTS public.idx_bad");
            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        // Issue #2: Test that BUILDING state (from prior JVM crash) is treated like INVALID
        @Test
        @DisplayName("should drop BUILDING index (from prior crash) before rebuilding")
        void dropsBuildingIndexBeforeRebuild() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_stale"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: BUILDING (indisready=false, indisvalid=true)
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_stale")))
                    .thenReturn(List.of(pgIndexRow(false, true)));

            service = createService();
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing: BUILDING -> DROP -> MISSING -> CREATE -> READY
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_stale")))
                    .thenReturn(List.of(pgIndexRow(false, true)));

            setupDataSourceMock();

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            verify(jdbcTemplate).execute("DROP INDEX IF EXISTS public.idx_stale");
            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should transition to FAILED when index creation throws exception")
        void failsOnCreateException() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_fail"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            // During triggerIndexing: MISSING -> executeWithAutoCommit throws
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());

            setupDataSourceMock();
            when(statement.execute(anyString())).thenThrow(new RuntimeException("Out of disk space"));

            service.triggerIndexing();
            waitForState(IndexLifecycleState.FAILED, 5000);

            assertEquals(IndexLifecycleState.FAILED, service.getState());
        }

        @Test
        @DisplayName("should preserve FAILED item state when live index is still not ready")
        void preservesFailedItemStateWhenLiveIndexIsNotReady() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_fail"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            setupDataSourceMock();
            when(statement.execute(anyString())).thenThrow(new RuntimeException("permission denied"));

            service.triggerIndexing();
            waitForState(IndexLifecycleState.FAILED, 5000);

            IndexItemStatus status = service.getIndexStatus().get(0);

            assertEquals(IndexItemState.FAILED, status.state());
            assertNotNull(status.errorMessage());
        }

        @Test
        @DisplayName("should continue building remaining indexes when one fails (Tier 1.1)")
        void continuesAfterSingleFailure() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(
                    dbIndex("idx_fail"), dbIndex("idx_ok"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_ok")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            setupDataSourceMock();
            // First statement call (idx_fail) throws; second (idx_ok) succeeds
            when(statement.execute(anyString()))
                    .thenThrow(new RuntimeException("disk full"))
                    .thenReturn(false);

            service.triggerIndexing();
            waitForState(IndexLifecycleState.FAILED, 5000);

            // Overall state is FAILED because one index failed
            assertEquals(IndexLifecycleState.FAILED, service.getState());

            // Both CREATE INDEX calls were attempted — idx_ok was not skipped when idx_fail threw.
            // (getIndexStatus() re-queries live pg_index which the mock can't reflect mid-test,
            // so we verify at the JDBC level instead.)
            verify(statement, times(2)).execute(anyString());
        }

        @Test
        @DisplayName("should skip already READY indexes during indexing")
        void skipsReadyIndexes() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_ok"), dbIndex("idx_new"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: idx_ok=READY, idx_new=MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_ok")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_new")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_ok")))
                    .thenReturn(List.of(pgIndexRow(true, true)));   // check in loop: READY, skip
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_new")))
                    .thenReturn(Collections.emptyList());           // check in loop: MISSING

            setupDataSourceMock();

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            // Only idx_new's CREATE should have been executed via raw connection
            verify(statement, times(1)).execute(anyString());
        }

        @Test
        @DisplayName("should not trigger twice (compareAndSet guard)")
        void doesNotTriggerTwice() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            setupDataSourceMock();

            service.triggerIndexing(); // first call: PENDING -> APPLYING
            service.triggerIndexing(); // second call: should be no-op

            // No exception, state machine is safe
            assertNotEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should re-trigger from FAILED state on next sync cycle and reach READY")
        void failedLifecycleStateRetriggersOnNextSyncCycle() throws Exception {
            List<IndexCatalog.DbIndex> indexes = List.of(dbIndex("idx_retry"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_retry")))
                    .thenReturn(Collections.emptyList());

            service = createService();
            service.init();

            // Fail on first call (permanent), succeed on second (after re-trigger)
            setupDataSourceMock();
            when(statement.execute(anyString()))
                    .thenThrow(new RuntimeException("disk full"))
                    .thenReturn(false);

            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_retry")))
                    .thenReturn(Collections.emptyList());

            service.triggerIndexing();
            waitForState(IndexLifecycleState.FAILED, 5000);
            assertEquals(IndexLifecycleState.FAILED, service.getState());

            SyncStatus dto = SyncStatus.builder().synced(true).build();
            when(syncStatusService.getSyncStatus()).thenReturn(dto);

            service.checkSyncAndTrigger();

            waitForState(IndexLifecycleState.READY, 5000);
            assertEquals(IndexLifecycleState.READY, service.getState());
        }
    }
}
