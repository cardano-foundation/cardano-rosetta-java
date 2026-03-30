package org.cardanofoundation.rosetta.yaciindexer.health;

import com.bloxbean.cardano.yaci.store.adminui.dto.SyncStatusDto;
import com.bloxbean.cardano.yaci.store.adminui.service.SyncStatusService;
import com.bloxbean.cardano.yaci.store.common.domain.HealthStatus;
import com.bloxbean.cardano.yaci.store.core.service.HealthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YaciSyncHealthIndicatorTest {

    @Mock
    private HealthService healthService;

    @Mock
    private SyncStatusService syncStatusService;

    @InjectMocks
    private YaciSyncHealthIndicator indicator;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private HealthStatus buildStatus(boolean connectionAlive, boolean error,
                                     boolean receivingBlocks, long timeSinceLastBlock) {
        return HealthStatus.builder()
                .isConnectionAlive(connectionAlive)
                .isError(error)
                .isReceivingBlocks(receivingBlocks)
                .timeSinceLastBlock(timeSinceLastBlock)
                .build();
    }

    private SyncStatusDto syncedStatus() {
        return SyncStatusDto.builder()
                .synced(true)
                .slot(118_000_000L)
                .networkSlot(118_000_000L)
                .syncPercentage(100.0)
                .build();
    }

    private SyncStatusDto syncingStatus() {
        return SyncStatusDto.builder()
                .synced(false)
                .slot(50_000_000L)
                .networkSlot(118_000_000L)
                .syncPercentage(42.4)
                .build();
    }

    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("When connection is alive, no error, and synced to tip")
    class WhenSynced {

        @Test
        @DisplayName("health() returns UP")
        void returnsUp() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));
            when(syncStatusService.getSyncStatus()).thenReturn(syncedStatus());

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
        }

        @Test
        @DisplayName("health() includes connectionAlive=true, syncStatus=Synced and slot details")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));
            when(syncStatusService.getSyncStatus()).thenReturn(syncedStatus());

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("connectionAlive", true)
                    .containsEntry("receivingBlocks", true)
                    .containsEntry("timeSinceLastBlockMs", 5_000L)
                    .containsEntry("syncStatus", "Synced")
                    .containsEntry("indexedSlot", 118_000_000L)
                    .containsEntry("networkSlot", 118_000_000L)
                    .containsEntry("syncPercentage", 100.0);
        }
    }

    @Nested
    @DisplayName("When connection is alive but indexer is still syncing")
    class WhenSyncing {

        @Test
        @DisplayName("health() returns DOWN")
        void returnsDown() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));
            when(syncStatusService.getSyncStatus()).thenReturn(syncingStatus());

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("health() includes syncStatus=Syncing and slot details")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));
            when(syncStatusService.getSyncStatus()).thenReturn(syncingStatus());

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("syncStatus", "Syncing")
                    .containsEntry("syncPercentage", 42.4)
                    .containsEntry("indexedSlot", 50_000_000L)
                    .containsEntry("networkSlot", 118_000_000L);
        }
    }

    @Nested
    @DisplayName("When connection is not alive")
    class WhenConnectionDead {

        @Test
        @DisplayName("health() returns DOWN")
        void returnsDown() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(false, false, false, 120_000L));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("health() includes connectionAlive=false and syncStatus detail")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(false, false, false, 120_000L));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("connectionAlive", false)
                    .containsEntry("syncStatus", "Connection lost or sync error");
        }
    }

    @Nested
    @DisplayName("When an error is flagged")
    class WhenError {

        @Test
        @DisplayName("health() returns DOWN even when connection is technically alive")
        void returnsDownOnError() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, true, false, 5_000L));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("health() includes error=true and syncStatus detail")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, true, false, 5_000L));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("error", true)
                    .containsEntry("syncStatus", "Connection lost or sync error");
        }
    }

    @Nested
    @DisplayName("When scheduled to stop")
    class WhenScheduledToStop {

        @Test
        @DisplayName("health() returns OUT_OF_SERVICE")
        void returnsOutOfService() {
            HealthStatus status = HealthStatus.builder()
                    .isConnectionAlive(true)
                    .isError(false)
                    .isReceivingBlocks(false)
                    .timeSinceLastBlock(0L)
                    .isScheduleToStop(true)
                    .build();
            when(healthService.getHealthStatus()).thenReturn(status);

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        }

        @Test
        @DisplayName("health() includes syncStatus=Scheduled to stop")
        void includesDetails() {
            HealthStatus status = HealthStatus.builder()
                    .isConnectionAlive(true)
                    .isError(false)
                    .isReceivingBlocks(false)
                    .timeSinceLastBlock(0L)
                    .isScheduleToStop(true)
                    .build();
            when(healthService.getHealthStatus()).thenReturn(status);

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("syncStatus", "Scheduled to stop");
        }
    }
}
