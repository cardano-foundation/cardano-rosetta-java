//package org.cardanofoundation.rosetta.consumer;
//
//import org.cardanofoundation.rosetta.consumer.kafka.KafkaConsumer;
//import org.cardanofoundation.rosetta.consumer.kafka.KafkaProducer;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.TestPropertySource;
//
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//
//@DirtiesContext
//@TestPropertySource("classpath:application.yaml")
//@SpringBootTest(classes = CardanoRosettaConsumerApplication.class)
////@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:29091", "port=29091" })
//class EmbeddedKafkaIntegrationTest {
//
//    @Autowired
//    private KafkaConsumer consumer;
//
//    @Autowired
//    private KafkaProducer producer;
//
//    @Value("${test.topic}")
//    private String topic;
//
//    @Test
//    public void givenEmbeddedKafkaBroker_whenSendingWithSimpleProducer_thenMessageReceived(){
//        try {
//            String data = "Sending with our own simple KafkaProducer";
//
//            producer.send(topic, data);
//
//            boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
//            assertTrue(messageConsumed);
//        }catch (Exception ex){
//            System.out.println(ex);
//        }
//
//    }
//}