package org.cardanofoundation.rosetta.api.block.model.repository;

import com.bloxbean.cardano.yaci.store.governance.storage.impl.model.DelegationVoteEntity;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.model.DelegationVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrepVoteDelegationRepository extends JpaRepository<DelegationVoteEntity, DelegationVoteId> {

  List<DelegationVoteEntity> findByTxHashIn(List<String> txHashes);

}
