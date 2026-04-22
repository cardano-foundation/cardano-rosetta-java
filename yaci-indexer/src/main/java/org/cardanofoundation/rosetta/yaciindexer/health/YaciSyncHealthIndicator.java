package org.cardanofoundation.rosetta.yaciindexer.health;

import com.bloxbean.cardano.yaci.store.common.domain.HealthStatus;
import com.bloxbean.cardano.yaci.store.common.domain.SyncStatus;
import com.bloxbean.cardano.yaci.store.core.service.HealthService;
import com.bloxbean.cardano.yaci.store.core.service.SyncStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.RosettaIndexLifecycleService;
import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.IndexLifecycleState;

/**
 * Readiness health indicator for the Yaci Indexer.
 *
 * <p>The indicator is named {@code "yaciSync"} and is included in the readiness
 * group via {@code application.properties}:
 * <pre>
 *   management.endpoint.health.group.startup.include=db,yaciConnection
 *   management.endpoint.health.group.liveness.include=livenessState,yaciConnection,rosettaIndexStall
 *   management.endpoint.health.group.readiness.include=readinessState,db,yaciSync
 * </pre>
 *
 * <p>The readiness probe uses a long failure threshold (5 days) to accommodate initial sync
 * and index creation. Liveness stall detection is handled by {@link RosettaIndexStallIndicator}.
 *
 * <p>Health states:
 * <ul>
 *   <li><b>UP</b> — connection alive, synced to tip, and all Rosetta indexes are READY</li>
 *   <li><b>DOWN (Syncing)</b> — connection alive but still catching up to tip</li>
 *   <li><b>DOWN (Applying Indexes)</b> — synced to tip but index lifecycle is not READY
 *       (PENDING, APPLYING, or FAILED)</li>
 *   <li><b>DOWN (Connection)</b> — connection lost or sync error</li>
 *   <li><b>OUT_OF_SERVICE</b> — scheduled to stop</li>
 * </ul>
 */
@Component("yaciSync")
@RequiredArgsConstructor
public class YaciSyncHealthIndicator implements HealthIndicator {

    private final HealthService healthService;
    private final SyncStatusService syncStatusService;
    private final RosettaIndexLifecycleService rosettaIndexLifecycleService;

    @Override
    public Health health() {
        HealthStatus status = healthService.getHealthStatus();

        Health.Builder builder = new Health.Builder()
                .withDetail("connectionAlive", status.isConnectionAlive())
                .withDetail("receivingBlocks", status.isReceivingBlocks())
                .withDetail("error", status.isError())
                .withDetail("timeSinceLastBlockMs", status.getTimeSinceLastBlock());

        if (status.isScheduleToStop()) {
            return builder.outOfService()
                    .withDetail("syncStatus", "Scheduled to stop")
                    .build();
        }

        if (status.isError() || !status.isConnectionAlive()) {
            return builder.down()
                    .withDetail("syncStatus", "Connection lost or sync error")
                    .build();
        }

        SyncStatus syncStatus = syncStatusService.getSyncStatus();

        builder.withDetail("indexedSlot", syncStatus.slot())
               .withDetail("networkSlot", syncStatus.networkSlot())
               .withDetail("syncPercentage", syncStatus.syncPercentage());

        if (!syncStatus.synced()) {
            return builder.down()
                    .withDetail("syncStatus", "Syncing")
                    .build();
        }

        IndexLifecycleState indexState = rosettaIndexLifecycleService.getState();
        builder.withDetail("indexLifecycleState", indexState.name());

        if (indexState != IndexLifecycleState.READY) {
            return builder.down()
                    .withDetail("syncStatus", "Applying Indexes")
                    .build();
        }

        return builder.up()
                .withDetail("syncStatus", "Synced")
                .build();
    }
}
