package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import javax.annotation.Nullable;

public record IndexItemStatus(
    String name,
    IndexItemState state,
    @Nullable String errorMessage
) {}
