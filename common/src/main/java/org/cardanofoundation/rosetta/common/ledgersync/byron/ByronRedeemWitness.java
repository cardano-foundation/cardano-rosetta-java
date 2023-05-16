package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronRedeemWitness implements ByronTxWitnesses {

  public static final String TYPE = "ByronRedeemWitness";

  private String redeemPublicKey;
  private String redeemSignature;

  @Override
  public String getType() {
    return TYPE;
  }
}
