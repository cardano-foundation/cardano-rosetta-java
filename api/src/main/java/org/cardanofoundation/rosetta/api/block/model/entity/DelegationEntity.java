package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "delegation")
@IdClass(DelegationId.class)
public class DelegationEntity extends BlockAwareEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private long certIndex;

  @Column(name = "credential")
  private String credential;

  @Column(name = "pool_id")
  private String poolId;

  @Column(name = "address")
  private String address;

  @Column(name = "epoch")
  private Integer epoch;

  @Column(name = "slot")
  private Long slot;

  @Column(name = "block_hash")
  private String blockHash;
}
