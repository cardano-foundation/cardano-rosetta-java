package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface PoolRetirementProjection extends FindTransactionFieldResult {

  Integer getEpoch();

  byte[] getAddress();

  byte[] getTxHash();

}
