package org.cardanofoundation.rosetta.crawler.projection.dto;

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
  private byte[] address;
  private byte[] txHash;
  private byte[] poolHashKey;

  public FindPoolRetirements(Integer epoch, byte[] address, byte[] txHash) {
    this.epoch = epoch;
    this.address = address;
    this.txHash = txHash;
  }
}
