package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface DelegationEntityToDelegation {

  Delegation toDto(DelegationEntity entity);

}
