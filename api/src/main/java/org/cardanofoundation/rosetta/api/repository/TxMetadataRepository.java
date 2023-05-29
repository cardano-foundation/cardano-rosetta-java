package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.api.repository.customrepository.CustomTxMetadataRepository;
import org.cardanofoundation.rosetta.common.entity.TxMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxMetadataRepository extends JpaRepository<TxMetadata, Long>,
    CustomTxMetadataRepository {
}