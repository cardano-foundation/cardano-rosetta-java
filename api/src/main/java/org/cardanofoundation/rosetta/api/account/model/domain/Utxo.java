package org.cardanofoundation.rosetta.api.account.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Utxo {

  private String txHash;
  private Integer outputIndex;
  private String ownerAddr;

  @NotNull
  @Builder.Default
  private List<Amt> amounts = new ArrayList<>();

  public Utxo(String txHash, Integer outputIndex) {
    this.txHash = txHash;
    this.outputIndex = outputIndex;
  }

}
