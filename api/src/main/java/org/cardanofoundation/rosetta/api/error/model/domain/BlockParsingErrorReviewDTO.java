package org.cardanofoundation.rosetta.api.error.model.domain;

import java.time.LocalDateTime;

public record BlockParsingErrorReviewDTO(
        Integer id,
        Long block,
        String errorCode,
        String reason,
        String details,
        ReviewStatus status,
        String comment,
        String checkedBy,
        String note,
        LocalDateTime lastUpdated
) {}
