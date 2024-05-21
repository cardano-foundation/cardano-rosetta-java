package org.cardanofoundation.rosetta.api.block.mapper;


import java.util.concurrent.TimeUnit;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {BlockTxToEntity.class})
public interface BlockToEntity {

  @Mapping(target = "previousBlockHash", source = "prev.hash", defaultExpression = "java(entity.getHash())")
  @Mapping(target = "previousBlockNumber", source = "prev.number", defaultValue = "0L")
  @Mapping(target = "transactionsCount", source = "noOfTxs")
  @Mapping(target = "epochNo", source = "epochNumber")
  @Mapping(target = "createdBy", source = "slotLeader")
  @Mapping(target = "createdAt", source = "blockTimeInSeconds", qualifiedByName = "toMillis")
  @Mapping(target = "size", source = "blockBodySize")
  @Mapping(target = "slotNo", source = "slot")
  Block fromEntity(BlockEntity entity);

  @Named("toMillis")
  default Long toMillis(Long seconds) {
    return TimeUnit.SECONDS.toMillis(seconds);
  }

}
