package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.TokenLogoEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.TokenMetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.MetadataReferenceNftRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.TokenLogoRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.TokenMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

/**
 * Read-only service for querying token metadata from CIP-26 and CIP-68 tables
 * populated by yaci-store assets-ext in the indexer.
 * <p>
 * Abstracts the CIP standard details. Results are merged with CIP-68 taking priority
 * over CIP-26 (first non-null value wins in priority order).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenQueryService {

    private static final String CIP68_FUNGIBLE_TOKEN_PREFIX = "0014df10";
    private static final String CIP68_REFERENCE_TOKEN_PREFIX = "000643b0";
    private static final int CIP68_LABEL_FT = 333;

    private final TokenMetadataRepository tokenMetadataRepository;
    private final TokenLogoRepository tokenLogoRepository;
    private final MetadataReferenceNftRepository metadataReferenceNftRepository;

    @Value("${cardano.rosetta.TOKEN_REGISTRY_LOGO_FETCH:false}")
    private boolean logoEnabled;

    /**
     * Batch query merged token metadata for multiple asset fingerprints.
     * CIP-26 lookups are batched; CIP-68 is per-subject (label-filtered query).
     *
     * @param fingerprints asset fingerprints (policyId + symbol hex)
     * @return map of asset fingerprint -> merged currency data
     */
    public Map<AssetFingerprint, TokenRegistryCurrencyData> queryMetadataBatch(Collection<AssetFingerprint> fingerprints) {
        if (fingerprints.isEmpty()) {
            return Map.of();
        }

        List<String> subjects = fingerprints.stream()
                .map(AssetFingerprint::toSubject)
                .toList();

        // Fork CIP-26 metadata and logo queries in parallel
        Map<String, TokenMetadataEntity> cip26Map;
        Map<String, String> cip26Logos;

        try (StructuredTaskScope.ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<Map<String, TokenMetadataEntity>> metadataTask = scope.fork(() ->
                    tokenMetadataRepository.findAllBySubjectIn(subjects).stream()
                            .collect(Collectors.toMap(TokenMetadataEntity::getSubject, m -> m)));

            StructuredTaskScope.Subtask<Map<String, String>> logoTask = scope.fork(() ->
                    logoEnabled
                            ? tokenLogoRepository.findAllBySubjectIn(subjects).stream()
                                .filter(l -> l.getLogo() != null)
                                .collect(Collectors.toMap(TokenLogoEntity::getSubject, TokenLogoEntity::getLogo))
                            : Map.<String, String>of());

            scope.join().throwIfFailed();

            cip26Map = metadataTask.get();
            cip26Logos = logoTask.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token metadata batch query interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Token metadata batch query failed", e);
        }

        Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();
        for (AssetFingerprint fingerprint : fingerprints) {
            String subject = fingerprint.toSubject();

            TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder = TokenRegistryCurrencyData.builder()
                    .policyId(fingerprint.getPolicyId())
                    .subject(subject);

            // CIP-26 base
            TokenMetadataEntity cip26 = cip26Map.get(subject);
            if (cip26 != null) {
                applyCip26Fields(builder, cip26);
                if (logoEnabled) {
                    applyCip26Logo(builder, cip26Logos.get(subject));
                }
            }

            // CIP-68 overrides (higher priority)
            findCip68ReferenceNft(fingerprint).ifPresent(cip68 -> applyCip68(builder, cip68));

            result.put(fingerprint, builder.build());
        }
        return result;
    }

    /**
     * Looks up the CIP-68 reference NFT metadata for a fungible token fingerprint.
     * Converts the fungible token symbol prefix ({@code 0014df10}) to the reference NFT
     * prefix ({@code 000643b0}) and fetches the latest entry by slot.
     * Returns empty if the symbol is not a CIP-68 fungible token.
     */
    private Optional<MetadataReferenceNftEntity> findCip68ReferenceNft(AssetFingerprint fingerprint) {
        String symbol = fingerprint.getSymbol();
        if (symbol.length() <= CIP68_FUNGIBLE_TOKEN_PREFIX.length()
                || !symbol.startsWith(CIP68_FUNGIBLE_TOKEN_PREFIX)) {
            return Optional.empty();
        }

        String refNftAssetName = CIP68_REFERENCE_TOKEN_PREFIX
                + symbol.substring(CIP68_FUNGIBLE_TOKEN_PREFIX.length());

        return metadataReferenceNftRepository
                .findFirstByPolicyIdAndAssetNameAndLabelOrderBySlotDesc(fingerprint.getPolicyId(), refNftAssetName, CIP68_LABEL_FT);
    }

    private void applyCip26Fields(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                                   TokenMetadataEntity cip26) {
        builder.name(cip26.getName());
        builder.description(cip26.getDescription());
        Optional.ofNullable(cip26.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip26.getUrl()).ifPresent(builder::url);
        // Leave decimals null if CIP-26 doesn't provide it; downstream mappers apply the default.
        if (cip26.getDecimals() != null) {
            builder.decimals(cip26.getDecimals().intValue());
        }
    }

    private void applyCip26Logo(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                                @Nullable String logo) {
        if (logo != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                    .value(logo)
                    .build());
        }
    }

    private void applyCip68(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            MetadataReferenceNftEntity cip68) {
        Optional.ofNullable(cip68.getName()).ifPresent(builder::name);
        Optional.ofNullable(cip68.getDescription()).ifPresent(builder::description);
        Optional.ofNullable(cip68.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip68.getUrl()).ifPresent(builder::url);
        Optional.ofNullable(cip68.getVersion()).ifPresent(v -> builder.version(BigDecimal.valueOf(v)));
        if (cip68.getDecimals() != null) {
            builder.decimals(cip68.getDecimals().intValue());
        }
        if (logoEnabled && cip68.getLogo() != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(detectLogoFormat(cip68.getLogo()))
                    .value(cip68.getLogo())
                    .build());
        }
    }

    /**
     * Detects whether a CIP-68 logo string is a URL or base64-encoded image.
     * URLs are identified by known schemes; everything else is treated as base64.
     * Note: {@code data:} URIs are treated as base64 since consumers decode them, not fetch them.
     */
    private static TokenRegistryCurrencyData.LogoFormat detectLogoFormat(String logo) {
        String lower = logo.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("ipfs://") || lower.startsWith("ar://")) {
            return TokenRegistryCurrencyData.LogoFormat.URL;
        }
        return TokenRegistryCurrencyData.LogoFormat.BASE64;
    }
}
