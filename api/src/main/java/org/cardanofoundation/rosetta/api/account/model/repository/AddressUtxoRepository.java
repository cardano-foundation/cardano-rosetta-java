package org.cardanofoundation.rosetta.api.account.model.repository;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for address UTXO operations using JPA.
 * For complex transaction history queries, use AddressTransactionHistoryService.
 */
@Repository
public interface AddressUtxoRepository extends JpaRepository<AddressUtxoEntity, UtxoId> {

  @Query(value =
      """
      SELECT a FROM AddressUtxoEntity a
      WHERE a.ownerAddr = :address
      AND NOT EXISTS (
        SELECT 1
        FROM TxInputEntity i
        WHERE a.txHash = i.txHash
          AND a.outputIndex = i.outputIndex
      )
      """)
  List<AddressUtxoEntity> findunspentUtxosByAddress(@Param("address") String address);

  @Query(value =
      """
      SELECT a.txHash FROM AddressUtxoEntity a
      WHERE a.ownerAddr = :address
      OR a.ownerStakeAddr = :address
      """)
  List<String> findTxHashesByOwnerAddr(@Param("address") String ownerAddr);

  @Query(value =
      """
      SELECT a FROM AddressUtxoEntity a WHERE
      a.ownerAddr = :address
      AND NOT EXISTS(SELECT 1 FROM TxInputEntity o WHERE o.txHash = a.txHash AND o.outputIndex = a.outputIndex AND o.spentAtBlock <= :block)
      AND a.blockNumber <= :block
      """)
  List<AddressUtxoEntity> findUnspentUtxosByAddressAndBlock(@Param("address") String address, @Param("block") long block);

  List<AddressUtxoEntity> findByTxHashIn(List<String> utxHashes);

  /**
   * Find all transaction hashes where the address received outputs.
   */
  @Query(value = """
      SELECT DISTINCT au.txHash FROM AddressUtxoEntity au
      WHERE au.ownerAddr = :address OR au.ownerStakeAddr = :address
      """)
  List<String> findOutputTransactionsByAddress(@Param("address") String address);

  /**
   * Find all transaction hashes where the address's outputs were spent as inputs.
   */
  @Query(value = """
      SELECT DISTINCT ti.spentTxHash
      FROM TxInputEntity ti
      INNER JOIN AddressUtxoEntity au ON (
          ti.txHash = au.txHash AND 
          ti.outputIndex = au.outputIndex
      )
      WHERE au.ownerAddr = :address OR au.ownerStakeAddr = :address
      """)
  List<String> findInputTransactionsByAddress(@Param("address") String address);

}
