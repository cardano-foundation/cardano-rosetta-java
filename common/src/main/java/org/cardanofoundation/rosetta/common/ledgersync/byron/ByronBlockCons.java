package org.cardanofoundation.rosetta.common.ledgersync.byron;

import org.cardanofoundation.rosetta.common.ledgersync.Epoch;
import org.cardanofoundation.rosetta.common.ledgersync.byron.signature.BlockSignature;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
