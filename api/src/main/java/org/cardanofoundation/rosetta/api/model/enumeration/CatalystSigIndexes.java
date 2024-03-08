package org.cardanofoundation.rosetta.api.model.enumeration;

public enum CatalystSigIndexes {
  VOTING_SIGNATURE(1);

  private final Integer value;

  CatalystSigIndexes(int value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }
}