package org.cardanofoundation.rosetta.api.error.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

import javax.annotation.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockParsingErrorsReadRequest {

    @Nullable
    private ReviewStatus status; // filtering by review status is optional

}
