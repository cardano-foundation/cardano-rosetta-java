package org.cardanofoundation.rosetta.api.account.model.entity;

import java.io.Serializable;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class UtxoId implements Serializable {

  @Column(name = "tx_hash")
  private String txHash;

  @Column(name = "output_index")
  private Integer outputIndex;
}
