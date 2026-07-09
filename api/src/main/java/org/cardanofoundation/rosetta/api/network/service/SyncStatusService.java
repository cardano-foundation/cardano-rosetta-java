package org.cardanofoundation.rosetta.api.network.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.common.enumeration.SyncStage;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.openapitools.client.model.SyncStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for calculating sync status based on blockchain tip
 * and database index readiness (checking if required indexes are valid and ready).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncStatusService {

    private final OfflineSlotService offlineSlotService;
    private final SlotRangeChecker slotRangeChecker;
    private final IndexCreationMonitor indexCreationMonitor;
    private final LedgerBlockService ledgerBlockService;

    @Value("${cardano.rosetta.SYNC_GRACE_SLOTS_COUNT:200}")
    private int allowedSlotsDelta;

    @PostConstruct
    public void init() {
        log.info("[SyncStatusService] Initializing with allowedSlotsDelta: {}", allowedSlotsDelta);
    }

    /**
     * Calculates the sync status based on the latest block and current time.
     * A node is considered fully synced if:
     * 1. The latest block slot is within epsilon (allowedSlotsDelta) of the current time slot
     * 2. AND all required indexes exist and are valid (indisvalid=true) and ready (indisready=true)
     *
     * Sync stages:
     * - SYNCING: Node has not reached the blockchain tip yet
     * - APPLYING_INDEXES: Node reached tip but required indexes are missing, not valid, or not ready
     * - LIVE: Node is fully synced and all required indexes are valid and ready
     *
     * This ensures the state machine follows: SYNCING -> APPLYING_INDEXES -> LIVE
     * without transitioning to LIVE prematurely before indexes are applied.
     *
     * @param latestBlock the latest block from the blockchain
     * @return Optional containing SyncStatus if it can be calculated, empty otherwise
     */
    @Nullable
    public Optional<SyncStatus> calculateSyncStatus(BlockIdentifierExtended latestBlock) {
        Optional<Long> currentSlotBasedOnTimeOpt = offlineSlotService.getCurrentSlotBasedOnTime();

        if (currentSlotBasedOnTimeOpt.isEmpty()) {
            // When current slot cannot be determined based on time (e.g. on DevKit where slot converters are null),
            // we assume the tip is reached and we check if the required database indexes are ready.
            boolean indexesNotReady = indexCreationMonitor.isCreatingIndexes();
            SyncStage stage = indexesNotReady ? SyncStage.APPLYING_INDEXES : SyncStage.LIVE;
            boolean isSynced = !indexesNotReady;

            log.info("[SyncStatus] Converters unavailable (devkit). Returning status based on index readiness. Stage: {}, Synced: {}", stage, isSynced);

            return Optional.of(SyncStatus.builder()
                .targetIndex(latestBlock.getSlot())
                .currentIndex(latestBlock.getSlot())
                .synced(isSynced)
                .stage(stage.getValue())
                .build());
        }

        return currentSlotBasedOnTimeOpt.map(slotBasedOnTime -> {
            long slotBasedOnLatestBlock = latestBlock.getSlot();

            // Check if node has reached the tip
            boolean reachedTip = slotRangeChecker.isSlotWithinEpsilon(
                slotBasedOnTime,
                slotBasedOnLatestBlock,
                allowedSlotsDelta
            );

            // Check if required indexes are missing, not valid, or not ready
            boolean indexesNotReady = indexCreationMonitor.isCreatingIndexes();

            // Determine sync stage and synced status
            SyncStage stage;
            boolean isSynced;

            if (!reachedTip) {
                // Still syncing to blockchain tip
                stage = SyncStage.SYNCING;
                isSynced = false;
                log.debug(
                    "[SyncStatus] Stage: SYNCING - Current slot: {}, Latest block slot: {}, Delta: {}",
                    slotBasedOnTime,
                    slotBasedOnLatestBlock,
                    Math.abs(slotBasedOnTime - slotBasedOnLatestBlock)
                );
            } else if (indexesNotReady) {
                // Reached tip but required indexes are missing, not valid, or not ready
                stage = SyncStage.APPLYING_INDEXES;
                isSynced = false;
                log.info(
                    "[SyncStatus] Stage: APPLYING_INDEXES - Node reached tip but required indexes are not ready. " +
                    "Current slot: {}, Latest block slot: {}",
                    slotBasedOnTime,
                    slotBasedOnLatestBlock
                );

                // Log index status for visibility
                List<IndexCreationMonitor.IndexCreationProgress> progressList = indexCreationMonitor.getIndexCreationProgress();
                if (!progressList.isEmpty()) {
                    progressList.forEach(progress -> {
                        log.info("[SyncStatus] Index status: {}", progress.phase());
                    });
                }
            } else {
                // Fully synced and ready
                stage = SyncStage.LIVE;
                isSynced = true;
                log.debug(
                    "[SyncStatus] Stage: LIVE - Fully synced. Current slot: {}, Latest block slot: {}",
                    slotBasedOnTime,
                    slotBasedOnLatestBlock
                );
            }

            return SyncStatus.builder()
                .targetIndex(slotBasedOnTime)
                .currentIndex(slotBasedOnLatestBlock)
                .synced(isSynced)
                .stage(stage.getValue())
                .build();
        });
    }

    /**
     * Gets the sync status of the indexer. This is cached for 5 seconds to prevent
     * redundant database checks on every request.
     * <p>
     * {@code sync = true} ensures that when multiple concurrent requests miss the cache
     * at the same time (e.g. right after eviction, or under load-test level concurrency),
     * only one thread computes the value and queries the DB while the others block and then
     * read the cached result, instead of every thread independently querying the DB.
     *
     * @return an Optional containing the SyncStatus of the indexer if available, empty otherwise
     */
    @Cacheable(value = "syncStatusCache", sync = true)
    public Optional<SyncStatus> getSyncStatus() {
        log.info("[SyncStatusService] Cache miss - querying sync status from DB");
        BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
        return calculateSyncStatus(latestBlock);
    }

    /**
     * Periodically evicts all sync status entries from the cache.
     * The rate is configurable via properties, defaulting to 5 seconds.
     */
    @Scheduled(fixedRateString = "${cardano.rosetta.sync-status-cache-ttl-ms:5000}")
    @CacheEvict(value = "syncStatusCache", allEntries = true)
    public void evictSyncStatusCache() {
        log.trace("[SyncStatusService] Evicting syncStatusCache");
    }
}
