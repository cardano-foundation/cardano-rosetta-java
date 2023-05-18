package org.cardanofoundation.rosetta.consumer.projection;

import java.math.BigInteger;

public interface AddressTxBalanceProjection {

  String getAddress();

  BigInteger getBalance();

  String getTxHash();
}
