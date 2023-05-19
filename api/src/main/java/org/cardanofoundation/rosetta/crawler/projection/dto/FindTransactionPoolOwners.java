package org.cardanofoundation.rosetta.crawler.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindTransactionPoolOwners implements FindTransactionFieldResult {

  private Long updateId;

  private String owner;

  private byte[] txHash;
}
