package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;


public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

  @Query(value =
      "SELECT  b " +
          "FROM BlockEntity b " +
          "WHERE b.prev.hash IS NULL")
  List<BlockEntity> findGenesisBlock();

  Optional<BlockEntity> findByNumber(Long blockNumber);

  Optional<BlockEntity> findByHash(String blockHash);

  Optional<BlockEntity> findByNumberAndHash(Long blockNumber, String blockHash);

  @Query("SELECT number FROM BlockEntity  " +
      "ORDER BY number DESC LIMIT 1")
  Long findLatestBlockNumber();

  @Query("SELECT tx "
      + "FROM TxnEntity tx "
      + "WHERE tx.txHash = :hash "
      + "AND tx.block.hash = :blockHash")
  List<TxnEntity> findTransactionByHashAndBlock(
      @Param("hash") String hash,
      @Param("blockHash") String blockHash);

}