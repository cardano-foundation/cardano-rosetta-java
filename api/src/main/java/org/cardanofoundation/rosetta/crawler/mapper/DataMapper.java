package org.cardanofoundation.rosetta.crawler.mapper;

import static org.cardanofoundation.rosetta.crawler.common.constants.Constants.ADA;
import static org.cardanofoundation.rosetta.crawler.common.constants.Constants.ADA_DECIMALS;
import static org.cardanofoundation.rosetta.crawler.common.constants.Constants.MULTI_ASSET_DECIMALS;
import static org.cardanofoundation.rosetta.crawler.util.Formatters.hexStringFormatter;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.exception.AddressRuntimeException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.crawler.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.crawler.common.enumeration.CatalystSigIndexes;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.BalanceAtBlock;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.Coin;
import org.cardanofoundation.rosetta.crawler.model.rest.TokenBundleItem;
import org.cardanofoundation.rosetta.crawler.model.rest.Utxo;
import org.cardanofoundation.rosetta.crawler.projection.dto.BlockUtxos;
import org.cardanofoundation.rosetta.crawler.projection.dto.BlockUtxosMultiAssets;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Currency;


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


}





