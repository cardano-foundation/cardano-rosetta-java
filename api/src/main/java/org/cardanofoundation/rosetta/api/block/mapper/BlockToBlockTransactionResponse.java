package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class BlockToBlockTransactionResponse {

  final ModelMapper modelMapper;

  final TranToRosettaTransaction tranToRosettaTransaction;


  public BlockTransactionResponse toDto(Tran bt, String poolDeposit) {
    BlockTransactionResponse b = new BlockTransactionResponse(); //TODO rewrite to modelMapper
    b.setTransaction(tranToRosettaTransaction.toDto(bt, poolDeposit));
    return b;
  }
}
