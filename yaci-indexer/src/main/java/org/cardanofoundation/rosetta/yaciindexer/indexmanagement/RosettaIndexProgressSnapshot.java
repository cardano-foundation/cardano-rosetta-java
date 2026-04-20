package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record RosettaIndexProgressSnapshot(
    @NotNull IndexLifecycleState overallState,
    @NotNull List<IndexItemStatus> indexes,
    @Nullable Instant lastProgressAt,
    int totalRequired,
    int totalReady,
    int totalMissing,
    int totalFailed
) {}
