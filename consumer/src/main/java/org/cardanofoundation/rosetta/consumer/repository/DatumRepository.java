package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.projection.DatumProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface DatumRepository extends JpaRepository<Datum, Long> {


  @Query("SELECT d.hash as hash"
      + " FROM Datum as d"
      + " WHERE d.hash IN (:hashes)")
  Set<String> getExistHashByHashIn(@Param("hashes") Set<String> datumHashes);

  @Query("SELECT d.hash as hash,"
      + " d.id as id"
      + " FROM Datum as d"
      + " WHERE d.hash IN (:hashes)")
  @Transactional(readOnly = true)
  List<DatumProjection> getDatumByHashes(@Param("hashes") Set<String> hashes);

  @Modifying
  void deleteAllByTxIn(Collection<Tx> txs);
}
