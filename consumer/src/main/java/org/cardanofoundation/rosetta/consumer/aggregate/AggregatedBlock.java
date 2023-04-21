package org.cardanofoundation.rosetta.consumer.aggregate;

import com.sotatek.cardano.ledgersync.common.AuxData;
import com.sotatek.cardano.ledgersync.common.Era;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
