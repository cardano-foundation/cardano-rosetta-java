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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
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
     * Issues a constant number of DB queries regardless of batch size:
     * <ul>
     *   <li>1 for CIP-26 metadata (always)</li>
     *   <li>1 for CIP-26 logos (only if {@code TOKEN_REGISTRY_LOGO_FETCH=true})</li>
     *   <li>1 for CIP-68 reference NFTs (only if the batch contains CIP-68 fungible tokens)</li>
     * </ul>
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

        // Fork CIP-26 metadata and logo queries in parallel via structured concurrency.
        //
        // IMPORTANT — known caveat: Spring's @Transactional context is stored in a ThreadLocal
        // (TransactionSynchronizationManager). When we fork to virtual threads inside a
        // StructuredTaskScope, that thread-local state is NOT propagated. Each repository call
        // executed inside a fork therefore opens its OWN transaction and consumes its OWN
        // connection-pool slot — the class-level @Transactional(readOnly = true) on this service
        // does not apply to the forked work.
        //
        // Why we accept this: both queries are read-only point lookups against indexed columns,
        // and the underlying CIP-26 data (ft_offchain_metadata / ft_offchain_logo) is written
        // only during periodic offchain sync from the Cardano token registry git repo — i.e.
        // very rarely. The theoretical risk of read skew (metadata query sees state at time T,
        // logo query sees state at T+1 after a concurrent sync flushes rows) is negligible for
        // token metadata: a logo appearing or disappearing between two microsecond-apart queries
        // is harmless, and any such inconsistency self-heals on the next API call.
        //
        // When to revisit: if the connection pool is saturated, or if a future change introduces
        // frequent writes to these tables, drop the scope and chain the calls sequentially on the
        // transaction thread — a ~5-line diff that restores the outer readOnly transaction at the
        // cost of serializing the two queries.
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

        // Batch CIP-68 lookup: collect all fungible token candidates as concatenated
        // (policy_id || reference-NFT asset name) keys in one pass, then fire a single
        // window-function query against metadata_reference_nft. Policy IDs are fixed
        // 56-char hex so the concatenation is unambiguous. Mirrors the upstream
        // yaci-store-assets-ext findLatestByConcatenatedKeys query shape.
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

        Map<String, MetadataReferenceNftEntity> cip68Map = cip68ConcatenatedKeys.isEmpty()
                ? Map.of()
                : metadataReferenceNftRepository.findLatestByConcatenatedKeys(cip68ConcatenatedKeys, CIP68_LABEL_FT).stream()
                        .collect(Collectors.toMap(
                                e -> e.getPolicyId() + e.getAssetName(),
                                Function.identity()));

        // Merge CIP-26 base + CIP-68 overrides per fingerprint
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

            // CIP-68 overrides (higher priority) — O(1) lookup in the prefetched map
            String refNftKey = refNftKeyByFingerprint.get(fingerprint);
            if (refNftKey != null) {
                MetadataReferenceNftEntity cip68 = cip68Map.get(refNftKey);
                if (cip68 != null) {
                    applyCip68(builder, cip68);
                }
            }

            result.put(fingerprint, builder.build());
        }
        return result;
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
