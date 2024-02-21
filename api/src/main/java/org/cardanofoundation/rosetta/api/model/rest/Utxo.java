package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Utxo {

  private String value;
  private String transactionHash;
  private Integer index;
  private String name;
  private String policy;
  private String quantity;

}