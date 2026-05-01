package org.cardanofoundation.rosetta.yaciindexer.indexes;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Profile({"h2", "test-integration"})
public class NoOpIndexService implements IndexService {

    @Override
    public IndexLifecycleState getState() {
        return IndexLifecycleState.READY;
    }

    @Override
    public List<IndexItemStatus> getIndexStatus() {
        return Collections.emptyList();
    }

    @Override
    public Instant getLastProgressAt() {
        return Instant.EPOCH;
    }

    @Override
    public void triggerIndexing() {
        // NO-OP for H2 and test-integration
    }
}
