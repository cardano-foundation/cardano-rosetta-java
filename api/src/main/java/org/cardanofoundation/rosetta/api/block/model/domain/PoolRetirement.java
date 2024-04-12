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

  public static PoolRetirement fromEntity(PoolRetirementEntity entity) {
    return PoolRetirement.builder()
        .txHash(entity.getTxHash())
        .certIndex(entity.getCertIndex())
        .poolId(entity.getPoolId())
        .epoch(entity.getEpoch())
        .build();
  }
}
