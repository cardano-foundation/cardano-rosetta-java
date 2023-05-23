package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronTxOut {

  private ByronAddress address;
  private BigInteger amount;
}
