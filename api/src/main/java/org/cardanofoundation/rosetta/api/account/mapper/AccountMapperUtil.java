package org.cardanofoundation.rosetta.api.account.mapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import org.springframework.stereotype.Component;
import org.mapstruct.Named;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Coin;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.CoinTokens;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
public class AccountMapperUtil {

  @Named("mapAddressBalancesToAmounts")
  public List<Amount> mapAddressBalancesToAmounts(List<AddressBalance> balances) {
    BigInteger lovelaceAmount = balances.stream()
        .filter(b -> Constants.LOVELACE.equals(b.unit()))
        .map(AddressBalance::quantity)
        .findFirst()
        .orElse(BigInteger.ZERO);
    List<Amount> amounts = new ArrayList<>();
    if (lovelaceAmount.compareTo(BigInteger.ZERO) > 0) {
      amounts.add(DataMapper.mapAmount(String.valueOf(lovelaceAmount), null, null, null));
    }
    balances.stream()
        .filter(b -> !Constants.LOVELACE.equals(b.unit()))
        .forEach(b -> amounts.add(
            DataMapper.mapAmount(b.quantity().toString(),
                b.unit().substring(Constants.POLICY_ID_LENGTH),
                Constants.MULTI_ASSET_DECIMALS,
                new CurrencyMetadata(b.unit().substring(0, Constants.POLICY_ID_LENGTH)))
        ));
    return amounts;
  }

  @Named("mapUtxosToCoins")
  public List<Coin> mapUtxosToCoins(List<Utxo> utxos) {
    return utxos.stream().map(utxo -> {
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
    }).toList();
  }

  @Nullable
  private Map<String, List<CoinTokens>> mapCoinMetadata(Utxo utxo, String coinIdentifier) {
    List<CoinTokens> coinTokens =
        utxo.getAmounts().stream()
            .filter(Objects::nonNull)
            .filter(amount -> amount.getPolicyId() != null
                && amount.getAssetName() != null
                && amount.getQuantity() != null)
            .map(amount -> {
              CoinTokens tokens = new CoinTokens();
              tokens.setPolicyId(amount.getPolicyId());
              tokens.setTokens(List.of(DataMapper.mapAmount(amount.getQuantity().toString(),
                  // unit = assetName + policyId. To get the symbol policy ID must be removed from Unit. According to CIP67
                  amount.getUnit().replace(amount.getPolicyId(), ""),
                  Constants.MULTI_ASSET_DECIMALS, new CurrencyMetadata(amount.getPolicyId()))));
              return tokens;
            })
            .toList();
    return coinTokens.isEmpty() ? null : Map.of(coinIdentifier, coinTokens);
  }

  private Currency getAdaCurrency() {
    return Currency.builder()
        .symbol(Constants.ADA)
        .decimals(Constants.ADA_DECIMALS)
        .build();
  }
}
