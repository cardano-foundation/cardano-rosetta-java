package org.cardanofoundation.rosetta.consumer.configuration.kafka;


import org.cardanofoundation.rosetta.consumer.configuration.properties.KafkaProperties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@Slf4j
@EnableKafka
public class KafkaConfiguration {

  private final KafkaProperties kafkaProperties;

  @Autowired
  public KafkaConfiguration(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Bean
  public KafkaAdmin kafkaAdmin() {
    var configs = kafkaAdminConfigs(kafkaProperties);

    var kafkaAdmin = new KafkaAdmin(configs);

    if (Boolean.TRUE.equals(kafkaProperties.getAutoCreateTopics())) {

      var topicBuilders = buildTopics(kafkaProperties);

      kafkaAdmin.createOrModifyTopics(topicBuilders.toArray(NewTopic[]::new));
    }

    return kafkaAdmin;
  }

  private Set<NewTopic> buildTopics(KafkaProperties kafkaProperties) {
    var topics = kafkaProperties.getTopics();

    var newTopics = new HashSet<NewTopic>();
    topics.forEach((k, v) -> {
      var newTopic = TopicBuilder.name(v.getName())
          .partitions(v.getPartitions())
          .replicas(v.getReplicationFactor())
          .configs(v.getConfigs())
          .compact()
          .build();
      newTopics.add(newTopic);
    });
    return newTopics;

  }

  private Map<String, Object> kafkaAdminConfigs(KafkaProperties kafkaProperties) {
    var admin = kafkaProperties.getAdmin();
    var bootstrapServers = admin.getBootstrapServers();

    var configs = new HashMap<String, Object>();

    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    return configs;
  }
}
