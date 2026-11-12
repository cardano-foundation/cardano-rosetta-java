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
) {
    /**
     * Hibernate 7 types the JPQL {@code CASE} on a string-valued enum as {@code String}.
     * This constructor keeps the canonical record component a {@link ReviewStatus}.
     */
    public BlockParsingErrorReviewDTO(
            Integer id,
            Long block,
            String errorCode,
            String reason,
            String details,
            String status,
            String comment,
            String checkedBy,
            String note,
            LocalDateTime lastUpdated
    ) {
        this(id, block, errorCode, reason, details,
                status == null ? null : ReviewStatus.valueOf(status),
                comment, checkedBy, note, lastUpdated);
    }
}
