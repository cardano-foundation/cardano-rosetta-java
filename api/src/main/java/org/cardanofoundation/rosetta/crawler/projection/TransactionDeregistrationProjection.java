package org.cardanofoundation.rosetta.crawler.projection;

import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionDeregistrationProjection extends FindTransactionFieldResult {

  Long getAmount();

  String getAddress();

  byte[] getTxHash();

}
