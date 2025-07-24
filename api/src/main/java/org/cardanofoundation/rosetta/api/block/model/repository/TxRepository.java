package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Repository
public interface TxRepository extends JpaRepository<TxnEntity, Long> {

  List<TxnEntity> findTransactionsByBlockHash(@Param("blockHash") String blockHash);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        LEFT JOIN InvalidTransactionEntity invalidTx ON tx.txHash = invalidTx.txHash
        WHERE
        (:txHashes IS NULL OR tx.txHash IN :txHashes) AND
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) AND
        (:blockHash IS NULL OR tx.block.hash = :blockHash) AND
        (:blockNo IS NULL OR tx.block.number = :blockNo) AND
        (:isSuccess IS NULL OR
         (:isSuccess = TRUE AND invalidTx.txHash IS NULL) OR
         (:isSuccess = FALSE AND invalidTx.txHash IS NOT NULL))
        ORDER BY COALESCE(tx.block.blockTimeInSeconds, tx.block.slot) DESC
        """)
  Page<TxnEntity> searchTxnEntitiesAND(@Nullable @Param("txHashes") Set<String> txHashes,
                                       @Nullable @Param("blockHash") String blockHash,
                                       @Nullable @Param("blockNo") Long blockNumber,
                                       @Nullable @Param("maxBlock") Long maxBlock,
                                       @Nullable @Param("isSuccess") Boolean isSuccess,
                                       Pageable pageable);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        LEFT JOIN AddressUtxoEntity utxo ON tx.txHash = utxo.txHash
        LEFT JOIN InvalidTransactionEntity invalidTx ON tx.txHash = invalidTx.txHash
        WHERE
        ((:txHashes IS NULL OR tx.txHash IN :txHashes) OR
         (:maxBlock IS NULL OR tx.block.number <= :maxBlock) OR
         (:blockHash IS NULL OR tx.block.hash = :blockHash) OR
         (:blockNo IS NULL OR tx.block.number = :blockNo)) AND
        (:isSuccess IS NULL OR
         (:isSuccess = TRUE AND invalidTx.txHash IS NULL) OR
         (:isSuccess = FALSE AND invalidTx.txHash IS NOT NULL))
        ORDER BY COALESCE(tx.block.blockTimeInSeconds, tx.block.slot) DESC
        """)
  Page<TxnEntity> searchTxnEntitiesOR(@Nullable @Param("txHashes") Set<String> txHashes,
                                      @Nullable @Param("blockHash") String blockHash,
                                      @Nullable @Param("blockNo") Long blockNumber,
                                      @Nullable @Param("maxBlock") Long maxBlock,
                                      @Nullable @Param("isSuccess") Boolean isSuccess,
                                      Pageable pageable);

}
