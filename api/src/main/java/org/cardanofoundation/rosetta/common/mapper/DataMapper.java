package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
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
   *
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
   *
   * @param networkStatus The network status
   * @return The NetworkOptionsResponse
   */
  public NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    BlockIdentifierExtended latestBlock = networkStatus.getLatestBlock();
    BlockIdentifierExtended genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
    return NetworkStatusResponse.builder()
        .currentBlockIdentifier(
            BlockIdentifier.builder().index(latestBlock.getNumber())
                .hash(latestBlock.getHash()).build())
        .currentBlockTimestamp(TimeUnit.SECONDS.toMillis(latestBlock.getBlockTimeInSeconds()))
        .genesisBlockIdentifier(BlockIdentifier.builder().index(
                genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0)
            .hash(genesisBlock.getHash()).build())
        .peers(peers.stream().map(peer -> new Peer(peer.getPeerId(), null)).toList())
        .build();
  }


  /**
   * Basic mapping if a value is spent or not.
   *
   * @param value value to be mapped
   * @param spent if the value is spent. Will add a "-" in front of the value if spent.
   * @return the mapped value
   */
  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }


  /**
   * Creates a Rosetta compatible Amount for ADA. The value is the amount in lovelace and the
   * currency is ADA.
   *
   * @param value The amount in lovelace
   * @return The Rosetta compatible Amount
   */
  public static Amount mapAmount(String value) {
    if (Objects.isNull(value)) {
      return null;
    }

    Currency currency = getAdaCurrency();
    return Amount.builder().value(value).currency(currency).build();
  }

  /**
   * Creates a Rosetta compatible Amount. Symbol and decimals are optional. If not provided, ADA and
   * 6 decimals are used.
   *
   * @param value    The amount of the token
   * @param symbol   The symbol of the token - it will be hex encoded
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
        .metadata(metadata)
        .build());
    return amount;
  }

  /**
   * Maps a list of AddressBalanceDTOs to a Rosetta compatible AccountBalanceResponse.
   *
   * @param block    The block from where the balances are calculated into the past
   * @param balances The list of filtered balances up to {@code block} number. Each unit should
   *                 occur only one time with the latest balance. Native assets should be present
   *                 only as a lovelace unit.
   * @return The Rosetta compatible AccountBalanceResponse
   */
  public static AccountBalanceResponse mapToAccountBalanceResponse(BlockIdentifierExtended block,
      List<AddressBalance> balances) {
    BigInteger lovelaceAmount = balances.stream()
        .filter(b -> Constants.LOVELACE.equals(b.unit()))
        .map(AddressBalance::quantity)
        .findFirst()
        .orElse(BigInteger.ZERO);
    List<Amount> amounts = new ArrayList<>();
    if (lovelaceAmount.compareTo(BigInteger.ZERO) > 0) {
      amounts.add(mapAmount(String.valueOf(lovelaceAmount)));
    }
    balances.stream()
        .filter(b -> !Constants.LOVELACE.equals(b.unit()))
        .forEach(b -> amounts.add(
            mapAmount(b.quantity().toString(),
                b.unit().substring(Constants.POLICY_ID_LENGTH),
                Constants.MULTI_ASSET_DECIMALS,
                new CurrencyMetadata(b.unit().substring(0, Constants.POLICY_ID_LENGTH)))
        ));
    return AccountBalanceResponse.builder()
        .blockIdentifier(BlockIdentifier.builder()
            .hash(block.getHash())
            .index(block.getNumber())
            .build())
        .balances(amounts)
        .build();
  }

  public static AccountBalanceResponse mapToStakeAddressBalanceResponse(
      BlockIdentifierExtended block,
      BigInteger quantity) {
    return AccountBalanceResponse.builder()
        .blockIdentifier(BlockIdentifier.builder()
            .hash(block.getHash())
            .index(block.getNumber())
            .build())
        .balances(List.of(Objects.requireNonNull(mapAmount(quantity.toString()))))
        .build();
  }

  public static AccountCoinsResponse mapToAccountCoinsResponse(BlockIdentifierExtended block,
      List<Utxo> utxos) {
    return AccountCoinsResponse.builder()
        .blockIdentifier(new BlockIdentifier(block.getNumber(), block.getHash()))
        .coins(utxos.stream().map(utxo -> {
              Amt adaAsset = utxo.getAmounts().stream()
                  .filter(amt -> Constants.LOVELACE.equals(amt.getAssetName()))
                  .findFirst()
                  .orElseGet(() -> new Amt(null, null, Constants.ADA, BigInteger.ZERO));
              String coinIdentifier = utxo.getTxHash() + ":" + utxo.getOutputIndex();
              return Coin.builder()
                  .coinIdentifier(new CoinIdentifier(coinIdentifier))
                  .amount(Amount.builder()
                      .value(adaAsset.getQuantity().toString())
                      .currency(getAdaCurrency())
                      .build())
                  .metadata(mapCoinMetadata(utxo, coinIdentifier))
                  .build();
            })
            .toList())
        .build();
  }

  @Nullable
  private static Map<String, List<CoinTokens>> mapCoinMetadata(Utxo utxo, String coinIdentifier) {
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
    return coinTokens.isEmpty() ? null : Map.of(coinIdentifier, coinTokens);
  }

  public ConstructionMetadataResponse mapToMetadataResponse(ProtocolParams protocolParams, Long ttl,
      Long suggestedFee) {
    return ConstructionMetadataResponse.builder()
        .metadata(ConstructionMetadataResponseMetadata.builder()
            .ttl(new BigDecimal(ttl))
            .protocolParameters(
                protocolParamsToRosettaProtocolParameters.toProtocolParameters(protocolParams))
            .build())
        .suggestedFee(List.of(Amount.builder()
            .value(suggestedFee.toString())
            .currency(getAdaCurrency())
            .build()))
        .build();
  }

  public static List<Signatures> mapRosettaSignatureToSignaturesList(List<Signature> signatures) {
    return signatures.stream().map(signature -> {
      String chainCode = null;
      String address = null;
      AccountIdentifier accountIdentifier = signature.getSigningPayload().getAccountIdentifier();
      if (!ObjectUtils.isEmpty(accountIdentifier)) {
        chainCode = accountIdentifier.getMetadata().getChainCode();
        address = accountIdentifier.getAddress();
      }
      return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
          chainCode, address);
    }).toList();
  }

  private static Currency getAdaCurrency() {
    return Currency.builder()
        .symbol(Constants.ADA)
        .decimals(Constants.ADA_DECIMALS)
        .build();
  }
}
