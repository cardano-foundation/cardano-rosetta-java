package org.cardanofoundation.rosetta.api.projection;

import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionPoolRelayProjection extends FindTransactionFieldResult {

  Long getUpdateId();

  String getIpv4();

  String getIpv6();

  Integer getPort();

  String getDnsName();

  byte[] getTxHash();

}
