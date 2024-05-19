package org.cardanofoundation.rosetta.api.block.mapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableInt;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Mapping(target = "transactionIdentifier.hash", source = "hash")
  @Mapping(target = "metadata.size", source = "size")
  @Mapping(target = "metadata.scriptSize", source = "scriptSize")
  @Mapping(target = "operations", source = "source")
  abstract Transaction toDto(BlockTx source);

  List<Operation> mapOperations(BlockTx source){
    List<Operation> operations = new ArrayList<>();
      MutableInt ix = new MutableInt(0);
//    inputToOperation.convert(source.getInputs(), status, ix);
    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
            .flatMap(List::stream)
            .map(input -> inputToOperation.toDto(input, status, ix.getAndIncrement()))
            .toList();
    operations.addAll(inpOps);
//    operations.addAll(withdrawalToOperation.convert(source.getWithdrawals(), status, ix));
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
//
//  final ModelMapper modelMapper;
//
//  final InputToOperation inputToOperation;
//  final OutputToOperation outputToOperation;
//  final StakeRegistrationToOperation stakeToOperation;
//  final DelegationToOperation delegationToOperation;
//  final PoolRetirementToOperation poolRetirementToOperation;
//  final PoolRegistrationToOperation poolRegistrationToOperation;
//  final WithdrawalToOperation withdrawalToOperation;
//
//
//
//  /**
//   * Maps a TransactionDto to a Rosetta compatible BlockTx.
//   *
//   * @param model       The Cardano transaction to be mapped
//   * @return The Rosetta compatible Transaction
//   */
//  public Transaction toDto(BlockTx model) {
//    return modelMapper.typeMap(BlockTx.class, Transaction.class)
//        .addMappings(mapper -> {
//          mapper.<String>map(BlockTx::getHash,
//              (dest, v) -> dest.getTransactionIdentifier().setHash(v));
//          mapper.<Long>map(BlockTx::getSize, (dest, v) -> dest.getMetadata().setSize(v));
//          mapper.<Long>map(BlockTx::getScriptSize,
//              (dest, v) -> dest.getMetadata().setScriptSize(v));
//        })
//
//        .setPostConverter(ctx -> {
//          MutableInt ix = new MutableInt(0);
//          @NotNull @Valid List<Operation> destOp = ctx.getDestination().getOperations();
//          List<Operation> inpOps = inputToOperation.convert(model.getInputs(), status, ix);
//          destOp.addAll(inpOps);
//          destOp.addAll(withdrawalToOperation.convert(model.getWithdrawals(), status, ix));
//          destOp.addAll(stakeToOperation.convert(model.getStakeRegistrations(), status, ix));
//          destOp.addAll(delegationToOperation.convert(model.getDelegations(), status, ix));
//          destOp.addAll(poolRegistrationToOperation.convert(model.getPoolRegistrations(), status, ix));
//          destOp.addAll(poolRetirementToOperation.convert(model.getPoolRetirements(), status, ix));
//
//          List<Operation> outOps = outputToOperation.convert(model.getOutputs(), status, ix);
//          outOps.forEach(op -> op.setRelatedOperations(getOperationIndexes(inpOps)));
//
//          destOp.addAll(outOps);
//          return ctx.getDestination();
//        })
//        .map(model);
//
//  }
//
//
  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
        .map(operation -> OperationIdentifier
            .builder()
            .index(operation.getOperationIdentifier().getIndex()).build())
        .toList();
  }
}
