package org.cardanofoundation.rosetta.crawler.repository;


import org.cardanofoundation.rosetta.crawler.projection.EpochParamProjection;
import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EpochParamRepository extends JpaRepository<EpochParam, Long> {
    @Query(value = "SELECT coins_per_utxo_size, " +
            "max_tx_size, max_val_size, " +
            "key_deposit, max_collateral_inputs, " +
            "min_fee_a, " +
            "min_fee_b," +
            " min_pool_cost, " +
            "pool_deposit, " +
            "protocol_major " +
            "FROM epoch_param " +
            "ORDER BY id DESC " +
            "LIMIT 1", nativeQuery = true)
    Page<EpochParamProjection> findProtocolParameters(Pageable pageable);
}