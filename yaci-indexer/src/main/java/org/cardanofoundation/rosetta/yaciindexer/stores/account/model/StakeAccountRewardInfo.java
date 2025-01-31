package org.cardanofoundation.rosetta.yaciindexer.stores.account.model;

import java.math.BigInteger;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StakeAccountRewardInfo {

    private String stakeAddress;
    private BigInteger withdrawableAmount;

}
