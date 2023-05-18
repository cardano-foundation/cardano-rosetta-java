package org.cardanofoundation.rosetta.common.ledgersync.byron.signature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class CommonSignature extends Delegation implements  BlockSignature{
  protected String signature;
}
