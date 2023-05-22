package org.cardanofoundation.rosetta.api.common.enumeration;

public enum CatalystLabels {
  DATA("61284"),
  SIG("61285");

  private final String label;

  CatalystLabels(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}