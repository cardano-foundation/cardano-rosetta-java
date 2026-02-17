package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "delegation")
@IdClass(PoolDelegationId.class)
public class PoolDelegationEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private long certIndex;

  @Column(name = "pool_id")
  private String poolId;

  @Column(name = "address")
  private String address;

}
