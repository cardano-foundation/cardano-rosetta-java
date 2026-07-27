package org.cardanofoundation.rosetta.yaciindexer.indexes;

import javax.annotation.Nullable;

public record IndexItemStatus(
    String name,
    IndexItemState state,
    @Nullable String errorMessage
) {}
