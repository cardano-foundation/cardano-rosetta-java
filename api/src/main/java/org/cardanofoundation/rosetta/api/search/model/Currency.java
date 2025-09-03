package org.cardanofoundation.rosetta.api.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Currency {

    @Nullable
    private String symbol;

    @Nullable
    private Integer decimals;

    @Nullable
    private String policyId;

}
