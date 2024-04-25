package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;

@Data
@Builder //TODO saa: refactor tests and remove builder and *argConstructor annotations
@NoArgsConstructor
@AllArgsConstructor
public class Delegation {

  private String txHash;

  private long certIndex;

  private String poolId;

  private String address;

}