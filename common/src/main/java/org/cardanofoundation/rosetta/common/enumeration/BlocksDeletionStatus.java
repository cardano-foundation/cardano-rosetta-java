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
public enum BlocksDeletionStatus {
  DELETED("deleted"),
  PENDING("pending");

  String value;

  private static final Map<String, BlocksDeletionStatus> blocksDeletionStatusMap = new HashMap<>();

  static {
    for (BlocksDeletionStatus type : BlocksDeletionStatus.values()) {
      blocksDeletionStatusMap.put(type.value, type);
    }
  }

  public static BlocksDeletionStatus fromValue(String value) {
    return blocksDeletionStatusMap.get(value);
  }

}