package org.cardanofoundation.rosetta.consumer.kafka;

import com.sotatek.cardano.ledgersync.common.kafka.CommonBlock;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockSyncService;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlockListener {

  private final BlockSyncService blockSyncService;
  private final CachedBlockRepository cachedBlockRepository;
  private final BlockRepository blockRepository;

  private final AtomicInteger blockCount = new AtomicInteger(0);
  private final AtomicLong lastMessageReceivedTime = new AtomicLong(System.currentTimeMillis());
  private AtomicLong blockHeight;

  @Value("${blocks.batch-size}")
  private Integer batchSize;

  @Value("${blocks.commitThreshold}")
  private Long commitThreshold;

  private long lastLog;

  @PostConstruct
  private void initBlockHeight() {
    long blockNo = blockRepository.getBlockHeight().orElse(0L);
    blockHeight = new AtomicLong(blockNo);
    log.info("Block height {}", blockNo);
  }

  /**
   * Consume listener block
   *
   * @param consumerRecord message topic
   */
  @KafkaListener(
      topics = "${kafka.listeners.block.topics}"
  )
  public void consume(ConsumerRecord<String, CommonBlock> consumerRecord,
      Acknowledgment acknowledgment) {
    try {
      long currentTime = System.currentTimeMillis();
      long lastReceivedTimeElapsed = currentTime - lastMessageReceivedTime.getAndSet(currentTime);
      var eraBlock = consumerRecord.value();
      if(currentTime - lastLog >= 500){//reduce log
        log.info("Block  number {}, slot_no {}, hash {}",
            eraBlock.getBlockNumber(), eraBlock.getSlot(), eraBlock.getBlockHash());
        lastLog = currentTime;
      }
      if (!eraBlock.isRollback()) {
        if(eraBlock.getBlockNumber() == 0){//EBB or genesis block
          boolean isExists = cachedBlockRepository.existsBlockByHash(eraBlock.getBlockHash());
          if (isExists) {
            log.warn("Skip existed block : number {}, slot_no {}, hash {}",
                eraBlock.getBlockNumber(),
                eraBlock.getSlot(), eraBlock.getBlockHash());
            acknowledgment.acknowledge();
            return;
          }
        }
        else if (eraBlock.getBlockNumber() <= blockHeight.get()) {
          log.warn("Skip block {}, hash {}, slot {} smaller than current block height {}",
              eraBlock.getBlockNumber(), eraBlock.getBlockHash(), eraBlock.getSlot(),
              blockHeight.get());
          acknowledgment.acknowledge();
          return;
        }
      }

//      if (eraBlock.isRollback()) {
//        if(Boolean.TRUE.equals(cachedBlockRepository.existsBlockByHash(eraBlock.getBlockHash()))){
//          //Skip this block as It can be safe block that crawler use to fetch when get rollback message
//          log.warn("Skip rollback block no {}, hash {}",eraBlock.getBlockNumber(),eraBlock.getBlockHash());
//          return;
//        }
//        else{// The real block that need to rollback
//          blockSyncService.startBlockSyncing();
//          rollbackService.rollBackFrom(eraBlock.getBlockNumber());
//          blockCount.set(0);
//          acknowledgment.acknowledge();
//        }
//      }

      if(eraBlock.getBlockNumber() != 0){// skip block height with ebb or genesis block
        blockHeight.set(eraBlock.getBlockNumber());
      }

//      AggregatedBlock aggregatedBlock = null;
//      blockDataService.saveAggregatedBlock(aggregatedBlock);

      int currentBlockCount = blockCount.incrementAndGet();
      if ((currentBlockCount % batchSize == 0) ||
          (lastReceivedTimeElapsed >= commitThreshold)) {
        blockSyncService.startBlockSyncing();
        acknowledgment.acknowledge();
        blockCount.set(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
      System.exit(1);
    }
  }
}
