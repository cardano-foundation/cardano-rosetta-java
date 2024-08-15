package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

public interface TxRepository extends JpaRepository<TxnEntity, Long> {

  List<TxnEntity> findTransactionsByBlockHash(@Param("blockHash") String blockHash);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        WHERE
        (:txHashes IS NULL OR tx.txHash IN :txHashes) AND
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) AND
        (:blockHash IS NULL OR tx.block.hash = :blockHash) AND
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  List<TxnEntity> searchTxnEntitiesAND(@Param("txHashes") Set<String> txHashes, @Param("blockHash") String blockHash, @Param("blockNo") Long blockNumber, @Param("maxBlock") Long maxBlock, Pageable pageable);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        LEFT JOIN AddressUtxoEntity utxo ON tx.txHash = utxo.txHash
        WHERE
        (:txHashes IS NULL OR tx.txHash IN :txHashes) OR
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) OR
        (:blockHash IS NULL OR tx.block.hash = :blockHash) OR
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  List<TxnEntity> searchTxnEntitiesOR(@Param("txHashes") Set<String> txHashes, @Param("blockHash") String blockHash, @Param("blockNo") Long blockNumber, @Param("maxBlock") Long maxBlock, Pageable pageable);

}
