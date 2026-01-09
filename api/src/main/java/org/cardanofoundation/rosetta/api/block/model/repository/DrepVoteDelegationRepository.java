package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DrepVoteDelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.DrepVoteDelegationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrepVoteDelegationRepository extends JpaRepository<DrepVoteDelegationEntity, DrepVoteDelegationId> {

  List<DrepVoteDelegationEntity> findByTxHashIn(List<String> txHashes);

}
