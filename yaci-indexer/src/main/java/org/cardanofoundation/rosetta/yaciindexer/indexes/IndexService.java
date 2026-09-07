package org.cardanofoundation.rosetta.yaciindexer.indexes;

import java.time.Instant;
import java.util.List;

public interface IndexService {
    IndexLifecycleState getState();
    List<IndexItemStatus> getIndexStatus();
    Instant getLastProgressAt();
    void triggerIndexing();
}
