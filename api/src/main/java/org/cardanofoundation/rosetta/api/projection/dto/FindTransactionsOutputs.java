package org.cardanofoundation.rosetta.api.projection.dto;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FindTransactionsOutputs extends FindTransactionInOutResult {

  private Short index;

  public FindTransactionsOutputs(Long id, String address, BigInteger value, String policy,
      String name, String txHash, BigInteger quantity, Short index) {
    super(id, address, value, policy, name, txHash, quantity);
    this.index = index;
  }
}
