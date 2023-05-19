package org.cardanofoundation.rosetta.api.common.enumeration;

public enum CatalystDataIndexes {
  VOTING_KEY(1),
  STAKE_KEY(2),
  REWARD_ADDRESS(3),
  VOTING_NONCE(4);

  private final Integer value;

  CatalystDataIndexes(int value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }
}