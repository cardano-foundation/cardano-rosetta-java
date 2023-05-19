package org.cardanofoundation.rosetta.crawler.projection;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionPoolRegistrationProjection extends FindTransactionFieldResult {

  byte[] getTxHash();

  Long getTxId();

  Long getUpdateId();

  byte[] getVrfKeyHash();

  BigInteger getPledge();

  Double getMargin();

  BigInteger getCost();

  String getAddress();

  byte[] getPoolHash();

  String getMetadataUrl();

  byte[] getMetadataHash();
}