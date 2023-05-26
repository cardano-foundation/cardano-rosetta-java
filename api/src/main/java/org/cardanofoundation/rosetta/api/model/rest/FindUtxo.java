package org.cardanofoundation.rosetta.api.model.rest;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindUtxo {

  private BigInteger value;
  private String txHash;
  private Integer index;
  private String policy;
  private String name;
  private BigInteger quantity;
}

