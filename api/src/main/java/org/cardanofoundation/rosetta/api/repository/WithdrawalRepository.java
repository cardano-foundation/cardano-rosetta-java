package org.cardanofoundation.rosetta.api.repository;


import org.cardanofoundation.rosetta.common.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
}