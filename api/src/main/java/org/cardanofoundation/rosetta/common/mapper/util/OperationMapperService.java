package org.cardanofoundation.rosetta.common.mapper.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.MutableInt;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.mapstruct.Named;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Component
@RequiredArgsConstructor
public class OperationMapperService {

  final TransactionMapper transactionMapper;
  final TokenRegistryService tokenRegistryService;

  final OperationStatus successOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.SUCCESS_OPERATION_STATUS.getStatus())
          .build();

  final OperationStatus invalidOperationStatus = OperationStatus.builder()
          .status(RosettaConstants.INVALID_OPERATION_STATUS.getStatus())
          .build();

  @Named("mapTransactionsToOperations")
  public List<Operation> mapTransactionsToOperations(BlockTx source) {
    List<Operation> operations = new ArrayList<>();
    MutableInt ix = new MutableInt(0);
    OperationStatus txStatus = source.isInvalid() ? invalidOperationStatus: successOperationStatus;

    // Collect all native token assets from inputs and outputs for batch metadata fetch
    Map<Asset, CurrencyMetadataResponse> tokenMetadataMap = collectAndFetchTokenMetadata(source);

    List<Operation> inpOps = Optional.ofNullable(source.getInputs()).stream()
            .flatMap(List::stream)
            .map(input -> transactionMapper.mapInputUtxoToOperation(input, txStatus, ix.getAndIncrement(), tokenMetadataMap))
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
                        txStatus, ix.getAndIncrement(), tokenMetadataMap);
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

  /**
   * Collects all native token assets from transaction inputs and outputs,
   * then makes a single batch call to fetch token metadata for all assets.
   */
  private Map<Asset, CurrencyMetadataResponse> collectAndFetchTokenMetadata(BlockTx source) {
    Set<Asset> allAssets = new HashSet<>();
    
    // Collect assets from inputs
    Optional.ofNullable(source.getInputs()).ifPresent(inputs ->
        inputs.forEach(input ->
            Optional.ofNullable(input.getAmounts()).ifPresent(amounts ->
                allAssets.addAll(extractAssetsFromAmounts(amounts)))));
    
    // Collect assets from outputs
    Optional.ofNullable(source.getOutputs()).ifPresent(outputs ->
        outputs.forEach(output ->
            Optional.ofNullable(output.getAmounts()).ifPresent(amounts ->
                allAssets.addAll(extractAssetsFromAmounts(amounts)))));
    
    // If no native tokens, return empty map
    if (allAssets.isEmpty()) {
      return Collections.emptyMap();
    }

    // Make single batch call to fetch metadata for all assets
    return tokenRegistryService.getTokenMetadataBatch(allAssets);
  }

  /**
   * Extracts Asset objects from a list of amounts, filtering out ADA/lovelace.
   */
  private Set<Asset> extractAssetsFromAmounts(List<Amt> amounts) {
    return amounts.stream()
        .filter(amount -> !LOVELACE.equals(amount.getAssetName()))
        .map(amount -> Asset.builder()
            .policyId(amount.getPolicyId())
            .assetName(amount.getAssetName())
            .build())
        .collect(Collectors.toSet());
  }

  public List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
            .map(operation -> OperationIdentifier
                    .builder()
                    .index(operation.getOperationIdentifier().getIndex()).build())
            .toList();
  }

}
