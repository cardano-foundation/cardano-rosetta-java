package org.cardanofoundation.rosetta.api.network.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.openapitools.client.model.SyncStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for calculating sync status based on blockchain tip
 * and database index creation progress.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncStatusService {

    private final OfflineSlotService offlineSlotService;
    private final SlotRangeChecker slotRangeChecker;
    private final IndexCreationMonitor indexCreationMonitor;

    @Value("${cardano.rosetta.SYNC_GRACE_SLOTS_COUNT:100}")
    private int allowedSlotsDelta;

    @PostConstruct
    public void init() {
        log.info("[SyncStatusService] Initializing with allowedSlotsDelta: {}", allowedSlotsDelta);
    }

    /**
     * Calculates the sync status based on the latest block and current time.
     * A node is considered fully synced if:
     * 1. The latest block slot is within epsilon (allowedSlotsDelta) of the current time slot
     * 2. AND no indexes are currently being created in the database
     *
     * @param latestBlock the latest block from the blockchain
     * @return Optional containing SyncStatus if it can be calculated, empty otherwise
     */
    @Nullable
    public Optional<SyncStatus> calculateSyncStatus(BlockIdentifierExtended latestBlock) {
        return offlineSlotService.getCurrentSlotBasedOnTime().map(slotBasedOnTime -> {
            long slotBasedOnLatestBlock = latestBlock.getSlot();

            // Check if node has reached the tip
            boolean reachedTip = slotRangeChecker.isSlotWithinEpsilon(
                slotBasedOnTime,
                slotBasedOnLatestBlock,
                allowedSlotsDelta
            );

            // Check if indexes are being created
            boolean indexesBeingCreated = indexCreationMonitor.isCreatingIndexes();

            // Node is fully synced only if it reached the tip AND no indexes are being created
            boolean isSynced = reachedTip && !indexesBeingCreated;

            if (reachedTip && indexesBeingCreated) {
                log.info(
                    "[SyncStatus] Node reached tip but indexes are being created. " +
                    "Reporting as not synced. Current slot: {}, Latest block slot: {}",
                    slotBasedOnTime,
                    slotBasedOnLatestBlock
                );

                // Log index creation progress for visibility
                List<IndexCreationMonitor.IndexCreationProgress> progressList = indexCreationMonitor.getIndexCreationProgress();
                progressList.forEach(progress -> {
                    var completion = progress.getCompletionPercentage();
                    String completionStr = completion != null
                        ? String.format("%.2f%%", completion)
                        : "unknown";
                    log.info(
                        "[SyncStatus] Index creation progress - phase: {}, completion: {}",
                        progress.phase(),
                        completionStr
                    );
                });
            }

            log.debug(
                "[SyncStatus] Sync calculation - reachedTip: {}, indexesBeingCreated: {}, isSynced: {}",
                reachedTip,
                indexesBeingCreated,
                isSynced
            );

            return SyncStatus.builder()
                .targetIndex(slotBasedOnTime)
                .currentIndex(slotBasedOnLatestBlock)
                .synced(isSynced)
                .build();
        });
    }
}
