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
  List<AddressUtxoEntity> findUtxosByAddress(@Param("address") String address);
}
