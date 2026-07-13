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
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * Holds the most recently computed sync status. Populated on startup and refreshed
     * periodically by {@link #refreshSyncStatus()}, so request-time reads via
     * {@link #getSyncStatus()} never block on DB access or clock/index computation.
     */
    private final AtomicReference<Optional<SyncStatus>> cachedSyncStatus =
        new AtomicReference<>(Optional.empty());

    @PostConstruct
    public void init() {
        log.info("[SyncStatusService] Initializing with allowedSlotsDelta: {}", allowedSlotsDelta);
        refreshSyncStatus();
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
     * Gets the most recently computed sync status of the indexer. This never performs DB
     * access or computation itself - it simply reads the value last stored by the periodic
     * background refresh ({@link #refreshSyncStatus()}), so request-time callers get an
     * instant response.
     *
     * @return an Optional containing the SyncStatus of the indexer if available, empty otherwise
     */
    public Optional<SyncStatus> getSyncStatus() {
        return cachedSyncStatus.get();
    }

    /**
     * Periodically recomputes the sync status from the DB and current time, and stores the
     * result for {@link #getSyncStatus()} to serve. The rate is configurable via properties,
     * defaulting to 10 seconds. Also invoked once on startup via {@link #init()} so the first
     * requests don't see an empty status while waiting for the first scheduled tick.
     */
    @Scheduled(fixedRateString = "${cardano.rosetta.sync-status-refresh-rate-ms:10000}")
    public void refreshSyncStatus() {
        log.debug("[SyncStatusService] Refreshing sync status from DB");
        BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
        cachedSyncStatus.set(calculateSyncStatus(latestBlock));
    }
}
