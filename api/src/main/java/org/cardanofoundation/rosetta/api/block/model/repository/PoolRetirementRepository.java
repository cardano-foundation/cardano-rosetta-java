package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementId;

public interface PoolRetirementRepository extends
    JpaRepository<PoolRetirementEntity, PoolRetirementId> {

  List<PoolRetirementEntity> findByTxHash(String txHash);
}
