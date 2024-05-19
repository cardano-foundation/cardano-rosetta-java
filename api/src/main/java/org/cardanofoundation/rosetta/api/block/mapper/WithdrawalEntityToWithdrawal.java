package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface WithdrawalEntityToWithdrawal {

  @Mapping(target = "stakeAddress", source = "address")
  Withdrawal fromEntity(WithdrawalEntity model);
}
