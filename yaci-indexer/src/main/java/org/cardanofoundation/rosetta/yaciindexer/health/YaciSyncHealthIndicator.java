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
 * Liveness and readiness health indicator for the Yaci Indexer.
 *
 * <p>The indicator is named {@code "yaciSync"} and is included in both liveness and readiness
 * groups via {@code application.properties}:
 * <pre>
 *   management.endpoint.health.group.liveness.include=livenessState,yaciSync
 *   management.endpoint.health.group.readiness.include=readinessState,db,yaciSync
 * </pre>
 *
 * <p>Both probes check the same condition — the indexer is healthy only when synced to tip.
 * The difference is in the Kubernetes probe timeout:
 * <ul>
 *   <li><b>Readiness</b> — long failure threshold (5 days) to accommodate initial sync</li>
 *   <li><b>Liveness</b> — short failure threshold (15 minutes) to detect a stuck/dead process</li>
 * </ul>
 *
 * <p>Health states:
 * <ul>
 *   <li><b>UP</b> — connection alive, no error, and {@link SyncStatus#synced()} is true</li>
 *   <li><b>DOWN</b> — connection lost, sync error, or still catching up to tip</li>
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
        builder.withDetail("indexLifecycleState", indexState);

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
