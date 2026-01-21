package org.cardanofoundation.rosetta.api.network.service;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncStatusServiceTest {

    @Mock
    private OfflineSlotService offlineSlotService;

    @Mock
    private SlotRangeChecker slotRangeChecker;

    @Mock
    private IndexCreationMonitor indexCreationMonitor;

    private SyncStatusService syncStatusService;

    private static final int ALLOWED_SLOTS_DELTA = 100;

    @BeforeEach
    void setUp() {
        syncStatusService = new SyncStatusService(
            offlineSlotService,
            slotRangeChecker,
            indexCreationMonitor
        );
        ReflectionTestUtils.setField(syncStatusService, "allowedSlotsDelta", ALLOWED_SLOTS_DELTA);
    }

    @Nested
    @DisplayName("calculateSyncStatus tests")
    class CalculateSyncStatusTests {

        @Test
        @DisplayName("Should return synced when node reached tip and no indexes being created")
        void shouldReturnSyncedWhenReachedTipAndNoIndexes() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(false);

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isTrue();
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return not synced when node reached tip but indexes are being created")
        void shouldReturnNotSyncedWhenReachedTipButIndexesBeingCreated() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 990L;
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(true);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(true);

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return not synced when node has not reached tip")
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
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }

        @Test
        @DisplayName("Should return empty when current slot cannot be determined")
        void shouldReturnEmptyWhenCurrentSlotUnavailable() {
            // Given
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(1000L)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.empty());

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return not synced when not reached tip and indexes being created")
        void shouldReturnNotSyncedWhenNotReachedTipAndIndexesBeingCreated() {
            // Given
            long currentSlot = 1000L;
            long latestBlockSlot = 500L; // Far behind
            BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
                .slot(latestBlockSlot)
                .build();

            when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));
            when(slotRangeChecker.isSlotWithinEpsilon(currentSlot, latestBlockSlot, ALLOWED_SLOTS_DELTA))
                .thenReturn(false);
            when(indexCreationMonitor.isCreatingIndexes()).thenReturn(true);

            // When
            Optional<SyncStatus> result = syncStatusService.calculateSyncStatus(latestBlock);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSynced()).isFalse();
            assertThat(result.get().getTargetIndex()).isEqualTo(currentSlot);
            assertThat(result.get().getCurrentIndex()).isEqualTo(latestBlockSlot);
        }
    }
}
