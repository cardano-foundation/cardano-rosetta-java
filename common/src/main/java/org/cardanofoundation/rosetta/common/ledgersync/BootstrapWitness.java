package org.cardanofoundation.rosetta.common.ledgersync;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BootstrapWitness {

  private String publicKey;
  private String signature;
  private String chainCode;
  private String attributes;
}
