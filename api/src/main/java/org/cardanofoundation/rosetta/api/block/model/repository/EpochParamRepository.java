package org.cardanofoundation.rosetta.api.block.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.cardanofoundation.rosetta.api.block.model.entity.EpochParamEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;

public interface EpochParamRepository extends JpaRepository<EpochParamEntity, Integer> {

  @Query("SELECT e.params FROM EpochParamEntity e ORDER BY e.epoch DESC LIMIT 1")
  ProtocolParams findLatestProtocolParams();
}
