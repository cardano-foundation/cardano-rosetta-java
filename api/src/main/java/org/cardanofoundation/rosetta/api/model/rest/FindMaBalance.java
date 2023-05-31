package org.cardanofoundation.rosetta.api.model.rest;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindMaBalance {

  private String name;
  private String policy;
  private BigInteger value;
}
