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
  private Long slot;
  private String blockHash;
  private Integer epoch;
  private String ownerAddr;
  private String ownerAddrFull;
  private String ownerStakeAddr;
  private String ownerPaymentCredential;
  private String ownerStakeCredential;
  private BigInteger lovelaceAmount;
  private List<Amt> amounts;
  private String dataHash;
  private String inlineDatum;
  private String scriptRef;
  private String referenceScriptHash;
  private Boolean isCollateralReturn;

  public Utxo(String txHash, Integer outputIndex) {
    this.txHash = txHash;
    this.outputIndex = outputIndex;
  }
}
