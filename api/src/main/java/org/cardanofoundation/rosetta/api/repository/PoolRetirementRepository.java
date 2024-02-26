package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.common.model.PoolRetirementEntity;
import org.cardanofoundation.rosetta.common.model.PoolRetirementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoolRetirementRepository extends JpaRepository<PoolRetirementEntity, PoolRetirementId> {

    List<PoolRetirementEntity> findByTxHash(String txHash);
}
