package org.cardanofoundation.rosetta.crawler.projection;

import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionDelegationProjection extends FindTransactionFieldResult {

  String getAddress();

  byte[] getTxHash();

  byte[] getPoolHash();

}
