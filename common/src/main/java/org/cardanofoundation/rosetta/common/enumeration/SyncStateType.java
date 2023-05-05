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
public enum SyncStateType {
  LAGGING("lagging"),
  FOLLOWING("following");

  private static final Map<String, SyncStateType> rewardTypeMap = new HashMap<>();

  static {
    for (SyncStateType type : SyncStateType.values()) {
      rewardTypeMap.put(type.value, type);
    }
  }

  String value;

  public static SyncStateType fromValue(String value) {
    return rewardTypeMap.get(value);
  }
}