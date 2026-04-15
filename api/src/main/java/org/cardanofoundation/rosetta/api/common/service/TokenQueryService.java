package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;

import java.util.Collection;
import java.util.Map;

/**
 * Read-only access to merged token metadata from the yaci-store assets-ext tables
 * ({@code ft_offchain_metadata}, {@code ft_offchain_logo}, {@code metadata_reference_nft}).
 * <p>
 * Implementations are responsible for:
 * <ul>
 *   <li>Reading CIP-26 (offchain token registry) and CIP-68 (on-chain reference NFT) rows
 *       in a constant number of DB round-trips per batch</li>
 *   <li>Merging values with CIP-68 taking priority over CIP-26 (first non-null wins)</li>
 *   <li>Handling the CIP-68 fungible → reference NFT prefix conversion
 *       ({@code 0014df10} → {@code 000643b0}) transparently</li>
 * </ul>
 * The returned map always contains an entry for every input fingerprint; tokens with no
 * metadata get a fallback entry populated only with {@code policyId} — all other fields,
 * including {@code subject} and {@code decimals}, are {@code null}. Callers that need a
 * numeric decimal value must apply their own default.
 * <p>
 * Gated by {@code cardano.rosetta.TOKEN_REGISTRY_ENABLED} (default {@code false}). When the
 * flag is off, implementations MUST return the same fallback shape for every fingerprint
 * without hitting the database.
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
