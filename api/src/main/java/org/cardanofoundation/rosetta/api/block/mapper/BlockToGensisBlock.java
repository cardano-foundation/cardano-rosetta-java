package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface BlockToGensisBlock {

  GenesisBlock toGenesisBlock(BlockEntity entity);

}
