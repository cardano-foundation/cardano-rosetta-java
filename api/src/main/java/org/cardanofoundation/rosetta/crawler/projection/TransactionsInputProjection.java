package org.cardanofoundation.rosetta.crawler.projection;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionsInputProjection extends FindTransactionFieldResult {

  Long getId();

  String getAddress();

  BigInteger getValue();

  byte[] getTxHash();

  byte[] getSourceTxHash();

  Short getSourceTxIndex();

  byte[] getPolicy();

  byte[] getName();

  BigInteger getQuantity();
}
