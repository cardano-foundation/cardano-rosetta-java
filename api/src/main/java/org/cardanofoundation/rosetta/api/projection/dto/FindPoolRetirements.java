package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindPoolRetirements implements FindTransactionFieldResult {

  private Integer epoch;
  private String address;
  private String txHash;
  private String poolHashKey;

  public FindPoolRetirements(Integer epoch, String address, String txHash) {
    this.epoch = epoch;
    this.address = address;
    this.txHash = txHash;
  }
}
