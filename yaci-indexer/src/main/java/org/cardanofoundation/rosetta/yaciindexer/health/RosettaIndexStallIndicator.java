package org.cardanofoundation.rosetta.yaciindexer.health;

import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.IndexLifecycleState;
import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.RosettaIndexLifecycleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component("rosettaIndexStall")
public class RosettaIndexStallIndicator implements HealthIndicator {

    private final RosettaIndexLifecycleService lifecycleService;
    private final int stallTimeoutMinutes;

    public RosettaIndexStallIndicator(RosettaIndexLifecycleService lifecycleService,
                                      @Value("${cardano.rosetta.index.stall-timeout-minutes:15}") int stallTimeoutMinutes) {
        this.lifecycleService = lifecycleService;
        this.stallTimeoutMinutes = stallTimeoutMinutes;
    }

    @Override
    public Health health() {
        IndexLifecycleState state = lifecycleService.getState();
        Instant lastProgress = lifecycleService.getLastProgressAt();

        if (state != IndexLifecycleState.APPLYING) {
            return Health.up().withDetail("indexLifecycleState", state.name()).build();
        }

        if (lastProgress == null) {
            return Health.up().withDetail("indexLifecycleState", state.name()).withDetail("status", "Starting").build();
        }

        long minutesSinceLastProgress = Duration.between(lastProgress, Instant.now()).toMinutes();

        if (minutesSinceLastProgress >= stallTimeoutMinutes) {
            return Health.down()
                    .withDetail("indexLifecycleState", state.name())
                    .withDetail("error", "Index creation stalled")
                    .withDetail("minutesSinceLastProgress", minutesSinceLastProgress)
                    .withDetail("stallTimeoutMinutes", stallTimeoutMinutes)
                    .build();
        }

        return Health.up()
                .withDetail("indexLifecycleState", state.name())
                .withDetail("minutesSinceLastProgress", minutesSinceLastProgress)
                .build();
    }
}
