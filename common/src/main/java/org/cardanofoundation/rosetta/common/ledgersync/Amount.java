package org.cardanofoundation.rosetta.common.ledgersync;

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
@Builder(toBuilder = true)

public class Amount {

  private String unit;
  private String policyId;
  private byte[] assetName;
  private BigInteger quantity;
}
