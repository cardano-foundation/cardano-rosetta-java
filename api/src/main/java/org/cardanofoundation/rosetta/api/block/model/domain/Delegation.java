package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.Builder;
import lombok.Data;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;

@Data
@Builder
public class Delegation {

  private String txHash;

  private long certIndex;

  private String poolId;

  private String address;

  public static Delegation fromEntity(DelegationEntity entity) {
    return Delegation.builder()
        .txHash(entity.getTxHash())
        .certIndex(entity.getCertIndex())
        .poolId(entity.getPoolId())
        .address(entity.getAddress())
        .build();
  }
}