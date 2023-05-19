package org.cardanofoundation.rosetta.crawler.projection.dto;

import java.math.BigInteger;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class FindTransactionInOutResult implements FindTransactionFieldResult {

  protected Long id;
  protected String address;
  protected BigInteger value;
  protected byte[] policy;
  protected byte[] name;
  protected byte[] txHash;
  protected BigInteger quantity;

  public FindTransactionInOutResult(Long id, String address, BigInteger value, byte[] policy,
      byte[] name, byte[] txHash, BigInteger quantity) {
    this.id = id;
    this.address = address;
    this.value = value;
    this.policy = policy;
    this.name = name;
    this.txHash = txHash;
    this.quantity = quantity;
  }
}
