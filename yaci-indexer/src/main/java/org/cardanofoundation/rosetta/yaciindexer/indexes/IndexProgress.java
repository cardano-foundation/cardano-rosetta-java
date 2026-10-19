package org.cardanofoundation.rosetta.yaciindexer.indexes;

import java.time.Instant;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record IndexProgress(
    @Nonnull IndexLifecycleState overallState,
    @Nonnull List<IndexItemStatus> indexes,
    @Nullable Instant lastProgressAt,
    int totalRequired,
    int totalReady,
    int totalMissing,
    int totalFailed
) {}
