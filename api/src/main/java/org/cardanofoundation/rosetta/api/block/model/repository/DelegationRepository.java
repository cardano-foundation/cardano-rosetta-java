package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DelegationRepository extends JpaRepository<DelegationEntity, DelegationId> {

    List<DelegationEntity> findByTxHash(String txHash);
}
