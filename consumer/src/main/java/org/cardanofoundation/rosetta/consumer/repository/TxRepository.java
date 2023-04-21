package org.cardanofoundation.rosetta.consumer.repository;

import com.sotatek.cardano.common.entity.Tx;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxRepository extends JpaRepository<Tx, Long> {
}
