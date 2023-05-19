package org.cardanofoundation.rosetta.crawler.repository;

import java.util.List;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.crawler.projection.FindTransactionProjection;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionWithdrawals;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionsInputs;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionsOutputs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TxRepository extends JpaRepository<Tx, Long> {

  @Query("SELECT "
      + "   tx.hash as hash, "
      + "   tx.fee as fee, "
      + "   tx.size as size, "
      + "   tx.validContract AS validContract, "
      + "   tx.scriptSize AS scriptSize, "
      + "   block.hash AS blockHash "
      + "FROM Tx tx JOIN Block block on block.id = tx.block.id "
      + "WHERE ("
      + "       :blockNumber IS NULL "
      + "       OR (block.blockNo = :blockNumber "
      + "       OR (block.blockNo IS NULL AND :blockNumber = 0))"
      + ") "
      + "   AND (:blockHash IS NULL OR block.hash = :blockHash)")
  List<FindTransactionProjection> findTransactionsByBlock(@Param("blockNumber") Long blockNumber,
      @Param("blockHash") byte[] blockHash);

  //  @Query("SELECT tx.id AS id, "
//      + "  sourceTxOut.address AS address,"
//      + "  sourceTxOut.value AS value,"
//      + "  tx.hash AS txHash, "
//      + "  sourceTx.hash AS sourceTxHash, "
//      + "  txIn.txOutIndex AS sourceTxIndex, "
//      + "  asset.policy AS policy, "
//      + "  asset.name AS name, "
//      + "  sourceMaTxOut.quantity AS quantity "
//      + "FROM Tx tx "
//      + "JOIN TxIn txIn ON txIn.txInput.id = tx.id "
//      + "JOIN TxOut sourceTxOut ON txIn.txOut.id = sourceTxOut.tx.id "
//      + "JOIN Tx sourceTx ON sourceTxOut.tx.id = sourceTx.id "
//      + "LEFT JOIN MaTxOut AS sourceMaTxOut ON sourceMaTxOut.txOut.id = sourceMaTxOut.id "
//      + "LEFT JOIN MultiAsset AS asset ON asset.id = sourceMaTxOut.ident.id "
//      + "WHERE tx.hash IN :hashList "
//      + "ORDER BY policy, name, id")
//  List<TransactionsInputProjection> findTransactionsInputs(@Param("hashList") List<byte[]> hashes);
  @Query("SELECT new org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionsInputs "
      + "(txIn.id , "
      + "  sourceTxOut.address ,"
      + "  sourceTxOut.value, "
      + "  tx.hash, "
      + "  sourceTx.hash , "
      + "  txIn.txOutIndex, "
      + "  asset.policy , "
      + "  asset.name , "
      + "  sourceMaTxOut.quantity) "
      + "FROM Tx tx "
      + "JOIN TxIn txIn ON txIn.txInput.id = tx.id "
      + "JOIN TxOut sourceTxOut "
      + "   ON (txIn.txOut.id = sourceTxOut.tx.id "
      + "   AND txIn.txOutIndex = sourceTxOut.index) "
      + "JOIN Tx sourceTx ON sourceTxOut.tx.id = sourceTx.id "
      + "LEFT JOIN MaTxOut AS sourceMaTxOut ON sourceMaTxOut.txOut.id = sourceTxOut.id "
      + "LEFT JOIN MultiAsset AS asset ON asset.id = sourceMaTxOut.ident.id "
      + "WHERE tx.hash IN :hashList "
      + "ORDER BY asset.policy, asset.name, tx.id")
  List<FindTransactionsInputs> findTransactionsInputs(@Param("hashList") List<byte[]> hashes);

  //  @Query("SELECT txOut.id as id,"
//      + "  txOut.address as address,"
//      + "  txOut.value as value, "
//      + "  tx.hash as txHash, "
//      + "  txOut.index as index, "
//      + "  asset.policy as policy, "
//      + "  asset.name as name, "
//      + "  maTxOut.quantity as quantity "
//      + "FROM Tx tx "
//      + "JOIN TxOut txOut ON tx.id = txOut.tx.id "
//      + "LEFT JOIN MaTxOut maTxOut ON maTxOut.txOut.id = txOut.id "
//      + "LEFT JOIN MultiAsset asset ON asset.id = maTxOut.ident.id "
//      + "WHERE tx.hash IN :hashList "
//      + "ORDER BY policy, name, id")
//  List<TransactionsOutputProjection> findTransactionsOutputs(@Param("hashList") List<byte[]> hashes);
  @Query("SELECT new org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionsOutputs "
      + "(txOut.id ,"
      + "  txOut.address ,"
      + "  txOut.value , "
      + "  asset.policy , "
      + "  asset.name , "
      + "  tx.hash , "
      + "  maTxOut.quantity, "
      + "  txOut.index) "
      + "FROM Tx tx "
      + "JOIN TxOut txOut ON tx.id = txOut.tx.id "
      + "LEFT JOIN MaTxOut maTxOut ON maTxOut.txOut.id = txOut.id "
      + "LEFT JOIN MultiAsset asset ON asset.id = maTxOut.ident.id "
      + "WHERE tx.hash IN :hashList "
      + "ORDER BY asset.policy,  asset.name , txOut.id")
  List<FindTransactionsOutputs> findTransactionsOutputs(@Param("hashList") List<byte[]> hashes);

  //  @Query("SELECT w.amount as amount, "
//      + "   sa.view as address, "
//      + "   tx.hash as txHash "
//      + "FROM Withdrawal w "
//      + "INNER JOIN Tx tx on w.tx.id = tx.id "
//      + "INNER JOIN StakeAddress sa on w.addr.id = sa.id "
//      + "WHERE tx.hash IN :hashList")
//  List<TransactionWithdrawalProjection> findTransactionWithdrawals(@Param("hashList") List<byte[]> hashes);
  @Query(
      "SELECT new org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionWithdrawals"
          + "(sa.view , "
          + "w.amount , "
          + " tx.hash  )"
          + "FROM Withdrawal w "
          + "INNER JOIN Tx tx on w.tx.id = tx.id "
          + "INNER JOIN StakeAddress sa on w.addr.id = sa.id "
          + "WHERE tx.hash IN :hashList")
  List<FindTransactionWithdrawals> findTransactionWithdrawals(
      @Param("hashList") List<byte[]> hashes);
}