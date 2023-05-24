package org.cardanofoundation.rosetta.common.ledgersync.byron.signature;

import java.math.BigInteger;
import lombok.Builder;

@Builder
public class EpochRange {
  private BigInteger start;
  private BigInteger end;
}
