package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationId;

@Repository
public interface DelegationRepository extends JpaRepository<DelegationEntity, DelegationId> {

  List<DelegationEntity> findByTxHashIn(List<String> txHashes);

}
