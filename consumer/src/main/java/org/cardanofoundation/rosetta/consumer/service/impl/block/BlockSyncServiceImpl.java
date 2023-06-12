package org.cardanofoundation.rosetta.consumer.service.impl.block;

import io.micrometer.core.annotation.Timed;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedSlotLeader;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlockSyncServiceImpl implements BlockSyncService {

  BlockRepository blockRepository;
  TxRepository txRepository;

  TransactionService transactionService;
  BlockDataService blockDataService;
  SlotLeaderService slotLeaderService;
  EpochService epochService;
  EpochParamService epochParamService;

  @Override
  @Transactional
  @Timed(value = "block.syncing.time", description = "Time taken to process the method of block syncing")
  public void startBlockSyncing() {
    if (blockDataService.getBlockSize() == 0) {
      return;
    }

    Map<String, Block> blockMap = new LinkedHashMap<>();
    var firstAndLastBlock = blockDataService.getFirstAndLastBlock();
    log.info("Commit from block {} to block {} ",
        firstAndLastBlock.getFirst().getBlockNo(),
        firstAndLastBlock.getSecond().getBlockNo());

    // Initialize block entities
    Collection<AggregatedBlock> allAggregatedBlocks = blockDataService.getAllAggregatedBlocks();
    allAggregatedBlocks.forEach(aggregatedBlock -> handleBlock(aggregatedBlock, blockMap));
    blockRepository.saveAll(blockMap.values());

    // Prepare and handle transaction contents
    Tx latestSavedTx = txRepository.findFirstByOrderByIdDesc();
    transactionService.prepareAndHandleTxs(blockMap, allAggregatedBlocks);

    // Handle epoch data
    epochService.handleEpoch(allAggregatedBlocks);

    // Handle epoch param
    epochParamService.handleEpochParams();

    // Finally, clear the aggregated data
    blockDataService.clearBatchBlockData();
  }

  private void handleBlock(AggregatedBlock aggregatedBlock, Map<String, Block> blockMap) {
    Block block = new Block();

    block.setHash(aggregatedBlock.getHash());
    block.setEpochNo(aggregatedBlock.getEpochNo());
    block.setEpochSlotNo(aggregatedBlock.getEpochSlotNo());
    block.setSlotNo(aggregatedBlock.getSlotNo());
    block.setBlockNo(aggregatedBlock.getBlockNo());

    Optional.ofNullable(blockMap.get(aggregatedBlock.getPrevBlockHash()))
        .or(() -> blockRepository.findBlockByHash(aggregatedBlock.getPrevBlockHash()))
        .ifPresentOrElse(block::setPrevious, () -> {
          log.error(
              "Prev block not found. Block number: {}, block hash: {}, prev hash: {}",
              aggregatedBlock.getBlockNo(), aggregatedBlock.getHash(),
              aggregatedBlock.getPrevBlockHash());
          throw new IllegalStateException();
        });

    AggregatedSlotLeader aggregatedSlotLeader = aggregatedBlock.getSlotLeader();
    SlotLeader slotLeader = getSlotLeader(aggregatedSlotLeader);
    block.setSlotLeader(slotLeader);

    block.setSize(aggregatedBlock.getBlockSize());
    block.setTime(aggregatedBlock.getBlockTime());
    block.setTxCount(aggregatedBlock.getTxCount());
    block.setProtoMajor(aggregatedBlock.getProtoMajor());
    block.setProtoMinor(aggregatedBlock.getProtoMinor());
    block.setVrfKey(aggregatedBlock.getVrfKey());
    block.setOpCert(aggregatedBlock.getOpCert());
    block.setOpCertCounter(aggregatedBlock.getOpCertCounter());

    blockMap.put(block.getHash(), block);
  }

  private SlotLeader getSlotLeader(AggregatedSlotLeader aggregatedSlotLeader) {
    if (Objects.isNull(aggregatedSlotLeader)) {
      // If aggregated slot leader is null, current block is Byron EB block
      return SlotLeader.builder().id(1L).build(); // hard-code for now
    }

    return slotLeaderService.getSlotLeader(
        aggregatedSlotLeader.getHashRaw(), aggregatedSlotLeader.getPrefix());
  }
}
