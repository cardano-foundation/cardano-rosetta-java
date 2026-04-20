package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import java.time.Instant;
import java.util.List;

public interface RosettaIndexLifecycleService {
    IndexLifecycleState getState();
    List<IndexItemStatus> getIndexStatus();
    Instant getLastProgressAt();
    void triggerIndexing();
}
