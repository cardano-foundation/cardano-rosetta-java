package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class WithdrawalId implements Serializable {

  private String address;
  private String txHash;
}
