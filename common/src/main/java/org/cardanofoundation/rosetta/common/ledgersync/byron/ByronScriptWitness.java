package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronScriptWitness implements ByronTxWitnesses {

  public static final String TYPE = "ByronScriptWitness";

  private ByronScript validator;
  private ByronScript redeemer;

  @Override
  public String getType() {
    return null;
  }
}
