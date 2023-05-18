package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.consumer.projection.MaTxOutProjection;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MultiAssetTxOutRepository extends JpaRepository<MaTxOut,Long> {

  @Query("SELECT mto.txOutId AS txOutId, "
      + "mto.ident.fingerprint AS fingerprint, "
      + "mto.quantity AS quantity "
      + "FROM MaTxOut mto "
      + "WHERE mto.txOutId IN (:txOutIds)")
  List<MaTxOutProjection> findAllByTxOutIdsIn(Collection<Long> txOutIds);
}
