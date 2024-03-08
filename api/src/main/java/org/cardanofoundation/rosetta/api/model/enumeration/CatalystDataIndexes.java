package org.cardanofoundation.rosetta.api.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

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
      // TODO EPAM: Null-safe equals is always a better choice, Objects have to be compared with equals method.
      // TODO EPAM: Even though all the values will be in a LongCache and == should work, it's better to use equals.
      if (Objects.equals(a.getValue(), value)) {
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
