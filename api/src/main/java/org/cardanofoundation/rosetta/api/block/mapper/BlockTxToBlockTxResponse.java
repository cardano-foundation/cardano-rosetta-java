package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockTransactionResponse;

@Mapper(config = BaseMapper.class, uses = {BlockTxToRosettaTransaction.class})
public interface BlockTxToBlockTxResponse {

  @Mapping(target = "transaction", source = "model", qualifiedByName = "toDto")
  BlockTransactionResponse toDto(BlockTx model);

}
