package org.cardanofoundation.rosetta.api.account.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

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

  public static Utxo fromUtxoKey(UtxoKey utxoKey) {
    return new Utxo(utxoKey.getTxHash(), utxoKey.getOutputIndex());
  }

  public void populateFromUtxoEntity(AddressUtxoEntity entity) {
    this.txHash = entity.getTxHash();
    this.outputIndex = entity.getOutputIndex();
    this.ownerAddr = entity.getOwnerAddr();
    this.amounts = entity.getAmounts();
  }
}
