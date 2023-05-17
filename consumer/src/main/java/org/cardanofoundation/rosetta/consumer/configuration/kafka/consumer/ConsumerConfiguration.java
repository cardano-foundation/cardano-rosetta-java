package org.cardanofoundation.rosetta.consumer.configuration.kafka.consumer;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.cardanofoundation.rosetta.consumer.configuration.properties.KafkaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Profile("!test-integration")
public class ConsumerConfiguration {

  private static final String JSON_CONSUMER = "json-consumer";
  private static final int CONCURRENCY_NUMBER = 6;
  private static final int POLL_TIMEOUT = 3000;
  KafkaProperties kafkaProperties;

  @Autowired
  public ConsumerConfiguration(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Bean
  @SneakyThrows
  public Map<String, Object> consumerConfigs() {

    var configs = kafkaProperties.getConsumers().get(JSON_CONSUMER);
    var props = new HashMap<String, Object>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configs.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, configs.getGroupId());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(configs.getKeyDeserializer()));
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(configs.getValueDeserializer()));
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configs.getAutoOffsetReset());
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configs.getEnableAutoCommit());
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, configs.getPollTimeout());
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, configs.getSessionTimeoutMs());
    props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, configs.getAllowAutoCreateTopics());
    props.put(JsonDeserializer.TRUSTED_PACKAGES, configs.getTrustedPackages());
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CommonBlock.class);
    return props;
  }

  @Bean
  @Primary
  public ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(CONCURRENCY_NUMBER);
    factory.getContainerProperties().setPollTimeout(POLL_TIMEOUT);
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }
}