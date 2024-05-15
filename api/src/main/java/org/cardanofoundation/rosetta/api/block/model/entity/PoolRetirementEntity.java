package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "pool_retirement")
@IdClass(PoolRetirementId.class)
@DynamicUpdate
public class PoolRetirementEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private int certIndex;

  @Column(name = "pool_id")
  private String poolId;

  // TODO check which epoch to use. Current or retirement epoch
//  @Column(name = "retirement_epoch")
//  private int retirementEpoch;

  @Column(name = "epoch")
  private Integer epoch; //current epoch

}
