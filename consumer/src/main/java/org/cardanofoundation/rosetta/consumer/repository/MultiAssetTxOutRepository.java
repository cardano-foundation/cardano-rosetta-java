package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.projection.MaTxOutProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MultiAssetTxOutRepository extends JpaRepository<MaTxOut, Long> {

  @Query(
      "SELECT new org.cardanofoundation.rosetta.consumer.projection.MaTxOutProjection("
          + "mto.ident.fingerprint, "
          + "mto.txOutId, "
          + "mto.quantity) "
          + "FROM MaTxOut mto "
          + "WHERE mto.txOutId IN (:txOutIds)")
  List<MaTxOutProjection> findAllByTxOutIdsIn(@Param("txOutIds") Collection<Long> txOutIds);

  @Modifying
  void deleteAllByTxOutTxIn(Collection<Tx> txs);
}
