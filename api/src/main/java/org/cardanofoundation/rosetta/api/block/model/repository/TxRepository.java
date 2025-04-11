package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.Set;
import javax.annotation.Nullable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

@Repository
public interface TxRepository extends JpaRepository<TxnEntity, Long> {

  Slice<TxnEntity> findTransactionsByBlockHash(@Param("blockHash") String blockHash);

  @Query(value = """
        SELECT DISTINCT tx FROM TxnEntity tx
        WHERE
        (:txHashes IS NULL OR tx.txHash IN :txHashes) AND
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) AND
        (:blockHash IS NULL OR tx.block.hash = :blockHash) AND
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  Slice<TxnEntity> searchTxnEntitiesAND(@Nullable @Param("txHashes") Set<String> txHashes,
                                        @Nullable @Param("blockHash") String blockHash,
                                        @Nullable @Param("blockNo") Long blockNumber,
                                        @Nullable @Param("maxBlock") Long maxBlock,
                                        Pageable pageable);

  @Query(value = """
        SELECT DISTINCT tx FROM TxnEntity tx
        LEFT JOIN AddressUtxoEntity utxo ON tx.txHash = utxo.txHash
        WHERE
        (:txHashes IS NULL OR tx.txHash IN :txHashes) OR
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) OR
        (:blockHash IS NULL OR tx.block.hash = :blockHash) OR
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  Slice<TxnEntity> searchTxnEntitiesOR(@Nullable @Param("txHashes") Set<String> txHashes,
                                      @Nullable @Param("blockHash") String blockHash,
                                      @Nullable @Param("blockNo") Long blockNumber,
                                      @Nullable @Param("maxBlock") Long maxBlock,
                                      Pageable pageable);

}
