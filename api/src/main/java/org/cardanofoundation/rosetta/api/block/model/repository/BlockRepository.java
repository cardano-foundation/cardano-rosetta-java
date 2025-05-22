package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.projection.BlockIdentifierProjection;

@Repository
public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

  @Query("FROM BlockEntity b WHERE b.number = 0 ORDER BY b.number ASC LIMIT 1")
  Optional<BlockIdentifierProjection> findGenesisBlockIdentifier();

  @Query("FROM BlockEntity b WHERE b.number = :blockNumber ORDER BY b.number ASC LIMIT 1")
  Optional<BlockIdentifierProjection> findBlockProjectionByNumber(@Param("blockNumber") long blockNumber);

  Optional<BlockEntity> findByNumber(Long blockNumber);

  Optional<BlockEntity> findByHash(String blockHash);

  Optional<BlockEntity> findByNumberAndHash(Long blockNumber, String blockHash);

  @Query("FROM BlockEntity b ORDER BY b.number DESC LIMIT 1")
  Optional<BlockEntity> findLatestBlock();

  @Query("FROM BlockEntity b ORDER BY b.number DESC LIMIT 1")
  Optional<BlockIdentifierProjection> findLatestBlockIdentifier();

  Optional<BlockIdentifierProjection> findBlockIdentifierByNumber(Long blockNumber);

  Optional<BlockIdentifierProjection> findBlockIdentifierByHash(String blockHash);

  Optional<BlockIdentifierProjection> findBlockIdentifierByNumberAndHash(
          Long blockNumber,
          String blockHash);
}
