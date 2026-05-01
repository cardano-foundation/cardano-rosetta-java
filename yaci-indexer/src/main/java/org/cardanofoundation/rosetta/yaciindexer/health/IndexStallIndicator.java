package org.cardanofoundation.rosetta.yaciindexer.health;

import org.cardanofoundation.rosetta.yaciindexer.indexes.IndexLifecycleState;
import org.cardanofoundation.rosetta.yaciindexer.indexes.IndexService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component("indexStall")
public class IndexStallIndicator implements HealthIndicator {

    private final IndexService indexService;
    private final int stallTimeoutMinutes;

    public IndexStallIndicator(IndexService indexService,
                                      @Value("${cardano.rosetta.index.stall-timeout-minutes:15}") int stallTimeoutMinutes) {
        this.indexService = indexService;
        this.stallTimeoutMinutes = stallTimeoutMinutes;
    }

    @Override
    public Health health() {
        IndexLifecycleState state = indexService.getState();
        Instant lastProgress = indexService.getLastProgressAt();

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
