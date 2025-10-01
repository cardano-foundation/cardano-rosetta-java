package org.cardanofoundation.rosetta.api.common.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.*;
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
public class TokenRegistryServiceImpl implements TokenRegistryService {

    private final TokenRegistryHttpGateway tokenRegistryHttpGateway;

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> getTokenMetadataBatch(@NotNull Set<AssetFingerprint> assetFingerprints) {
        if (assetFingerprints.isEmpty()) {
            return Map.of();
        }

        // Convert assets to subjects for the gateway call
        Set<String> subjects = assetFingerprints.stream()
                .map(AssetFingerprint::toSubject)
                .collect(Collectors.toSet());

        // Make the batch call to the gateway
        Map<String, Optional<TokenSubject>> tokenSubjectMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

        // Convert back to Asset -> TokenRegistryCurrencyData mapping
        Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();

        for (AssetFingerprint assetFingerprint : assetFingerprints) {
            String subject = assetFingerprint.toSubject();
            Optional<TokenSubject> tokenSubject = tokenSubjectMap.get(subject);

            if (tokenSubject != null && tokenSubject.isPresent()) {
                result.put(assetFingerprint, extractTokenMetadata(assetFingerprint.getPolicyId(), tokenSubject.get()));
            } else {
                // Always return fallback metadata with at least policyId
                result.put(assetFingerprint, createFallbackMetadata(assetFingerprint.getPolicyId()));
            }
        }

        return result;
    }

    private TokenRegistryCurrencyData extractTokenMetadata(String policyId,
                                                           TokenSubject tokenSubject) {
        TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder = TokenRegistryCurrencyData.builder()
                .policyId(policyId);

        TokenMetadata tokenMeta = tokenSubject.getMetadata();

        // Mandatory fields from registry API related to token data
        builder.subject(tokenSubject.getSubject());
        builder.name(tokenMeta.getName().getValue());
        builder.description(tokenMeta.getDescription().getValue());

        // Optional fields
        Optional.ofNullable(tokenMeta.getTicker()).ifPresent(ticker -> builder.ticker(ticker.getValue()));
        Optional.ofNullable(tokenMeta.getUrl()).ifPresent(url -> builder.url(url.getValue()));
        Optional.ofNullable(tokenMeta.getLogo()).ifPresent(logo -> builder.logo(convertToLogoData(logo)));
        Optional.ofNullable(tokenMeta.getVersion()).ifPresent(version -> builder.version(BigDecimal.valueOf(version.getValue())));

        // Set decimals, defaulting to 0 if not found
        int decimals = Optional.ofNullable(tokenMeta.getDecimals())
                .map(TokenPropertyNumber::getValue)
                .map(Long::intValue)
                .orElse(0);
        builder.decimals(decimals);

        return builder.build();
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromBlockTx(@NonNull BlockTx blockTx) {
        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();
        
        // Collect assets from inputs
        Optional.ofNullable(blockTx.getInputs()).ifPresent(inputs ->
            inputs.forEach(input ->
                Optional.ofNullable(input.getAmounts()).ifPresent(amounts ->
                    allAssetFingerprints.addAll(extractAssetsFromAmounts(amounts)))));
        
        // Collect assets from outputs
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
            .filter(amount -> !LOVELACE.equals(amount.getUnit())) // Filter out ADA
            .map(amount -> {
                String symbol = amount.getSymbolHex();

                return AssetFingerprint.of(amount.getPolicyId(), symbol);
            })
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

            // Also check the amount field if it contains native tokens
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

        // Extract all assets from all transactions in the list
        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();

        for (BlockTx tx : blockTxList) {
            allAssetFingerprints.addAll(extractAssetsFromBlockTx(tx));
        }

        // If there are native tokens, make single batch call for metadata
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
            .map(b -> {
                String symbol = b.getSymbol();
                String policyId = b.getPolicyId();

                return AssetFingerprint.of(policyId, symbol);
            })
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
                if (amount.getPolicyId() != null && !LOVELACE.equals(amount.getUnit())) { // Filter out ADA and null policyId
                    String symbol = amount.getSymbolHex();

                    assetFingerprints.add(AssetFingerprint.of(amount.getPolicyId(), symbol));
                }
            }
        }

        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }

        return getTokenMetadataBatch(assetFingerprints);
    }

    @Nullable
    private TokenRegistryCurrencyData.LogoData convertToLogoData(TokenProperty logoProperty) {
        if (logoProperty == null) {
            return null;
        }
        String source = logoProperty.getSource();
        String value = logoProperty.getValue();

        return TokenRegistryCurrencyData.LogoData.builder()
                .format(getLogoFormat(source))
                .value(value)
                .build();
    }

    @Nullable
    private static TokenRegistryCurrencyData.LogoFormat getLogoFormat(@NonNull String source) {
        return switch (source.toLowerCase()) {
            case "cip_26" -> TokenRegistryCurrencyData.LogoFormat.BASE64;
            case "cip_68" -> TokenRegistryCurrencyData.LogoFormat.URL;
            default -> null;
        };
    }

    private TokenRegistryCurrencyData createFallbackMetadata(String policyId) {
        return TokenRegistryCurrencyData.builder()
                .policyId(policyId)
                .build();
    }

}
