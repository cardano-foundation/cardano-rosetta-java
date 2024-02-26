package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.common.model.PoolRegistrationEnity;
import org.cardanofoundation.rosetta.common.model.PoolRegistrationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoolRegistrationRepository extends JpaRepository<PoolRegistrationEnity, PoolRegistrationId> {

    List<PoolRegistrationEnity> findByTxHash(String txHash);
}
