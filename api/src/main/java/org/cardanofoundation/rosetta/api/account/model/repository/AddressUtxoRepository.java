package org.cardanofoundation.rosetta.api.account.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;

public interface AddressUtxoRepository extends JpaRepository<AddressUtxoEntity, UtxoId> {

  List<AddressUtxoEntity> findAddressUtxoEntitiesByOutputIndexAndTxHash(Integer outputIndex,
      String txHash);

  @Query(value =
      "SELECT a FROM AddressUtxoEntity a LEFT OUTER JOIN TxInputEntity i ON a.txHash = i.txHash AND a.outputIndex = i.outputIndex WHERE a.ownerAddr = :address AND i.txHash IS NULL AND i.outputIndex IS NULL"
  )
  List<AddressUtxoEntity> findUtxosByAddress(@Param("address") String address);

  @Query(value =
      """
      SELECT a FROM AddressUtxoEntity a WHERE
      a.ownerAddr = :address
      AND NOT EXISTS(SELECT 1 FROM TxInputEntity o WHERE o.txHash = a.txHash AND o.outputIndex = a.outputIndex AND o.spentAtBlock <= :block)
      AND a.blockNumber <= :block
      """)
  List<AddressUtxoEntity> findUnspentUtxosByAddressAndBlock(@Param("address") String address, @Param("block") long block);

  @Query(value =
      """
      SELECT a FROM AddressUtxoEntity a WHERE
      a.ownerStakeAddr = :address
      AND NOT EXISTS(SELECT o FROM TxInputEntity o WHERE o.txHash = a.txHash AND o.outputIndex = a.outputIndex AND o.spentAtBlock <= :block)
      AND a.blockNumber <= :block
      """)
  List<AddressUtxoEntity> findUnspentUtxosByStakeAddressAndBlock(@Param("address") String stakeAddress, @Param("block") long block);
}
