package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMetadataDto implements FindTransactionFieldResult {

  private String signature;
  private String data;
  private String txHash;

}
