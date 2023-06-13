package org.cardanofoundation.rosetta.consumer.aggregate;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.rosetta.common.ledgersync.AuxData;
import org.cardanofoundation.rosetta.common.ledgersync.Era;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatedBlock {

  Era era;
  int network;
  String hash;
  Integer epochNo;
  Integer epochSlotNo;
  Long slotNo;
  Long blockNo;
  String prevBlockHash;
  AggregatedSlotLeader slotLeader;
  int blockSize;
  Timestamp blockTime;
  Long txCount;
  int protoMajor;
  int protoMinor;
  String vrfKey;
  String opCert;
  Long opCertCounter;
  List<AggregatedTx> txList;
  Map<Integer, AuxData> auxiliaryDataMap; // Key is tx index in block
}
