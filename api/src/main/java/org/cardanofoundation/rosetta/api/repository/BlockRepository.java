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
            "SELECT  b.hash as hash, " +
                    "b.blockNo as index  " +
                    "FROM Block b " +
                    "WHERE b.previous.id IS NULL",
            countQuery = "SELECT count(hash) FROM Block WHERE previous IS NULL")
    Page<GenesisBlockProjection> findGenesisBlock(Pageable pageable);
    @Query("SELECT " +
            "b.hash AS hash, " +
            "b.blockNo AS number, " +
            "b.time AS createdAt, " +
            "CASE " +
            "   WHEN b2.blockNo IS NOT NULL THEN b2.blockNo " +
            "   WHEN b3.blockNo IS NOT NULL THEN b3.blockNo " +
            "ELSE 0 " +
            "END AS previousBlockNumber, " +
            "CASE " +
            "   WHEN b2.blockNo IS NOT NULL THEN b2.hash " +
            "   WHEN b3.blockNo IS NOT NULL THEN b3.hash " +
            "   WHEN b.blockNo = 1 THEN b3.hash " + // block 1
            "ELSE b.hash " + // genesis
            "END AS previousBlockHash, " +
            "b.txCount AS transactionsCount, " +
            "s.description AS createdBy, " +
            "b.size AS size, " +
            "b.epochNo AS epochNo, " +
            "b.slotNo AS slotNo " +
            "FROM " +
            "Block b " +
            "LEFT JOIN SlotLeader s ON b.slotLeaderId = s.id " +
            "LEFT JOIN Block b2 ON b.previous.id = b2.id " +
            "LEFT JOIN Block b3 ON b2.previous.id = b3.id " +
            "WHERE (:blockNumber IS NULL OR b.blockNo = :blockNumber)  " +
            "AND (:blockHash IS NULL OR b.hash = :blockHash)")
    List<BlockProjection> findBlock(@Param("blockNumber") Long blockNumber,
                                    @Param("blockHash")String blockHash);
    @Query("SELECT blockNo  FROM Block  " +
            "WHERE blockNo IS NOT NULL ORDER BY blockNo DESC")
    Page<Long> findLatestBlockNumber(Pageable pageable);

  @Query("SELECT tx.hash as hash, "
      + "tx.fee as fee, "
      + "tx.size as size, "
      + "tx.validContract as validContract, "
      + "tx.scriptSize as scriptSize, "
      + "block.hash as blockHash "
      + "FROM Tx tx JOIN Block block on block.id = tx.block.id "
      + "WHERE tx.hash = :hash "
      + "AND (block.blockNo = :blockNumber OR (block.blockNo is null AND :blockNumber = 0)) "
      + "AND block.hash = :blockHash")
  List<FindTransactionProjection> findTransactionByHashAndBlock(
      @Param("hash") String hash,
      @Param("blockNumber") Long blockNumber,
      @Param("blockHash") String blockHash);

}