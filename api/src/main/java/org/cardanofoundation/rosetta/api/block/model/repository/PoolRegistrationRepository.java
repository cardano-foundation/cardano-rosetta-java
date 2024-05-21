package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationId;

public interface PoolRegistrationRepository extends
    JpaRepository<PoolRegistrationEntity, PoolRegistrationId> {

  List<PoolRegistrationEntity> findByTxHashIn(List<String> list);
}
