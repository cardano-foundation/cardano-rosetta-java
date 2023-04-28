package org.cardanofoundation.rosetta.consumer.kafka;

import com.sotatek.cardano.ledgersync.common.kafka.CommonBlock;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlockListener {

  private final BlockRepository blockRepository;

  @PostConstruct
  private void initBlockHeight() {
    long blockNo = blockRepository.getBlockHeight().orElse(0L);
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
      var eraBlock = consumerRecord.value();
      log.info("BlockHash: " + eraBlock.getBlockHash());
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
      System.exit(1);
    }
  }
}
