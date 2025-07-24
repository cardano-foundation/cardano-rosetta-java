package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tx_input")
@IdClass(UtxoId.class)
public class TxInputEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;
  @Id
  @Column(name = "output_index")
  private Integer outputIndex;

  @Column(name = "spent_at_block")
  private Long spentAtBlock;

  @Column(name = "spent_tx_hash")
  private String spentTxHash;

}
