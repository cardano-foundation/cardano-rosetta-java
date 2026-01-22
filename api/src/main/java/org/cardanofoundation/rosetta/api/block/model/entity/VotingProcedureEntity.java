package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a voting procedure record from the voting_procedure table.
 *
 * This entity stores governance voting procedures for all voter types:
 * - Constitutional Committee members
 * - DReps (Delegated Representatives)
 * - SPOs (Stake Pool Operators)
 *
 * For SPO voting specifically, filter by voterType = STAKING_POOL_KEY_HASH
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "voting_procedure")
@IdClass(VotingProcedureId.class)
public class VotingProcedureEntity {

  /**
   * Unique identifier for this voting procedure record
   */
  @NotNull
  @Column(name = "id", nullable = false)
  private UUID id;

  /**
   * Transaction hash containing this vote (part of composite key)
   */
  @NotNull
  @jakarta.persistence.Id
  @Column(name = "tx_hash", nullable = false, length = 64)
  private String txHash;

  /**
   * Hash of the voter's credential (part of composite key)
   */
  @NotNull
  @jakarta.persistence.Id
  @Column(name = "voter_hash", nullable = false, length = 56)
  private String voterHash;

  /**
   * Type of voter (part of composite key)
   */
  @NotNull
  @jakarta.persistence.Id
  @Column(name = "voter_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private VoterType voterType;

  /**
   * Transaction hash of the governance action being voted on (part of composite key)
   */
  @NotNull
  @jakarta.persistence.Id
  @Column(name = "gov_action_tx_hash", nullable = false, length = 64)
  private String govActionTxHash;

  /**
   * Index of the governance action within its transaction (part of composite key)
   */
  @NotNull
  @jakarta.persistence.Id
  @Column(name = "gov_action_index", nullable = false)
  private Integer govActionIndex;

  /**
   * Index of this voting procedure within the transaction
   */
  @NotNull
  @Column(name = "idx", nullable = false)
  private Integer idx;

  /**
   * Transaction index
   */
  @NotNull
  @Column(name = "tx_index", nullable = false)
  private Integer txIndex;

  /**
   * The vote cast: YES, NO, or ABSTAIN
   */
  @NotNull
  @Column(name = "vote", nullable = false, length = 10)
  @Enumerated(EnumType.STRING)
  private Vote vote;

  /**
   * URL of the vote rationale anchor (optional metadata)
   */
  @Nullable
  @Column(name = "anchor_url")
  private String anchorUrl;

  /**
   * Hash of the vote rationale anchor data (optional metadata)
   */
  @Nullable
  @Column(name = "anchor_hash", length = 64)
  private String anchorHash;

  /**
   * Epoch in which this vote was cast
   */
  @NotNull
  @Column(name = "epoch")
  private Integer epoch;

  /**
   * Slot number in which this vote was cast
   */
  @NotNull
  @Column(name = "slot")
  private Long slot;

  /**
   * Block number in which this vote was included
   */
  @NotNull
  @Column(name = "block")
  private Long blockNumber;

  /**
   * Block timestamp
   */
  @NotNull
  @Column(name = "block_time")
  private Long blockTime;

  /**
   * Last update timestamp for this record
   */
  @NotNull
  @Column(name = "update_datetime")
  private LocalDateTime updateDateTime;

}
