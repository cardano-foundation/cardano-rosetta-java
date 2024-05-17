package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "pool_retirement")
@IdClass(PoolRetirementId.class)
public class PoolRetirementEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private int certIndex;

  @Column(name = "pool_id")
  private String poolId;

  // TO check which epoch to use. Current or retirement epoch
  // Could be used another column to fetch the retirement epoch (retirement_epoch)

  @Column(name = "epoch")
  private Integer epoch; //current epoch

}
