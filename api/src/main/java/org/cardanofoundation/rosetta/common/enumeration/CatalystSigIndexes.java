package org.cardanofoundation.rosetta.common.enumeration;

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