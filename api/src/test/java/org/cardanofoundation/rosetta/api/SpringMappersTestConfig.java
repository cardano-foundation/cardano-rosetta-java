package org.cardanofoundation.rosetta.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.openapitools.jackson.nullable.JsonNullableModule;

@TestConfiguration
@ComponentScan(basePackages = {
    "org.cardanofoundation.rosetta.api.block.mapper",
    "org.cardanofoundation.rosetta.common.mapper"})
public class SpringMappersTestConfig {

  @Bean
  public JsonNullableModule jsonNullableModule() {
    return new JsonNullableModule();
  }
}
