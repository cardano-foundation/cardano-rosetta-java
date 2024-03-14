package org.cardanofoundation.rosetta.api.account.model.dto;

import java.math.BigInteger;

import lombok.Builder;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;

@Builder
public record AddressBalanceDTO(String address,
                                String unit,
                                Long slot,
                                BigInteger quantity,
                                String policy,
                                String assetName) {

  public static AddressBalanceDTO fromEntity(AddressBalanceEntity addressBalanceEntity) {
    return AddressBalanceDTO.builder()
        .assetName(addressBalanceEntity.getAssetName())
        .address(addressBalanceEntity.getAddress())
        .unit(addressBalanceEntity.getUnit())
        .slot(addressBalanceEntity.getSlot())
        .quantity(addressBalanceEntity.getQuantity())
        .policy(addressBalanceEntity.getPolicy())
        .build();
  }
}
