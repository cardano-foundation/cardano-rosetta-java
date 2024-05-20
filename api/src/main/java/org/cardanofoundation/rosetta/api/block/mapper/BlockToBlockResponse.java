package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {BlockTxToRosettaTransaction.class})
public interface BlockToBlockResponse {

  @Mapping(target = "block.blockIdentifier.hash", source = "hash")
  @Mapping(target = "block.blockIdentifier.index", source = "number")
  @Mapping(target = "block.parentBlockIdentifier.hash", source = "previousBlockHash")
  @Mapping(target = "block.parentBlockIdentifier.index", source = "previousBlockNumber")
  @Mapping(target = "block.timestamp", source = "createdAt")
  @Mapping(target = "block.metadata.transactionsCount", source = "transactionsCount")
  @Mapping(target = "block.metadata.createdBy", source = "createdBy")
  @Mapping(target = "block.metadata.size", source = "size")
  @Mapping(target = "block.metadata.slotNo", source = "slotNo")
  @Mapping(target = "block.metadata.epochNo", source = "epochNo")
  @Mapping(target = "block.transactions", source = "transactions")
  BlockResponse toDto(Block model);

}
