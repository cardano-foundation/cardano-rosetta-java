package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class TranToBlockTxResponse {

  final ModelMapper modelMapper;

  final BlockTxToRosettaTransaction blockTxToRosettaTx;


  public BlockTransactionResponse toDto(BlockTx model, String poolDeposit) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(BlockTx.class, BlockTransactionResponse.class))
        .orElseGet(() -> modelMapper.createTypeMap(BlockTx.class, BlockTransactionResponse.class))
        .setPostConverter(ctx -> {
          ctx.getDestination().setTransaction(blockTxToRosettaTx.toDto(model, poolDeposit));
          return ctx.getDestination();
        }).map(model);
  }
}
