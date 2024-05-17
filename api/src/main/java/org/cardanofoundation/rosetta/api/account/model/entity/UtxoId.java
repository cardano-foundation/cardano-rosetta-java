package org.cardanofoundation.rosetta.api.account.model.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UtxoId implements Serializable {

  private String txHash;
  private Integer outputIndex;
}
