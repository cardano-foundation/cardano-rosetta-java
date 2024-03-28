package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.TokenBundleItem;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.Amt;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

@Slf4j
class OperationDataMapper {

  @NotNull
  public static List<Operation> getAllOperations(Tran transaction, String poolDeposit,
      OperationStatus status) {
    List<List<Operation>> totalOperations = new ArrayList<>();
    List<Operation> inputsAsOperations = getInputTransactionsAsOperations(transaction, status);
    totalOperations.add(inputsAsOperations);
// TODO Withdrawal currently not supported via Yaci-store. Will implemented it as soon as the PR is merged.
//        List<Operation> withdrawalsAsOperations = getWithdrawlOperations(transaction, status, totalOperations);
//        totalOperations.add(withdrawalsAsOperations);

    List<Operation> stakeRegistrationOperations = getStakeRegistrationOperations(transaction,
        status, totalOperations);
    totalOperations.add(stakeRegistrationOperations);

    List<Operation> delegationOperations = getDelegationOperations(transaction, status,
        totalOperations);
    totalOperations.add(delegationOperations);

    List<Operation> poolRegistrationOperations =
        getPoolRegistrationOperations(transaction, status, totalOperations, poolDeposit);
    totalOperations.add(poolRegistrationOperations);

    List<Operation> poolRetirementOperations =
        getPoolRetirementOperations(transaction, status, totalOperations);
    totalOperations.add(poolRetirementOperations);

    List<OperationIdentifier> relatedOperations = getOperationIndexes(inputsAsOperations);
    List<Operation> outputsAsOperations = getOutputsAsOperations(transaction, totalOperations,
        status, relatedOperations);

    totalOperations.add(outputsAsOperations);
    return totalOperations.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

//    @NotNull
//    private static List<Operation> getWithdrawlOperations(TransactionDto transactionDto, OperationStatus status, List<List<Operation>> totalOperations) {
//        return IntStream.range(0,
//                        transactionDto.getWithdrawals().size())
//                .mapToObj(index -> {
//                    Withdrawal withdrawal = transaction.getWithdrawals().get(index);
//                    return Operation.builder()
//                            .operationIdentifier(OperationIdentifier.builder().index(
//                                    getOperationCurrentIndex(totalOperations, index)).build())
//                            .type(OperationType.WITHDRAWAL.getValue())
//                            .status(status.getStatus())
//                            .account(AccountIdentifier.builder().address(withdrawal.getStakeAddress()).build())
//                            .metadata(OperationMetadata.builder()
//                                    .withdrawalAmount(DataMapper.mapAmount("-" + withdrawal.getAmount()))
//                                    .build())
//                            .build();
//                }).toList();
//    }

  @NotNull
  public static List<Operation> getInputTransactionsAsOperations(Tran transaction,
      OperationStatus status) {

    return IntStream.range(0, nullable(transaction.getInputs()).size())
        .mapToObj(index -> {
          Utxo utxoModel = transaction.getInputs().get(index);
          return createOperation((long) index,
              Constants.INPUT,
              status.getStatus(),
              utxoModel.getOwnerAddr(),
              DataMapper.mapValue(utxoModel.getLovelaceAmount().toString(), true),
              null,
              null,
              DataMapper.getCoinChange(utxoModel.getOutputIndex(), utxoModel.getTxHash(),
                  CoinAction.SPENT),
              mapToOperationMetaData(true, utxoModel.getAmounts()));
        }).toList();
  }

  private static <T> List<T> nullable(List<T> list) {
    return Optional.ofNullable(list).orElse(Collections.emptyList());
  }

  public static List<Operation> getPoolRegistrationOperations(Tran tranDto,
      OperationStatus status, List<List<Operation>> totalOperations, String poolDeposit) {
    return IntStream.range(0, nullable(tranDto.getPoolRegistrations()).size())
        .mapToObj(index -> {
          PoolRegistration poolRegistration = tranDto.getPoolRegistrations().get(index);
          return Operation.builder()
              .operationIdentifier(OperationIdentifier.builder().index(
                  getOperationCurrentIndex(totalOperations, index)).build())
              .type(OperationType.POOL_REGISTRATION.getValue())
              .status(status.getStatus())
              .account(AccountIdentifier.builder().address(poolRegistration.getPoolId()).build())
              .metadata(OperationMetadata.builder()
                  .depositAmount(DataMapper.mapAmount(poolDeposit))
                  .poolRegistrationParams(PoolRegistrationParams.builder()
                      .pledge(poolRegistration.getPledge())
                      .rewardAddress(poolRegistration.getRewardAccount())
                      .cost(poolRegistration.getCost())
                      .poolOwners(poolRegistration.getOwners().stream().toList())
                      .marginPercentage(poolRegistration.getMargin())
                      .vrfKeyHash(poolRegistration.getVrfKeyHash())
                      .relays(poolRegistration.getRelays())
                      .build())
                  .build())
              .build();
        }).toList();
  }

  public static List<Operation> getPoolRetirementOperations(Tran tranDto,
      OperationStatus status, List<List<Operation>> totalOperations) {
    return IntStream.range(0,
            nullable(tranDto.getPoolRetirements()).size())
        .mapToObj(index -> {
          PoolRetirement poolRetirement = tranDto.getPoolRetirements().get(index);
          return Operation.builder()
              .operationIdentifier(OperationIdentifier.builder().index(
                  getOperationCurrentIndex(totalOperations, index)).build())
              .type(OperationType.POOL_RETIREMENT.getValue())
              .status(status.getStatus())
              .account(AccountIdentifier.builder().address(poolRetirement.getPoolId()).build())
              .metadata(OperationMetadata.builder()
                  .epoch(poolRetirement.getEpoch())
                  .build())
              .build();
        }).toList();
  }

  public static List<Operation> getStakeRegistrationOperations(Tran transaction,
      OperationStatus status, List<List<Operation>> totalOperations) {
    return IntStream.range(0,
            nullable(transaction.getStakeRegistrations()).size())
        .mapToObj(index -> {
          StakeRegistration stakeRegistration = transaction.getStakeRegistrations().get(index);
          OperationType operationType =
              stakeRegistration.getType().equals(CertificateType.STAKE_REGISTRATION)
                  ? OperationType.STAKE_KEY_REGISTRATION :
                  stakeRegistration.getType().equals(CertificateType.STAKE_DEREGISTRATION)
                      ? OperationType.STAKE_KEY_DEREGISTRATION : null;
          if (operationType == null) {
            log.error("Invalid stake registration type {}", stakeRegistration.getType());
            return null;
          }
          return Operation.builder()
              .operationIdentifier(OperationIdentifier.builder().index(
                  getOperationCurrentIndex(totalOperations, index)).build())
              .type(operationType.getValue())
              .status(status.getStatus())
              .account(AccountIdentifier.builder().address(stakeRegistration.getAddress()).build())
              .metadata(OperationMetadata.builder()

                  .depositAmount(DataMapper.mapAmount("2000000", ADA, ADA_DECIMALS,
                      null)) // TODO need to get this from protocolparams
                  .build())
              .build();
        }).toList();
  }

  public static List<Operation> getDelegationOperations(Tran transaction,
      OperationStatus status, List<List<Operation>> totalOperations) {
    return IntStream.range(0,
            nullable(transaction.getDelegations()).size())
        .mapToObj(index -> {
          Delegation delegation = transaction.getDelegations().get(index);
          return Operation.builder()
              .operationIdentifier(OperationIdentifier.builder().index(
                  getOperationCurrentIndex(totalOperations, index)).build())
              .type(OperationType.STAKE_DELEGATION.getValue())
              .status(status.getStatus())
              .account(AccountIdentifier.builder().address(delegation.getAddress()).build())
              .metadata(OperationMetadata.builder()
                  .poolKeyHash(delegation.getPoolId())
                  .build())
              .build();
        }).toList();
  }

  @NotNull
  public static List<Operation> getOutputsAsOperations(Tran transaction,
      List<List<Operation>> totalOperations, OperationStatus status,
      List<OperationIdentifier> relatedOperations) {
    return IntStream.range(0, nullable(transaction.getOutputs()).size())
        .mapToObj(index -> {
          Utxo utxoModel = transaction.getOutputs().get(index);
          return createOperation(getOperationCurrentIndex(totalOperations, index),
              Constants.OUTPUT,
              status.getStatus(),
              utxoModel.getOwnerAddr(),
              DataMapper.mapValue(utxoModel.getLovelaceAmount().toString(), false),
              relatedOperations,
              Long.valueOf(utxoModel.getOutputIndex()),
              DataMapper.getCoinChange(utxoModel.getOutputIndex(), utxoModel.getTxHash(),
                  CoinAction.CREATED),
              mapToOperationMetaData(false, utxoModel.getAmounts()));
        }).toList();
  }

  public static Operation createOperation(Long index, String type, String status, String address,
      String value, List<OperationIdentifier> relatedOperations, Long networkIndex,
      CoinChange coinChange, OperationMetadata tokenBundleMetadata) {
    return Operation.builder()
        .operationIdentifier(OperationIdentifier.builder()
            .index(index)
            .networkIndex(networkIndex).build()
        )
        .type(type)
        .status(status)
        .account(AccountIdentifier.builder()
            .address(address).build()
        )
        .amount(DataMapper.mapAmount(value, null, null, null))
        .coinChange(coinChange)
        .relatedOperations(relatedOperations)
        .metadata(tokenBundleMetadata).build();
  }

  private static OperationMetadata mapToOperationMetaData(boolean spent, List<Amt> amounts) {
    OperationMetadata operationMetadata = new OperationMetadata();
    for (Amt amount : amounts) {
      if (!amount.getAssetName().equals(Constants.LOVELACE)) {
        TokenBundleItem tokenBundleItem = new TokenBundleItem();
        tokenBundleItem.setPolicyId(amount.getPolicyId());
        Amount amt = new Amount();
        amt.setValue(DataMapper.mapValue(amount.getQuantity().toString(), spent));
        String hexAssetName = Hex.encodeHexString(amount.getAssetName().getBytes());
        amt.setCurrency(Currency.builder()
            .symbol(hexAssetName)
            .decimals(0)
            .build());
        tokenBundleItem.setTokens(List.of(amt));
        operationMetadata.addTokenBundleItem(tokenBundleItem);
      }
    }
    return operationMetadata;
  }

  public static long getOperationCurrentIndex(List<List<Operation>> operationsList,
      int relativeIndex) {
    return relativeIndex + operationsList.stream()
        .mapToLong(List::size)
        .sum();
  }

  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
        .map(operation -> OperationIdentifier.builder().index(operation.getOperationIdentifier()
            .getIndex()).build()).collect(Collectors.toList());
  }
}
