package org.cardanofoundation.rosetta.yaciindexer.txSizeStore;

import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSizeRepository extends JpaRepository<TransactionSizeEntity, Long> {

}
