package org.cardanofoundation.rosetta.crawler.projection.dto;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindTransactionWithdrawals implements FindTransactionFieldResult {

  private String address;
  private BigInteger amount;
  private byte[] txHash;
}
