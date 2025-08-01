package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.InvalidTransactionEntity;

@Repository
public interface InvalidTransactionRepository extends JpaRepository<InvalidTransactionEntity, String> {

  List<InvalidTransactionEntity> findByTxHashIn(List<String> txHashes);

}
