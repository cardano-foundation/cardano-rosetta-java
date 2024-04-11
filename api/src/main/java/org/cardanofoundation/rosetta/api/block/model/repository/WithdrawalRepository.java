package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, WithdrawalId> {

  List<WithdrawalEntity> findByTxHash(String txHash);
}
