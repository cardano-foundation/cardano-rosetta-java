package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DelegationId;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolDelegationEntity;

@Repository
public interface PoolDelegationRepository extends JpaRepository<PoolDelegationEntity, DelegationId> {

  List<PoolDelegationEntity> findByTxHashIn(List<String> txHashes);

}
