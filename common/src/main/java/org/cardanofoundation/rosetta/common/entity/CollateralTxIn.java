package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "collateral_tx_in")
public class CollateralTxIn extends BaseEntity {


  @NotNull
  @Column(name = "tx_in_id", nullable = false)
  private Long txInId;

  @NotNull
  @Column(name = "tx_out_id", nullable = false)
  private Long txOutId;

  @Column(name = "tx_out_index")
  private Short txOutIndex;

}