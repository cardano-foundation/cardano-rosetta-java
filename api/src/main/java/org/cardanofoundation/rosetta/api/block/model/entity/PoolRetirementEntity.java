package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
