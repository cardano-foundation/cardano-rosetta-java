package org.cardanofoundation.rosetta.consumer.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatedSlotLeader {

  String hashRaw;
  String prefix;
}
