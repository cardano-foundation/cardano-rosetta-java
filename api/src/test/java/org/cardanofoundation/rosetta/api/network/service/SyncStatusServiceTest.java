package org.cardanofoundation.rosetta.api.network.service;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.common.enumeration.SyncStage;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.SyncStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SyncStatusServiceTest {

    @Mock
    private OfflineSlotService offlineSlotService;

    @Mock
    private SlotRangeChecker slotRangeChecker;

    @Mock
    private IndexCreationMonitor indexCreationMonitor;

    @Mock
    private LedgerBlockService ledgerBlockService;

    private SyncStatusService syncStatusService;

    private static final int ALLOWED_SLOTS_DELTA = 100;

    @BeforeEach
    void setUp() {
        syncStatusService = new SyncStatusService(
            offlineSlotService,
            slotRangeChecker,
            indexCreationMonitor,
            ledgerBlockService
        );
        ReflectionTestUtils.setField(syncStatusService, "allowedSlotsDelta", ALLOWED_SLOTS_DELTA);
    }

    @Nested
    @DisplayName("calculateSyncStatus tests")
    class CalculateSyncStatusTests {

        @Test
        @DisplayName("Should return synced with LIVE stage when node reached tip and all required indexes are valid and ready")
        void shouldReturnSyncedWhenReachedTipAndAllIndexesReady() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false); // All indexes ready

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isTrue();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.LIVE.getValue());
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return not synced with APPLYING_INDEXES stage when node reached tip but required indexes are not ready")
        void shouldReturnNotSyncedWhenReachedTipButIndexesNotReady() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(true); // Indexes not ready yet

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.APPLYING_INDEXES.getValue());
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return not synced with SYNCING stage when node has not reached tip")
        void shouldReturnNotSyncedWhenNotReachedTip() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 800L; // Far behind
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(false);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.SYNCING.getValue());
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return status based on index readiness when current slot cannot be determined (e.g. devkit)")
        void shouldReturnStatusBasedOnIndexesWhenCurrentSlotUnavailable() {
            // Given
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(1000L)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.empty());
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isTrue();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.LIVE.getValue());
            assertThat(result.get().getTargetIndex()).isEqualTo(1000L);
            assertThat(result.get().getCurrentIndex()).isEqualTo(1000L);

            // And given indexes are not ready
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(true);

            // When
            result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.APPLYING_INDEXES.getValue());
        }

        @Test
        @DisplayName("Should return not synced with SYNCING stage when not reached tip even if indexes are not ready")
        void shouldReturnNotSyncedWhenNotReachedTipEvenIfIndexesNotReady() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 500L; // Far behind
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(false);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(true); // Indexes not ready

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getStage()).isEqualTo(SyncStage.SYNCING.getValue());
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }
    }

    @Nested
    @DisplayName("getSyncStatus tests")
    class GetSyncStatusTests {

        @Test
        @DisplayName("Should return empty when no background refresh has occurred yet")
        void shouldReturnEmptyBeforeAnyRefresh() {
            // When
            Optional<SyncStatus> resultOpt = syncStatusService.getSyncStatus();

            // Then
            assertThat(resultOpt).isEmpty();
            verify(ledgerBlockService, times(0)).findLatestBlockIdentifier();
        }

        @Test
        @DisplayName("Should return the value last stored by refreshSyncStatus without querying the DB again")
        void shouldReturnLastRefreshedValueWithoutQueryingDbAgain() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);
            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            syncStatusService.refreshSyncStatus();

            // When
            Optional<SyncStatus> resultOpt = syncStatusService.getSyncStatus();
            Optional<SyncStatus> resultOpt2 = syncStatusService.getSyncStatus();

            // Then
            assertThat(resultOpt).isPresent();
            SyncStatus result = resultOpt.get();
            assertThat(result.getSynced()).isTrue();
            assertThat(result.getStage()).isEqualTo(SyncStage.LIVE.getValue());
            assertThat(resultOpt2).isEqualTo(resultOpt);
            // getSyncStatus() must never itself trigger a DB query - only refreshSyncStatus() does.
            verify(ledgerBlockService, times(1)).findLatestBlockIdentifier();
        }
    }

    @Nested
    @DisplayName("refreshSyncStatus tests")
    class RefreshSyncStatusTests {

        @Test
        @DisplayName("Should query latest block, recalculate sync status, and store it for getSyncStatus")
        void shouldQueryLatestBlockAndStoreCalculatedSyncStatus() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);
            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            // When
            syncStatusService.refreshSyncStatus();

            // Then
            verify(ledgerBlockService, times(1)).findLatestBlockIdentifier();
            Optional<SyncStatus> resultOpt = syncStatusService.getSyncStatus();
            assertThat(resultOpt).isPresent();
            assertThat(resultOpt.get().getSynced()).isTrue();
            assertThat(resultOpt.get().getStage()).isEqualTo(SyncStage.LIVE.getValue());
        }

        @Test
        @DisplayName("Should overwrite the previously stored sync status on each call")
        void shouldOverwritePreviouslyStoredSyncStatus() {
            // Given: first refresh reports SYNCING
            BlockIdentifierExtended firstBlock = BlockIdentifierExtended.builder().slot(800L).build();
            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(firstBlock);
            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(1000L));
            when(slotRangeChecker.isSlotWithinEpsilon(1000L, 800L, ALLOWED_SLOTS_DELTA)).thenReturn(false);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            syncStatusService.refreshSyncStatus();
            assertThat(syncStatusService.getSyncStatus().orElseThrow().getStage())
                .isEqualTo(SyncStage.SYNCING.getValue());

            // When: second refresh reports LIVE
            BlockIdentifierExtended secondBlock = BlockIdentifierExtended.builder().slot(1000L).build();
            when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(secondBlock);
            when(slotRangeChecker.isSlotWithinEpsilon(1000L, 1000L, ALLOWED_SLOTS_DELTA)).thenReturn(true);

            syncStatusService.refreshSyncStatus();

            // Then
            assertThat(syncStatusService.getSyncStatus().orElseThrow().getStage())
                .isEqualTo(SyncStage.LIVE.getValue());
            verify(ledgerBlockService, times(2)).findLatestBlockIdentifier();
        }
    }
}
