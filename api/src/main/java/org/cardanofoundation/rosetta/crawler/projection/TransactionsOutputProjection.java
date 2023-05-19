package org.cardanofoundation.rosetta.crawler.projection;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionsOutputProjection extends FindTransactionFieldResult {

  Long getId();

  String getAddress();

  BigInteger getValue();

  byte[] getTxHash();

  Short getIndex();

  byte[] getPolicy();

  byte[] getName();

  BigInteger getQuantity();

}
