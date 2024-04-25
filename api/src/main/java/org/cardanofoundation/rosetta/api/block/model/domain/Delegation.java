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

}