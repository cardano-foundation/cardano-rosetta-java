package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.dto.FindPoolRetirements;
import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PoolRetireRepository extends JpaRepository<PoolRetire, Long> {
  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindPoolRetirements"
      + "(pr.retiringEpoch, "
      + "ph.hashRaw , "
      + "tx.hash ) "
      + "FROM PoolRetire pr "
      + "INNER JOIN PoolHash ph ON pr.poolHash.id = ph.id "
      + "INNER JOIN Tx tx ON tx.id =pr.announcedTx.id "
      + "WHERE tx.hash IN :hashList")
  List<FindPoolRetirements> findPoolRetirements(@Param("hashList") List<String> blocNumbers);
}