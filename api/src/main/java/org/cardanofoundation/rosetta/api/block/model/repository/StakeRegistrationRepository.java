package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationId;

public interface StakeRegistrationRepository extends
    JpaRepository<StakeRegistrationEntity, StakeRegistrationId> {

  List<StakeRegistrationEntity> findByTxHashIn(List<String> txHashes);
}
