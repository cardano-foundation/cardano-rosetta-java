package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.BlockProjection;
import org.cardanofoundation.rosetta.api.projection.FindTransactionProjection;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockProjection;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BlockRepository extends JpaRepository<Block, Long> {
    @Query(value =
            "SELECT  b " +
            "FROM Block b " +
            "WHERE b.previous.hash IS NULL")
    List<Block> findGenesisBlock();
    List<Block> findByNumber(Long blockNumber);
    List<Block> findByHash(String blockHash);
    List<Block> findByNumberAndHash(Long blockNumber, String blockHash);

    @Query("SELECT number FROM Block  " +
            "ORDER BY number DESC LIMIT 1")
    Long findLatestBlockNumber();

  @Query("SELECT tx.hash as hash, "
      + "tx.fee as fee, "
      + "tx.size as size, "
      + "tx.validContract as validContract, "
      + "tx.scriptSize as scriptSize, "
      + "block.hash as blockHash "
      + "FROM Tx tx JOIN Block block on block.id = tx.block.id "
      + "WHERE tx.hash = :hash "
      + "AND (block.number = :blockNumber OR (block.number is null AND :blockNumber = 0)) "
      + "AND block.hash = :blockHash")
  List<FindTransactionProjection> findTransactionByHashAndBlock(
      @Param("hash") String hash,
      @Param("blockNumber") Long blockNumber,
      @Param("blockHash") String blockHash);

}