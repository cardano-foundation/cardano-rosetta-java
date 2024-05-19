package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.mutable.MutableInt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionIdentifier;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

@Mapper(config = BaseMapper.class, uses = {InputToOperation.class, StakeRegistrationToOperation.class,
    DelegationToOperation.class, PoolRegistrationToOperation.class, PoolRetirementToOperation.class,
    WithdrawalToOperation.class, OutputToOperation.class})
public abstract class BlockTxToRosettaTransaction {

  @Autowired
  InputToOperation inputToOperation;
  @Autowired
  StakeRegistrationToOperation stakeRegistrationToOperation;
  @Autowired
  DelegationToOperation delegationToOperation;
  @Autowired
  PoolRegistrationToOperation poolRegistrationToOperation;
  @Autowired
  PoolRetirementToOperation poolRetirementToOperation;
  @Autowired
  WithdrawalToOperation withdrawalToOperation;
  @Autowired
  OutputToOperation outputToOperation;


  final OperationStatus status = OperationStatus.builder()
      .status(SUCCESS_OPERATION_STATUS.getStatus())
      .build();

  @Mapping(target = "transactionIdentifier", source = "hash", qualifiedByName = "getTransactionIdentifier")
  @Mapping(target = "metadata.size", source = "size")
  @Mapping(target = "metadata.scriptSize", source = "scriptSize")
  @Mapping(target = "operations", source = "source", qualifiedByName = "mapOperations")
  @Named("toRosettaTransaction")
  abstract Transaction toDto(BlockTx source);

  @Named("getTransactionIdentifier")
  public TransactionIdentifier getTransactionIdentifier(String hash) {
    return TransactionIdentifier.builder().hash(hash).build();
  }

  @Named("mapOperations")
  List<Operation> mapOperations(BlockTx source){
    List<Operation> operations = new ArrayList<>();
      MutableInt ix = new MutableInt(0);
    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
            .flatMap(List::stream)
            .map(input -> inputToOperation.toDto(input, status, ix.getAndIncrement()))
            .toList();
    operations.addAll(inpOps);
    operations.addAll(Optional.ofNullable(source.getWithdrawals()).stream()
            .flatMap(List::stream)
            .map(withdrawal -> withdrawalToOperation.toDto(withdrawal, status, ix.getAndIncrement()))
            .toList());
    operations.addAll(Optional.ofNullable(source.getStakeRegistrations()).stream()
            .flatMap(List::stream)
            .map(stakeRegistration -> stakeRegistrationToOperation.toDto(stakeRegistration, status, ix.getAndIncrement()))
            .toList());
    operations.addAll(Optional.ofNullable(source.getDelegations()).stream()
            .flatMap(List::stream)
            .map(delegation -> delegationToOperation.toDto(delegation, status, ix.getAndIncrement()))
            .toList());
    operations.addAll(Optional.ofNullable(source.getPoolRegistrations()).stream()
            .flatMap(List::stream)
            .map(poolRegistration -> poolRegistrationToOperation.toDto(poolRegistration, status, ix.getAndIncrement()))
            .toList());
    operations.addAll(Optional.ofNullable(source.getPoolRetirements()).stream()
            .flatMap(List::stream)
            .map(poolRetirement -> poolRetirementToOperation.toDto(poolRetirement, status, ix.getAndIncrement()))
            .toList());

      List<Operation> outOps = Optional.ofNullable(source.getOutputs()).stream()
              .flatMap(List::stream)
              .map(output -> outputToOperation.toDto(output, status, ix.getAndIncrement()))
              .toList();
      outOps.forEach(op -> op.setRelatedOperations(getOperationIndexes(inpOps)));

    operations.addAll(outOps);

    return operations;
  }

  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
        .map(operation -> OperationIdentifier
            .builder()
            .index(operation.getOperationIdentifier().getIndex()).build())
        .toList();
  }
}
