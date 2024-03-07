package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.common.model.AddressUtxoEntity;
import org.cardanofoundation.rosetta.common.model.UtxoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressUtxoRepository extends JpaRepository<AddressUtxoEntity, UtxoId> {

    @Query(value =
    "SELECT a FROM AddressUtxoEntity a WHERE a.ownerAddr = :address AND a.blockHash = :blockHash")
    List<AddressUtxoEntity> findUtxoByAddressAndBlock(@Param("address") String address, @Param("blockHash") String blockHash);

    List<AddressUtxoEntity> findAddressUtxoEntitiesByOutputIndexAndTxHash(Integer outputIndex, String txHash);

    @Query(value =
    "SELECT a FROM AddressUtxoEntity a LEFT OUTER JOIN TxInputEntity i ON a.txHash = i.txHash AND a.outputIndex = i.outputIndex WHERE a.ownerAddr = :address AND i.txHash IS NULL AND i.outputIndex IS NULL"
    )
    List<AddressUtxoEntity> findUtxosByAddress(@Param("address") String address);
}
