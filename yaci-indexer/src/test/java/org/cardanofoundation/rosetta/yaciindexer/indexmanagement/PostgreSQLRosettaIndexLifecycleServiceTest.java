package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import com.bloxbean.cardano.yaci.store.adminui.dto.SyncStatusDto;
import com.bloxbean.cardano.yaci.store.adminui.service.SyncStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgreSQLRosettaIndexLifecycleServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RosettaIndexConfig indexConfig;

    @Mock
    private SyncStatusService syncStatusService;

    private PostgreSQLRosettaIndexLifecycleService service;

    private static final String PG_INDEX_SQL =
            "SELECT indisready, indisvalid FROM pg_index i " +
            "JOIN pg_class c ON c.oid = i.indexrelid " +
            "WHERE c.relname = ?";

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private RosettaIndexConfig.DbIndex dbIndex(String name) {
        return new RosettaIndexConfig.DbIndex(name, "CREATE INDEX CONCURRENTLY IF NOT EXISTS " + name + " ON test_table (col)");
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

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should set READY when configured list is empty")
        void readyWhenEmptyList() {
            when(indexConfig.getDbIndexes()).thenReturn(Collections.emptyList());

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should set READY when all indexes already exist and are valid")
        void readyWhenAllIndexesExist() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(List.of(pgIndexRow(true, true)));

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.READY, service.getState());
            assertEquals(2, service.getIndexStatus().size());
        }

        @Test
        @DisplayName("should set PENDING when some indexes are missing")
        void pendingWhenSomeMissing() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList()); // MISSING

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should detect INVALID index state from pg_index")
        void detectsInvalidIndex() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(true, false))); // indisvalid=false

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
            IndexItemStatus status = service.getIndexStatus().get(0);
            assertEquals(IndexItemState.INVALID, status.state());
        }

        @Test
        @DisplayName("should detect BUILDING index state from pg_index")
        void detectsBuildingIndex() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            // BUILDING = indisready=false, indisvalid not false (i.e. true falls to else branch)
            // But per actual code: indisvalid=false -> INVALID regardless of indisready.
            // BUILDING is the else branch: isValid is NOT true AND NOT false (null),
            // OR isValid=true but isReady=false.
            // Realistic scenario: indisready=false, indisvalid=true (concurrent build finishing)
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(List.of(pgIndexRow(false, true))); // indisready=false, indisvalid=true

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
            IndexItemStatus status = service.getIndexStatus().get(0);
            assertEquals(IndexItemState.BUILDING, status.state());
        }
    }

    // ---------------------------------------------------------------------------
    // checkSyncAndTrigger Tests
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("checkSyncAndTrigger()")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class CheckSyncAndTrigger {

        @BeforeEach
        void setUp() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();
        }

        @Test
        @DisplayName("should not trigger when sync is not complete")
        void doesNotTriggerWhenNotSynced() {
            SyncStatusDto dto = SyncStatusDto.builder().synced(false).build();
            when(syncStatusService.getSyncStatus()).thenReturn(dto);

            service.checkSyncAndTrigger();

            assertEquals(IndexLifecycleState.PENDING, service.getState());
        }

        @Test
        @DisplayName("should trigger indexing when sync is complete")
        void triggersWhenSynced() {
            SyncStatusDto dto = SyncStatusDto.builder().synced(true).build();
            when(syncStatusService.getSyncStatus()).thenReturn(dto);

            // After trigger, queryIndexState will be called again during indexing thread
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList())   // MISSING check in triggerIndexing
                    .thenReturn(List.of(pgIndexRow(true, true))); // Final check

            service.checkSyncAndTrigger();

            // State should transition from PENDING to APPLYING (at minimum)
            assertNotEquals(IndexLifecycleState.PENDING, service.getState());
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
        void createsIndexesAndBecomesReady() throws InterruptedException {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"), dbIndex("idx_2"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: both MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList());

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing: MISSING -> execute CREATE -> final check READY
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList())            // check in loop
                    .thenReturn(List.of(pgIndexRow(true, true)));   // final check
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_2")))
                    .thenReturn(Collections.emptyList())            // check in loop
                    .thenReturn(List.of(pgIndexRow(true, true)));   // final check

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            assertEquals(IndexLifecycleState.READY, service.getState());
            assertNotNull(service.getLastProgressAt());
        }

        @Test
        @DisplayName("should drop INVALID index before rebuilding")
        void dropsInvalidIndexBeforeRebuild() throws InterruptedException {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_bad"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: INVALID
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_bad")))
                    .thenReturn(List.of(pgIndexRow(true, false)));

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing: INVALID -> DROP -> MISSING -> CREATE -> final check READY
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_bad")))
                    .thenReturn(List.of(pgIndexRow(true, false)))   // check in loop: INVALID
                    .thenReturn(List.of(pgIndexRow(true, true)));   // final check

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            verify(jdbcTemplate).execute("DROP INDEX IF EXISTS idx_bad");
            assertEquals(IndexLifecycleState.READY, service.getState());
        }

        @Test
        @DisplayName("should transition to FAILED when index creation throws exception")
        void failsOnCreateException() throws InterruptedException {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_fail"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            // During triggerIndexing: MISSING -> execute throws
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_fail")))
                    .thenReturn(Collections.emptyList());
            doThrow(new RuntimeException("Out of disk space"))
                    .when(jdbcTemplate).execute(anyString());

            service.triggerIndexing();
            waitForState(IndexLifecycleState.FAILED, 5000);

            assertEquals(IndexLifecycleState.FAILED, service.getState());
        }

        @Test
        @DisplayName("should skip already READY indexes during indexing")
        void skipsReadyIndexes() throws InterruptedException {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_ok"), dbIndex("idx_new"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);

            // init: idx_ok=READY, idx_new=MISSING
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_ok")))
                    .thenReturn(List.of(pgIndexRow(true, true)));
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_new")))
                    .thenReturn(Collections.emptyList());

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();
            assertEquals(IndexLifecycleState.PENDING, service.getState());

            // During triggerIndexing
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_ok")))
                    .thenReturn(List.of(pgIndexRow(true, true)));   // check in loop: READY, skip
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_new")))
                    .thenReturn(Collections.emptyList())            // check in loop: MISSING
                    .thenReturn(List.of(pgIndexRow(true, true)));   // final check

            service.triggerIndexing();
            waitForState(IndexLifecycleState.READY, 5000);

            // Only idx_new's CREATE should have been executed
            verify(jdbcTemplate, times(1)).execute(anyString());
        }

        @Test
        @DisplayName("should not trigger twice (compareAndSet guard)")
        void doesNotTriggerTwice() {
            List<RosettaIndexConfig.DbIndex> indexes = List.of(dbIndex("idx_1"));
            when(indexConfig.getDbIndexes()).thenReturn(indexes);
            when(jdbcTemplate.queryForList(eq(PG_INDEX_SQL), eq("idx_1")))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(List.of(pgIndexRow(true, true)));

            service = new PostgreSQLRosettaIndexLifecycleService(jdbcTemplate, indexConfig, syncStatusService);
            service.init();

            service.triggerIndexing(); // first call: PENDING -> APPLYING
            service.triggerIndexing(); // second call: should be no-op

            // No exception, state machine is safe
            assertNotEquals(IndexLifecycleState.PENDING, service.getState());
        }
    }
}
