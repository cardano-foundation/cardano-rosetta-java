package org.cardanofoundation.rosetta.consumer.kafka;

import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
@Profile("test-integration")
public class TestBlockListener {

    private CountDownLatch latch = new CountDownLatch(5);

    @Autowired
    private BlockListener blockListener;

    @KafkaListener(topics = "${test.topic1}", groupId = "${spring.kafka.consumer.group-id}",
            topicPartitions = {
                    @TopicPartition(topic = "${test.topic1}", partitions = "0")
            })
    public void receive(ConsumerRecord<String, CommonBlock> consumerRecord) {
        latch.countDown();
        blockListener.consume(consumerRecord, null);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setCountdown(int countdown) {
        latch = new CountDownLatch(countdown);
    }
}