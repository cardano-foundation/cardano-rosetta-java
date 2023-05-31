package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindTransactionDelegations implements FindTransactionFieldResult {

  private String address;
  private String poolHash;
  private String txHash;

}
