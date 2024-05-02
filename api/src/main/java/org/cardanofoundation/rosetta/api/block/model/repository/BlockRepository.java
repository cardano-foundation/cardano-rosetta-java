package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;


public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

  @Query(value = "SELECT  b FROM BlockEntity b WHERE b.prev.hash IS NULL")
  Optional<BlockEntity> findGenesisBlock();

  Optional<BlockEntity> findByNumber(Long blockNumber);

  Optional<BlockEntity> findByHash(String blockHash);

  Optional<BlockEntity> findByNumberAndHash(Long blockNumber, String blockHash);

  @Query("FROM BlockEntity b ORDER BY b.number DESC LIMIT 1")
  BlockEntity findLatestBlock();
}
