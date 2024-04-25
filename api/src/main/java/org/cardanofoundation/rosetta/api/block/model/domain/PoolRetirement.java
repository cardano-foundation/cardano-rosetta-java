package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.Builder;
import lombok.Data;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;

@Data
@Builder
public class PoolRetirement {

  private String txHash;

  private int certIndex;

  private String poolId;

  private Integer epoch;

}
