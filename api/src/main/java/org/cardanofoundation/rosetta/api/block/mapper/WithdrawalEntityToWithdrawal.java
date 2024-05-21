package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface WithdrawalEntityToWithdrawal {

  @Mapping(target = "stakeAddress", source = "address")
  Withdrawal fromEntity(WithdrawalEntity model);
}
