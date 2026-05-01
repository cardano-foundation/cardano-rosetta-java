package org.cardanofoundation.rosetta.yaciindexer.indexes;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Endpoint(id = "rosetta-indexes")
@RequiredArgsConstructor
public class IndexesEndpoint {

    private final IndexService indexService;

    @ReadOperation
    public IndexProgress getIndexProgress() {
        var statuses = indexService.getIndexStatus();
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

        return new IndexProgress(
            indexService.getState(),
            statuses,
            indexService.getLastProgressAt(),
            totalRequired,
            totalReady,
            totalMissing,
            totalFailed
        );
    }
}
