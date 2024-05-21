package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface PoolRetirementEntityToPoolRetirement {

  PoolRetirement toDto(PoolRetirementEntity entity);

}
