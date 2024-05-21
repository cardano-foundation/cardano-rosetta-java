package org.cardanofoundation.rosetta.api.account.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;

public interface AddressUtxoRepository extends JpaRepository<AddressUtxoEntity, UtxoId> {

  @Query(value =
      "SELECT a FROM AddressUtxoEntity a LEFT OUTER JOIN TxInputEntity i ON a.txHash = i.txHash AND a.outputIndex = i.outputIndex WHERE a.ownerAddr = :address AND i.txHash IS NULL AND i.outputIndex IS NULL"
  )
  List<AddressUtxoEntity> findUtxosByAddress(@Param("address") String address);

  List<AddressUtxoEntity> findByTxHashIn(List<String> txHashes);
}
