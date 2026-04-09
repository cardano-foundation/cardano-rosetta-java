package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;

import java.util.List;

/**
 * Custom fragment of {@link MetadataReferenceNftRepository} exposing a batched lookup
 * that replaces the N+1 per-subject query pattern with a single window-function query.
 */
public interface MetadataReferenceNftRepositoryCustom {

    /**
     * Fetches the latest (highest-slot) CIP-68 reference NFT row for each given
     * {@code (policyId, assetName)} pair, filtered by label.
     * <p>
     * Issues a single native SQL query using {@code ROW_NUMBER() OVER (PARTITION BY ...)}
     * and a row-value IN expression. Portable across H2 2.2.x and PostgreSQL 12+.
     *
     * @param pairs policy/asset-name pairs to look up (reference-NFT asset names, already
     *              prefix-converted from fungible token prefix {@code 0014df10} to
     *              reference NFT prefix {@code 000643b0})
     * @param label CIP-68 label (e.g. 333 for fungible tokens)
     * @return one entity per pair that has at least one matching row; pairs with no data are omitted
     */
    List<MetadataReferenceNftEntity> findLatestByPolicyAssetPairs(List<PolicyAssetPair> pairs, int label);

    /**
     * Lightweight pair used as query input. Kept nested to keep the public type
     * surface of the repository package small.
     */
    record PolicyAssetPair(String policyId, String assetName) {
    }
}
