package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;
import org.cardanofoundation.rosetta.api.block.model.entity.TxInputEntity;

@Repository
public interface TxInputRepository extends JpaRepository<TxInputEntity, UtxoId> {

  @Query(value = """
        SELECT spentTxHash FROM TxInputEntity
        WHERE txHash = :txHash AND outputIndex = :outputIndex
    """)
  List<String> findSpentTxHashByUtxoKey(@Param("txHash") String txHash, @Param("outputIndex") Integer outputIndex);

}
