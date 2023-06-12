package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PoolRetireRepository extends JpaRepository<PoolRetire, Long> {

  @Modifying
  void deleteAllByAnnouncedTxIn(Collection<Tx> announcedTxs);
}