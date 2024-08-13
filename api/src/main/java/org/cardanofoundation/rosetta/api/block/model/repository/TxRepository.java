package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;

import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

public interface TxRepository extends JpaRepository<TxnEntity, Long> {

  List<TxnEntity> findTransactionsByBlockHash(@Param("blockHash") String blockHash);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        LEFT JOIN AddressUtxoEntity utxo ON tx.txHash = utxo.txHash
        WHERE
        (:txHash IS NULL OR tx.txHash = :txHash) AND
        (:address IS NULL OR utxo.ownerAddr = :address) AND
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) AND
        (:blockHash IS NULL OR tx.block.hash = :blockHash) AND
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  List<TxnEntity> searchTxnEntitiesAND(@Param("txHash") String txHash, @Param("address") String address, @Param("blockHash") String blockHash, @Param("blockNo") Long blockNumber, @Param("maxBlock") Long maxBlock, Pageable pageable);

  @Query(value = """
        SELECT tx FROM TxnEntity tx
        LEFT JOIN AddressUtxoEntity utxo ON tx.txHash = utxo.txHash
        WHERE
        (:txHash IS NULL OR tx.txHash = :txHash) OR
        (:address IS NULL OR utxo.ownerAddr = :address) OR
        (:maxBlock IS NULL OR tx.block.number <= :maxBlock) OR
        (:blockHash IS NULL OR tx.block.hash = :blockHash) OR
        (:blockNo IS NULL OR tx.block.number = :blockNo)
        """)
  List<TxnEntity> searchTxnEntitiesOR(@Param("txHash") String txHash, @Param("address") String address, @Param("blockHash") String blockHash, @Param("blockNo") Long blockNumber, @Param("maxBlock") Long maxBlock, Pageable pageable);
  //
//  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsInputs "
//      + "(txIn.id , "
//      + "  sourceTxOut.address ,"
//      + "  sourceTxOut.value, "
//      + "  tx.hash, "
//      + "  sourceTx.hash , "
//      + "  txIn.txOutIndex, "
//      + "  asset.policy , "
//      + "  asset.name , "
//      + "  sourceMaTxOut.quantity) "
//      + "FROM Tx tx "
//      + "JOIN TxIn txIn ON txIn.txInput.id = tx.id "
//      + "JOIN TxOut sourceTxOut "
//      + "   ON (txIn.txOut.id = sourceTxOut.tx.id "
//      + "   AND txIn.txOutIndex = sourceTxOut.index) "
//      + "JOIN Tx sourceTx ON sourceTxOut.tx.id = sourceTx.id "
//      + "LEFT JOIN MaTxOut AS sourceMaTxOut ON sourceMaTxOut.txOut.id = sourceTxOut.id "
//      + "LEFT JOIN MultiAsset AS asset ON asset.id = sourceMaTxOut.ident.id "
//      + "WHERE tx.hash IN :hashList "
//      + "ORDER BY asset.policy, asset.name, tx.id")
//  List<FindTransactionsInputs> findTransactionsInputs(@Param("hashList") List<String> hashes);
//
//  @Query("SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsOutputs "
//      + "(txOut.id ,"
//      + "  txOut.address ,"
//      + "  txOut.value , "
//      + "  asset.policy , "
//      + "  asset.name , "
//      + "  tx.hash , "
//      + "  maTxOut.quantity, "
//      + "  txOut.index) "
//      + "FROM Tx tx "
//      + "JOIN TxOut txOut ON tx.id = txOut.tx.id "
//      + "LEFT JOIN MaTxOut maTxOut ON maTxOut.txOut.id = txOut.id "
//      + "LEFT JOIN MultiAsset asset ON asset.id = maTxOut.ident.id "
//      + "WHERE tx.hash IN :hashList "
//      + "ORDER BY asset.policy,  asset.name , txOut.id")
//  List<FindTransactionsOutputs> findTransactionsOutputs(@Param("hashList") List<String> hashes);
//
//
//  @Query(
//      "SELECT new org.cardanofoundation.rosetta.api.projection.dto.FindTransactionWithdrawals"
//          + "(sa.view , "
//          + "w.amount , "
//          + " tx.hash  )"
//          + "FROM Withdrawal w "
//          + "INNER JOIN Tx tx on w.tx.id = tx.id "
//          + "INNER JOIN StakeAddress sa on w.addr.id = sa.id "
//          + "WHERE tx.hash IN :hashList")
//  List<FindTransactionWithdrawals> findTransactionWithdrawals(
//      @Param("hashList") List<String> hashes);
}
