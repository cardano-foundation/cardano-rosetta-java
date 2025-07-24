package org.cardanofoundation.rosetta.api.account.model.repository;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
