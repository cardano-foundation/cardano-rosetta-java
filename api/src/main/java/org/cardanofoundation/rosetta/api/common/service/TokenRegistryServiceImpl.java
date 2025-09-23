package org.cardanofoundation.rosetta.api.common.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.Asset;
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

import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Service
@RequiredArgsConstructor
public class TokenRegistryServiceImpl implements TokenRegistryService {

    private final TokenRegistryHttpGateway tokenRegistryHttpGateway;

    @Override
    public Map<Asset, CurrencyMetadataResponse> getTokenMetadataBatch(@NotNull Set<Asset> assets) {
        if (assets.isEmpty()) {
            return Map.of();
        }
        
        // Convert assets to subjects for the gateway call
        Set<String> subjects = assets.stream()
                .map(Asset::toSubject)
                .collect(Collectors.toSet());
        
        // Make the batch call to the gateway
        Map<String, Optional<TokenSubject>> tokenSubjectMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

        // Convert back to Asset -> CurrencyMetadataResponse mapping
        Map<Asset, CurrencyMetadataResponse> result = new HashMap<>();
        for (Asset asset : assets) {
            String subject = asset.toSubject();
            Optional<TokenSubject> tokenSubject = tokenSubjectMap.get(subject);
            
            if (tokenSubject != null && tokenSubject.isPresent()) {
                result.put(asset, extractTokenMetadata(asset.getPolicyId(), tokenSubject.get()));
            } else {
                // Always return fallback metadata with at least policyId
                result.put(asset, createFallbackMetadata(asset.getPolicyId()));
            }
        }
        
        return result;
    }

    private CurrencyMetadataResponse extractTokenMetadata(String policyId, TokenSubject tokenSubject) {
        CurrencyMetadataResponse.CurrencyMetadataResponseBuilder builder = CurrencyMetadataResponse.builder()
                .policyId(policyId);
        
        TokenMetadata tokenMeta = tokenSubject.getMetadata();
        
        // Mandatory fields from registry API related to token data
        builder.subject(tokenSubject.getSubject());
        builder.name(tokenMeta.getName().getValue());
        builder.description(tokenMeta.getDescription().getValue());
        
        // Optional fields
        Optional.ofNullable(tokenMeta.getTicker()).ifPresent(ticker -> builder.ticker(ticker.getValue()));
        Optional.ofNullable(tokenMeta.getUrl()).ifPresent(url -> builder.url(url.getValue()));
        Optional.ofNullable(tokenMeta.getLogo()).ifPresent(logo -> builder.logo(convertToLogoType(logo)));
        Optional.ofNullable(tokenMeta.getVersion()).ifPresent(version -> builder.version(BigDecimal.valueOf(version.getValue())));

        // Set decimals, defaulting to 0 if not found
        int decimals = Optional.ofNullable(tokenMeta.getDecimals())
                .map(TokenPropertyNumber::getValue)
                .map(Long::intValue)
                .orElse(0);
        builder.decimals(decimals);
        
        return builder.build();
    }

    @Nullable
    private LogoType convertToLogoType(TokenProperty logoProperty) {
        if (logoProperty == null) {
            return null;
        }
        String source = logoProperty.getSource();
        String value = logoProperty.getValue();

        return LogoType.builder()
                .format(getFormatEnum(source))
                .value(value)
                .build();
    }

    @Nullable
    private static LogoType.FormatEnum getFormatEnum(@NonNull String source) {
        return switch (source.toLowerCase()) {
            case "cip_26" -> LogoType.FormatEnum.BASE64;
            case "cip_68" -> LogoType.FormatEnum.URL;
            default -> null;
        };
    }

    private CurrencyMetadataResponse createFallbackMetadata(String policyId) {
        return CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .build();
    }
    
    @Override
    public Set<Asset> extractAssetsFromBlockTx(@NonNull BlockTx blockTx) {
        Set<Asset> allAssets = new HashSet<>();
        
        // Collect assets from inputs
        Optional.ofNullable(blockTx.getInputs()).ifPresent(inputs ->
            inputs.forEach(input ->
                Optional.ofNullable(input.getAmounts()).ifPresent(amounts ->
                    allAssets.addAll(extractAssetsFromAmounts(amounts)))));
        
        // Collect assets from outputs
        Optional.ofNullable(blockTx.getOutputs()).ifPresent(outputs ->
            outputs.forEach(output ->
                Optional.ofNullable(output.getAmounts()).ifPresent(amounts ->
                    allAssets.addAll(extractAssetsFromAmounts(amounts)))));
        
        return allAssets;
    }
    
    @Override
    public Set<Asset> extractAssetsFromAmounts(@NonNull List<Amt> amounts) {
        return amounts.stream()
            .filter(amount -> !LOVELACE.equals(amount.getAssetName()))
            .map(amount -> Asset.builder()
                .policyId(amount.getPolicyId())
                .assetName(amount.getAssetName())
                .build())
            .collect(Collectors.toSet());
    }
    
    @Override
    public Set<Asset> extractAssetsFromBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Set.of();
        }

        Set<Asset> allAssets = new HashSet<>();

        for (BlockTransaction blockTx : transactions) {
            Transaction tx = blockTx.getTransaction();
            // getOperations() returns @NotNull List (never null, at least empty list)
            allAssets.addAll(extractAssetsFromOperations(tx.getOperations()));
        }

        return allAssets;
    }
    
    @Override
    public Set<Asset> extractAssetsFromOperations(@NotNull List<Operation> operations) {
        if (operations.isEmpty()) {
            return Set.of();
        }

        Set<Asset> allAssets = new HashSet<>();

        for (Operation operation : operations) {
            // Check operation metadata for token bundles (tokenBundle can be null - NOT_REQUIRED)
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
                                    String assetName = currency.getSymbol();
                                    if (!LOVELACE.equals(assetName)) {
                                        allAssets.add(Asset.builder()
                                            .policyId(policyId)
                                            .assetName(assetName)
                                            .build());
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
                    if (!LOVELACE.equals(symbol)) {
                        CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
                        if (currencyMetadata != null && currencyMetadata.getPolicyId() != null) {
                            allAssets.add(Asset.builder()
                                .policyId(currencyMetadata.getPolicyId())
                                .assetName(symbol)
                                .build());
                        }
                    }
                }
            }
        }

        return allAssets;
    }

    @Override
    public Map<Asset, CurrencyMetadataResponse> fetchMetadataForBlockTx(@NotNull BlockTx blockTx) {
        Set<Asset> assets = extractAssetsFromBlockTx(blockTx);
        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assets);
    }

    @Override
    public Map<Asset, CurrencyMetadataResponse> fetchMetadataForBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Asset> assets = extractAssetsFromBlockTransactions(transactions);
        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }

        return getTokenMetadataBatch(assets);
    }

    @Override
    public Map<Asset, CurrencyMetadataResponse> fetchMetadataForBlockTxList(@NotNull List<BlockTx> blockTxList) {
        if (blockTxList == null || blockTxList.isEmpty()) {
            return Collections.emptyMap();
        }

        // Extract all assets from all transactions in the list
        Set<Asset> allAssets = new HashSet<>();
        for (BlockTx tx : blockTxList) {
            allAssets.addAll(extractAssetsFromBlockTx(tx));
        }

        // If there are native tokens, make single batch call for metadata
        if (!allAssets.isEmpty()) {
            return getTokenMetadataBatch(allAssets);
        }

        return Collections.emptyMap();
    }

    @Override
    public Map<Asset, CurrencyMetadataResponse> fetchMetadataForAddressBalances(@NotNull List<AddressBalance> balances) {
        Set<Asset> assets = balances.stream()
            .filter(b -> !LOVELACE.equals(b.unit()))
            .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH)
            .map(b -> {
                String symbol = b.unit().substring(Constants.POLICY_ID_LENGTH);
                String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);
                return Asset.builder()
                    .policyId(policyId)
                    .assetName(symbol)
                    .build();
            })
            .collect(Collectors.toSet());

        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }

        return getTokenMetadataBatch(assets);
    }

    @Override
    public Map<Asset, CurrencyMetadataResponse> fetchMetadataForUtxos(@NotNull List<Utxo> utxos) {
        Set<Asset> assets = new HashSet<>();
        for (Utxo utxo : utxos) {
            for (Amt amount : utxo.getAmounts()) {
                if (!LOVELACE.equals(amount.getAssetName()) && amount.getPolicyId() != null) {
                    assets.add(Asset.builder()
                        .policyId(amount.getPolicyId())
                        .assetName(amount.getAssetName())
                        .build());
                }
            }
        }

        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }

        return getTokenMetadataBatch(assets);
    }

}
