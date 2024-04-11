package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.modelmapper.ModelMapper;

@OpenApiMapper
@AllArgsConstructor
public class WithdrawalEntityToWithdrawal {

  private final ModelMapper modelMapper;

  public Withdrawal fromEntity(WithdrawalEntity model) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(WithdrawalEntity.class, Withdrawal.class))
        .orElseGet(() -> modelMapper.createTypeMap(WithdrawalEntity.class, Withdrawal.class))
        .addMappings(mp -> {
          mp.map(WithdrawalEntity::getAddress, Withdrawal::setStakeAddress);
        })
        .map(model);
  }
}
