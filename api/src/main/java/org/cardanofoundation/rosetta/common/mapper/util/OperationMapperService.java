package org.cardanofoundation.rosetta.common.mapper.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationMapperService {

  final TransactionMapper transactionMapper;

  final OperationStatus successOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.SUCCESS_OPERATION_STATUS.getStatus())
          .build();

  final OperationStatus invalidOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.INVALID_OPERATION_STATUS.getStatus())
          .build();


  @Named("mapTransactionsToOperationsWithMetadata")
  public List<Operation> mapTransactionsToOperationsWithMetadata(BlockTx source,
                                                                 @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap) {
    List<Operation> operations = new ArrayList<>();
    MutableInt ix = new MutableInt(0);
    OperationStatus txStatus = source.isInvalid() ? invalidOperationStatus: successOperationStatus;

    // Use the pre-fetched metadata map instead of fetching again
    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
            .flatMap(List::stream)
            .map(input -> transactionMapper.mapInputUtxoToOperation(input, txStatus, ix.getAndIncrement(), metadataMap))
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
                        txStatus, ix.getAndIncrement(), metadataMap);
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

    // The Rosetta schema requires AccountIdentifier.address whenever an account object is present.
    // Some operations (e.g. inputs referencing a UTXO that is missing from the index, such as on
    // pruned instances) can end up with an account object whose address was never populated. Drop
    // the empty account object in that case so the response stays schema-valid.
    operations.forEach(OperationMapperService::removeEmptyAccount);

    return operations;
  }

  private static void removeEmptyAccount(Operation operation) {
    AccountIdentifier account = operation.getAccount();
    if (account != null && (account.getAddress() == null || account.getAddress().isBlank())) {
      operation.setAccount(null);
    }
  }

  public List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
            .map(operation -> OperationIdentifier
                    .builder()
                    .index(operation.getOperationIdentifier().getIndex()).build())
            .toList();
  }

}
