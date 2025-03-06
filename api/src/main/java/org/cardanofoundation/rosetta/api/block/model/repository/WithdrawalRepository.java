package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalId;

@Repository
public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, WithdrawalId> {

  List<WithdrawalEntity> findByTxHashIn(List<String> txHashes);

}
