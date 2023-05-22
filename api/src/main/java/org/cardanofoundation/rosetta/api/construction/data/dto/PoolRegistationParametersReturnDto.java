package org.cardanofoundation.rosetta.api.construction.data.dto;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
