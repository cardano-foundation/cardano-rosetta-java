package org.cardanofoundation.rosetta.api.projection.dto;

import java.math.BigInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FindTransactionsInputs extends FindTransactionInOutResult {

  private byte[] sourceTxHash;
  private Short sourceTxIndex;

  public FindTransactionsInputs(Long id, String address, BigInteger value, byte[] txHash,
      byte[] sourceTxHash, Short sourceTxIndex, byte[] policy, byte[] name, BigInteger quantity
  ) {
    super(id, address, value, policy, name, txHash, quantity);
    this.sourceTxHash = sourceTxHash;
    this.sourceTxIndex = sourceTxIndex;
  }
}
