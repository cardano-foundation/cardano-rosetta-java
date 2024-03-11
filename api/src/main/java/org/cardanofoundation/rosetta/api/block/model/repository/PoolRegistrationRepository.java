package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEnity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoolRegistrationRepository extends JpaRepository<PoolRegistrationEnity, PoolRegistrationId> {

    List<PoolRegistrationEnity> findByTxHash(String txHash);
}
