package org.cardanofoundation.rosetta.api;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"org.cardanofoundation.rosetta.api.block.mapper"})
public class SpringMappersTestConfig {

  @Bean
  public JsonNullableModule jsonNullableModule() {
    return new JsonNullableModule();
  }
}
