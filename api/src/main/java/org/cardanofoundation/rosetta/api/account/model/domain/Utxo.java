package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Utxo {

  private String txHash;
  private Integer outputIndex;
  private String ownerAddr;
  private List<Amt> amounts;

  public Utxo(String txHash, Integer outputIndex) {
    this.txHash = txHash;
    this.outputIndex = outputIndex;
  }
}
