package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionPoolOwnerProjection extends FindTransactionFieldResult {

  Long getUpdateId();

  String getOwener();

  byte[] getTxHash();

}
