package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetadataReferenceNftRepository extends JpaRepository<MetadataReferenceNftEntity, MetadataReferenceNftId> {

    /**
     * Find the latest (highest slot) CIP-68 reference NFT metadata for a given policy, asset name and label.
     * Label 333 = fungible token.
     */
    Optional<MetadataReferenceNftEntity> findFirstByPolicyIdAndAssetNameAndLabelOrderBySlotDesc(
            String policyId, String assetName, int label);
}
