package org.cardanofoundation.rosetta.crawler.projection;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.crawler.projection.dto.FindTransactionFieldResult;

public interface TransactionWithdrawalProjection extends FindTransactionFieldResult {

  BigInteger getAmount();

  String getAddress();

  byte[] getTxHash();
}
