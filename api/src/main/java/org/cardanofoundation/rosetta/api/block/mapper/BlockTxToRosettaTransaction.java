package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.mutable.MutableInt;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Transaction;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

@OpenApiMapper
@AllArgsConstructor
public class BlockTxToRosettaTransaction {

  final ModelMapper modelMapper;

  @Autowired
  private InputToOperation inputToOperation;

  final OutputToOperation outputToOperation;
  final StakeRegistrationToOperation stakeToOperation;
  final DelegationToOperation delegationToOperation;
  final PoolRetirementToOperation poolRetirementToOperation;
  final PoolRegistrationToOperation poolRegistrationToOperation;
  final WithdrawalToOperation withdrawalToOperation;

  private static final OperationStatus status = OperationStatus.builder()
      .status(SUCCESS_OPERATION_STATUS.getStatus())
      .build();


  /**
   * Maps a TransactionDto to a Rosetta compatible BlockTx.
   *
   * @param model       The Cardano transaction to be mapped
   * @return The Rosetta compatible Transaction
   */
  public Transaction toDto(BlockTx model) {
    return modelMapper.typeMap(BlockTx.class, Transaction.class)
        .addMappings(mapper -> {
          mapper.<String>map(BlockTx::getHash,
              (dest, v) -> dest.getTransactionIdentifier().setHash(v));
          mapper.<Long>map(BlockTx::getSize, (dest, v) -> dest.getMetadata().setSize(v));
          mapper.<Long>map(BlockTx::getScriptSize,
              (dest, v) -> dest.getMetadata().setScriptSize(v));
        })

        .setPostConverter(ctx -> {
          MutableInt ix = new MutableInt(0);
          @NotNull @Valid List<Operation> destOp = ctx.getDestination().getOperations();
          List<Operation> inpOps = inputToOperation.convert(model.getInputs(), status, ix);
          destOp.addAll(inpOps);
          destOp.addAll(withdrawalToOperation.convert(model.getWithdrawals(), status, ix));
          destOp.addAll(stakeToOperation.convert(model.getStakeRegistrations(), status, ix));
          destOp.addAll(delegationToOperation.convert(model.getDelegations(), status, ix));
          destOp.addAll(poolRegistrationToOperation.convert(model.getPoolRegistrations(), status, ix));
          destOp.addAll(poolRetirementToOperation.convert(model.getPoolRetirements(), status, ix));

          List<Operation> outOps = outputToOperation.convert(model.getOutputs(), status, ix);
          outOps.forEach(op -> op.setRelatedOperations(getOperationIndexes(inpOps)));

          destOp.addAll(outOps);
          return ctx.getDestination();
        })
        .map(model);

  }


  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
        .map(operation -> OperationIdentifier
            .builder()
            .index(operation.getOperationIdentifier().getIndex()).build())
        .toList();
  }
}
