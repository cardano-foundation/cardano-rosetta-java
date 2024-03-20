package org.cardanofoundation.rosetta.testgenerator.transactions;

import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;

public interface TransactionRunner {

  void init();
  GeneratedTestDataDTO runTransactions(GeneratedTestDataDTO generatedTestData);


}
