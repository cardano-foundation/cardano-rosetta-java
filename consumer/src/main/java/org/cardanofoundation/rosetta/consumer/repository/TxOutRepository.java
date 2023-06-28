package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.projection.LatestTxOutProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TxOutRepository extends JpaRepository<TxOut, Long>, CustomTxOutRepository {

  @Query("SELECT "
      + "new org.cardanofoundation.rosetta.consumer.projection.LatestTxOutProjection("
      + "txOut.txId, txOut.address) FROM TxOut txOut "
      + "WHERE txOut.tx IN (:txs)")
  List<LatestTxOutProjection> findAllByTxIn(@Param("txs") Collection<Tx> txs);

  @Modifying
  void deleteAllByTxIn(Collection<Tx> txs);
}