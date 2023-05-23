package org.cardanofoundation.rosetta.common.ledgersync.byron.signature;

import lombok.Builder;

import java.math.BigInteger;

@Builder
public class EpochRange {
  private BigInteger start;
  private BigInteger end;
}
