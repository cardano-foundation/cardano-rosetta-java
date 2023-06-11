package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.AddressTokenBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressTokenBalanceRepository
    extends JpaRepository<AddressTokenBalance, Long>, CustomAddressTokenBalanceRepository {

}
