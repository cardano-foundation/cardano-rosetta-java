package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {BlockTxToRosettaTransaction.class})
public interface BlockTxToBlockTxResponse {

  @Mapping(target = "transaction", source = "model")
  BlockTransactionResponse toDto(BlockTx model);

}
