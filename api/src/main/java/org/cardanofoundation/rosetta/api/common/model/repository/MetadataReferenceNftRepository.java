package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetadataReferenceNftRepository
        extends JpaRepository<MetadataReferenceNftEntity, MetadataReferenceNftId>,
                MetadataReferenceNftRepositoryCustom {
}
