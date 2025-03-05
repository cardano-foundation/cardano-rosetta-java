package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationId;

@Repository
public interface PoolRegistrationRepository extends
    JpaRepository<PoolRegistrationEntity, PoolRegistrationId> {

  List<PoolRegistrationEntity> findByTxHashIn(List<String> txHashes);

}
