package org.cardanofoundation.rosetta.api.account.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceId;

public interface AddressBalanceRepository extends
    JpaRepository<AddressBalanceEntity, AddressBalanceId> {

  //    @Query(value =
//    "SELECT new AddressBalanceEntity (b.address, b.unit, MAX(b.slot),  b.quantity, b.addrFull, b.policy, b.assetName, b.paymentCredential, b.stakeAddress, b.blockHash, b.epoch) " +
//            "FROM AddressBalanceEntity b " +
//            "WHERE b.address = :address AND b.blockNumber <= :number " +
//            "GROUP BY b.address, b.unit,  b.quantity, b.addrFull, b.policy, b.assetName, b.paymentCredential, b.stakeAddress, b.blockHash, b.epoch")
  @Query(value =
      "SELECT b from AddressBalanceEntity b WHERE b.blockNumber in (SELECT MAX(c.blockNumber) FROM AddressBalanceEntity c WHERE c.address = :address AND c.blockNumber <= :number GROUP BY c.unit) AND b.address = :address")
  List<AddressBalanceEntity> findAddressBalanceByAddressAndBlockNumber(
      @Param("address") String address, @Param("number") Long number);
}
