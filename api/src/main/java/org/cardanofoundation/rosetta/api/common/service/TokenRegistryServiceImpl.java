package org.cardanofoundation.rosetta.api.common.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
                result.put(assetFingerprint, extractTokenMetadata(assetFingerprint, tokenSubject.get()));
            } else {
                // Always return fallback metadata with at least policyId
                result.put(assetFingerprint, createFallbackMetadata(assetFingerprint.getPolicyId()));
            }
        }

        return result;
    }

    /**
     * Maps a token registry V2 subject to Rosetta currency metadata, returning what the registry returns.
     *
     * <p>The policy ID and subject always come from the requested asset. Every metadata property is
     * optional and passed through as served: since cf-token-metadata-registry 2.0.0 the V2 API (default
     * {@code ?validation=ALL}) also serves tokens that break their CIP, together with their
     * {@code validation_errors}, so {@code name} and {@code description} can be missing. A token with no
     * metadata at all (e.g. a CIP-113 programmable token without CIP-26/CIP-68 metadata) is served with
     * an empty {@code metadata} block. {@code decimals} defaults to 0, as CIP-26 defines.</p>
     */
    private TokenRegistryCurrencyData extractTokenMetadata(@NotNull AssetFingerprint assetFingerprint,
                                                           @NotNull TokenSubject tokenSubject) {
        // metadata is required by the registry's V2 contract; an empty block guards the HTTP boundary
        TokenMetadata tokenMeta = Optional.ofNullable(tokenSubject.getMetadata()).orElseGet(TokenMetadata::new);

        int decimals = Optional.ofNullable(tokenMeta.getDecimals())
                .map(TokenPropertyNumber::getValue)
                .map(Long::intValue)
                .orElse(0);

        return TokenRegistryCurrencyData.builder()
                .policyId(assetFingerprint.getPolicyId())
                .subject(assetFingerprint.toSubject())
                .name(propertyValue(tokenMeta.getName()))
                .description(propertyValue(tokenMeta.getDescription()))
                .ticker(propertyValue(tokenMeta.getTicker()))
                .url(propertyValue(tokenMeta.getUrl()))
                .logo(Optional.ofNullable(tokenMeta.getLogo()).map(this::convertToLogoData).orElse(null))
                .version(Optional.ofNullable(tokenMeta.getVersion())
                        .map(TokenPropertyNumber::getValue)
                        .map(BigDecimal::valueOf)
                        .orElse(null))
                .decimals(decimals)
                .build();
    }

    @Nullable
    private static String propertyValue(@Nullable TokenProperty property) {
        return Optional.ofNullable(property).map(TokenProperty::getValue).orElse(null);
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

        TokenRegistryCurrencyData.LogoFormat format = getLogoFormat(source);
        String encodedValue = encodeLogoValue(format, value);

        if (encodedValue == null && value != null
                && format == TokenRegistryCurrencyData.LogoFormat.BASE64) {
            return null;
        }

        return TokenRegistryCurrencyData.LogoData.builder()
                .format(format)
                .value(encodedValue)
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

    @Nullable
    private String encodeLogoValue(
            @Nullable TokenRegistryCurrencyData.LogoFormat format,
            @Nullable String value) {
        if (format != TokenRegistryCurrencyData.LogoFormat.BASE64) {
            return value;
        }
        if (value == null || value.isEmpty()) {
            return null;
        }
        return toBase64Logo(value);
    }

    /**
     * Normalises a CIP-26 logo from the token registry's V2 {@code metadata} block to base64.
     *
     * <p>cf-token-metadata-registry before 2.0.0 serves this logo hex-encoded; 2.0.0 and later serve it
     * base64-encoded, as CIP-26 defines it (cf-token-metadata-registry#88). Both are supported, so Rosetta
     * works against either registry version.</p>
     *
     * <p>Hex is checked first because a hex string can also be well-formed base64 (e.g. {@code "deadbeef"}),
     * whereas a base64-encoded PNG (which starts with {@code "iVBORw0KGgo"}) is never pure hex.</p>
     *
     * <p>CIP-26 base64 is padded. {@link Base64.Decoder} (and Guava's {@code BaseEncoding.canDecode})
     * also accept unpadded input, so the length is required to be a multiple of 4 as well.</p>
     *
     * @param value the non-empty logo value as served by the registry
     * @return the logo as base64, or null if the value is neither hex nor base64
     */
    @Nullable
    private static String toBase64Logo(@NotNull String value) {
        if (value.length() % 2 == 0 && value.chars().allMatch(HexFormat::isHexDigit)) {
            return Base64.getEncoder().encodeToString(HexFormat.of().parseHex(value));
        }
        if (value.length() % 4 == 0) {
            try {
                Base64.getDecoder().decode(value);
                return value;
            } catch (IllegalArgumentException e) {
                log.debug("CIP-26 logo value is not valid base64: {}", e.getMessage());
            }
        }
        log.warn("CIP-26 logo value is neither hex nor base64 (length={}), skipping logo", value.length());

        return null;
    }

    private TokenRegistryCurrencyData createFallbackMetadata(String policyId) {
        return TokenRegistryCurrencyData.builder()
                .policyId(policyId)
                .build();
    }

}
