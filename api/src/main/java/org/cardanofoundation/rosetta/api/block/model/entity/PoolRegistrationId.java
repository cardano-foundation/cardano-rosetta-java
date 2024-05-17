package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PoolRegistrationId implements Serializable {

  private String txHash;
  private int certIndex;
}
