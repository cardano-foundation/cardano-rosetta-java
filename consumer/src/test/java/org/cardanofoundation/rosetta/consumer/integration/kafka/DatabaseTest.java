package org.cardanofoundation.rosetta.consumer.integration.kafka;

import org.cardanofoundation.rosetta.consumer.CardanoRosettaConsumerApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

@Profile("test-integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
    CardanoRosettaConsumerApplication.class, DatabaseTest.DataSourceInitializer.class})
public abstract class DatabaseTest {

  private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(
      "postgres:12.9-alpine");

  public static class DataSourceInitializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          applicationContext,
          "spring.test.database.replace=none",
          "spring.datasource.url=" + database.getJdbcUrl(),
          "spring.datasource.username=" + database.getUsername(),
          "spring.datasource.password=" + database.getPassword()
      );
    }
  }
}