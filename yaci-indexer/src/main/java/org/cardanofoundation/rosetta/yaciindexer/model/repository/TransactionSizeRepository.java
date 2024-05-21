package org.cardanofoundation.rosetta.yaciindexer.model.repository;

import org.cardanofoundation.rosetta.yaciindexer.model.entity.TransactionSizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSizeRepository extends JpaRepository<TransactionSizeEntity, Long> {

}
