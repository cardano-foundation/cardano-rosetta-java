package org.cardanofoundation.rosetta.api.projection;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionFieldResult;

public interface TransactionWithdrawalProjection extends FindTransactionFieldResult {

  BigInteger getAmount();

  String getAddress();

  byte[] getTxHash();
}
