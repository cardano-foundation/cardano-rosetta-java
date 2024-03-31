package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class TranToBlockTxResponse {

  final ModelMapper modelMapper;

  final TranToRosettaTransaction tranToRosettaTransaction;


  public BlockTransactionResponse toDto(Tran model, String poolDeposit) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Tran.class, BlockTransactionResponse.class))
        .orElseGet(() -> modelMapper.createTypeMap(Tran.class, BlockTransactionResponse.class))
        .setPostConverter(ctx -> {
          ctx.getDestination().setTransaction(tranToRosettaTransaction.toDto(model, poolDeposit));
          return ctx.getDestination();
        }).map(model);
  }
}
