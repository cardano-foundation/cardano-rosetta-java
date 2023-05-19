package org.cardanofoundation.rosetta.api.projection.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindTransactionMetadata implements FindTransactionFieldResult {

  private Object data;
  private Object signature;
  private byte[] txHash;
}
