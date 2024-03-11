package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StakeRegistrationRepository extends JpaRepository<StakeRegistrationEntity, StakeRegistrationId>{

    List<StakeRegistrationEntity> findByTxHash(String txHash);
}
