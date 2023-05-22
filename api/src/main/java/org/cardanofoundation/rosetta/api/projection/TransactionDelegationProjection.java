package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionDelegationProjection extends FindTransactionFieldResult {

  String getAddress();

  byte[] getTxHash();

  byte[] getPoolHash();

}
