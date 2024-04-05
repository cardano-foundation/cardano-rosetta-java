package org.cardanofoundation.rosetta.testgenerator.transactions;

import java.util.Map;

import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

public interface TransactionRunner {

  void init();

  /** Populating the generatedDataMap with the transaction data. On every transaction, map is
   * populated with a new entry - name of the transaction as a key and the TransactionBlockDetails
   * object as a value, where all necessary transaction data is stored.
   *
   * @return a map of transaction block details
   */
  Map<String, TransactionBlockDetails> runTransactions();
}
