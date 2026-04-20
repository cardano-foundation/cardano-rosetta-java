package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record IndexItemStatus(
    @NotNull String name,
    @NotNull IndexItemState state,
    @Nullable String errorMessage
) {}
