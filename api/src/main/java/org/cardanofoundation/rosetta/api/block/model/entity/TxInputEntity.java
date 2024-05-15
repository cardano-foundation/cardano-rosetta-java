package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.DynamicUpdate;

import org.cardanofoundation.rosetta.api.account.model.entity.UtxoId;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tx_input")
@IdClass(UtxoId.class)
@DynamicUpdate
public class TxInputEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;
  @Id
  @Column(name = "output_index")
  private Integer outputIndex;
}
