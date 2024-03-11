package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoolRetirementRepository extends JpaRepository<PoolRetirementEntity, PoolRetirementId> {

    List<PoolRetirementEntity> findByTxHash(String txHash);
}
