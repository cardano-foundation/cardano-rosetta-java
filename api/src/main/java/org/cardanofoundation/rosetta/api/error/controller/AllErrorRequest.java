package org.cardanofoundation.rosetta.api.error.controller;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

import javax.annotation.Nullable;

@Data
@Builder
public class AllErrorRequest {

    @Nullable
    public ReviewStatus status;

}
