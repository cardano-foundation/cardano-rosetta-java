package org.cardanofoundation.rosetta.common.mapper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.mutable.MutableInt;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;

@Component
@RequiredArgsConstructor
public class OperationMapperUtils {

  final ProtocolParamService protocolParamService;
  final TransactionMapper transactionMapper;

  final OperationStatus successOperationStatus = OperationStatus.builder()
      .status(RosettaConstants.SUCCESS_OPERATION_STATUS.getStatus())
      .build();

  @Named("mapTransactionsToOperations")
  public List<Operation> mapTransactionsToOperations(BlockTx source){
    List<Operation> operations = new ArrayList<>();
    MutableInt ix = new MutableInt(0);
    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
        .flatMap(List::stream)
        .map(input -> transactionMapper.mapInputUtxoToOperation(input, successOperationStatus, ix.getAndIncrement()))
        .toList();
    operations.addAll(inpOps);
    operations.addAll(Optional.ofNullable(source.getWithdrawals()).stream()
        .flatMap(List::stream)
        .map(withdrawal -> transactionMapper.mapWithdrawalToOperation(withdrawal, successOperationStatus, ix.getAndIncrement()))
        .toList());
    operations.addAll(Optional.ofNullable(source.getStakeRegistrations()).stream()
        .flatMap(List::stream)
        .map(stakeRegistration -> transactionMapper.mapStakeRegistrationToOperation(stakeRegistration,
            successOperationStatus, ix.getAndIncrement()))
        .toList());
    operations.addAll(Optional.ofNullable(source.getDelegations()).stream()
        .flatMap(List::stream)
        .map(delegation -> transactionMapper.mapDelegationToOperation(delegation, successOperationStatus, ix.getAndIncrement()))
        .toList());
    operations.addAll(Optional.ofNullable(source.getPoolRegistrations()).stream()
        .flatMap(List::stream)
        .map(poolRegistration -> transactionMapper.mapPoolRegistrationToOperation(poolRegistration,
            successOperationStatus, ix.getAndIncrement()))
        .toList());
    operations.addAll(Optional.ofNullable(source.getPoolRetirements()).stream()
        .flatMap(List::stream)
        .map(poolRetirement -> transactionMapper.mapPoolRetirementToOperation(poolRetirement,
            successOperationStatus, ix.getAndIncrement()))
        .toList());

    List<Operation> outOps = Optional.ofNullable(source.getOutputs()).stream()
        .flatMap(List::stream)
        .map(output -> transactionMapper.mapOutputUtxoToOperation(output, successOperationStatus, ix.getAndIncrement()))
        .toList();
    outOps.forEach(op -> op.setRelatedOperations(getOperationIndexes(inpOps)));

    operations.addAll(outOps);

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
