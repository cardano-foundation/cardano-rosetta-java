package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.util.Constants;

import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.Cip26StorageReader;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model.TokenMetadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.model.FungibleTokenMetadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.Cip68StorageReader;

import lombok.NonNull;

import org.openapitools.client.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRegistryServiceImpl implements TokenRegistryService {

    private final Cip26StorageReader cip26StorageReader;
    private final Cip68StorageReader cip68StorageReader;

    @Value("${cardano.rosetta.TOKEN_REGISTRY_LOGO_FETCH:false}")
    private boolean logoEnabled;

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> getTokenMetadataBatch(@NotNull Set<AssetFingerprint> assetFingerprints) {
        if (assetFingerprints.isEmpty()) {
            return Map.of();
        }

        List<String> subjects = assetFingerprints.stream()
                .map(AssetFingerprint::toSubject)
                .toList();

        // Batch CIP-26 lookup
        Map<String, TokenMetadata> cip26Map = cip26StorageReader.findBySubjects(subjects).stream()
                .collect(Collectors.toMap(TokenMetadata::getSubject, m -> m));

        // Batch CIP-26 logos (only if enabled)
        Map<String, String> cip26Logos = logoEnabled
                ? cip26StorageReader.findLogosBySubjects(subjects)
                : Map.of();

        Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();

        for (AssetFingerprint assetFingerprint : assetFingerprints) {
            String subject = assetFingerprint.toSubject();

            TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder = TokenRegistryCurrencyData.builder()
                    .policyId(assetFingerprint.getPolicyId())
                    .subject(subject);

            // Start with CIP-26 as base
            TokenMetadata cip26 = cip26Map.get(subject);
            if (cip26 != null) {
                applyCip26(builder, cip26, cip26Logos.get(subject));
            }

            // CIP-68 overrides (higher priority)
            Optional<FungibleTokenMetadata> cip68 = cip68StorageReader.findBySubject(subject);
            if (cip68.isPresent()) {
                applyCip68(builder, cip68.get());
            }

            result.put(assetFingerprint, builder.build());
        }

        return result;
    }

    private void applyCip26(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            @NotNull TokenMetadata cip26,
                            @Nullable String logo) {
        builder.name(cip26.getName());
        builder.description(cip26.getDescription());
        Optional.ofNullable(cip26.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip26.getUrl()).ifPresent(builder::url);
        builder.decimals(cip26.getDecimals() != null ? cip26.getDecimals().intValue() : 0);

        if (logoEnabled && logo != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                    .value(logo)
                    .build());
        }
    }

    private void applyCip68(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            @NotNull FungibleTokenMetadata cip68) {
        Optional.ofNullable(cip68.name()).ifPresent(builder::name);
        Optional.ofNullable(cip68.description()).ifPresent(builder::description);
        Optional.ofNullable(cip68.ticker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip68.url()).ifPresent(builder::url);
        Optional.ofNullable(cip68.version()).ifPresent(v -> builder.version(BigDecimal.valueOf(v)));
        if (cip68.decimals() != null) {
            builder.decimals(cip68.decimals().intValue());
        }

        if (logoEnabled && cip68.logo() != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.URL)
                    .value(cip68.logo())
                    .build());
        }
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromBlockTx(@NonNull BlockTx blockTx) {
        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();

        Optional.ofNullable(blockTx.getInputs()).ifPresent(inputs ->
            inputs.forEach(input ->
                Optional.ofNullable(input.getAmounts()).ifPresent(amounts ->
                    allAssetFingerprints.addAll(extractAssetsFromAmounts(amounts)))));

        Optional.ofNullable(blockTx.getOutputs()).ifPresent(outputs ->
            outputs.forEach(output ->
                Optional.ofNullable(output.getAmounts()).ifPresent(amounts ->
                    allAssetFingerprints.addAll(extractAssetsFromAmounts(amounts)))));

        return allAssetFingerprints;
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromAmounts(@NonNull List<Amt> amounts) {
        return amounts.stream()
            .filter(amount -> amount.getPolicyId() != null)
            .filter(amount -> !LOVELACE.equals(amount.getUnit()))
            .map(amount -> AssetFingerprint.of(amount.getPolicyId(), amount.getSymbolHex()))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Set.of();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();
        for (BlockTransaction blockTx : transactions) {
            Transaction tx = blockTx.getTransaction();
            allAssetFingerprints.addAll(extractAssetsFromOperations(tx.getOperations()));
        }

        return allAssetFingerprints;
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromOperations(@NotNull List<Operation> operations) {
        if (operations.isEmpty()) {
            return Set.of();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();

        for (Operation operation : operations) {
            OperationMetadata metadata = operation.getMetadata();
            if (metadata != null) {
                List<TokenBundleItem> tokenBundle = metadata.getTokenBundle();
                if (tokenBundle != null) {
                    for (TokenBundleItem bundleItem : tokenBundle) {
                        String policyId = bundleItem.getPolicyId();
                        List<Amount> tokens = bundleItem.getTokens();
                        if (tokens != null) {
                            for (Amount tokenAmount : tokens) {
                                CurrencyResponse currency = tokenAmount.getCurrency();
                                if (currency != null) {
                                    String symbol = currency.getSymbol();
                                    if (!ADA.equals(symbol) && !LOVELACE.equals(symbol)) {
                                        allAssetFingerprints.add(AssetFingerprint.of(policyId, currency.getSymbol()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Amount amount = operation.getAmount();
            if (amount != null) {
                CurrencyResponse currency = amount.getCurrency();
                if (currency != null) {
                    String symbol = currency.getSymbol();
                    if (!LOVELACE.equals(symbol) && !ADA.equals(symbol)) {
                        CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
                        if (currencyMetadata != null && currencyMetadata.getPolicyId() != null) {
                            allAssetFingerprints.add(AssetFingerprint.of(currencyMetadata.getPolicyId(), symbol));
                        }
                    }
                }
            }
        }

        return allAssetFingerprints;
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTx(@NotNull BlockTx blockTx) {
        Set<AssetFingerprint> assetFingerprints = extractAssetsFromBlockTx(blockTx);
        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<AssetFingerprint> assetFingerprints = extractAssetsFromBlockTransactions(transactions);
        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTxList(@NotNull List<BlockTx> blockTxList) {
        if (blockTxList == null || blockTxList.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();
        for (BlockTx tx : blockTxList) {
            allAssetFingerprints.addAll(extractAssetsFromBlockTx(tx));
        }

        if (!allAssetFingerprints.isEmpty()) {
            return getTokenMetadataBatch(allAssetFingerprints);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForAddressBalances(@NotNull List<AddressBalance> balances) {
        Set<AssetFingerprint> assetFingerprints = balances.stream()
            .filter(b -> !LOVELACE.equals(b.unit()))
            .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH)
            .map(b -> AssetFingerprint.of(b.getPolicyId(), b.getSymbol()))
            .collect(Collectors.toSet());

        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForUtxos(@NotNull List<Utxo> utxos) {
        Set<AssetFingerprint> assetFingerprints = new HashSet<>();
        for (Utxo utxo : utxos) {
            for (Amt amount : utxo.getAmounts()) {
                if (amount.getPolicyId() != null && !LOVELACE.equals(amount.getUnit())) {
                    assetFingerprints.add(AssetFingerprint.of(amount.getPolicyId(), amount.getSymbolHex()));
                }
            }
        }

        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }
}
