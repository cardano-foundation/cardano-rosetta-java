package org.cardanofoundation.rosetta.yaciindexer.health;

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
class YaciConnectionHealthIndicatorTest {

    @Mock
    private HealthService healthService;

    @InjectMocks
    private YaciConnectionHealthIndicator indicator;

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

    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("When connected to node and no errors")
    class WhenConnected {

        @Test
        @DisplayName("health() returns UP")
        void returnsUp() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
        }

        @Test
        @DisplayName("health() includes connectionAlive=true and connectionStatus=Connected and receiving blocks")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 5_000L));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("connectionAlive", true)
                    .containsEntry("receivingBlocks", true)
                    .containsEntry("timeSinceLastBlockMs", 5_000L)
                    .containsEntry("connectionStatus", "Connected and receiving blocks");
        }
    }

    @Nested
    @DisplayName("When connected but still syncing (connection must still be UP)")
    class WhenConnectedButSyncing {

        @Test
        @DisplayName("health() returns UP during sync — only checks connection, not sync progress")
        void returnsUpDuringSyncing() {
            // Simulates early sync: connected but very few blocks received yet
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, false, true, 30_000L));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
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
        @DisplayName("health() includes connectionAlive=false and connectionStatus detail")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(false, false, false, 120_000L));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("connectionAlive", false)
                    .containsEntry("connectionStatus", "Connection lost or error");
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
        @DisplayName("health() includes error=true and connectionStatus detail")
        void includesDetails() {
            when(healthService.getHealthStatus())
                    .thenReturn(buildStatus(true, true, false, 5_000L));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("error", true)
                    .containsEntry("connectionStatus", "Connection lost or error");
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
        @DisplayName("health() includes connectionStatus=Scheduled to stop")
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
                    .containsEntry("connectionStatus", "Scheduled to stop");
        }
    }
}
