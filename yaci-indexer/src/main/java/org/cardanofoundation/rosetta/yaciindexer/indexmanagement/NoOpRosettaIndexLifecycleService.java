package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Profile({"h2", "test-integration"})
public class NoOpRosettaIndexLifecycleService implements RosettaIndexLifecycleService {

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
        return Instant.now();
    }

    @Override
    public void triggerIndexing() {
        // NO-OP for H2
    }
}
