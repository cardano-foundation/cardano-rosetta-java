package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum CatalystLabels {
  DATA("61284"),
  SIG("61285");

  private final String label;

  CatalystLabels(String value) {
    this.label = value;
  }

  public static CatalystLabels findByValue(String label) {
    for (CatalystLabels a : CatalystLabels.values()) {
      // TODO EPAM: Equals should be used here.
      if (Objects.equals(a.getLabel(), label)) {
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
    return String.valueOf(label);
  }
}
