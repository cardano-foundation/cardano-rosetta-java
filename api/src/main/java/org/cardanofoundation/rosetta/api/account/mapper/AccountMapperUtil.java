package org.cardanofoundation.rosetta.api.account.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.mapstruct.Named;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountMapperUtil {

    private final TokenRegistryService tokenRegistryService;

    @Named("mapAddressBalancesToAmounts")
    public List<Amount> mapAddressBalancesToAmounts(List<AddressBalance> balances) {
        BigInteger lovelaceAmount = balances.stream()
                .filter(b -> Constants.LOVELACE.equals(b.unit()))
                .map(AddressBalance::quantity)
                .findFirst()
                .orElse(BigInteger.ZERO);

        List<Amount> amounts = new ArrayList<>();
        // always adding lovelace amount to the beginning of the list. Even if lovelace amount is 0
        amounts.add(DataMapper.mapAmount(String.valueOf(lovelaceAmount), null, null, null));

        // Filter native token balances (those with proper unit format)
        List<AddressBalance> nativeTokenBalances = balances.stream()
                .filter(b -> !Constants.LOVELACE.equals(b.unit()))
                .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH) // Has policyId + assetName (assetName can be empty)
                .toList();

        if (nativeTokenBalances.isEmpty()) {
            return amounts;
        }

        // Collect all unique assets for bulk metadata fetch
        Set<Asset> assets = nativeTokenBalances.stream()
                .map(b -> {
                    String symbol = b.unit().substring(Constants.POLICY_ID_LENGTH);
                    String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);
                    return Asset.builder()
                            .policyId(policyId)
                            .assetName(symbol)
                            .build();
                })
                .collect(Collectors.toSet());

        // Bulk fetch token metadata
        //Map<Asset, CurrencyMetadataResponse> metadataMap = tokenRegistryService.getTokenMetadataBatch(assets);
        Map<Asset, CurrencyMetadataResponse> metadataMap = new HashMap<>();

        // Process each native token balance with metadata
        for (AddressBalance b : nativeTokenBalances) {
            String symbol = b.unit().substring(Constants.POLICY_ID_LENGTH);
            String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);

            Asset asset = Asset.builder()
                    .policyId(policyId)
                    .assetName(symbol)
                    .build();

            // Get metadata from pre-fetched map
            CurrencyMetadataResponse metadataResponse = metadataMap.get(asset);

            amounts.add(
                    DataMapper.mapAmount(b.quantity().toString(),
                            symbol,
                            getDecimalsWithFallback(metadataResponse),
                            metadataResponse)
            );
        }

        return amounts;
    }

    @Named("mapUtxosToCoins")
    public List<Coin> mapUtxosToCoins(List<Utxo> utxos) {
        return utxos.stream().map(utxo -> {
            Amt adaAsset = utxo.getAmounts().stream()
                    .filter(amt -> Constants.LOVELACE.equals(amt.getUnit()))
                    .findFirst()
                    .orElseGet(() -> new Amt(null, null, Constants.ADA, BigInteger.ZERO));
            String coinIdentifier = "%s:%d".formatted(utxo.getTxHash(), utxo.getOutputIndex());

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
        // Filter only native tokens (non-ADA amounts with policyId)
        List<Amt> nativeTokenAmounts = utxo.getAmounts().stream()
                .filter(Objects::nonNull)
                .filter(amount -> amount.getPolicyId() != null
                        && amount.getAssetName() != null // assetName can be empty string for tokens with no name
                        && amount.getQuantity() != null)
                .filter(amount -> !Constants.LOVELACE.equals(amount.getAssetName())) // exclude ADA
                .toList();

        if (nativeTokenAmounts.isEmpty()) {
            return null;
        }

        // Collect all unique assets for bulk metadata fetch
        Set<Asset> assets = nativeTokenAmounts.stream()
                .map(amount -> Asset.builder()
                        .policyId(amount.getPolicyId())
                        .assetName(amount.getAssetName())
                        .build())
                .collect(Collectors.toSet());

        // Bulk fetch token metadata
        //Map<Asset, CurrencyMetadataResponse> metadataMap = tokenRegistryService.getTokenMetadataBatch(assets);
        Map<Asset, CurrencyMetadataResponse> metadataMap = new HashMap<>();

        // Create separate CoinTokens entry for each native token (one token per entry)
        List<CoinTokens> coinTokens = nativeTokenAmounts.stream()
                .map(amount -> {
                    String policyId = amount.getPolicyId();

                    Asset asset = Asset.builder()
                            .policyId(policyId)
                            .assetName(amount.getAssetName())
                            .build();

                    // Get metadata from pre-fetched map
                    CurrencyMetadataResponse metadataResponse = metadataMap.get(asset);

                    Amount tokenAmount = DataMapper.mapAmount(
                            amount.getQuantity().toString(),
                            // unit = assetName + policyId. To get the symbol policy ID must be removed from Unit. According to CIP67
                            amount.getUnit().replace(amount.getPolicyId(), ""),
                            getDecimalsWithFallback(metadataResponse),
                            metadataResponse
                    );

                    CoinTokens tokens = new CoinTokens();
                    tokens.setPolicyId(policyId);
                    tokens.setTokens(List.of(tokenAmount));
                    return tokens;
                })
                .toList();

        return coinTokens.isEmpty() ? null : Map.of(coinIdentifier, coinTokens);
    }

    private static int getDecimalsWithFallback(CurrencyMetadataResponse metadataResponse) {
        return Optional.ofNullable(metadataResponse.getDecimals()).orElse(0);
    }

    private CurrencyResponse getAdaCurrency() {
        return CurrencyResponse.builder()
                .symbol(Constants.ADA)
                .decimals(Constants.ADA_DECIMALS)
                .build();
    }

}
