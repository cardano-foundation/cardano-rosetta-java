package org.cardanofoundation.rosetta.api.network.health;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.network.service.SyncStatusService;
import org.cardanofoundation.rosetta.common.enumeration.SyncStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.SyncStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncStatusHealthIndicator Tests")
class SyncStatusHealthIndicatorTest {

    @Mock
    private SyncStatusService syncStatusService;

    @Mock
    private LedgerBlockService ledgerBlockService;

    private SyncStatusHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new SyncStatusHealthIndicator(syncStatusService, ledgerBlockService);
    }

    @Nested
    @DisplayName("When calculating health status")
    class CalculateHealthTests {

        @Test
        @DisplayName("Should return UP when node is fully synced and LIVE")
        void shouldReturnUpWhenFullySynced() {
            // Given
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder().slot(1000L).build();
            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);

            SyncStatus syncStatus = new SyncStatus()
                    .synced(true)
                    .stage(SyncStage.LIVE.getValue())
                    .currentIndex(1000L)
                    .targetIndex(1000L);
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            // When
            Health health = healthIndicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("stage", SyncStage.LIVE.getValue());
        }

        @Test
        @DisplayName("Should return DOWN when node is not fully synced")
        void shouldReturnDownWhenNotSynced() {
            // Given
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder().slot(800L).build();
            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);

            SyncStatus syncStatus = new SyncStatus()
                    .synced(false)
                    .stage(SyncStage.SYNCING.getValue())
                    .currentIndex(800L)
                    .targetIndex(1000L);
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            // When
            Health health = healthIndicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("stage", SyncStage.SYNCING.getValue());
            assertThat(health.getDetails()).containsEntry("message", "Node is not fully synced yet");
        }

        @Test
        @DisplayName("Should return DOWN when sync status cannot be determined")
        void shouldReturnDownWhenSyncStatusEmpty() {
            // Given
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder().slot(800L).build();
            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.empty());

            // When
            Health health = healthIndicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("message", "Cannot determine sync status");
        }

        @Test
        @DisplayName("Should return DOWN when exception occurs")
        void shouldReturnDownWhenExceptionThrown() {
            // Given
            when(ledgerBlockService.findLatestBlockIdentifier()).thenThrow(new RuntimeException("DB Connection Error"));

            // When
            Health health = healthIndicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("message", "DB Connection Error");
        }
    }
}
