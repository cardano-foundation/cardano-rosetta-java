package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Transaction;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

@OpenApiMapper
@AllArgsConstructor
public class TranToRosettaTransaction {

  final ModelMapper modelMapper;

  /**
   * Maps a TransactionDto to a Rosetta compatible BlockTx.
   *
   * @param model       The Cardano transaction to be mapped
   * @param poolDeposit The pool deposit
   * @return The Rosetta compatible Transaction
   */
  public Transaction toDto(BlockTx model,
      String poolDeposit) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(BlockTx.class, Transaction.class))
        .orElseGet(() -> modelMapper.createTypeMap(BlockTx.class, Transaction.class))
        .addMappings(mapper -> {
          mapper.<String>map(BlockTx::getHash,
              (dest, v) -> dest.getTransactionIdentifier().setHash(v));

          mapper.<Long>map(BlockTx::getSize, (dest, v) -> dest.getMetadata().setSize(v));
          mapper.<Long>map(BlockTx::getScriptSize, (dest, v) -> dest.getMetadata().setScriptSize(v));

        })
        .setPostConverter(ctx -> {

          OperationStatus status = OperationStatus.builder()
              .status(SUCCESS_OPERATION_STATUS.getStatus()) // TODO need to check the right status
              .build();

          List<Operation> operations =
              OperationDataMapper.getAllOperations(model, poolDeposit, status);

          ctx.getDestination().setOperations(operations);
          return ctx.getDestination();

        }).map(model);

  }
}
