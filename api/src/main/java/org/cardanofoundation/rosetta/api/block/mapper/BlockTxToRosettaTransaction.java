package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;

import org.apache.commons.lang3.mutable.MutableInt;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Transaction;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

@OpenApiMapper
@AllArgsConstructor
public class BlockTxToRosettaTransaction {

  final ModelMapper modelMapper;

  final InputToOperation inputToOperation;
  final OutputToOperation outputToOperation;
  final StakeRegistrationToOperation stakeToOperation;
  final DelegationToOperation delegationToOperation;
  final PoolRetirementToOperation poolRetirementToOperation;
  final PoolRegistrationToOperation poolRegistrationToOperation;

  private static final OperationStatus status = OperationStatus.builder()
      .status(SUCCESS_OPERATION_STATUS.getStatus()) // TODO saa: need to check the right status
      .build();


  /**
   * Maps a TransactionDto to a Rosetta compatible BlockTx.
   *
   * @param model       The Cardano transaction to be mapped
   * @param poolDeposit The pool deposit //TODO saa: make injectable from the protocol params
   * @return The Rosetta compatible Transaction
   */
  public Transaction toDto(BlockTx model, @Deprecated String poolDeposit) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(BlockTx.class, Transaction.class))
        .orElseGet(() -> modelMapper.createTypeMap(BlockTx.class, Transaction.class))
        .addMappings(mapper -> {
          mapper.<String>map(BlockTx::getHash,
              (dest, v) -> dest.getTransactionIdentifier().setHash(v));

          mapper.<Long>map(BlockTx::getSize, (dest, v) -> dest.getMetadata().setSize(v));
          mapper.<Long>map(BlockTx::getScriptSize,
              (dest, v) -> dest.getMetadata().setScriptSize(v));

        })

        .setPostConverter(ctx -> {
//          List<Operation> operations =
//              OperationDataMapper.getAllOperations(model, poolDeposit, status);
//
//          ctx.getDestination().setOperations(operations);
//          return ctx.getDestination();
          MutableInt ix = new MutableInt(0);
          @NotNull @Valid List<Operation> destOp = ctx.getDestination().getOperations();
          destOp.addAll(inputToOperation.convert(model.getInputs(), status, ix));
          destOp.addAll(stakeToOperation.convert(model.getStakeRegistrations(), status, ix));
          destOp.addAll(delegationToOperation.convert(model.getDelegations(), status, ix));
          destOp.addAll(poolRegistrationToOperation.convert(model.getPoolRegistrations(), status, ix));
          destOp.addAll(poolRetirementToOperation.convert(model.getPoolRetirements(), status, ix));
          destOp.addAll(outputToOperation.convert(model.getOutputs(), status, ix));
          return ctx.getDestination();
        })
        .map(model);

  }
}
