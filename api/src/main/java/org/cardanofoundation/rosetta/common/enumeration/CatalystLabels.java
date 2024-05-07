package org.cardanofoundation.rosetta.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystLabels {
  DATA("61284"),
  SIG("61285");

  private final String label;

  CatalystLabels(String value) {
    this.label = value;
  }

  public static CatalystLabels findByValue(String label) {
    for (CatalystLabels a : CatalystLabels.values()) {
      if (a.getLabel().equals(label)) {
        return a;
      }
    }
    return null;
  }

  @JsonValue
  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
