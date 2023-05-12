package org.cardanofoundation.rosetta.consumer.dto;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatumDTO {
  Witnesses transactionWitness;
  AggregatedTx transactionBody;
  Tx tx;
  Map<String, Datum> datums;
}
