package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.EpochParam;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EpochParamRepository extends JpaRepository<EpochParam, Long> {

  @Query(value = "SELECT ep from EpochParam ep"
      + " WHERE ep.epochNo = (SELECT MAX(e.epochNo) FROM EpochParam e)")
  Optional<EpochParam> findLastEpochParam();

  Optional<EpochParam> findEpochParamByEpochNo(Integer epochNo);
}
