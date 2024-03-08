package org.cardanofoundation.rosetta.api.model.enumeration;

public enum OperationTypeStatus {
  SUCCESS("success"),
  INVALID("invalid");
  private final String value;

  OperationTypeStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
