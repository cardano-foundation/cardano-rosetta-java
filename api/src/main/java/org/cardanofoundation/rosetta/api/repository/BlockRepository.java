package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.BlockProjection;
import org.cardanofoundation.rosetta.api.projection.FindTransactionProjection;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockProjection;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Tx;
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

  @Query("SELECT tx "
      + "FROM Tx tx "
      + "WHERE tx.hash = :hash "
      + "AND tx.block.hash = :blockHash")
  List<Tx> findTransactionByHashAndBlock(
      @Param("hash") String hash,
      @Param("blockHash") String blockHash);

}