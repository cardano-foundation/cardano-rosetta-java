package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpochRepository extends JpaRepository<Epoch, Long> {

  @Transactional(readOnly = true)
  Optional<Epoch> findEpochByNo(Integer no);

  List<Epoch> findAllByNoIn(Collection<Integer> no);
}
