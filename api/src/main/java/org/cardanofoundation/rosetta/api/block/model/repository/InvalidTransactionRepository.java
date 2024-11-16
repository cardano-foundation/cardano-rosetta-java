package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.InvalidTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTransactionRepository extends JpaRepository<InvalidTransactionEntity, String> {

}
