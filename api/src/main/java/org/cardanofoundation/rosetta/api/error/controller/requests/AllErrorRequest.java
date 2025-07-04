package org.cardanofoundation.rosetta.api.error.controller.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

import javax.annotation.Nullable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllErrorRequest {

    @Nullable
    public ReviewStatus status;

}
