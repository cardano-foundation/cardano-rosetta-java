package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vote_delegation")
@IdClass(DrepVoteDelegationId.class)
public class DrepVoteDelegationEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private long certIndex;

  @Column(name = "slot")
  private Long slot;

  @Column(name = "block_number")
  private Long blockNumber;

  @Column(name = "block_hash")
  private String blockHash;

  @Column(name = "address")
  private String address;

  @Column(name = "drep_hash")
  private String drepHash;

  @Column(name = "drep_id")
  private String drepId;

  @Column(name = "drep_type")
  private String drepType;

  @Column(name = "credential")
  private String credential;

}
