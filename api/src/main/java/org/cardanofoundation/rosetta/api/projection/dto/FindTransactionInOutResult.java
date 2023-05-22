package org.cardanofoundation.rosetta.api.projection.dto;

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
  protected String policy;
  protected String name;
  protected String txHash;
  protected BigInteger quantity;

  public FindTransactionInOutResult(Long id, String address, BigInteger value, String policy,
      String name, String txHash, BigInteger quantity) {
    this.id = id;
    this.address = address;
    this.value = value;
    this.policy = policy;
    this.name = name;
    this.txHash = txHash;
    this.quantity = quantity;
  }
}
