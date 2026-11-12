package org.springframework.boot.autoconfigure.jooq;

import org.jooq.impl.DefaultConfiguration;

/**
 * Binary compatibility for the Spring Boot 3 jOOQ customizer.
 * Boot 4 moved it to {@code org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer}.
 */
@FunctionalInterface
public interface DefaultConfigurationCustomizer
    extends org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer {

  @Override
  void customize(DefaultConfiguration configuration);
}
