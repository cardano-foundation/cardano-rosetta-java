package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.VoterType;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying voting procedures from the voting_procedure table.
 */
@Repository
public interface VotingProcedureRepository extends JpaRepository<VotingProcedureEntity, VotingProcedureId> {

  /**
   * Find all SPO voting procedures for transactions with the given hashes.
   *
   * @param txHashes list of transaction hashes to search for
   * @param voterType the type of voter to filter by
   * @return list of SPO voting procedures found in those transactions
   */
  List<VotingProcedureEntity> findByTxHashInAndVoterType(List<String> txHashes, VoterType voterType);

  /**
   * Find all SPO voting procedures for a specific transaction hash.
   *
   * @param txHash transaction hash to search for
   * @param voterType the type of voter to filter by
   * @return list of SPO voting procedures in that transaction
   */
  List<VotingProcedureEntity> findByTxHashAndVoterType(String txHash, VoterType voterType);

}
