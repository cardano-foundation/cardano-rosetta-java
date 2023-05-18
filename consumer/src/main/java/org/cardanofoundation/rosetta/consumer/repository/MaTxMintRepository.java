package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.MaTxMint;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.projection.MaTxMintProjection;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaTxMintRepository extends JpaRepository<MaTxMint,Long> {

  @Query("SELECT mtm.identId as identId, "
      + "mtm.quantity as quantity "
      + "FROM MaTxMint mtm WHERE mtm.tx IN (:txs)")
  List<MaTxMintProjection> findAllByTxIn(Collection<Tx> txs);
}
