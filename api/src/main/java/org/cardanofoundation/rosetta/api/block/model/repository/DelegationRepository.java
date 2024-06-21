package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationId;

public interface DelegationRepository extends JpaRepository<DelegationEntity, DelegationId> {

  List<DelegationEntity> findByTxHashIn(List<String> txHashes);
}
