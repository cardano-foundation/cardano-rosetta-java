package org.cardanofoundation.rosetta.common.model.cardano.pool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PoolRegistationParametersReturnDto {

  private BigInteger cost;
  private BigInteger pledge;
  private BigInteger numerator;
  private BigInteger denominator;


}
