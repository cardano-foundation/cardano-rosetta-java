package org.cardanofoundation.rosetta.consumer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemerPointer<T> {
  String scriptHash;
  T targetReference;
}
