package org.cardanofoundation.rosetta.crawler.projection;

import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionPoolOwnerProjection extends FindTransactionFieldResult {

  Long getUpdateId();

  String getOwener();

  byte[] getTxHash();

}
