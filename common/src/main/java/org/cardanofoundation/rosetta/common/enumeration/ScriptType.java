package org.cardanofoundation.rosetta.common.enumeration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum ScriptType {
  MULTISIG("multisig"),
  TIMELOCK("timelock"),
  PLUTUSV1("plutusv1"),
  PLUTUSV2("plutusv2");

  String value;

  private static final Map<String, ScriptType> rewardTypeMap = new HashMap<>();

  static {
    for (ScriptType type : ScriptType.values()) {
      rewardTypeMap.put(type.value, type);
    }
  }

  public static ScriptType fromValue(String value) {
    return rewardTypeMap.get(value);
  }
}