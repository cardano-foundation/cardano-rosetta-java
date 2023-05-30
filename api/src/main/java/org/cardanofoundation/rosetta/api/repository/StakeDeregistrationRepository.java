package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDeregistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionRegistrations;
import org.cardanofoundation.rosetta.common.entity.StakeDeregistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StakeDeregistrationRepository extends JpaRepository<StakeDeregistration, Long> {

  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionRegistrations "
      + "(tx.deposit , "
      + "   sa.view , "
      + "   tx.hash ) "
      + "FROM StakeRegistration sr "
      + "INNER JOIN Tx  tx ON tx.id = sr.tx.id "
      + "INNER JOIN StakeAddress sa ON sr.addr.id = sa.id "
      + "WHERE tx.hash IN :hashList")
  List<FindTransactionRegistrations> findTransactionRegistrations(@Param("hashList") List<String> hashes);
  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDeregistrations"
      + "(sa.view , "
      + "tx.deposit , "
      + "tx.hash ) "
      + "FROM StakeDeregistration sd "
      + "INNER JOIN StakeAddress sa ON sd.addr.id = sa.id "
      + "INNER JOIN Tx tx ON tx.id = sd.tx.id "
      + "WHERE tx.hash IN :hashList ")
  List<FindTransactionDeregistrations> findTransactionDeregistrations(@Param("hashList") List<String> hashes);
}
