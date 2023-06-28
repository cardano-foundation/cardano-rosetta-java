package org.cardanofoundation.rosetta.consumer.dto.cache;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LatestTxObject {

  String txHash;
  Long blockNo;
  Integer epochNo;
  Long slotNo;
  Set<String> fromAddresses;
  Set<String> toAddresses;
  String timestamp;
}
