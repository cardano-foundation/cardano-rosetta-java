package org.cardanofoundation.rosetta.yaciindexer.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a Boot 3 {@code RestTemplateBuilder} so yaci-store's admin UI auto-configuration can
 * inject the type it was compiled against.
 */
@Configuration
public class LegacyRestTemplateBuilderConfig {

  @Bean
  public org.springframework.boot.web.client.RestTemplateBuilder legacyRestTemplateBuilder(
      RestTemplateBuilder restTemplateBuilder) {
    return new org.springframework.boot.web.client.RestTemplateBuilder(restTemplateBuilder);
  }
}
