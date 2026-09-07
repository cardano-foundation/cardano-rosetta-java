package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.TokenMetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.MetadataReferenceNftRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.TokenMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
@Slf4j
public class TokenQueryServiceImpl implements TokenQueryService {

    private static final String CIP68_FUNGIBLE_TOKEN_PREFIX = "0014df10";
    private static final String CIP68_REFERENCE_TOKEN_PREFIX = "000643b0";

    private final TokenMetadataRepository tokenMetadataRepository;
    private final MetadataReferenceNftRepository metadataReferenceNftRepository;

    /**
     * Controls whether the {@code logo} field is exposed on the wire. With the V2 assets-ext
     * schema the logo lives on the CIP-26 row itself, so the value is always read from the
     * database — but base64 PNGs can run to ~87 KB, and many deployments don't want to ship
     * that on every response. Keeping the flag preserves the existing operational lever for
     * controlling response size; the only thing it no longer does is save a separate DB query.
     */
    @Value("${cardano.rosetta.TOKEN_REGISTRY_LOGO_FETCH:false}")
    private boolean logoEnabled;

    /**
     * Batch query merged token metadata for multiple asset fingerprints.
     * <p>
     * Runs unconditionally — this service is <em>not</em> gated by {@code TOKEN_REGISTRY_ENABLED}.
     * Its core job is to resolve the correct {@code decimals} for every fingerprint, which
     * Rosetta expects as a mandatory field on every {@code Currency}. Whether the remaining
     * enrichment fields are exposed on the wire is decided downstream by the serialization
     * layer.
     * <p>
     * Issues a constant number of DB queries regardless of batch size:
     * <ul>
     *   <li>1 for CIP-26 metadata (with logo embedded on the same row)</li>
     *   <li>1 for CIP-68 reference NFTs (only if the batch contains CIP-68 fungible tokens)</li>
     * </ul>
     *
     * @param fingerprints asset fingerprints (policyId + symbol hex)
     * @return map of asset fingerprint -> merged currency data (every fingerprint is keyed,
     *         {@code decimals} is never null)
     */
    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> queryMetadataBatch(Collection<AssetFingerprint> fingerprints) {
        if (fingerprints.isEmpty()) {
            return Map.of();
        }

        List<String> subjects = fingerprints.stream()
                .map(AssetFingerprint::toSubject)
                .toList();

        Map<String, TokenMetadataEntity> cip26 = tokenMetadataRepository.findAllBySubjectIn(subjects).stream()
                .collect(Collectors.toMap(TokenMetadataEntity::getSubject, Function.identity()));
        Cip68Batch cip68 = fetchCip68(fingerprints);

        Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();
        for (AssetFingerprint fingerprint : fingerprints) {
            result.put(fingerprint, mergeMetadata(fingerprint, cip26, cip68));
        }

        log.debug("Resolved metadata for {} fingerprint(s): {} CIP-26 match(es), {} CIP-68 match(es)",
                fingerprints.size(), cip26.size(), cip68.entitiesByRefNftKey().size());
        return result;
    }

    /**
     * Collects CIP-68 fungible-token candidates from the input fingerprints, issues a single
     * window-function query against {@code cip68_metadata}, and returns both the result map
     * (keyed by reference-NFT concat key) and a reverse lookup from fingerprint to concat key
     * so the merge phase can resolve entries in O(1).
     */
    private Cip68Batch fetchCip68(Collection<AssetFingerprint> fingerprints) {
        Map<AssetFingerprint, String> refNftKeyByFingerprint = new HashMap<>();
        List<String> cip68ConcatenatedKeys = new ArrayList<>();

        for (AssetFingerprint fingerprint : fingerprints) {
            String refNftAssetName = toCip68ReferenceNftAssetName(fingerprint.getSymbol());
            if (refNftAssetName != null) {
                String concatenatedKey = fingerprint.getPolicyId() + refNftAssetName;
                cip68ConcatenatedKeys.add(concatenatedKey);
                refNftKeyByFingerprint.put(fingerprint, concatenatedKey);
            }
        }

        Map<String, MetadataReferenceNftEntity> entitiesByRefNftKey = cip68ConcatenatedKeys.isEmpty()
                ? Map.of()
                : metadataReferenceNftRepository.findLatestByConcatenatedKeys(cip68ConcatenatedKeys).stream()
                        .collect(Collectors.toMap(
                                e -> e.getPolicyId() + e.getAssetName(),
                                Function.identity()));

        return new Cip68Batch(entitiesByRefNftKey, refNftKeyByFingerprint);
    }

    /**
     * Builds the merged currency data for a single fingerprint from the pre-fetched batches.
     * CIP-68 fields take priority over CIP-26 where both are populated.
     * <p>
     * {@code decimals} is seeded to {@code 0} — the canonical default for tokens that do not
     * advertise a decimal count — and overridden only when CIP-26 or CIP-68 supplies an
     * explicit value. This keeps {@link TokenRegistryCurrencyData#getDecimals()} non-null for
     * every fingerprint, even those with no registry entry at all, so mappers can treat it as
     * a guaranteed {@code int}.
     * <p>
     * {@code policyId} and {@code subject} are always populated from the input fingerprint so
     * downstream code can rely on identity information regardless of whether any metadata
     * row was found. (The serialization layer decides separately whether to expose {@code
     * subject} on the wire based on {@code TOKEN_REGISTRY_ENABLED}.)
     */
    private TokenRegistryCurrencyData mergeMetadata(AssetFingerprint fingerprint,
                                                    Map<String, TokenMetadataEntity> cip26,
                                                    Cip68Batch cip68) {
        String subject = fingerprint.toSubject();

        TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder = TokenRegistryCurrencyData.builder()
                .policyId(fingerprint.getPolicyId())
                .subject(subject)
                .decimals(0); // default; overridden below if CIP-26 or CIP-68 has an explicit value

        // CIP-26 base
        TokenMetadataEntity cip26Entity = cip26.get(subject);
        if (cip26Entity != null) {
            applyCip26(builder, cip26Entity);
        }

        // CIP-68 overrides (higher priority)
        MetadataReferenceNftEntity cip68Entity = cip68.findFor(fingerprint);
        if (cip68Entity != null) {
            applyCip68(builder, cip68Entity);
        }

        return builder.build();
    }

    /**
     * Converts a fungible token symbol (starting with prefix {@code 0014df10}) into its
     * corresponding CIP-68 reference NFT asset name (prefix {@code 000643b0}).
     * Returns {@code null} if the symbol is not a CIP-68 fungible token.
     */
    @Nullable
    private static String toCip68ReferenceNftAssetName(String symbol) {
        if (symbol.length() <= CIP68_FUNGIBLE_TOKEN_PREFIX.length()
                || !symbol.startsWith(CIP68_FUNGIBLE_TOKEN_PREFIX)) {
            return null;
        }

        return CIP68_REFERENCE_TOKEN_PREFIX + symbol.substring(CIP68_FUNGIBLE_TOKEN_PREFIX.length());
    }

    /**
     * Applies CIP-26 fields (and optionally logo) to the builder. Only overrides decimals if
     * the source has an explicit value — the 0 default seeded in {@link #mergeMetadata} stays
     * otherwise.
     */
    private void applyCip26(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            TokenMetadataEntity cip26) {
        builder.name(cip26.getName());
        builder.description(cip26.getDescription());
        Optional.ofNullable(cip26.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip26.getUrl()).ifPresent(builder::url);

        if (cip26.getDecimals() != null) {
            builder.decimals(Math.toIntExact(cip26.getDecimals()));
        }
        if (logoEnabled && cip26.getLogo() != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                    .value(cip26.getLogo())
                    .build());
        }
    }

    /**
     * Applies CIP-68 fields (and optionally logo) to the builder. Unlike CIP-26, CIP-68 logos
     * may be stored as URLs or base64 blobs — the format is detected from the value.
     */
    private void applyCip68(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            MetadataReferenceNftEntity cip68) {
        Optional.ofNullable(cip68.getName()).ifPresent(builder::name);
        Optional.ofNullable(cip68.getDescription()).ifPresent(builder::description);
        Optional.ofNullable(cip68.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip68.getUrl()).ifPresent(builder::url);
        Optional.ofNullable(cip68.getVersion()).ifPresent(v -> builder.version(BigDecimal.valueOf(v)));

        if (cip68.getDecimals() != null) {
            builder.decimals(Math.toIntExact(cip68.getDecimals()));
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

    /**
     * CIP-68 batch-fetch result. Holds the reference-NFT rows keyed by their concat key
     * ({@code policy_id || asset_name}) plus a reverse lookup from fingerprint to concat key
     * so callers can resolve entries in O(1) without re-parsing subject prefixes.
     */
    private record Cip68Batch(
            Map<String, MetadataReferenceNftEntity> entitiesByRefNftKey,
            Map<AssetFingerprint, String> refNftKeyByFingerprint) {

        @Nullable
        MetadataReferenceNftEntity findFor(AssetFingerprint fingerprint) {
            String key = refNftKeyByFingerprint.get(fingerprint);
            return key == null ? null : entitiesByRefNftKey.get(key);
        }
    }

}
