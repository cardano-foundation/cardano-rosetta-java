package org.cardanofoundation.rosetta.common.enumeration;

public enum StakeAddressPrefix {
  MAIN("stake"),
  TEST("stake_test");

  private final String prefix;

  StakeAddressPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getPrefix() {
    return prefix;
  }
}
