package org.cardanofoundation.rosetta.common.mapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.entity.AmtEntity;

public class AmtEntityToAmt {

  public static Amt fromEntity(AmtEntity amtEntity) {
    return Amt.builder()
        .assetName(amtEntity.getAssetName())
        .unit(amtEntity.getUnit())
        .quantity(amtEntity.getQuantity())
        .policyId(amtEntity.getPolicyId())
        .build();
  }
}
