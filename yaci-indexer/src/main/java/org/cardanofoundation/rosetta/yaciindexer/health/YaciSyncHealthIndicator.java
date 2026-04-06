package org.cardanofoundation.rosetta.yaciindexer.health;

import com.bloxbean.cardano.yaci.store.adminui.dto.SyncStatusDto;
import com.bloxbean.cardano.yaci.store.adminui.service.SyncStatusService;
import com.bloxbean.cardano.yaci.store.common.domain.HealthStatus;
import com.bloxbean.cardano.yaci.store.core.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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
 *   <li><b>UP</b> — connection alive, no error, and {@link SyncStatusDto#isSynced()} is true</li>
 *   <li><b>DOWN</b> — connection lost, sync error, or still catching up to tip</li>
 *   <li><b>OUT_OF_SERVICE</b> — scheduled to stop</li>
 * </ul>
 */
@Component("yaciSync")
@RequiredArgsConstructor
public class YaciSyncHealthIndicator implements HealthIndicator {

    private final HealthService healthService;
    private final SyncStatusService syncStatusService;

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

        SyncStatusDto syncStatus = syncStatusService.getSyncStatus();

        builder.withDetail("indexedSlot", syncStatus.getSlot())
               .withDetail("networkSlot", syncStatus.getNetworkSlot())
               .withDetail("syncPercentage", syncStatus.getSyncPercentage());

        if (!syncStatus.isSynced()) {
            return builder.down()
                    .withDetail("syncStatus", "Syncing")
                    .build();
        }

        return builder.up()
                .withDetail("syncStatus", "Synced")
                .build();
    }
}
