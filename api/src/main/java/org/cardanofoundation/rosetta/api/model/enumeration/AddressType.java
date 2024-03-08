package org.cardanofoundation.rosetta.api.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AddressType { //TODO EPAM: for the package ...enumeration: maybe rosetta openapi has this data models?
  ENTERPRISE("Enterprise"),
  BASE("Base"),
  REWARD("Reward"),
  POOL_KEY_HASH("Pool_Hash");

  private final String value;

  AddressType(String value) {
    this.value = value;
  }

  public static AddressType findByValue(String value) {
    for (AddressType a : AddressType.values()) {
      if (a.getValue().equals(value)) {
        return a;
      }
    }
    return null;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
