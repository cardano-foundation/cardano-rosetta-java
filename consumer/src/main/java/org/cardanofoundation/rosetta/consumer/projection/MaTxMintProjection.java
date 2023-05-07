package org.cardanofoundation.rosetta.consumer.projection;

import java.math.BigInteger;

public interface MaTxMintProjection {
  Long getIdentId();

  BigInteger getQuantity();
}
