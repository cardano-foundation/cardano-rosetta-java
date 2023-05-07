package org.cardanofoundation.rosetta.consumer.service.impl.block;

import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.RollbackHistory;
import org.cardanofoundation.rosetta.common.enumeration.BlocksDeletionStatus;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.RollbackHistoryRepository;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.RollbackService;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackServiceImpl implements RollbackService {

  BlockRepository blockRepository;
  RollbackHistoryRepository rollbackHistoryRepository;

  EpochService epochService;


  @Override
  @Transactional
  public void rollBackFrom(long blockNo) {
    long startTime = System.currentTimeMillis();
    log.warn("Roll back from block no {}", blockNo);
    Optional<Block> blockRollBack = blockRepository.findBlockByBlockNo(blockNo);
    if (blockRollBack.isEmpty()) {
      log.warn("Block {} for roll back not found", blockNo);
      return;
    }

    startRollback(blockRollBack.get());

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.info("Rollback block running in {} ms, {} second", totalTime, totalTime / 1000f);
  }

  private void startRollback(Block rollbackBlock) {
    var blocksForRollback = blockRepository
        .findAllByBlockNoGreaterThanOrderByBlockNoDesc(rollbackBlock.getBlockNo());
    blocksForRollback.add(rollbackBlock);
    var lastRollbackBlock = blocksForRollback.get(0);

    // Rollback epoch stats
    epochService.rollbackEpochStats(blocksForRollback);

    // Add to rollback history. Reason field can be used later, for now let it be null
    var rollbackHistory = buildRollbackHistory(rollbackBlock, lastRollbackBlock, null);
    rollbackHistoryRepository.save(rollbackHistory);

    log.warn("Roll back to block no {}", lastRollbackBlock.getBlockNo());
    blockRepository.deleteAll(blocksForRollback);
  }

  private RollbackHistory buildRollbackHistory(Block startBlock, Block endBlock, String reason) {
    return RollbackHistory.builder()
        .blockNoStart(startBlock.getBlockNo())
        .blockSlotStart(startBlock.getSlotNo())
        .blockHashStart(startBlock.getHash())
        .blockNoEnd(endBlock.getBlockNo())
        .blockSlotEnd(endBlock.getSlotNo())
        .blockHashEnd(endBlock.getHash())
        .reason(reason)
        .blocksDeletionStatus(BlocksDeletionStatus.PENDING)
        .build();
  }
}
