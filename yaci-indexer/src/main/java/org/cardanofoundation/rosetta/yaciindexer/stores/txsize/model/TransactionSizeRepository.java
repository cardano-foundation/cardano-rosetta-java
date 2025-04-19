package org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSizeRepository extends JpaRepository<TransactionSizeEntity, Long> {
}
