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
public enum RewardType {
  LEADER("leader"),
  MEMBER("member"),
  RESERVES("reserves"),
  TREASURY("treasury"),
  REFUND("refund");

  String value;

  private static final Map<String, RewardType> rewardTypeMap = new HashMap<>();

  static {
    for (RewardType type : RewardType.values()) {
      rewardTypeMap.put(type.value, type);
    }
  }

  public static RewardType fromValue(String value) {
    return rewardTypeMap.get(value);
  }

}