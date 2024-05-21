package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalId;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, WithdrawalId> {

  List<WithdrawalEntity> findByTxHashIn(List<String> txHashes);
}
