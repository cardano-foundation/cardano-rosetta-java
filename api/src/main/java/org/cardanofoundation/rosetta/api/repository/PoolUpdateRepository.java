package org.cardanofoundation.rosetta.api.repository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolOwners;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRegistrationsData;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRelays;
import org.cardanofoundation.rosetta.common.entity.PoolUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PoolUpdateRepository extends JpaRepository<PoolUpdate, Long> {
  String poolRegistrationQuery = "( SELECT "
      + "tx.hash AS txHash, "
      + "tx.id AS txId, "
      + "pu.id AS updateId, "
      + "pu.vrfKeyHash AS vrfKeyHash, "
      + "pu.pledge AS pledge, "
      + "pu.margin AS margin, "
      + "pu.fixedCost AS cost, "
      + "sa.view AS address, "
      + "ph.hashRaw AS poolHash, "
      + "pm.url AS metadataUrl, "
      + "pm.hash AS metadataHash "
      + "FROM PoolUpdate pu "
      + "JOIN Tx tx ON pu.registeredTx.id = tx.id "
      + "JOIN PoolHash ph ON ph.id = pu.poolHash.id "
      + "JOIN StakeAddress sa ON sa.id = pu.rewardAddr.id "
      + "LEFT JOIN PoolMetadataRef pm ON pu.meta.id = pm.id "
      + "WHERE tx.hash IN :hashList )";
//  @Query("SELECT "
//      + "tx.hash AS txHash, "
//      + "tx.id AS txId, "
//      + "pu.id AS updateId, "
//      + "pu.vrfKeyHash AS vrfKeyHash, "
//      + "pu.pledge AS pledge, "
//      + "pu.margin AS margin, "
//      + "pu.fixedCost AS cost, "
//      + "sa.view AS address, "
//      + "ph.hashRaw AS poolHash, "
//      + "pm.url AS metadataUrl, "
//      + "pm.hash AS metadataHash "
//      + "FROM PoolUpdate pu "
//      + "JOIN Tx tx ON pu.registeredTx.id = tx.id "
//      + "JOIN PoolHash ph ON ph.id = pu.poolHash.id "
//      + "JOIN StakeAddress sa ON sa.id = pu.rewardAddr.id "
//      + "LEFT JOIN PoolMetadataRef pm ON pu.meta.id = pm.id "
//      + "WHERE tx.hash IN :hashList")
//  List<TransactionPoolRegistrationProjection> findTransactionPoolRegistrationsData(@Param("hashList") List<byte[]> hashes);
@Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRegistrationsData"
    + "(tx.hash , "
    + "tx.id , "
    + "pu.id , "
    + "pu.vrfKeyHash , "
    + "pu.pledge , "
    + "pu.margin , "
    + "pu.fixedCost , "
    + "sa.view , "
    + "ph.hashRaw , "
    + "pm.url , "
    + "pm.hash )"
    + "FROM PoolUpdate pu "
    + "JOIN Tx tx ON pu.registeredTx.id = tx.id "
    + "JOIN PoolHash ph ON ph.id = pu.poolHash.id "
    + "JOIN StakeAddress sa ON sa.id = pu.rewardAddr.id "
    + "LEFT JOIN PoolMetadataRef pm ON pu.meta.id = pm.id "
    + "WHERE tx.hash IN :hashList")
List<FindTransactionPoolRegistrationsData> findTransactionPoolRegistrationsData(@Param("hashList") List<String> hashes);
//  @Query("SELECT "
//      + "  po.poolUpdate.id AS updateId, "
//      + "  sa.view AS owner, "
//      + "  pr.txHash AS txHash "
//      + "FROM " + poolRegistrationQuery + " AS pr "
//      + "JOIN PoolOwner po ON po.poolUpdate.id = pr.updateId "
//      + "JOIN StakeAddress sa ON po.stakeAddress.id = sa.id "
//      + "ORDER BY po.id DESC")
//  List<TransactionPoolOwnerProjection> findTransactionPoolOwners(@Param("hashList") List<byte[]> hashes);
@Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolOwners"
    + "(po.poolUpdate.id , "
    + "  sa.view , "
    + "  pr.txHash )"
    + "FROM " + poolRegistrationQuery + " AS pr "
    + "JOIN PoolOwner po ON po.poolUpdate.id = pr.updateId "
    + "JOIN StakeAddress sa ON po.stakeAddress.id = sa.id "
    + "ORDER BY po.id DESC")
List<FindTransactionPoolOwners> findTransactionPoolOwners(@Param("hashList") List<String> hashes);
  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRelays "
      + "(prelay.poolUpdate.id , "
      + "prelay.ipv4 , "
      + "prelay.ipv6 , "
      + "prelay.port , "
      + "prelay.dnsName , "
      + "pr.txHash ) "
      + "FROM " + poolRegistrationQuery + "AS pr "
      + "JOIN PoolRelay AS prelay ON prelay.poolUpdate.id = pr.updateId ")
  List<FindTransactionPoolRelays> findTransactionPoolRelays(@Param("hashList") List<String> hashes);
}