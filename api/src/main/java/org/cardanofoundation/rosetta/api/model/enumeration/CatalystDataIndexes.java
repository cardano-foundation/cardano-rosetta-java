package org.cardanofoundation.rosetta.api.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystDataIndexes {
  VOTING_KEY(1L),
  STAKE_KEY(2L),
  REWARD_ADDRESS(3L),
  VOTING_NONCE(4L);

  private final Long value;

  CatalystDataIndexes(Long value) {
    this.value = value;
  }

  public static CatalystDataIndexes findByValue(Long value) {
    for (CatalystDataIndexes a : CatalystDataIndexes.values()) {
      if (a.getValue() == value) {
        return a;
      }
    }
    return null;
  }

  @JsonValue
  public Long getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
