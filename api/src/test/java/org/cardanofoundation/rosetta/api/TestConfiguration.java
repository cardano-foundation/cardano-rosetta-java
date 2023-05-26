package org.cardanofoundation.rosetta.api;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
  @Bean
  public JsonNullableModule jsonNullableModule() {
    return new JsonNullableModule();
  }
}
