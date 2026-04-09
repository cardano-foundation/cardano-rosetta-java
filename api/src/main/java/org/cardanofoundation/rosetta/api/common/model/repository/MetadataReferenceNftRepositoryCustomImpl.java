package org.cardanofoundation.rosetta.api.common.model.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of {@link MetadataReferenceNftRepositoryCustom}.
 * Uses a native SQL window-function query to fetch the latest row per
 * {@code (policy_id, asset_name)} pair in a single round-trip, replacing
 * the previous N+1 per-subject pattern.
 */
@SuppressWarnings("unchecked")
public class MetadataReferenceNftRepositoryCustomImpl implements MetadataReferenceNftRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MetadataReferenceNftEntity> findLatestByPolicyAssetPairs(List<PolicyAssetPair> pairs, int label) {
        if (pairs.isEmpty()) {
            return List.of();
        }

        // Generate placeholders like "(?2, ?3), (?4, ?5), ..." — ?1 is reserved for label.
        String tupleList = IntStream.range(0, pairs.size())
                .mapToObj(i -> "(?" + (i * 2 + 2) + ", ?" + (i * 2 + 3) + ")")
                .collect(Collectors.joining(", "));

        String sql = """
                SELECT * FROM (
                    SELECT *, ROW_NUMBER() OVER (PARTITION BY policy_id, asset_name ORDER BY slot DESC) AS rn
                    FROM metadata_reference_nft
                    WHERE label = ?1
                      AND (policy_id, asset_name) IN (%s)
                ) ranked WHERE ranked.rn = 1
                """.formatted(tupleList);

        Query query = entityManager.createNativeQuery(sql, MetadataReferenceNftEntity.class);
        query.setParameter(1, label);
        int position = 2;
        for (PolicyAssetPair pair : pairs) {
            query.setParameter(position++, pair.policyId());
            query.setParameter(position++, pair.assetName());
        }

        return query.getResultList();
    }
}
