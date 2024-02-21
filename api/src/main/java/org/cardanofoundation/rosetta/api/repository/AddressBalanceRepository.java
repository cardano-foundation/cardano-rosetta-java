package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.common.model.AddressBalanceEntity;
import org.cardanofoundation.rosetta.common.model.AddressBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressBalanceRepository extends JpaRepository<AddressBalanceEntity, AddressBalanceId> {

//    @Query(value =
//    "SELECT new AddressBalanceEntity (b.address, b.unit, MAX(b.slot),  b.quantity, b.addrFull, b.policy, b.assetName, b.paymentCredential, b.stakeAddress, b.blockHash, b.epoch) " +
//            "FROM AddressBalanceEntity b " +
//            "WHERE b.address = :address AND b.blockNumber <= :number " +
//            "GROUP BY b.address, b.unit,  b.quantity, b.addrFull, b.policy, b.assetName, b.paymentCredential, b.stakeAddress, b.blockHash, b.epoch")
    @Query(value =
    "SELECT b from AddressBalanceEntity b WHERE b.slot in (SELECT MAX(c.slot) FROM AddressBalanceEntity c WHERE c.address = :address AND c.blockNumber <= :number GROUP BY c.unit) AND b.address = :address")
    List<AddressBalanceEntity> findAdressBalanceByAddressAndBlockNumber(@Param("address") String address, @Param("number") Long number);
}
