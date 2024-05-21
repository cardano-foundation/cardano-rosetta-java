package org.cardanofoundation.rosetta.api.account.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class StakeAddressBalanceId implements Serializable {

  private String address;
  private Long slot;
}
