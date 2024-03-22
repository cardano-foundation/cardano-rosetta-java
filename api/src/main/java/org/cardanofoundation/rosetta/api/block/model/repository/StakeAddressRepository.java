package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.block.model.entity.StakeAddressBalanceEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.StakeAddressBalanceId;

public interface StakeAddressRepository extends
    JpaRepository<StakeAddressBalanceEntity, StakeAddressBalanceId> {

  @Query(value =
      "SELECT b from StakeAddressBalanceEntity b WHERE b.slot in (SELECT MAX(c.slot) FROM StakeAddressBalanceEntity c WHERE c.address = :address AND c.blockNumber <= :number) AND b.address = :address")
  List<StakeAddressBalanceEntity> findStakeAddressBalanceByAddressAndBlockNumber(
      @Param("address") String address, @Param("number") Long number);
}
