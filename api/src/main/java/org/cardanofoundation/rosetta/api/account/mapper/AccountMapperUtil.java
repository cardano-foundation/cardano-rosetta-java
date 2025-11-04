package org.cardanofoundation.rosetta.api.account.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AccountMapperUtil {

    private final DataMapper dataMapper;

    @Named("mapAddressBalancesToAmounts")
    public List<Amount> mapAddressBalancesToAmounts(List<AddressBalance> balances,
                                                    @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap) {
        BigInteger lovelaceAmount = balances.stream()
                .filter(b -> Constants.LOVELACE.equals(b.unit()))
                .map(AddressBalance::quantity)
                .findFirst()
                .orElse(BigInteger.ZERO);

        List<Amount> amounts = new ArrayList<>();
        // always adding lovelace amount to the beginning of the list. Even if lovelace amount is 0
        amounts.add(dataMapper.mapAmount(String.valueOf(lovelaceAmount), null, null, null));

        // Filter native token balances (those with proper unit format)
        List<AddressBalance> nativeTokenBalances = balances.stream()
                .filter(b -> !Constants.LOVELACE.equals(b.unit()))
                .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH) // Has policyId + assetName (assetName can be empty)
                .toList();

        if (nativeTokenBalances.isEmpty()) {
            return amounts;
        }

        // Use pre-fetched metadata passed via @Context from service layer
        // Process each native token balance with metadata
        for (AddressBalance b : nativeTokenBalances) {
            String symbol = b.getSymbol();
            String policyId = b.getPolicyId();

            AssetFingerprint assetFingerprint = AssetFingerprint.of(policyId, symbol);

            // Get metadata from pre-fetched map
            TokenRegistryCurrencyData metadata = metadataMap.get(assetFingerprint);

            amounts.add(
                    dataMapper.mapAmount(b.quantity().toString(),
                            symbol,
                            getDecimalsWithFallback(metadata),
                            metadata)
            );
        }

        return amounts;
    }

    @Named("mapUtxosToCoins")
    public List<Coin> mapUtxosToCoins(List<Utxo> utxos,
                                      @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap) {
        return utxos.stream().map(utxo -> {
            Amt adaAsset = utxo.getAmounts().stream()
                    .filter(amt -> Constants.LOVELACE.equals(amt.getUnit()))
                    .findFirst()
                    .orElseGet(() -> new Amt(null, Constants.ADA, BigInteger.ZERO));

            String coinIdentifier = "%s:%d".formatted(utxo.getTxHash(), utxo.getOutputIndex());

            return Coin.builder()
                    .coinIdentifier(new CoinIdentifier(coinIdentifier))
                    .amount(Amount.builder()
                            .value(adaAsset.getQuantity().toString())
                            .currency(getAdaCurrency())
                            .build())

                    .metadata(mapCoinMetadata(utxo, coinIdentifier, metadataMap))
                    .build();
        }).toList();
    }

    @Nullable
    private Map<String, List<CoinTokens>> mapCoinMetadata(Utxo utxo, String coinIdentifier,
                                                          Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap) {
        // Filter only native tokens (non-ADA amounts with policyId)
        List<Amt> nativeTokenAmounts = utxo.getAmounts().stream()
                .filter(Objects::nonNull)
                .filter(amount -> amount.getPolicyId() != null
                        && amount.getQuantity() != null)
                .filter(amount -> !Constants.LOVELACE.equals(amount.getUnit())) // exclude ADA
                .toList();

        if (nativeTokenAmounts.isEmpty()) {
            return null;
        }

        // Use pre-fetched metadata passed via @Context from service layer
        // Create separate CoinTokens entry for each native token (one token per entry)
        List<CoinTokens> coinTokens = nativeTokenAmounts.stream()
                .map(amount -> {
                    String policyId = amount.getPolicyId();
                    String symbol = amount.getSymbolHex();

                    AssetFingerprint assetFingerprint = AssetFingerprint.of(policyId, symbol);

                    // Get metadata from pre-fetched map
                    TokenRegistryCurrencyData metadata = metadataMap.get(assetFingerprint);

                    Amount tokenAmount = dataMapper.mapAmount(
                            amount.getQuantity().toString(),
                            symbol,
                            getDecimalsWithFallback(metadata),
                            metadata
                    );

                    CoinTokens tokens = new CoinTokens();
                    tokens.setPolicyId(policyId);
                    tokens.setTokens(List.of(tokenAmount));

                    return tokens;
                })
                .toList();

        return coinTokens.isEmpty() ? null : Map.of(coinIdentifier, coinTokens);
    }

    private static int getDecimalsWithFallback(@NotNull TokenRegistryCurrencyData metadata) {
        return Optional.ofNullable(metadata.getDecimals())
                .orElse(0);
    }

    private CurrencyResponse getAdaCurrency() {
        return CurrencyResponse.builder()
                .symbol(Constants.ADA)
                .decimals(Constants.ADA_DECIMALS)
                .build();
    }

}
