package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class BlockTxToBlockTxResponse {

  final ModelMapper modelMapper;

  final BlockTxToRosettaTransaction blockTxToRosettaTx;


  public BlockTransactionResponse toDto(BlockTx model) {
    return modelMapper.typeMap(BlockTx.class, BlockTransactionResponse.class)
        .setPostConverter(ctx -> {
          ctx.getDestination().setTransaction(blockTxToRosettaTx.toDto(model));
          return ctx.getDestination();
        }).map(model);
  }
}
