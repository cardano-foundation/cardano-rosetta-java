package org.cardanofoundation.rosetta.api.mapper;

import static org.cardanofoundation.rosetta.api.common.constants.Constants.ADA;
import static org.cardanofoundation.rosetta.api.common.constants.Constants.ADA_DECIMALS;
import static org.cardanofoundation.rosetta.api.common.constants.Constants.MULTI_ASSET_DECIMALS;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringFormatter;
import static org.cardanofoundation.rosetta.api.util.Formatters.remove0xPrefix;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.INVALID_OPERATION_STATUS;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.exception.AddressRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystSigIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.NetworkStatus;
import org.cardanofoundation.rosetta.api.model.Peer;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.api.model.rest.BalanceAtBlock;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Coin;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.api.model.rest.OperationStatus;
import org.cardanofoundation.rosetta.api.model.rest.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.FindTransactionProjection;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.BlockMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxosMultiAssets;
import org.cardanofoundation.rosetta.api.projection.dto.Delegation;
import org.cardanofoundation.rosetta.api.projection.dto.Deregistration;
import org.cardanofoundation.rosetta.api.projection.dto.FindPoolRetirements;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDelegations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDeregistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionInOutResult;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolOwners;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRegistrationsData;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRelays;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionRegistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionWithdrawals;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsInputs;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsOutputs;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.OperationMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistration;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRelay;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRetirement;
import org.cardanofoundation.rosetta.api.projection.dto.PopulatedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.Registration;
import org.cardanofoundation.rosetta.api.projection.dto.Token;
import org.cardanofoundation.rosetta.api.projection.dto.TokenBundle;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionInOut;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionInput;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionOutput;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionPoolRegistrations;
import org.cardanofoundation.rosetta.api.projection.dto.VoteRegistration;
import org.cardanofoundation.rosetta.api.projection.dto.VoteRegistrationMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.Withdrawal;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Block;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionIdentifier;


@Slf4j
public class DataMapper {

  public static final String COIN_SPENT_ACTION = "coin_spent";
  public static final String COIN_CREATED_ACTION = "coin_created";

  public static boolean isBlockUtxos(Object block) {
    return block instanceof BlockUtxos;
  }

  public static AccountBalanceResponse mapToAccountBalanceResponse(
      Object blockBalanceData) {
    if (isBlockUtxos(blockBalanceData)) {
      BlockUtxosMultiAssets blockUtxosMultiAssets = (BlockUtxosMultiAssets) blockBalanceData;
      List<Amount> maBalances = new ArrayList<>();
      if (blockUtxosMultiAssets.getMaBalances().size() > 0) {
        blockUtxosMultiAssets.getMaBalances().stream()
            .map(utxosMultiAssetsMaBalance -> mapAmount(utxosMultiAssetsMaBalance.getValue(),
                utxosMultiAssetsMaBalance.getName(),
                MULTI_ASSET_DECIMALS,
                utxosMultiAssetsMaBalance.getPolicy()))
            .forEach(maBalances::add);

      }
      BigInteger adaBalance = blockUtxosMultiAssets.getUtxos().stream()
          .reduce(BigInteger.ZERO, (totalAmount, current) -> {
            List<Utxo> utxos = blockUtxosMultiAssets.getUtxos();
            Utxo previous =
                utxos.indexOf(current) > 0 ? utxos.get(utxos.indexOf(current) - 1) : null;
            if (Objects.isNull(previous) || !areEqualUtxos(previous, current)) {
              return totalAmount.add(new BigInteger(current.getValue()));
            }
            return totalAmount;
          }, BigInteger::add);

      List<Amount> totalBalance = new ArrayList<>();
      Amount amountAdaBalance = mapAmount(adaBalance.toString());
      if (Objects.nonNull(amountAdaBalance)) {
        totalBalance.add(amountAdaBalance);
      }
      totalBalance.addAll(maBalances);

      return AccountBalanceResponse.builder()
          .blockIdentifier(new BlockIdentifier(blockUtxosMultiAssets.getBlock().getNumber(),
              blockUtxosMultiAssets.getBlock().getHash()))
          .balances(
              totalBalance.isEmpty() ? Collections.singletonList(mapAmount("0")) : totalBalance)
          .build();
    } else {
      BalanceAtBlock balanceAtBlock = (BalanceAtBlock) blockBalanceData;
      return AccountBalanceResponse.builder()
          .blockIdentifier(new BlockIdentifier(balanceAtBlock.getBlock().getNumber(),
              (balanceAtBlock).getBlock().getHash()))
          .balances(Collections.singletonList(mapAmount(balanceAtBlock.getBalance())))
          .build();
    }
  }

  public static boolean areEqualUtxos(Utxo firstUtxo, Utxo secondUtxo) {
    return (firstUtxo.getIndex() == secondUtxo.getIndex())
        && (firstUtxo.getTransactionHash().equals(secondUtxo.getTransactionHash()));
  }

  public static Amount mapAmount(String value, String symbol, int decimals, String policyId) {
    if (Objects.isNull(value)) {
      return null;
    }
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("policyId", policyId);
    Currency currency = new Currency()
        .decimals(decimals)
        .symbol(hexStringFormatter(symbol))
        .metadata(metadata);

    return new Amount().value(value).currency(currency);
  }

  public static Amount mapAmount(String value) {
    if (Objects.isNull(value)) {
      return null;
    }
    Map<String, Object> metadata = new HashMap<>();
    Currency currency = new Currency()
        .decimals(ADA_DECIMALS)
        .symbol(hexStringFormatter(ADA));
    return new Amount().value(value).currency(currency);
  }


  public static boolean isHexString(Object value) {
    if (value instanceof String str) {
      return str.matches("^(0x)?[0-9a-fA-F]+$");
    }
    return false;
  }

  public static boolean validateVoteDataFields(Map<String, Object> object) {
    List<CatalystDataIndexes> hexStringIndexes = Arrays.asList(
        CatalystDataIndexes.REWARD_ADDRESS,
        CatalystDataIndexes.STAKE_KEY,
        CatalystDataIndexes.VOTING_KEY
    );
    boolean isValidVotingNonce =
        object.containsKey(CatalystDataIndexes.VOTING_NONCE.getValue().toString())
            && object.get(CatalystDataIndexes.VOTING_NONCE.getValue().toString()) instanceof Number;

    return isValidVotingNonce
        && hexStringIndexes.stream().allMatch(index ->
        object.containsKey(index.getValue().toString()) && isHexString(
            object.get(index.getValue().toString()).toString()));
  }

  public static boolean isVoteSignatureValid(Object jsonObject) {
    boolean isObject = jsonObject instanceof Map;
    List<String> dataIndexes = Arrays.stream(CatalystSigIndexes.class.getFields())
        .map(Field::getName)
        .filter(key -> Integer.parseInt(key) > 0)
        .toList();
    return isObject && dataIndexes.stream().allMatch(index ->
        ((Map<String, Object>) jsonObject).containsKey(index)
            && isHexString(((Map<String, Object>) jsonObject).get(index)));
  }

  public static boolean isVoteDataValid(Object jsonObject) {
    boolean isObject = Objects.nonNull(jsonObject);

    return isObject && validateVoteDataFields((Map<String, Object>) jsonObject);

  }

  public static Address getAddressFromHexString(String hexAddress) {
    try {

      return new Address(hexAddress);
    } catch (AddressRuntimeException e) {
      return null;
    }
  }

  public static AccountCoinsResponse mapToAccountCoinsResponse(BlockUtxos blockCoinsData) {
    Map<String, Coin> mappedUtxos = blockCoinsData.getUtxos().stream().reduce(
        new HashMap<>(),
        (adaCoins, current) -> {
          int index = blockCoinsData.getUtxos().indexOf(current);
          Utxo previousValue = index > 0 ? blockCoinsData.getUtxos().get(index - 1) : null;
          String coinId = current.getTransactionHash() + ":" + current.getIndex();
          if (Objects.isNull(previousValue) || !areEqualUtxos(previousValue, current)) {
            adaCoins.put(coinId, new Coin()
                .coinIdentifier(new CoinIdentifier().identifier(coinId))
                .amount(mapAmount(current.getValue()))
            );
          }
          if (Objects.nonNull(current.getPolicy()) &&
              Objects.nonNull(current.getName()) &&
              Objects.nonNull(current.getQuantity())) {
            // MultiAsset
            Coin relatedCoin = adaCoins.get(coinId);
            if (Objects.nonNull(relatedCoin)) {
              Coin updatedCoin = updateMetadataCoin(relatedCoin, current.getPolicy(),
                  current.getQuantity(), current.getName());
              adaCoins.put(coinId, updatedCoin);
            }
          }
          return adaCoins;
        },
        (map1, map2) -> {
          map1.putAll(map2);
          return map1;
        }
    );
    return AccountCoinsResponse.builder()
        .blockIdentifier(new BlockIdentifier(blockCoinsData.getBlock().getNumber(),
            blockCoinsData.getBlock().getHash()))
        .coins(new ArrayList<>(mappedUtxos.values()))
        .build();

  }

  public static Coin updateMetadataCoin(Coin coin, String policy, String quantity, String name) {
    Coin updatedCoin = coin;
    String coinId = coin.getCoinIdentifier().getIdentifier();
    if (Objects.nonNull(updatedCoin.getMetadata()) && updatedCoin.getMetadata()
        .containsKey(coinId)) {

      Optional<TokenBundleItem> existsPolicyId = updatedCoin.getMetadata()
          .get(coinId)
          .stream()
          .filter(tokenBundle -> tokenBundle.getPolicyId().equals(policy))
          .findFirst();
      if (existsPolicyId.isPresent()) {
        TokenBundleItem tokenBundle = existsPolicyId.get();
        int policyIndex = updatedCoin.getMetadata()
            .get(coinId)
            .indexOf(tokenBundle);
        tokenBundle.getTokens().add(mapAmount(quantity, name, MULTI_ASSET_DECIMALS, policy));
      } else {
        TokenBundleItem tokenBundle = new TokenBundleItem()
            .policyId(policy)
            .tokens(new ArrayList<Amount>(Collections.singletonList(
                mapAmount(quantity, name, MULTI_ASSET_DECIMALS, policy))
            ));
        updatedCoin.getMetadata().get(coinId).add(tokenBundle);
      }
    } else {
      TokenBundleItem tokenBundle = new TokenBundleItem()
          .policyId(policy)
          .tokens(new ArrayList<>(Collections.singletonList(
              mapAmount(quantity, name, MULTI_ASSET_DECIMALS, policy)
          )));
      Map<String, List<TokenBundleItem>> metadata = new HashMap<>();
      metadata.put(coinId, new ArrayList<>(Collections.singletonList(tokenBundle)));
      updatedCoin.setMetadata(metadata);
    }
    return updatedCoin;
  }

  public static NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    BlockDto latestBlock = networkStatus.getLatestBlock();
    GenesisBlockDto genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
    return NetworkStatusResponse.builder()
        .currentBlockIdentifier(
            BlockIdentifier.builder().index(latestBlock.getNumber()).hash(latestBlock.getHash())
                .build())
        .currentBlockTimeStamp(latestBlock.getCreatedAt())
        .genesisBlockIdentifier(BlockIdentifier.builder().index(
                genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0)
            .hash(genesisBlock.getHash()).build())
        .peers(peers.stream().map(peer -> new Peer(peer.getAddr())).toList())
        .build();
  }

  public static NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO)
        .network(supportedNetwork.getNetworkId()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }

  public static List<TransactionPoolRegistrations> mapToTransactionPoolRegistrations(
      List<FindTransactionPoolRegistrationsData> poolRegistrations,
      List<FindTransactionPoolOwners> poolOwners, List<FindTransactionPoolRelays> poolRelays) {
    List<TransactionPoolRegistrations> transactionPoolRegistrations = new ArrayList<>();
    for (FindTransactionPoolRegistrationsData poolRegistration : poolRegistrations) {
      List<String> owners = poolOwners.stream()
          .filter(owner -> owner.getUpdateId().equals(poolRegistration.getUpdateId()))
          .map(FindTransactionPoolOwners::getOwner)
          .collect(Collectors.toList());

      List<PoolRelay> relays = poolRelays.stream()
          .filter(relay -> relay.getUpdateId().equals(poolRegistration.getUpdateId()))
          .map(relay -> PoolRelay.builder()
              .dnsName(relay.getDnsName())
              .ipv4(relay.getIpv4())
              .ipv6(relay.getIpv6())
              .port(relay.getPort().toString())
              .build())
          .collect(Collectors.toList());

      TransactionPoolRegistrations transactionPoolRegistration = TransactionPoolRegistrations.builder()
          .txHash(poolRegistration.getTxHash())
          .vrfKeyHash(poolRegistration.getVrfKeyHash())
          .pledge(poolRegistration.getPledge().toString())
          .margin(poolRegistration.getMargin())
          .cost(poolRegistration.getCost())
          .address(poolRegistration.getAddress())
          .poolHash(poolRegistration.getPoolHash())
          .owners(owners)
          .relays(relays)
          .metadataHash(poolRegistration.getMetadataHash())
          .metadataHash(poolRegistration.getMetadataHash())
          .build();

      transactionPoolRegistrations.add(transactionPoolRegistration);
    }
    return transactionPoolRegistrations;
  }

  public static List<TransactionDto> parseTransactionRows(
      List<FindTransactionProjection> findTransactionProjections) {
    return findTransactionProjections.stream()
        .map(findTransactionProjection -> TransactionDto.builder()
            .hash(findTransactionProjection.getHash())
            .blockHash(
                findTransactionProjection.getBlockHash())
            .blockNo(findTransactionProjection.getBlockNo())
            .fee(findTransactionProjection.getFee())
            .size(findTransactionProjection.getSize())
            .scriptSize(findTransactionProjection.getScriptSize())
            .validContract(findTransactionProjection.getValidContract())
            .build())
        .collect(Collectors.toList());
  }

  public static Block mapToRosettaBlock(BlockDto blockDto, List<PopulatedTransaction> transactions,
      String poolDeposit) {
    return new Block()
        .blockIdentifier(new org.openapitools.client.model.BlockIdentifier()
            .hash(blockDto.getHash())
            .index(blockDto.getNumber()))
        .parentBlockIdentifier(new org.openapitools.client.model.BlockIdentifier()
            .index(blockDto.getPreviousBlockNumber())
            .hash(blockDto.getPreviousBlockHash()))
        .timestamp(blockDto.getCreatedAt())
        .metadata(BlockMetadata.builder()
            .transactionsCount(blockDto.getTransactionsCount())
            .createdBy(blockDto.getCreatedBy())
            .size(blockDto.getSize())
            .epochNo(blockDto.getEpochNo())
            .slotNo(blockDto.getSlotNo())
            .build())
        .transactions(Objects.nonNull(transactions) ? transactions.stream()
            .map(transaction -> mapToRosettaTransaction(transaction, poolDeposit)).collect(
                Collectors.toList()) : null);
  }

  public static Transaction mapToRosettaTransaction(PopulatedTransaction transaction,
      String poolDeposit) {
    OperationStatus status =
        Boolean.TRUE.equals(transaction.getValidContract()) ? SUCCESS_OPERATION_STATUS
            : INVALID_OPERATION_STATUS;
    List<Operation> inputsAsOperations = IntStream.range(0, transaction.getInputs().size())
        .mapToObj(index -> {
          TransactionInput input = transaction.getInputs().get(index);
          return createOperation((long) index,
              OperationType.INPUT.getValue(),
              status.getStatus(),
              input.getAddress(),
              mapValue(input.getValue(), true),
              null,
              null,
              getCoinChange(input.getSourceTransactionIndex(), input.getSourceTransactionHash(),
                  CoinAction.SPENT),
              mapTokenBundleToMetadata(true, input.getTokenBundle()));
        }).toList();

    List<List<Operation>> totalOperations = new ArrayList<>();
    totalOperations.add(inputsAsOperations);

    List<Operation> withdrawalsAsOperations = IntStream.range(0,
            transaction.getWithdrawals().size())
        .mapToObj(index -> {
          Withdrawal withdrawal = transaction.getWithdrawals().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.WITHDRAWAL.getValue())
              .status(status.getStatus())
              .account(new AccountIdentifier().address(withdrawal.getStakeAddress()))
              .metadata(OperationMetadata.builder()
                  .withdrawalAmount(mapAmount("-" + withdrawal.getAmount()))
                  .build());
        }).toList();
    totalOperations.add(withdrawalsAsOperations);

    List<Operation> registrationsAsOperations = IntStream.range(0,
            transaction.getRegistrations().size())
        .mapToObj(index -> {
          Registration registration = transaction.getRegistrations().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.STAKE_KEY_REGISTRATION.getValue())
              .status(status.getStatus())
              .account(new AccountIdentifier().address(registration.getStakeAddress()))
              .metadata(OperationMetadata.builder()
                  .depositAmount(mapAmount(registration.getAmount()))
                  .build());
        }).toList();
    totalOperations.add(registrationsAsOperations);

    List<Operation> poolRetirementOperations = IntStream.range(0,
            transaction.getPoolRetirements().size())
        .mapToObj(index -> {
          PoolRetirement poolRetirement = transaction.getPoolRetirements().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.POOL_RETIREMENT.getValue())
              .status(status.getStatus())
              .account(new AccountIdentifier().address(poolRetirement.getAddress()))
              .metadata(OperationMetadata.builder()
                  .epoch(poolRetirement.getEpoch())
                  .build());
        }).toList();
    totalOperations.add(poolRetirementOperations);

    List<Operation> deregistrationsAsOperations = IntStream.range(0,
            transaction.getDeregistrations().size())
        .mapToObj(index -> {
          Deregistration deregistration = transaction.getDeregistrations().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.STAKE_KEY_DEREGISTRATION.getValue())
              .status(status.getStatus())
              .account(new AccountIdentifier().address(deregistration.getStakeAddress()))
              .metadata(OperationMetadata.builder()
                  .refundAmount(mapAmount(deregistration.getAmount()))
                  .build());
        }).toList();
    totalOperations.add(deregistrationsAsOperations);

    List<Operation> poolRegistrationsAsOperations = IntStream.range(0,
            transaction.getPoolRegistrations().size())
        .mapToObj(index -> {
          PoolRegistration poolRegistration = transaction.getPoolRegistrations().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.POOL_REGISTRATION.getValue())
              .status(status.getStatus())
              .account(new AccountIdentifier().address(poolRegistration.getPoolHash()))
              .metadata(OperationMetadata.builder()
                  .depositAmount(mapAmount(poolDeposit))
                  .poolRegistrationParams(PoolRegistrationParams.builder()
                      .pledge(poolRegistration.getPledge())
                      .rewardAddress(poolRegistration.getAddress())
                      .cost(poolRegistration.getCost())
                      .poolOwners(poolRegistration.getOwners())
                      .marginPercentage(poolRegistration.getMargin())
                      .vrfKeyHash(poolRegistration.getVrfKeyHash())
                      .relays(poolRegistration.getRelays())
                      .build())
                  .build());
        }).toList();
    totalOperations.add(poolRegistrationsAsOperations);

    List<Operation> voteRegistrationsAsOperations = IntStream.range(0,
            transaction.getVoteRegistrations().size())
        .mapToObj(index -> {
          VoteRegistration voteRegistration = transaction.getVoteRegistrations().get(index);
          return new Operation()
              .operationIdentifier(new OperationIdentifier().index(
                  getOperationCurrentIndex(totalOperations, index)))
              .type(OperationType.VOTE_REGISTRATION.getValue())
              .status(status.getStatus())
              .metadata(OperationMetadata.builder()
                  .voteRegistrationMetadata(VoteRegistrationMetadata.builder()
                      .stakeKey(new PublicKey()
                          .hexBytes(voteRegistration.getStakeKey())
                          .curveType(CurveType.EDWARDS25519))
                      .votingKey(new PublicKey()
                          .hexBytes(voteRegistration.getVotingKey())
                          .curveType(CurveType.EDWARDS25519))
                      .rewardAddress(voteRegistration.getRewardAddress())
                      .votingNonce(voteRegistration.getVotingNonce())
                      .votingSignature(voteRegistration.getVotingSignature())
                      .build())
                  .build());
        }).toList();
    totalOperations.add(voteRegistrationsAsOperations);

    List<OperationIdentifier> relatedOperations = getOperationIndexes(inputsAsOperations);
    relatedOperations.addAll(getOperationIndexes(withdrawalsAsOperations));
    relatedOperations.addAll(getOperationIndexes(deregistrationsAsOperations));
    List<Operation> outputsAsOperations = IntStream.range(0, transaction.getOutputs().size())
        .mapToObj(index -> {
          TransactionOutput output = transaction.getOutputs().get(index);
          return createOperation(getOperationCurrentIndex(totalOperations, index),
              OperationType.OUTPUT.getValue(),
              status.getStatus(),
              output.getAddress(),
              output.getValue(),
              relatedOperations,
              Long.valueOf(output.getIndex()),
              getCoinChange(output.getIndex(), transaction.getHash(),
                  CoinAction.CREATED),
              mapTokenBundleToMetadata(false, output.getTokenBundle()));
        }).toList();
    totalOperations.add(outputsAsOperations);
    List<Operation> operations = totalOperations.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    return new Transaction()
        .transactionIdentifier(new TransactionIdentifier().hash(transaction.getHash()))
        .operations(operations)
        .metadata(TransactionMetadata.builder()
            .size(transaction.getSize())
            .scriptSize(transaction.getScriptSize())
            .build());
  }

  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
        .map(operation -> new OperationIdentifier().index(operation.getOperationIdentifier()
            .getIndex())).collect(Collectors.toList());
  }

  public static OperationMetadata mapTokenBundleToMetadata(boolean spent, TokenBundle tokenBundle) {
    return tokenBundle != null ? OperationMetadata.builder()
        .tokenBundle(mapTokenBundle(tokenBundle, spent)).build() : null;
  }

  public static List<TokenBundleItem> mapTokenBundle(TokenBundle tokenBundle, boolean spent) {
    List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
    for (Map.Entry<String, List<Token>> entry : tokenBundle.getTokens().entrySet()) {
      String policyId = entry.getKey();
      List<Token> tokens = entry.getValue();
      List<Amount> amounts = new ArrayList<>();
      for (Token token : tokens) {
        Amount amount = new Amount();
        Currency currency = new Currency();
        amount.currency(currency.symbol(hexStringFormatter(token.getName())).decimals(0))
            .value(mapValue(token.getQuantity(), spent));
        amounts.add(amount);
      }
      tokenBundleItems.add(new TokenBundleItem().policyId(policyId).tokens(amounts));
    }
    return tokenBundleItems;
  }

  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  public static CoinChange getCoinChange(int index, String hash, CoinAction coinAction) {

    return new CoinChange().coinIdentifier(new CoinIdentifier().identifier(hash + ":" + index))
        .coinAction(coinAction);
  }

  public static Operation createOperation(Long index, String type, String status, String address,
      String value, List<OperationIdentifier> relatedOperations, Long networkIndex,
      CoinChange coinChange, OperationMetadata tokenBundleMetadata) {
    return new Operation()
        .operationIdentifier(new OperationIdentifier()
            .index(index)
            .networkIndex(networkIndex)
        )
        .type(type)
        .status(status)
        .account(new AccountIdentifier()
            .address(address)
        )
        .amount(mapAmount(value))
        .coinChange(coinChange)
        .relatedOperations(relatedOperations)
        .metadata(tokenBundleMetadata);

  }

  public static Amount mapAmount(String value, String symbol, Integer decimals,
      Map<String, Object> metadata) {
    if (Objects.isNull(symbol)) {
      value = ADA;
    }
    if (Objects.isNull(decimals)) {
      decimals = ADA_DECIMALS;
    }
    Amount amount = new Amount();
    Currency currency = new Currency();
    return amount.value(value)
        .currency(
            currency.symbol(hexStringFormatter(symbol))
                .decimals(decimals)
                .metadata(metadata)
        );
  }

  public static long getOperationCurrentIndex(List<List<Operation>> operationsList,
      int relativeIndex) {
    return relativeIndex + operationsList.stream()
        .mapToLong(List::size)
        .sum();
  }

  public static Map<String, PopulatedTransaction> mapTransactionsToDict(
      List<TransactionDto> transactions) {
    Map<String, PopulatedTransaction> mappedTransactions = new HashMap<>();
    for (TransactionDto transaction : transactions) {
      String hash = transaction.getHash();
      mappedTransactions.put(hash, PopulatedTransaction.builder()
          .hash(hash)
          .blockHash(transaction.getBlockHash())
          .blockNo(transaction.getBlockNo())
          .fee(transaction.getFee())
          .size(transaction.getSize())
          .scriptSize(transaction.getScriptSize())
          .validContract(transaction.getValidContract())
          .inputs(new ArrayList<>())
          .outputs(new ArrayList<>())
          .withdrawals(new ArrayList<>())
          .registrations(new ArrayList<>())
          .deregistrations(new ArrayList<>())
          .delegations(new ArrayList<>())
          .poolRegistrations(new ArrayList<>())
          .poolRetirements(new ArrayList<>())
          .voteRegistrations(new ArrayList<>())
          .build());
    }
    return mappedTransactions;
  }
  public static <T extends FindTransactionFieldResult> Map<String, PopulatedTransaction> populateTransactionField(
      Map<String, PopulatedTransaction> transactionsMap,
      List<T> queryResults,
      BiFunction<PopulatedTransaction, T, PopulatedTransaction> populateTransaction) {
    Map<String, PopulatedTransaction> updatedTransactionsMap = new HashMap<>(transactionsMap);
    for (T queryResult : queryResults) {
      String txHash = queryResult.getTxHash();
      PopulatedTransaction transaction = transactionsMap.get(txHash);
      if (Objects.nonNull(transaction)) {
        PopulatedTransaction updatedTransaction = populateTransaction.apply(transaction,
            queryResult);
        updatedTransactionsMap.put(transaction.getHash(), updatedTransaction);
      }
    }
    return updatedTransactionsMap;
  }
  public static <T extends TransactionInOut, F extends FindTransactionInOutResult>
  PopulatedTransaction parseInOutRow(F row,
      PopulatedTransaction transaction,
      Function<PopulatedTransaction, List<T>> getCollection,
      Function<F, T> createInstance,
      BiFunction<PopulatedTransaction, List<T>, PopulatedTransaction> updateCollection) {
    // Get the collection where the input or output is stored
    List<T> collection = getCollection.apply(transaction);
    // Look for it in case it already exists. This is the case when there are multi-assets associated
    // to the same output so several rows will be returned for the same input or output
    int index = -1;
    for (int i = 0; i < collection.size(); i++) {
      if (Objects.equals(collection.get(i).getId(), row.getId())) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      // If it exists, it means that several MA were returned so we need to try to add the token
      T updated = tryAddToken(collection.get(index), row);
      // Proper item is updated in a copy of the collection
      List<T> newCollection = new ArrayList<>(collection);
      newCollection.set(index, updated);
      // Collection is updated in the PopulatedTransaction and then returned
      return updateCollection.apply(
          transaction,
          newCollection
      );
    }
    // If it's a new input or output create an instance
    T newInstance = createInstance.apply(row);
    // Then we try to populate its token if any
    T newInOut = tryAddToken(newInstance, row);
    collection.add(newInOut);
    return updateCollection.apply(
        transaction,
        collection
    );
  }

  public static boolean hasToken(FindTransactionInOutResult operation) {
    return Objects.nonNull(operation.getPolicy()) && Objects.nonNull(operation.getName())
        && Objects.nonNull(operation.getQuantity());
  }

  public static <T extends TransactionInOut> T tryAddToken(T inOut,
      FindTransactionInOutResult findResult) {
    if (hasToken(findResult)) {
      String policy = findResult.getPolicy();
      String name = findResult.getName();
      String quantity = findResult.getQuantity().toString();
      String policyAsHex = policy;
      String nameAsHex = name;
      TokenBundle tokenBundle = Objects.nonNull(inOut.getTokenBundle()) ? inOut.getTokenBundle()
          : TokenBundle.builder().tokens(new HashMap<>())
              .build();
      if (!tokenBundle.getTokens().containsKey(policyAsHex)) {
        tokenBundle.getTokens().put(policyAsHex, new ArrayList<>());
      }
      tokenBundle.getTokens().get(policyAsHex).add(new Token(nameAsHex, quantity));
      inOut.setTokenBundle(tokenBundle);
      return inOut;
    }
    return inOut;
  }

  public static PopulatedTransaction parseInputsRow(PopulatedTransaction populatedTransaction,
      FindTransactionsInputs input) {
    return parseInOutRow(
        input,
        populatedTransaction,
        PopulatedTransaction::getInputs,
        queryResult -> TransactionInput.builder()
            .id(queryResult.getId())
            .address(queryResult.getAddress())
            .value(queryResult.getValue().toString())
            .sourceTransactionHash(queryResult.getSourceTxHash())
            .sourceTransactionIndex(queryResult.getSourceTxIndex())
            .build(),
        (updatedTransaction, updatedCollection) -> {
          updatedTransaction.setInputs(updatedCollection);
          return updatedTransaction;
        }
    );

  }

  public static BiFunction<PopulatedTransaction, FindTransactionsInputs, PopulatedTransaction> parseInputsRowFactory() {
    return DataMapper::parseInputsRow;
  }

  public static PopulatedTransaction parseOutputsRow(PopulatedTransaction populatedTransaction,
      FindTransactionsOutputs output) {
    return parseInOutRow(
        output,
        populatedTransaction,
        PopulatedTransaction::getOutputs,
        queryResult -> TransactionOutput.builder()
            .id(queryResult.getId())
            .address(queryResult.getAddress())
            .value(queryResult.getValue().toString())
            .index(queryResult.getIndex())
            .build(),
        (updatedTransaction, updatedCollection) -> {
          updatedTransaction.setOutputs(updatedCollection);
          return updatedTransaction;
        });
  }


  public static BiFunction<PopulatedTransaction, FindTransactionsOutputs, PopulatedTransaction> parseOutputsRowFactory() {
    return DataMapper::parseOutputsRow;
  }

  public static PopulatedTransaction parseWithdrawalsRow(PopulatedTransaction transaction,
      FindTransactionWithdrawals withdrawal) {
    List<Withdrawal> withdrawals =
        Objects.nonNull(transaction.getWithdrawals()) ? transaction.getWithdrawals()
            : new ArrayList<>();
    withdrawals.add(new Withdrawal(withdrawal.getAddress(), withdrawal.getAmount().toString()));
    transaction.setWithdrawals(withdrawals);
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, FindTransactionWithdrawals, PopulatedTransaction> parseWithdrawalsRowFactory() {
    return DataMapper::parseWithdrawalsRow;
  }

  public static PopulatedTransaction parseRegistrationsRow(PopulatedTransaction transaction,
      FindTransactionRegistrations registration) {
    transaction.getRegistrations()
        .add(new Registration(registration.getAddress(), registration.getAmount().toString()));
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, FindTransactionRegistrations, PopulatedTransaction> parseRegistrationsRowFactory() {
    return DataMapper::parseRegistrationsRow;
  }

  public static PopulatedTransaction parseDeregistrationsRow(PopulatedTransaction transaction,
      FindTransactionDeregistrations findTransactionDeregistrations) {
    transaction.getDeregistrations()
        .add(new Deregistration(findTransactionDeregistrations.getAddress(),
            findTransactionDeregistrations.getAmount().toString()));
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, FindTransactionDeregistrations, PopulatedTransaction> parseDeregistrationsRowFactory() {
    return DataMapper::parseDeregistrationsRow;
  }

  public static PopulatedTransaction parseDelegationsRow(PopulatedTransaction transaction,
      FindTransactionDelegations findTransactionDelegation) {
    transaction.getDelegations().add(new Delegation(findTransactionDelegation.getAddress(),
        findTransactionDelegation.getPoolHash()));
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, FindTransactionDelegations, PopulatedTransaction> parseDelegationsRowFactory() {
    return DataMapper::parseDelegationsRow;
  }

  public static PopulatedTransaction parsePoolRetirementRow(PopulatedTransaction transaction,
      FindPoolRetirements findPoolRetirements) {
    transaction.getPoolRetirements().add(new PoolRetirement(findPoolRetirements.getEpoch(),
        findPoolRetirements.getAddress()));
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, FindPoolRetirements, PopulatedTransaction> parsePoolRetirementRowFactory() {
    return DataMapper::parsePoolRetirementRow;
  }
  public static PopulatedTransaction parseVoteRow(PopulatedTransaction transaction,
      TransactionMetadataDto findTransactionMetadata) {
    String data = findTransactionMetadata.getData();
    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, Object> mapJsonObject = mapper.readValue(data, new TypeReference<>() {
      });
      String signature = findTransactionMetadata.getSignature();
      if (isVoteDataValid(data) && isVoteSignatureValid(signature)) {
        Address rewardAddress = getAddressFromHexString(remove0xPrefix(
            (String) mapJsonObject.get(CatalystDataIndexes.REWARD_ADDRESS.getValue().toString())));
        // should consider
        if (Objects.nonNull(rewardAddress)) {
          String votingKey = remove0xPrefix(
              (String) mapJsonObject.get(CatalystDataIndexes.VOTING_KEY.getValue().toString()));
          String stakeKey = remove0xPrefix(
              (String) mapJsonObject.get(CatalystDataIndexes.STAKE_KEY.getValue().toString()));
          String votingNonce = (String) mapJsonObject.get(
              CatalystDataIndexes.VOTING_NONCE.getValue().toString());
          String votingSignature = remove0xPrefix((String) mapJsonObject.get(
              CatalystSigIndexes.VOTING_SIGNATURE.getValue().toString()));
          transaction.getVoteRegistrations().add(VoteRegistration.builder()
              .votingKey(votingKey)
              .stakeKey(stakeKey)
              .votingNonce(Long.valueOf(votingNonce))
              .votingSignature(votingSignature)
              .rewardAddress(rewardAddress.toBech32())
              .build());
          return transaction;
        }
      }
      return transaction;
    } catch (JsonProcessingException exception) {
      log.info("[isVoteDataValid] Json bug ");
      System.out.println("isVoteDataValid bug");
      return null;
    }
  }

  public static BiFunction<PopulatedTransaction, TransactionMetadataDto, PopulatedTransaction> parseVoteRowFactory() {
    return DataMapper::parseVoteRow;
  }
  public static PopulatedTransaction parsePoolRegistrationsRows(
      PopulatedTransaction transaction,
      TransactionPoolRegistrations poolRegistration
  ) {
    transaction.getPoolRegistrations().add(PoolRegistration.builder()
        .vrfKeyHash(poolRegistration.getVrfKeyHash())
        .pledge(poolRegistration.getPledge())
        .margin(poolRegistration.getMargin().toString())
        .cost(poolRegistration.getCost().toString())
        .address(poolRegistration.getAddress())
        .poolHash(poolRegistration.getPoolHash())
        .owners(poolRegistration.getOwners())
        .relays(poolRegistration.getRelays())
        .metadataHash(poolRegistration.getMetadataHash())
        .metadataUrl(poolRegistration.getMetadataUrl())
        .build());
    return transaction;
  }

  public static BiFunction<PopulatedTransaction, TransactionPoolRegistrations, PopulatedTransaction> parsePoolRegistrationsRowsFactory() {
    return DataMapper::parsePoolRegistrationsRows;
  }


}





