package org.cardanofoundation.rosetta.api.common.model.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.common.model.entity.Cip68MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip68MetadataId;

/**
 * Read-only access to yaci-store assets-ext {@code cip68_metadata}.
 */
@Repository
public interface Cip68MetadataRepository extends JpaRepository<Cip68MetadataEntity, Cip68MetadataId> {

    /**
     * Returns the latest row per {@code (policy_id, asset_name)} pair, regardless of label, in a
     * single round-trip. Same query as yaci-store assets-ext
     * {@code Cip68MetadataRepository#findLatestByConcatenatedKeys}: latest by {@code slot}, then
     * {@code tx_index} when a reference NFT is updated more than once in the same block.
     * <p>
     * Callers pass {@code policy_id || asset_name} keys. Policy IDs are always 56 hex characters,
     * so the concatenation is unambiguous.
     * <p>
     * No label filter: a single reference NFT may have rows tagged with different labels across
     * its history (the per-row label reflects whichever user-token prefix was co-minted in that
     * transaction), so filtering by label could return stale metadata.
     *
     * @param concatenatedKeys {@code policy_id || asset_name} keys (reference-NFT asset names)
     * @return one entity per matching key; keys with no data are omitted
     */
    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY policy_id, asset_name " +
            "ORDER BY slot DESC, tx_index DESC) AS rn FROM cip68_metadata " +
            "WHERE CONCAT(policy_id, asset_name) IN (:concatenatedKeys)) ranked WHERE rn = 1",
            nativeQuery = true)
    List<Cip68MetadataEntity> findLatestByConcatenatedKeys(
            @Param("concatenatedKeys") Collection<String> concatenatedKeys);

}
