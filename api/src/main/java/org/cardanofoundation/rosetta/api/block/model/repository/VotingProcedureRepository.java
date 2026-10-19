package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.VoterType;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureId;

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
