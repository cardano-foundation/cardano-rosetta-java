package org.cardanofoundation.rosetta.yaciindexer.indexes;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;

public record IndexProgress(
    @Nonnull IndexLifecycleState overallState,
    @Nonnull List<IndexItemStatus> indexes,
    @Nullable Instant lastProgressAt,
    int totalRequired,
    int totalReady,
    int totalMissing,
    int totalFailed
) {}
