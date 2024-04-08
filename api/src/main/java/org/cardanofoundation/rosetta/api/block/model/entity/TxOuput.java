package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;

//Not used
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxOuput implements Serializable {

  private String address;

  private List<Amt> amounts;
  private String dataHash;
  private String inlineDatum;
  private String referenceScriptHash;
}
