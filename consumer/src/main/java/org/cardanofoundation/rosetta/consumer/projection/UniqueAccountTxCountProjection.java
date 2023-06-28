package org.cardanofoundation.rosetta.consumer.projection;

public interface UniqueAccountTxCountProjection {

  String getAccount();

  Integer getTxCount();
}
