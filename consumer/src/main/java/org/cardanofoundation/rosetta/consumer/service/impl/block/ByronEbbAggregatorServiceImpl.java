package org.cardanofoundation.rosetta.consumer.service.impl.block;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronEbBlock;
import org.cardanofoundation.rosetta.consumer.service.BlockAggregatorService;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ByronEbbAggregatorServiceImpl extends BlockAggregatorService<ByronEbBlock> {

  public ByronEbbAggregatorServiceImpl(
      SlotLeaderService slotLeaderService,
      BlockDataService blockDataService) {
    super(slotLeaderService, blockDataService);
  }

  @Override
  public AggregatedBlock aggregateBlock(ByronEbBlock blockCddl) {
    var blockHash = blockCddl.getBlockHash();
    var consensusData = blockCddl.getHeader().getConsensusData();
    int epochNo = (int) consensusData.getEpochId();
    int blockSize = blockCddl.getCborSize();
    var blockTime = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
        blockCddl.getBlockTime(), 0, ZoneOffset.ofHours(0)));
    var prevBlockHash = blockCddl.getHeader().getPrevBlock();

    return AggregatedBlock.builder()
        .era(blockCddl.getEraType())
        .network(blockCddl.getNetwork())
        .hash(blockHash)
        .epochNo(epochNo)
        .prevBlockHash(prevBlockHash)
        .blockSize(blockSize)
        .blockTime(blockTime)
        .protoMajor(0)
        .protoMinor(0)
        .txList(Collections.emptyList())
        .auxiliaryDataMap(Collections.emptyMap())
        .build();
  }
}
