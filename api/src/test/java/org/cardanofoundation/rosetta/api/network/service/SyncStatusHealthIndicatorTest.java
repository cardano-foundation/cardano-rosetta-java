package org.cardanofoundation.rosetta.api.network.service;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.SyncStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncStatusHealthIndicatorTest {

    @Mock
    private SyncStatusService syncStatusService;

    @Mock
    private LedgerBlockService ledgerBlockService;

    @InjectMocks
    private SyncStatusHealthIndicator indicator;

    private BlockIdentifierExtended latestBlock;

    @BeforeEach
    void setUp() {
        latestBlock = BlockIdentifierExtended.builder()
                .slot(100_000_000L)
                .number(10_000_000L)
                .hash("abc123")
                .build();
        when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);
    }

    @Nested
    @DisplayName("When sync stage is LIVE")
    class WhenStageLive {

        @Test
        @DisplayName("health() returns UP status")
        void returnsHealthUp() {
            var syncStatus = SyncStatus.builder()
                    .synced(true)
                    .stage("LIVE")
                    .currentIndex(100_000_000L)
                    .targetIndex(100_000_050L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
        }

        @Test
        @DisplayName("health() includes stage, synced, currentIndex, targetIndex details")
        void includesSyncDetails() {
            var syncStatus = SyncStatus.builder()
                    .synced(true)
                    .stage("LIVE")
                    .currentIndex(100_000_000L)
                    .targetIndex(100_000_050L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("stage", "LIVE")
                    .containsEntry("synced", true)
                    .containsEntry("currentIndex", 100_000_000L)
                    .containsEntry("targetIndex", 100_000_050L);
        }
    }

    @Nested
    @DisplayName("When sync stage is SYNCING")
    class WhenStageSyncing {

        @Test
        @DisplayName("health() returns OUT_OF_SERVICE status")
        void returnsHealthOutOfService() {
            var syncStatus = SyncStatus.builder()
                    .synced(false)
                    .stage("SYNCING")
                    .currentIndex(50_000_000L)
                    .targetIndex(100_000_000L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        }

        @Test
        @DisplayName("health() includes stage and synced=false details")
        void includesSyncDetails() {
            var syncStatus = SyncStatus.builder()
                    .synced(false)
                    .stage("SYNCING")
                    .currentIndex(50_000_000L)
                    .targetIndex(100_000_000L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getDetails())
                    .containsEntry("stage", "SYNCING")
                    .containsEntry("synced", false);
        }
    }

    @Nested
    @DisplayName("When sync stage is APPLYING_INDEXES")
    class WhenStageApplyingIndexes {

        @Test
        @DisplayName("health() returns OUT_OF_SERVICE status")
        void returnsHealthOutOfService() {
            var syncStatus = SyncStatus.builder()
                    .synced(false)
                    .stage("APPLYING_INDEXES")
                    .currentIndex(100_000_000L)
                    .targetIndex(100_000_000L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        }

        @Test
        @DisplayName("health() includes stage=APPLYING_INDEXES in details")
        void includesApplyingIndexesStage() {
            var syncStatus = SyncStatus.builder()
                    .synced(false)
                    .stage("APPLYING_INDEXES")
                    .currentIndex(100_000_000L)
                    .targetIndex(100_000_000L)
                    .build();
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.of(syncStatus));

            Health health = indicator.health();

            assertThat(health.getDetails()).containsEntry("stage", "APPLYING_INDEXES");
        }
    }

    @Nested
    @DisplayName("When sync status Optional is empty")
    class WhenSyncStatusEmpty {

        @Test
        @DisplayName("health() returns OUT_OF_SERVICE status")
        void returnsHealthOutOfService() {
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.empty());

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        }

        @Test
        @DisplayName("health() includes a 'reason' detail explaining unavailability")
        void includesReasonDetail() {
            when(syncStatusService.calculateSyncStatus(latestBlock)).thenReturn(Optional.empty());

            Health health = indicator.health();

            assertThat(health.getDetails()).containsKey("reason");
        }
    }
}
