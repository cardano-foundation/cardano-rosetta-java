package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.common.model.Network;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.Coin;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.CoinTokens;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionMetadataResponseMetadata;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkStatusResponse;
import org.openapitools.client.model.Peer;
import org.openapitools.client.model.Signature;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.util.Constants;


@Slf4j
@PersistenceMapper
@RequiredArgsConstructor
public class DataMapper {

  private final ProtocolParamsToRosettaProtocolParameters protocolParamsToRosettaProtocolParameters;
  /**
   * Maps a NetworkRequest to a NetworkOptionsResponse.
   * @param supportedNetwork The supported network
   * @return The NetworkOptionsResponse
   */
  public NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO)
            .network(Objects.requireNonNull(
                NetworkEnum.fromProtocolMagic(supportedNetwork.getProtocolMagic())).getValue()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }

  /**
   * Maps a NetworkRequest to a NetworkOptionsResponse.
   * @param networkStatus The network status
   * @return The NetworkOptionsResponse
   */
  public NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    Block latestBlock = networkStatus.getLatestBlock();
    GenesisBlock genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
    return NetworkStatusResponse.builder()
            .currentBlockIdentifier(
                    BlockIdentifier.builder().index(latestBlock.getNumber()).hash(latestBlock.getHash())
                            .build())
            .currentBlockTimestamp(latestBlock.getCreatedAt())
            .genesisBlockIdentifier(BlockIdentifier.builder().index(
                            genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0)
                    .hash(genesisBlock.getHash()).build())
            .peers(peers.stream().map(peer -> new Peer(peer.getPeerId(), null)).toList())
            .build();
  }


  /**
   * Basic mapping if a value is spent or not.
   * @param value value to be mapped
   * @param spent if the value is spent. Will add a "-" in front of the value if spent.
   * @return the mapped value
   */
  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  public static CoinChange getCoinChange(int index, String hash, CoinAction coinAction) {
    CoinIdentifier coinIdentifier = new CoinIdentifier();
    coinIdentifier.setIdentifier(hash + ":" + index);

    return CoinChange.builder().coinIdentifier(coinIdentifier).coinAction(coinAction).build();
  }


  /**
   * Creates a Rosetta compatible Amount for ADA. The value is the amount in lovelace and the currency is ADA.
   * @param value The amount in lovelace
   * @return The Rosetta compatible Amount
   */
  public static Amount mapAmount(String value) {
    if (Objects.isNull(value)) {
      return null;
    }

    Currency currency = Currency.builder()
            .decimals(Constants.ADA_DECIMALS)
            .symbol(Constants.ADA).build();
    return Amount.builder().value(value).currency(currency).build();
  }

  /**
   * Creates a Rosetta compatible Amount. Symbol and decimals are optional. If not provided, ADA and 6 decimals are used.
   * @param value The amount of the token
   * @param symbol The symbol of the token - it will be hex encoded
   * @param decimals The number of decimals of the token
   * @param metadata The metadata of the token
   * @return The Rosetta compatible Amount
   */
  public static Amount mapAmount(String value, String symbol, Integer decimals,
                                 CurrencyMetadata metadata) {
    if (Objects.isNull(symbol)) {
      symbol = Constants.ADA;
    }
    if (Objects.isNull(decimals)) {
      decimals = Constants.ADA_DECIMALS;
    }
    Amount amount = new Amount();
    amount.setValue(value);
    amount.setCurrency(Currency.builder()
                            .symbol(symbol)
                            .decimals(decimals)
                            .metadata(metadata) // TODO check metadata for Amount
                            .build());
    return amount;
  }

  /**
   * Maps a list of AddressBalanceDTOs to a Rosetta compatible AccountBalanceResponse.
   * @param block The block from where the balances are calculated into the past
   * @param balances The balances of the addresses
   * @return The Rosetta compatible AccountBalanceResponse
   */
  public static AccountBalanceResponse mapToAccountBalanceResponse(Block block, List<AddressBalance> balances) {
    List<AddressBalance> nonLovelaceBalances = balances.stream().filter(balance -> !balance.unit().equals(Constants.LOVELACE) && !balance.unit().equals(Constants.ADA)).toList();
    long sum = balances.stream().filter(balance -> balance.unit().equals(Constants.LOVELACE) || balance.unit().equals(Constants.ADA)).mapToLong(value -> value.quantity().longValue()).sum();
    List<Amount> amounts = new ArrayList<>();
    if (sum > 0) {
      amounts.add(mapAmount(String.valueOf(sum)));
    }
    nonLovelaceBalances.forEach(balance -> amounts.add(mapAmount(balance.quantity().toString(), balance.unit().substring(Constants.POLICY_ID_LENGTH), Constants.MULTI_ASSET_DECIMALS, new CurrencyMetadata(
        balance.unit().substring(0, Constants.POLICY_ID_LENGTH)))));
    return AccountBalanceResponse.builder()
            .blockIdentifier(BlockIdentifier.builder()
                    .hash(block.getHash())
                    .index(block.getNumber())
                    .build())
            .balances(amounts)
            .build();
  }

  public static AccountBalanceResponse mapToStakeAddressBalanceResponse(Block block, StakeAddressBalance balance) {
    return AccountBalanceResponse.builder()
            .blockIdentifier(BlockIdentifier.builder()
                    .hash(block.getHash())
                    .index(block.getNumber())
                    .build())
            .balances(List.of(Objects.requireNonNull(mapAmount(balance.getQuantity().toString()))))
            .build();
  }

  public static AccountCoinsResponse mapToAccountCoinsResponse(Block block, List<Utxo> utxos) {
    return AccountCoinsResponse.builder()
        .blockIdentifier(BlockIdentifier.builder()
            .hash(block.getHash())
            .index(block.getNumber())
            .build())
        .coins(utxos.stream().map(utxo -> {
              Amt adaAsset = utxo.getAmounts().stream()
                  .filter(amt -> Constants.LOVELACE.equals(amt.getAssetName()))
                  .findFirst()
                  .orElseGet(() -> new Amt(null, null, Constants.ADA, BigInteger.ZERO));
              return Coin.builder()
                  .coinIdentifier(CoinIdentifier.builder()
                      .identifier(utxo.getTxHash() + ":" + utxo.getOutputIndex())
                      .build())
                  .amount(Amount.builder()
                      .value(adaAsset.getQuantity().toString()) // In the DB only Lovelace are persisted.
                      .currency(Currency.builder()
                          .symbol(Constants.ADA)
                          .decimals(Constants.ADA_DECIMALS)
                          .build())
                      .build())
                  .metadata(mapCoinMetadata(utxo))
                  .build();
            })
            .toList())
        .build();
  }

  @Nullable
  private static Map<String, List<CoinTokens>> mapCoinMetadata(Utxo utxo) {
    String key = utxo.getTxHash() + ":" + utxo.getOutputIndex();
    List<CoinTokens> coinTokens =
        utxo.getAmounts().stream()
            .filter(Objects::nonNull)
            .filter(amount -> amount.getPolicyId() != null
                && amount.getAssetName() != null
                && amount.getQuantity() != null)
            .map(amount -> {
              CoinTokens tokens = new CoinTokens();
              tokens.setPolicyId(amount.getPolicyId());
              tokens.setTokens(List.of(mapAmount(amount.getQuantity().toString(),
                  // unit = assetName + policyId. To get the symbol policy ID must be removed from Unit. According to CIP67
                  amount.getUnit().replace(amount.getPolicyId(), ""),
                  Constants.MULTI_ASSET_DECIMALS, new CurrencyMetadata(amount.getPolicyId()))));
              return tokens;
            })
            .toList();
    return coinTokens.isEmpty() ? null : Map.of(key, coinTokens);
  }

  public ConstructionMetadataResponse mapToMetadataResponse(ProtocolParams protocolParams, Long ttl, Long suggestedFee) {
    return ConstructionMetadataResponse.builder()
            .metadata(ConstructionMetadataResponseMetadata.builder()
                .ttl(new BigDecimal(ttl))
                .protocolParameters(protocolParamsToRosettaProtocolParameters.toProtocolParameters(protocolParams))
                .build())
            .suggestedFee(List.of(Amount.builder()
                            .value(suggestedFee.toString())
                            .currency(Currency.builder()
                                    .decimals(Constants.ADA_DECIMALS)
                                    .symbol(Constants.ADA)
                                    .build())
                    .build()))
            .build();
  }

  public static List<Signatures> mapRosettaSignatureToSignaturesList(List<Signature> signatures) {
    return signatures.stream().map(signature -> {
      String chainCode = null;
      String address = null;
      AccountIdentifier accountIdentifier = signature.getSigningPayload().getAccountIdentifier();
      if(!ObjectUtils.isEmpty(accountIdentifier)) {
        chainCode = accountIdentifier.getMetadata().getChainCode();
        address = accountIdentifier.getAddress();
      }
      return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(), chainCode, address);
    }).toList();
  }
}





