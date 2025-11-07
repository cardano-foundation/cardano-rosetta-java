package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DrepVoteDelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.DrepVoteDelegationId;

@Repository
public interface DrepVoteDelegationRepository extends JpaRepository<DrepVoteDelegationEntity, DrepVoteDelegationId> {

  List<DrepVoteDelegationEntity> findByTxHashInAndCertIndex(List<String> txHashes, long certIndex);

}
