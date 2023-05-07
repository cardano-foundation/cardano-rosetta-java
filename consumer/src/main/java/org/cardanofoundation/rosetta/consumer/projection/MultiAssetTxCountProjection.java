package org.cardanofoundation.rosetta.consumer.projection;

public interface MultiAssetTxCountProjection {

  Long getIdentId();

  Long getTxCount();
}
