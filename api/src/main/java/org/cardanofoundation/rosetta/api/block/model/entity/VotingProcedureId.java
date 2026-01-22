package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for VotingProcedureEntity.
 *
 * Corresponds to the primary key in the voting_procedure table:
 * (tx_hash, voter_hash, voter_type, gov_action_tx_hash, gov_action_index)
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VotingProcedureId implements Serializable {

  @Column(name = "tx_hash")
  private String txHash;

  @Column(name = "voter_hash")
  private String voterHash;

  @Column(name = "voter_type")
  @Enumerated(EnumType.STRING)
  private VoterType voterType;

  @Column(name = "gov_action_tx_hash")
  private String govActionTxHash;

  @Column(name = "gov_action_index")
  private Integer govActionIndex;

}
