package org.cardanofoundation.rosetta.common.ledgersync.byron.signature;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Delegation {
  protected String issuer;
  protected String delegate;
  protected String certificate;
}
