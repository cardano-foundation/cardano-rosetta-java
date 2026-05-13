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
     * Returns the latest reference NFT row per {@code (policy_id, asset_name)} pair, regardless of label.
     * Replaces the N+1 per-subject query pattern with a single round-trip.
     * <p>
     * Callers pass a list of {@code policy_id || asset_name} concatenated keys. Since policy IDs are
     * always exactly 56 hex characters, the concatenation is unambiguous.
     * <p>
     * Uses {@code ROW_NUMBER() OVER} window function for per-pair dedup.
     * Portable across PostgreSQL, H2, and MySQL 8+.
     * <p>
     * No label filter: a single reference NFT may have rows tagged with different labels across
     * its history (the per-row label reflects whichever user-token prefix was co-minted in that
     * transaction). Filtering by label here would silently return stale metadata when an FT's
     * newest update was co-minted alongside a 222 NFT. Matches upstream yaci-store
     * {@code Cip68MetadataRepository#findLatestByConcatenatedKeys}.
     *
     * @param concatenatedKeys list of {@code policy_id || asset_name} keys (reference-NFT asset names)
     * @return one entity per matching concatenated key; pairs with no data are omitted
     */
    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY policy_id, asset_name " +
            "ORDER BY slot DESC) AS rn FROM cip68_metadata " +
            "WHERE CONCAT(policy_id, asset_name) IN (:concatenatedKeys)) ranked WHERE rn = 1",
            nativeQuery = true)
    List<MetadataReferenceNftEntity> findLatestByConcatenatedKeys(
            @Param("concatenatedKeys") Collection<String> concatenatedKeys);
}
