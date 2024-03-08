package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.api.model.entity.PoolRetirementEntity;
import org.cardanofoundation.rosetta.api.model.entity.PoolRetirementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoolRetirementRepository extends JpaRepository<PoolRetirementEntity, PoolRetirementId> {

    List<PoolRetirementEntity> findByTxHash(String txHash);
}
