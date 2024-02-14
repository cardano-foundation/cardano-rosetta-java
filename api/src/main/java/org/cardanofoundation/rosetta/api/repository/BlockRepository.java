package org.cardanofoundation.rosetta.api.repository;

import java.util.List;

import org.cardanofoundation.rosetta.common.model.BlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BlockRepository extends JpaRepository<BlockEntity, Long> {
    @Query(value =
            "SELECT  b " +
            "FROM BlockEntity b " +
            "WHERE b.prev.hash IS NULL")
    List<BlockEntity> findGenesisBlock();
    List<BlockEntity> findByNumber(Long blockNumber);
    List<BlockEntity> findByHash(String blockHash);
    List<BlockEntity> findByNumberAndHash(Long blockNumber, String blockHash);

    @Query("SELECT number FROM BlockEntity  " +
            "ORDER BY number DESC LIMIT 1")
    Long findLatestBlockNumber();

//  @Query("SELECT tx "
//      + "FROM Tx tx "
//      + "WHERE tx.hash = :hash "
//      + "AND tx.block.hash = :blockHash")
//  List<Tx> findTransactionByHashAndBlock(
//      @Param("hash") String hash,
//      @Param("blockHash") String blockHash);

}