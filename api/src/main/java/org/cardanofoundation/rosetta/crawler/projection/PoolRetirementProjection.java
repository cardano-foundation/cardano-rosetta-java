package org.cardanofoundation.rosetta.crawler.projection;

import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface PoolRetirementProjection extends FindTransactionFieldResult {

  Integer getEpoch();

  byte[] getAddress();

  byte[] getTxHash();

}
