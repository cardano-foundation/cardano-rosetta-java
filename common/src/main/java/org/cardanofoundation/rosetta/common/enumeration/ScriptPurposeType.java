package org.cardanofoundation.rosetta.common.enumeration;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum ScriptPurposeType {
  SPEND("spend"),
  MINT("mint"),
  CERT("cert"),
  REWARD("reward");

  private static final Map<String, ScriptPurposeType> rewardTypeMap = new HashMap<>();

  static {
    for (ScriptPurposeType type : ScriptPurposeType.values()) {
      rewardTypeMap.put(type.value, type);
    }
  }

  String value;

  public static ScriptPurposeType fromValue(String value) {
    return rewardTypeMap.get(value);
  }
}