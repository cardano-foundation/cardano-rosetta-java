package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip68MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip26MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.Cip68MetadataRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.Cip26MetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    // CIP-68 asset name label prefixes (same as yaci-store assets-ext Cip68Constants)
    private static final String CIP68_REFERENCE_TOKEN_PREFIX = "000643b0";      // label 100: reference NFT holding the datum
    private static final String CIP68_FUNGIBLE_TOKEN_PREFIX = "0014df10";       // label 333: fungible token (FT)
    private static final String CIP68_RICH_FUNGIBLE_TOKEN_PREFIX = "001bc280";  // label 444: rich fungible token (RFT)

    /**
     * Highest decimal count CIP-26 and CIP-68 allow. yaci-store assets-ext stores tokens that
     * break their CIP (reported in {@code validation_errors}) instead of dropping them, so an
     * out-of-range value can reach us; it is treated as absent rather than served.
     */
    private static final long MAX_DECIMALS = 255;

    private final Cip26MetadataRepository cip26MetadataRepository;
    private final Cip68MetadataRepository cip68MetadataRepository;

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
     *   <li>1 for CIP-68 reference NFTs (only if the batch contains CIP-68 333 / 444 fungible tokens)</li>
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

        Map<String, Cip26MetadataEntity> cip26 = cip26MetadataRepository.findAllBySubjectIn(subjects).stream()
                .collect(Collectors.toMap(Cip26MetadataEntity::getSubject, Function.identity()));
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
     * Collects CIP-68 fungible-token candidates (333 and 444) from the input fingerprints, issues a single
     * window-function query against {@code cip68_metadata}, and returns both the result map
     * (keyed by reference-NFT concat key) and a reverse lookup from fingerprint to concat key
     * so the merge phase can resolve entries in O(1).
     */
    private Cip68Batch fetchCip68(Collection<AssetFingerprint> fingerprints) {
        Map<AssetFingerprint, String> refNftKeyByFingerprint = new HashMap<>();
        // a 333 and a 444 token of the same reference NFT share one key
        Set<String> cip68ConcatenatedKeys = new LinkedHashSet<>();

        for (AssetFingerprint fingerprint : fingerprints) {
            String refNftAssetName = toCip68ReferenceNftAssetName(fingerprint.getSymbol());
            if (refNftAssetName != null) {
                String concatenatedKey = fingerprint.getPolicyId() + refNftAssetName;
                cip68ConcatenatedKeys.add(concatenatedKey);
                refNftKeyByFingerprint.put(fingerprint, concatenatedKey);
            }
        }

        Map<String, Cip68MetadataEntity> entitiesByRefNftKey = cip68ConcatenatedKeys.isEmpty()
                ? Map.of()
                : cip68MetadataRepository.findLatestByConcatenatedKeys(cip68ConcatenatedKeys).stream()
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
                                                    Map<String, Cip26MetadataEntity> cip26,
                                                    Cip68Batch cip68) {
        String subject = fingerprint.toSubject();

        TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder = TokenRegistryCurrencyData.builder()
                .policyId(fingerprint.getPolicyId())
                .subject(subject)
                .decimals(0); // default; overridden below if CIP-26 or CIP-68 has an explicit value

        // CIP-26 base
        Cip26MetadataEntity cip26Entity = cip26.get(subject);
        if (cip26Entity != null) {
            applyCip26(builder, cip26Entity);
        }

        // CIP-68 overrides (higher priority)
        Cip68MetadataEntity cip68Entity = cip68.findFor(fingerprint);
        if (cip68Entity != null) {
            applyCip68(builder, cip68Entity, isRichFungibleToken(fingerprint.getSymbol()));
        }

        return builder.build();
    }

    /**
     * Converts a CIP-68 fungible token symbol, 333 (prefix {@code 0014df10}) or 444 (prefix
     * {@code 001bc280}), into the asset name of its reference NFT (prefix {@code 000643b0}), which
     * holds the metadata datum. Returns {@code null} for any other symbol: NFTs (222) and non-CIP-68
     * tokens are not served from CIP-68, as in yaci-store assets-ext.
     */
    @Nullable
    private static String toCip68ReferenceNftAssetName(String symbol) {
        if (symbol.length() <= CIP68_FUNGIBLE_TOKEN_PREFIX.length()
                || !(symbol.startsWith(CIP68_FUNGIBLE_TOKEN_PREFIX) || isRichFungibleToken(symbol))) {
            return null;
        }

        return CIP68_REFERENCE_TOKEN_PREFIX + symbol.substring(CIP68_FUNGIBLE_TOKEN_PREFIX.length());
    }

    private static boolean isRichFungibleToken(String symbol) {
        return symbol.startsWith(CIP68_RICH_FUNGIBLE_TOKEN_PREFIX);
    }

    /**
     * Applies CIP-26 fields (and optionally logo) to the builder. Only overrides decimals if
     * the source has an explicit value within the CIP range — the 0 default seeded in
     * {@link #mergeMetadata} stays otherwise.
     */
    private void applyCip26(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            Cip26MetadataEntity cip26) {
        builder.name(cip26.getName());
        builder.description(cip26.getDescription());
        Optional.ofNullable(cip26.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip26.getUrl()).ifPresent(builder::url);

        Optional.ofNullable(validDecimals(cip26.getDecimals(), cip26.getSubject())).ifPresent(builder::decimals);
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
     * <p>
     * A rich fungible token (444) has no {@code logo} in CIP-68: its picture is the {@code image},
     * which is served as the logo, as yaci-store assets-ext and cf-token-metadata-registry do. A
     * {@code logo} in a 444 datum is not part of CIP-68, so it is not served.
     */
    private void applyCip68(TokenRegistryCurrencyData.TokenRegistryCurrencyDataBuilder builder,
                            Cip68MetadataEntity cip68,
                            boolean richFungibleToken) {
        Optional.ofNullable(cip68.getName()).ifPresent(builder::name);
        Optional.ofNullable(cip68.getDescription()).ifPresent(builder::description);
        Optional.ofNullable(cip68.getTicker()).ifPresent(builder::ticker);
        Optional.ofNullable(cip68.getUrl()).ifPresent(builder::url);
        Optional.ofNullable(cip68.getVersion()).ifPresent(v -> builder.version(BigDecimal.valueOf(v)));

        Optional.ofNullable(validDecimals(cip68.getDecimals(), cip68.getPolicyId() + cip68.getAssetName()))
                .ifPresent(builder::decimals);

        String logo = richFungibleToken ? cip68.getImage() : cip68.getLogo();
        if (logoEnabled && logo != null) {
            builder.logo(TokenRegistryCurrencyData.LogoData.builder()
                    .format(detectLogoFormat(logo))
                    .value(logo)
                    .build());
        }
    }

    /**
     * Returns the decimal count if it is within the CIP range [0, {@value #MAX_DECIMALS}], or
     * {@code null} (treated as absent, so the next source or the 0 default applies) otherwise.
     */
    @Nullable
    private static Integer validDecimals(@Nullable Long decimals, String subject) {
        if (decimals == null) {
            return null;
        }
        if (decimals < 0 || decimals > MAX_DECIMALS) {
            log.debug("Ignoring out-of-range decimals {} for subject {}", decimals, subject);
            return null;
        }

        return decimals.intValue();
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
            Map<String, Cip68MetadataEntity> entitiesByRefNftKey,
            Map<AssetFingerprint, String> refNftKeyByFingerprint) {

        @Nullable
        Cip68MetadataEntity findFor(AssetFingerprint fingerprint) {
            String key = refNftKeyByFingerprint.get(fingerprint);
            return key == null ? null : entitiesByRefNftKey.get(key);
        }
    }

}
