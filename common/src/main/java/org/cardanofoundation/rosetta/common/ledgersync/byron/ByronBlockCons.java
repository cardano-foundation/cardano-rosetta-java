package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.Epoch;
import org.cardanofoundation.rosetta.common.ledgersync.byron.signature.BlockSignature;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronBlockCons {

  private Epoch slotId;
  private String pubKey;
  private BigInteger difficulty;
  private BlockSignature blockSig;
}
