package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TxOutRepository extends JpaRepository<TxOut, Long>, CustomTxOutRepository {

  @Modifying
  void deleteAllByTxIn(Collection<Tx> txs);
}