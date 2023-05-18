package org.cardanofoundation.rosetta.consumer.projection;

import java.math.BigInteger;

public interface MaTxOutProjection {

  String getFingerprint();

  Long getTxOutId();

  BigInteger getQuantity();
}
