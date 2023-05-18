package org.cardanofoundation.rosetta.consumer.service.impl.block;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedSlotLeader;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.BlockSyncService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import org.cardanofoundation.rosetta.consumer.service.TransactionService;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlockSyncServiceImpl implements BlockSyncService {

  CachedBlockRepository cachedBlockRepository;

  TransactionService transactionService;
  BlockDataService blockDataService;
  SlotLeaderService slotLeaderService;
  EpochService epochService;
  EpochParamService epochParamService;

  @Override
  public void startBlockSyncing() {
    if(blockDataService.getBlockSize() == 0){
      return;
    }
    Map<byte[], Tx> txMap = new ConcurrentHashMap<>();
    var firstAndLastBlock = blockDataService.getFirstAndLastBlock();
    log.info("Commit from block {} to block {} ",
        firstAndLastBlock.getFirst().getBlockNo(),
        firstAndLastBlock.getSecond().getBlockNo());
    /*
     * Blocks along with their txs and some of tx contents must be processed sequentially,
     * hence this code block must run first. This is also used to initialize blocks and tx
     * entities
     */
    blockDataService.forEachAggregatedBlock(aggregatedBlock -> {
      Block block = handleBlock(aggregatedBlock);
      txMap.putAll(transactionService.prepareTxs(block, aggregatedBlock));
    });

    // Handle all tx contents
    transactionService.handleTxs(txMap);


    // Handle epoch param

    epochParamService.handleEpochParams();

    // Finally, flush everything into db
    blockDataService.saveAll();
  }

  private Block handleBlock(AggregatedBlock aggregatedBlock) {
    Block block = new Block();

    block.setHash(aggregatedBlock.getHash());
    block.setEpochNo(aggregatedBlock.getEpochNo());
    block.setEpochSlotNo(aggregatedBlock.getEpochSlotNo());
    block.setSlotNo(aggregatedBlock.getSlotNo());
    block.setBlockNo(aggregatedBlock.getBlockNo());

    cachedBlockRepository.findBlockByHash(aggregatedBlock.getPrevBlockHash())
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

    cachedBlockRepository.save(block);
    epochService.handleEpoch(aggregatedBlock);
    return block;
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
