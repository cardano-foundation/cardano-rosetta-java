package org.cardanofoundation.rosetta.api.block.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.InvalidTransactionEntity;

public interface InvalidTransactionRepository extends JpaRepository<InvalidTransactionEntity, String> {

}
