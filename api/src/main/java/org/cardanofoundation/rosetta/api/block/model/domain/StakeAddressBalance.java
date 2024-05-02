package org.cardanofoundation.rosetta.api.block.model.domain;

import java.math.BigInteger;

import lombok.Builder;
import lombok.Data;

import org.cardanofoundation.rosetta.api.account.model.entity.StakeAddressBalanceEntity;

@Data
@Builder
public class StakeAddressBalance {

  private String address;
  private Long slot;
  private BigInteger quantity;
  private Long blockNumber;
  private Integer epoch;


  public static StakeAddressBalance fromEntity(StakeAddressBalanceEntity entity) {
    return StakeAddressBalance.builder()
        .address(entity.getAddress())
        .slot(entity.getSlot())
        .quantity(entity.getQuantity())
        .blockNumber(entity.getBlockNumber())
        .epoch(entity.getEpoch())
        .build();
  }
}
