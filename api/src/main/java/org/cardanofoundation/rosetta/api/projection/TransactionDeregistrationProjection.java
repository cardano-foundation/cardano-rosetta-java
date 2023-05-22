package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionDeregistrationProjection extends FindTransactionFieldResult {

  Long getAmount();

  String getAddress();

  byte[] getTxHash();

}
