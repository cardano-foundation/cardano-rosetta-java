package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;

import lombok.Builder;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;

@Builder
public record AddressBalance(String address,
                             String unit,
                             Long slot,
                             BigInteger quantity) {

  public static AddressBalance fromEntity(AddressBalanceEntity addressBalanceEntity) {
    return AddressBalance.builder()
        .address(addressBalanceEntity.getAddress())
        .unit(addressBalanceEntity.getUnit())
        .slot(addressBalanceEntity.getSlot())
        .quantity(addressBalanceEntity.getQuantity())
        .build();
  }
}
