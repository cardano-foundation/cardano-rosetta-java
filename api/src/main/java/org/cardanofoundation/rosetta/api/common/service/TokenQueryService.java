package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;

import java.util.Collection;
import java.util.Map;

/**
 * Read-only access to merged token metadata from the yaci-store assets-ext tables
 * ({@code cip26_metadata} and {@code cip68_metadata}).
 * <p>
 * This service runs unconditionally — it is <em>not</em> gated by {@code TOKEN_REGISTRY_ENABLED}.
 * Its primary job on every request is to resolve the correct {@code decimals} for each
 * fingerprint, because {@code Currency.decimals} is a mandatory cross-chain field in
 * Rosetta and must always be accurate. Whether the remaining enrichment fields
 * ({@code subject}, {@code name}, {@code description}, {@code ticker}, {@code url},
 * {@code logo}, {@code version}) are exposed on the wire is a separate concern handled
 * downstream in the serialization layer.
 * <p>
 * Implementations are responsible for:
 * <ul>
 *   <li>Reading CIP-26 (offchain token registry) and CIP-68 (on-chain reference NFT) rows
 *       in a constant number of DB round-trips per batch</li>
 *   <li>Merging values with CIP-68 taking priority over CIP-26 (first non-null wins)</li>
 *   <li>Handling the CIP-68 fungible → reference NFT prefix conversion
 *       ({@code 0014df10} → {@code 000643b0}) transparently</li>
 *   <li>Guaranteeing that {@link TokenRegistryCurrencyData#getDecimals()} is non-null for
 *       every returned entry — defaulting to {@code 0} when neither standard provides an
 *       explicit value — so downstream mappers can treat the field as a guaranteed
 *       {@code int}</li>
 * </ul>
 * The returned map always contains an entry for every input fingerprint; tokens with no
 * metadata still get an entry with {@code policyId}, {@code subject}, and {@code decimals = 0}.
 */
public interface TokenQueryService {

    /**
     * Batch query merged token metadata for multiple asset fingerprints.
     *
     * @param fingerprints asset fingerprints (policyId + symbol hex)
     * @return map of asset fingerprint → merged currency data; never null, never contains null values
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> queryMetadataBatch(Collection<AssetFingerprint> fingerprints);

}
