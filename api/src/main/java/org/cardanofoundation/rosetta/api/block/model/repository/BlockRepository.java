package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;


public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

  @Query("SELECT NEW org.cardanofoundation.rosetta.api.block.model.entity."
      + "BlockEntity(b.hash, b.number, b.blockTimeInSeconds) FROM BlockEntity b "
      + "WHERE b.prev.hash IS NULL ORDER BY b.number ASC LIMIT 1")
  Optional<BlockEntity> findGenesisBlockHashAndNumber();

  Optional<BlockEntity> findByNumber(Long blockNumber);

  Optional<BlockEntity> findByHash(String blockHash);

  Optional<BlockEntity> findByNumberAndHash(Long blockNumber, String blockHash);

  @Query("FROM BlockEntity b ORDER BY b.number DESC LIMIT 1")
  Optional<BlockEntity> findLatestBlock();

  @Query("SELECT NEW org.cardanofoundation.rosetta.api.block.model.entity. "
      + "BlockEntity(b.hash, b.number, b.blockTimeInSeconds) FROM BlockEntity b "
      + "ORDER BY b.number DESC LIMIT 1")
  Optional<BlockEntity> findLatestBlockHashAndNumber();

}
