package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * CoinActions are different state changes that a Coin can undergo. When a Coin is created, it is coin_created. When a Coin is spent, it is coin_spent. It is assumed that a single Coin cannot be created or spent more than once.
 */

public enum CoinAction {
  
  CREATED("coin_created"),
  
  SPENT("coin_spent");

  private String value;

  CoinAction(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static CoinAction fromValue(String value) {
    for (CoinAction b : CoinAction.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

