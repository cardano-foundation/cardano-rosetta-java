package org.cardanofoundation.rosetta.yaciindexer.health;

import com.bloxbean.cardano.yaci.store.common.domain.HealthStatus;
import com.bloxbean.cardano.yaci.store.core.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Connection health indicator for the Yaci Indexer.
 *
 * <p>Bean name {@code "yaciConnection"} is included in both the startup and liveness probe groups:
 * <pre>
 *   management.endpoint.health.group.startup.include=db,yaciConnection
 *   management.endpoint.health.group.liveness.include=livenessState,yaciConnection
 * </pre>
 *
 * <p>Checks only that the indexer has connected to the cardano-node and is not in an
 * error state. Does NOT check sync progress — that is the readiness probe's concern via
 * {@link YaciSyncHealthIndicator}.
 *
 * <p>Health states:
 * <ul>
 *   <li><b>UP</b> — connected to node and no errors</li>
 *   <li><b>DOWN</b> — connection lost or error</li>
 *   <li><b>OUT_OF_SERVICE</b> — scheduled to stop</li>
 * </ul>
 */
@Component("yaciConnection")
@ConditionalOnBean(HealthService.class)
@RequiredArgsConstructor
public class YaciConnectionHealthIndicator implements HealthIndicator {

    private final HealthService healthService;

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
                    .withDetail("connectionStatus", "Scheduled to stop")
                    .build();
        }

        if (status.isError() || !status.isConnectionAlive()) {
            return builder.down()
                    .withDetail("connectionStatus", "Connection lost or error")
                    .build();
        }

        return builder.up()
                .withDetail("connectionStatus", "Connected and receiving blocks")
                .build();
    }
}
