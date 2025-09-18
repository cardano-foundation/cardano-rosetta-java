package org.cardanofoundation.rosetta.common.mapper.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.MutableInt;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OperationMapperService {

  final TransactionMapper transactionMapper;

  final OperationStatus successOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.SUCCESS_OPERATION_STATUS.getStatus())
          .build();

  final OperationStatus invalidOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.INVALID_OPERATION_STATUS.getStatus())
          .build();

  @Named("mapTransactionsToOperations")
  public List<Operation> mapTransactionsToOperations(BlockTx source){
    List<Operation> operations = new ArrayList<>();
    MutableInt ix = new MutableInt(0);
    OperationStatus txStatus = source.isInvalid() ? invalidOperationStatus: successOperationStatus;

    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
            .flatMap(List::stream)
            .map(input -> transactionMapper.mapInputUtxoToOperation(input, txStatus, ix.getAndIncrement()))
            .toList();

    operations.addAll(inpOps);

    operations.addAll(Optional.ofNullable(source.getWithdrawals()).stream()
            .flatMap(List::stream)
            .map(withdrawal -> transactionMapper.mapWithdrawalToOperation(withdrawal, txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getStakeRegistrations()).stream()
            .flatMap(List::stream)
            .map(stakeRegistration -> transactionMapper.mapStakeRegistrationToOperation(stakeRegistration,
                    txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getStakePoolDelegations()).stream()
            .flatMap(List::stream)
            .map(delegation -> transactionMapper.mapStakeDelegationToOperation(delegation, txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getDRepDelegations()).stream()
            .flatMap(List::stream)
            .map(delegation -> transactionMapper.mapDRepDelegationToOperation(delegation, txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getPoolRegistrations()).stream()
            .flatMap(List::stream)
            .map(poolRegistration -> transactionMapper.mapPoolRegistrationToOperation(poolRegistration,
                    txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getGovernancePoolVotes()).stream()
            .flatMap(List::stream)
            .map(governanceVote -> transactionMapper.mapGovernanceVoteToOperation(governanceVote,
                    txStatus, ix.getAndIncrement()))
            .toList());

    operations.addAll(Optional.ofNullable(source.getPoolRetirements()).stream()
            .flatMap(List::stream)
            .map(poolRetirement -> transactionMapper.mapPoolRetirementToOperation(poolRetirement,
                    txStatus, ix.getAndIncrement()))
            .toList());

    if (!source.isInvalid()) {
      List<Operation> outOps = Optional.ofNullable(source.getOutputs()).stream()
              .flatMap(List::stream)
              .map(output -> {
                Operation operation = transactionMapper.mapOutputUtxoToOperation(output,
                        txStatus, ix.getAndIncrement());
                // It's needed to add output index for output Operations, this represents the output index of these utxos
                Optional.ofNullable(operation.getOperationIdentifier())
                        .ifPresent(operationIdentifier ->
                                Optional.ofNullable(output.getOutputIndex()).ifPresent(outputIndex ->
                                        operationIdentifier.networkIndex((long) outputIndex)));
                return operation;
              })
              .toList();
      outOps.forEach(op -> op.setRelatedOperations(getOperationIndexes(inpOps)));

      operations.addAll(outOps);
    }

    return operations;
  }

  public List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
            .map(operation -> OperationIdentifier
                    .builder()
                    .index(operation.getOperationIdentifier().getIndex()).build())
            .toList();
  }

}
