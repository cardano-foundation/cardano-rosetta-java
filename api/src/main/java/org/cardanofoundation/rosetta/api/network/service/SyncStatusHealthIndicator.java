package org.cardanofoundation.rosetta.api.network.service;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Custom readiness health indicator that reports the API service readiness based on
 * the blockchain sync stage. The API is only ready when the sync stage reaches LIVE
 * (node at chain tip AND all database indexes applied).
 *
 * <p>Bean name {@code "syncStatus"} must match the readiness group configuration in
 * {@code application.yaml}:
 * <pre>
 *   management.endpoint.health.group.readiness.include=readinessState,db,syncStatus
 * </pre>
 *
 * <p>Sync stage transitions:
 * <ul>
 *   <li>SYNCING → node has not yet reached the chain tip</li>
 *   <li>APPLYING_INDEXES → at tip, but required database indexes are not yet valid/ready</li>
 *   <li>LIVE → fully ready to serve traffic</li>
 * </ul>
 *
 * <p>Returns {@link Health#outOfService()} for any non-LIVE stage so that the readiness
 * probe signals NOT-READY (HTTP 503) without triggering a container restart (liveness is
 * separate and does not include this indicator).
 */
@Component("syncStatus")
@RequiredArgsConstructor
public class SyncStatusHealthIndicator implements HealthIndicator {

    private final SyncStatusService syncStatusService;
    private final LedgerBlockService ledgerBlockService;

    @Override
    @Nullable
    public Health health() {
        var latestBlock = ledgerBlockService.findLatestBlockIdentifier();
        var syncStatusOpt = syncStatusService.calculateSyncStatus(latestBlock);

        if (syncStatusOpt == null || syncStatusOpt.isEmpty()) {
            return Health.outOfService()
                    .withDetail("reason", "Sync status cannot be determined — no block data available yet")
                    .build();
        }

        var syncStatus = syncStatusOpt.get();

        if (Boolean.TRUE.equals(syncStatus.getSynced())) {
            return Health.up()
                    .withDetail("stage", syncStatus.getStage())
                    .withDetail("synced", true)
                    .withDetail("currentIndex", syncStatus.getCurrentIndex())
                    .withDetail("targetIndex", syncStatus.getTargetIndex())
                    .build();
        }

        return Health.outOfService()
                .withDetail("stage", syncStatus.getStage())
                .withDetail("synced", false)
                .withDetail("currentIndex", syncStatus.getCurrentIndex())
                .withDetail("targetIndex", syncStatus.getTargetIndex())
                .build();
    }
}
