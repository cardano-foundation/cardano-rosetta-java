package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MetadataReferenceNftRepository extends JpaRepository<MetadataReferenceNftEntity, MetadataReferenceNftId> {

    /**
     * Returns the latest reference NFT row per {@code (policy_id, asset_name)} pair, filtered by label.
     * Replaces the N+1 per-subject query pattern with a single round-trip.
     * <p>
     * Callers pass a list of {@code policy_id || asset_name} concatenated keys. Since policy IDs are
     * always exactly 56 hex characters, the concatenation is unambiguous.
     * <p>
     * Uses {@code ROW_NUMBER() OVER} window function with a label filter for per-pair dedup.
     * Portable across PostgreSQL, H2, and MySQL 8+.
     * <p>
     * Mirrors the upstream {@code yaci-store-assets-ext} query of the same name — the two
     * implementations stay in sync while this repository duplicates the upstream entities
     * (see #731 for the planned consolidation).
     *
     * @param concatenatedKeys list of {@code policy_id || asset_name} keys (reference-NFT asset names)
     * @param label            CIP-68 label to filter by (e.g. 333 for fungible tokens)
     * @return one entity per matching concatenated key; pairs with no data are omitted
     */
    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY policy_id, asset_name " +
            "ORDER BY slot DESC) AS rn FROM metadata_reference_nft WHERE label = :label " +
            "AND CONCAT(policy_id, asset_name) IN (:concatenatedKeys)) ranked WHERE rn = 1",
            nativeQuery = true)
    List<MetadataReferenceNftEntity> findLatestByConcatenatedKeys(
            @Param("concatenatedKeys") Collection<String> concatenatedKeys,
            @Param("label") int label);
}
