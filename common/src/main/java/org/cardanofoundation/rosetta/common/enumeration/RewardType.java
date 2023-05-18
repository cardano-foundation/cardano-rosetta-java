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
public enum RewardType {
  LEADER("leader"),
  MEMBER("member"),
  RESERVES("reserves"),
  TREASURY("treasury"),
  REFUND("refund");

  private static final Map<String, RewardType> rewardTypeMap = new HashMap<>();

  static {
    for (RewardType type : RewardType.values()) {
      rewardTypeMap.put(type.value, type);
    }
  }

  String value;

  public static RewardType fromValue(String value) {
    return rewardTypeMap.get(value);
  }

}