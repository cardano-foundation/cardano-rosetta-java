package org.cardanofoundation.rosetta.api.account.model.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AddressBalanceId implements Serializable {

  private String address;
  private String unit;
  private Long slot;
}
