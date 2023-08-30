package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDelegations;
import org.cardanofoundation.rosetta.common.entity.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DelegationRepository extends JpaRepository<Delegation, Long> {
@Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDelegations"
    + "(sa.view , "
    + "ph.hashRaw , "
    + "tx.hash ) "
    + "FROM Delegation d "
    + "INNER JOIN StakeAddress sa ON d.address.id = sa.id "
    + "INNER JOIN PoolHash ph ON d.poolHash.id = ph.id "
    + "INNER JOIN Tx tx ON d.tx.id = tx.id "
    + "WHERE tx.hash IN :hashList "
    + "ORDER BY d.id DESC")
List<FindTransactionDelegations> findTransactionDelegations(@Param("hashList") List<String> hashes);

}