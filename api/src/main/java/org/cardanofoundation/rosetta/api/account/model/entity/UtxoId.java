package org.cardanofoundation.rosetta.api.account.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class UtxoId implements Serializable {

  private String txHash;
  private Integer outputIndex;
}
