package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Endpoint(id = "rosettaIndexes")
@RequiredArgsConstructor
public class RosettaIndexesEndpoint {

    private final RosettaIndexLifecycleService lifecycleService;

    @ReadOperation
    public RosettaIndexProgressSnapshot getRosettaIndexProgress() {
        var statuses = lifecycleService.getIndexStatus();
        int totalRequired = statuses.size();
        int totalReady = 0;
        int totalMissing = 0;
        int totalFailed = 0;

        for (var status : statuses) {
            switch (status.state()) {
                case READY -> totalReady++;
                case MISSING -> totalMissing++;
                case FAILED -> totalFailed++;
                default -> {}
            }
        }

        return new RosettaIndexProgressSnapshot(
            lifecycleService.getState(),
            statuses,
            lifecycleService.getLastProgressAt(),
            totalRequired,
            totalReady,
            totalMissing,
            totalFailed
        );
    }
}
