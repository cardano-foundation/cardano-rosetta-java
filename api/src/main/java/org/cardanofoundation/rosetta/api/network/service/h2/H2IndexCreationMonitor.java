package org.cardanofoundation.rosetta.api.network.service.h2;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.service.IndexCreationMonitor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * H2-specific implementation of IndexCreationMonitor.
 * This is a NOOP implementation that always reports no indexes being created,
 * as H2 is used for testing and development where index monitoring is not required.
 */
@Slf4j
@Service
@Profile({"h2", "test-integration"})
public class H2IndexCreationMonitor implements IndexCreationMonitor {

    public H2IndexCreationMonitor() {
        log.info("[IndexMonitor] Using H2 NOOP implementation - index monitoring disabled");
    }

    @Override
    public boolean isCreatingIndexes() {
        log.trace("[IndexMonitor] H2 NOOP implementation - returning false (no indexes being created)");
        return false;
    }

    @Override
    public List<IndexCreationProgress> getIndexCreationProgress() {
        log.trace("[IndexMonitor] H2 NOOP implementation - returning empty list");
        return Collections.emptyList();
    }
}
