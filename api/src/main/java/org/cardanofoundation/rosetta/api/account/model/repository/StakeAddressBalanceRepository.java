package org.cardanofoundation.rosetta.api.account.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.account.model.entity.StakeAddressBalanceEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.StakeAddressBalanceId;
import org.cardanofoundation.rosetta.api.account.model.entity.projection.StakeAccountBalanceQuantityOnly;

public interface StakeAddressBalanceRepository extends
    JpaRepository<StakeAddressBalanceEntity, StakeAddressBalanceId> {

  @Query(value =
      """
          SELECT b from StakeAddressBalanceEntity b
          WHERE b.slot =
            (SELECT MAX(c.slot) FROM StakeAddressBalanceEntity c
            WHERE c.address = :address
            AND c.blockNumber <= :number)
          AND b.address = :address
      """)
  Optional<StakeAccountBalanceQuantityOnly> findStakeAddressBalanceQuantityByAddressAndBlockNumber(
      @Param("address") String address, @Param("number") Long number);
}
