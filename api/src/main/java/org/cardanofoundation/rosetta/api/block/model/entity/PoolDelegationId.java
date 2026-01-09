package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PoolDelegationId implements Serializable {

  private String txHash;
  private long certIndex;
}
