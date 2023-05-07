package org.cardanofoundation.rosetta.consumer.dto;

import com.bloxbean.cardano.client.transaction.spec.RedeemerTag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemerReference<T> {
  RedeemerTag redeemerTag;
  T targetReference;
}
