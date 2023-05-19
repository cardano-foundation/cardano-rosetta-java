package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionRegistrationProjection extends FindTransactionFieldResult {

  Long getAmount();

  String getAddress();

  byte[] getTxHash();

}
