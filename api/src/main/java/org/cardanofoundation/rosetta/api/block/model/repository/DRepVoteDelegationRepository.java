package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.DrepDelegationVoteEntity;

@Repository
public interface DRepVoteDelegationRepository extends JpaRepository<DrepDelegationVoteEntity, DrepDelegationVoteEntity>  {

    List<DrepDelegationVoteEntity> findByTxHashIn(List<String> txHashes);

}
