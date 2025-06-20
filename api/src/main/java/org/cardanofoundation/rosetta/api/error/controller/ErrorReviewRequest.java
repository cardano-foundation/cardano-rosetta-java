package org.cardanofoundation.rosetta.api.error.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class ErrorReviewRequest {

    private Integer id;

    @NotNull
    private ReviewStatus status;

    @Nullable
    private String comment;

    @Nullable
    private String checkedBy;

}
