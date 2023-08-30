package org.cardanofoundation.rosetta.consumer.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@Profile("test-integration")
@ActiveProfiles("test-integration")
public class KafkaProducer {

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  public void send(String topic, CommonBlock payload) {
    CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic,
            UUID.randomUUID().toString(), payload);
    future.whenComplete((result, ex) -> {
      if (ex != null) {
        log.error(ex.getMessage());
      } else {
        log.info("send:{}", result.getProducerRecord().value());
      }
    });
  }
}
