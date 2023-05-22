//package org.cardanofoundation.rosetta.consumer.mock;
//
//import org.cardanofoundation.rosetta.common.ledgersync.*;
//import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
//import org.cardanofoundation.rosetta.consumer.kafka.KafkaConsumer;
//import org.cardanofoundation.rosetta.consumer.kafka.KafkaProducer;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.context.junit.jupiter.EnabledIf;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//
//import java.util.concurrent.TimeUnit;
//
//@Profile("test-integration")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@EmbeddedKafka(partitions = 1,
//    brokerProperties = { "listeners=PLAINTEXT://localhost:39999", "port=39999" })
//@EnabledIf(value = "#{environment['spring.profiles.active'] == 'test-integration'}", loadContext = true)
//class EmbeddedKafkaIntegrationTest {
//  @Autowired
//  private KafkaConsumer consumer;
//
//  @Autowired
//  private KafkaProducer producer;
//
//  @Value("${test.topic}")
//  private String topic;
//
//  @Test
//   void givenEmbeddedKafkaBroker_whenSendingWithSimpleProducer_thenMessageReceived()
//      throws Exception {
//    CommonBlock data = Block.builder()
//        .header(BlockHeader.builder()
//            .headerBody(HeaderBody.builder()
//                .blockBodyHash("2")
//                .prevHash("2")
//                .blockNumber(2)
//                .blockBodySize(2)
//                .slotId(Epoch.builder()
//                    .slotId(0)
//                    .slotOfEpoch(1)
//                    .value(2)
//                    .build())
//                .build())
//            .build())
//        .build();
//    data.setEraType(Era.ALONZO);
//    producer.send(topic, data);
//
//    boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
//    Assertions.assertTrue(messageConsumed);
//    Assertions.assertEquals(consumer.getPayload().toString(), data.toString());
//  }
//}