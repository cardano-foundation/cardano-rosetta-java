package org.cardanofoundation.rosetta.api.block.model.domain;

import java.math.BigInteger;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;

@Data
@Builder
public class Withdrawal {
  private String stakeAddress;
  private BigInteger amount;

  public static Withdrawal fromEntity(WithdrawalEntity withdrawalEntity) {
    return Withdrawal.builder()
        .stakeAddress(withdrawalEntity.getAddress())
        .amount(withdrawalEntity.getAmount())
        .build();
  }
}
