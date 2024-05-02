package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class WithdrawalEntityToWithdrawal {

  private final ModelMapper modelMapper;

  public Withdrawal fromEntity(WithdrawalEntity model) {
    return modelMapper.typeMap(WithdrawalEntity.class, Withdrawal.class)
        .addMappings(mp -> mp.map(WithdrawalEntity::getAddress, Withdrawal::setStakeAddress))
        .map(model);
  }
}
