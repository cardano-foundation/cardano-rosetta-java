package org.cardanofoundation.rosetta.api.block.model.entity;

/**
 * Enum representing the different types of voters in Cardano governance
 * as stored in the voting_procedure table.
 *
 * Maps to the voter_type column in the database.
 */
public enum VoterType {

  /**
   * Constitutional Committee member voting with hot key hash
   */
  CONSTITUTIONAL_COMMITTEE_HOT_KEY_HASH,

  /**
   * Constitutional Committee member voting with hot script hash
   */
  CONSTITUTIONAL_COMMITTEE_HOT_SCRIPT_HASH,

  /**
   * Delegated Representative voting with key hash
   */
  DREP_KEY_HASH,

  /**
   * Delegated Representative voting with script hash
   */
  DREP_SCRIPT_HASH,

  /**
   * Stake Pool Operator voting with key hash (SPO voting)
   */
  STAKING_POOL_KEY_HASH

}
