package org.cardanofoundation.rosetta.api.mapper;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.cardanofoundation.rosetta.api.model.cardano.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.cardano.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.constants.Constants;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.model.entity.Amt;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.cardanofoundation.rosetta.api.model.constants.Constants.ADA;
import static org.cardanofoundation.rosetta.api.model.constants.Constants.ADA_DECIMALS;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringFormatter;

@Slf4j
public class OperationDataMapper {

    @NotNull
    public static List<Operation> getAllOperations(TransactionDto transactionDto, String poolDeposit, OperationStatus status) {
        List<List<Operation>> totalOperations = new ArrayList<>();
        List<Operation> inputsAsOperations = getInputTransactionsasOperations(transactionDto, status);
        totalOperations.add(inputsAsOperations);
// TODO Withdrawal currently not supported via Yaci-store. Will implemented it as soon as the PR is merged.
//        List<Operation> withdrawalsAsOperations = getWithdrawlOperations(transactionDto, status, totalOperations);
//        totalOperations.add(withdrawalsAsOperations);

        List<Operation> stakeRegistrationOperations = getStakeRegistrationOperations(transactionDto, status, totalOperations);
        totalOperations.add(stakeRegistrationOperations);

        List<Operation> delegationOperations = getDelegationOperations(transactionDto, status, totalOperations);
        totalOperations.add(delegationOperations);

        List<Operation> poolRegistrationOperations = getPoolRegistrationOperations(transactionDto, status, totalOperations, poolDeposit);
        totalOperations.add(poolRegistrationOperations);

        List<Operation> poolRetirementOperations = getPoolRetirementOperations(transactionDto, status, totalOperations);
        totalOperations.add(poolRetirementOperations);

        List<OperationIdentifier> relatedOperations = getOperationIndexes(inputsAsOperations);

        List<Operation> outputsAsOperations = getOutputsAsOperations(transactionDto, totalOperations, status, relatedOperations);

        totalOperations.add(outputsAsOperations);
        List<Operation> operations = totalOperations.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return operations;
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
    public static List<Operation> getInputTransactionsasOperations(TransactionDto transactionDto, OperationStatus status) {
        List<Operation> inputsAsOperations = IntStream.range(0, transactionDto.getInputs().size())
                .mapToObj(index -> {
                    UtxoDto utxoDto = transactionDto.getInputs().get(index);
                    return createOperation((long) index,
                            Constants.INPUT,
                            status.getStatus(),
                            utxoDto.getOwnerAddr(),
                            DataMapper.mapValue(utxoDto.getLovelaceAmount().toString(), true),
                            null,
                            null,
                            DataMapper.getCoinChange(utxoDto.getOutputIndex(), utxoDto.getTxHash(),
                                    CoinAction.SPENT),
                            mapToOperationMetaData(true, utxoDto.getAmounts()));
                }).toList();
        return inputsAsOperations;
    }

    public static List<Operation> getPoolRegistrationOperations(TransactionDto transactionDto, OperationStatus status, List<List<Operation>> totalOperations, String poolDeposit) {
        return IntStream.range(0,
                        transactionDto.getPoolRegistrations().size())
                .mapToObj(index -> {
                    PoolRegistrationDTO poolRegistration = transactionDto.getPoolRegistrations().get(index);
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

    public static List<Operation> getPoolRetirementOperations(TransactionDto transactionDto, OperationStatus status, List<List<Operation>> totalOperations) {
        return IntStream.range(0,
                        transactionDto.getPoolRetirements().size())
                .mapToObj(index -> {
                    PoolRetirementDTO poolRetirement = transactionDto.getPoolRetirements().get(index);
                    return Operation.builder()
                            .operationIdentifier(OperationIdentifier.builder().index(
                                    getOperationCurrentIndex(totalOperations, index)).build())
                            .type(OperationType.POOL_RETIREMENT.getValue())
                            .status(status.getStatus())
                            .account(AccountIdentifier.builder().address(poolRetirement.getPoolId()).build())
                            .metadata(OperationMetadata.builder()
                                    .epoch(poolRetirement.getEpoch().longValue())
                                    .build())
                            .build();
                }).toList();
    }

    public static List<Operation> getStakeRegistrationOperations(TransactionDto transactionDto, OperationStatus status, List<List<Operation>> totalOperations) {
        return IntStream.range(0,
                        transactionDto.getStakeRegistrations().size())
                .mapToObj(index -> {
                    StakeRegistrationDTO stakeRegistrationDTO = transactionDto.getStakeRegistrations().get(index);
                    OperationType operationType = stakeRegistrationDTO.getType().equals(CertificateType.STAKE_REGISTRATION) ? OperationType.STAKE_KEY_REGISTRATION :
                            stakeRegistrationDTO.getType().equals(CertificateType.STAKE_DEREGISTRATION) ? OperationType.STAKE_KEY_DEREGISTRATION : null;
                    if(operationType == null) {
                        log.error("Invalid stake registration type {}", stakeRegistrationDTO.getType());
                        return null;
                    }
                    return Operation.builder()
                            .operationIdentifier(OperationIdentifier.builder().index(
                                    getOperationCurrentIndex(totalOperations, index)).build())
                            .type(operationType.getValue())
                            .status(status.getStatus())
                            .account(AccountIdentifier.builder().address(stakeRegistrationDTO.getAddress()).build())
                            .metadata(OperationMetadata.builder()

                                    .depositAmount(DataMapper.mapAmount("2000000", ADA, ADA_DECIMALS, null)) // TODO need to get this from protocolparams
                                    .build())
                            .build();
                }).toList();
    }

    public static List<Operation> getDelegationOperations(TransactionDto transactionDto, OperationStatus status, List<List<Operation>> totalOperations) {
        return IntStream.range(0,
                        transactionDto.getDelegations().size())
                .mapToObj(index -> {
                    DelegationDTO delegation = transactionDto.getDelegations().get(index);
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
    public static List<Operation> getOutputsAsOperations(TransactionDto transactionDto, List<List<Operation>> totalOperations, OperationStatus status, List<OperationIdentifier> relatedOperations) {
        List<Operation> outputsAsOperations = IntStream.range(0, transactionDto.getOutputs().size())
                .mapToObj(index -> {
                    UtxoDto utxoDto = transactionDto.getOutputs().get(index);
                    return createOperation(getOperationCurrentIndex(totalOperations, index),
                            Constants.OUTPUT,
                            status.getStatus(),
                            utxoDto.getOwnerAddr(),
                            DataMapper.mapValue(utxoDto.getLovelaceAmount().toString(), false),
                            relatedOperations,
                            Long.valueOf(utxoDto.getOutputIndex()),
                            DataMapper.getCoinChange(utxoDto.getOutputIndex(), utxoDto.getTxHash(),
                                    CoinAction.CREATED),
                            mapToOperationMetaData(false, utxoDto.getAmounts()));
                }).toList();
        return outputsAsOperations;
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
            if(!amount.getAssetName().equals(Constants.LOVELACE)) {
                TokenBundleItem tokenBundleItem = new TokenBundleItem();
                tokenBundleItem.setPolicyId(amount.getPolicyId());
                Amount amt = new Amount();
                amt.setValue(DataMapper.mapValue(amount.getQuantity().toString(), spent));
                String hexAssetName = Hex.encodeHexString(amount.getAssetName().getBytes());
                amt.setCurrency(Currency.builder()
                        .symbol(hexStringFormatter(hexAssetName))
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
