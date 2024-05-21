package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.entity.projection.BlockIdentifierProjection;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface BlockIdentifierProjectionToBlockIdentifierExtended {

  BlockIdentifierExtended toBlockIdentifierExtended(BlockIdentifierProjection projection);

}
