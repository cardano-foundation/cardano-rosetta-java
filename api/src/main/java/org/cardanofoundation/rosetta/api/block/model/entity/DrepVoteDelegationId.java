package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class DrepVoteDelegationId implements Serializable {

  private String txHash;
  private long certIndex;
}
