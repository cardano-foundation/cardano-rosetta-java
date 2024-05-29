package org.cardanofoundation.rosetta.yaciindexer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup.BaseMappersConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { BaseMappersConfig.class})
public class BaseMapperSetup {

  @TestConfiguration
  @ComponentScan(basePackages = {
      "org.cardanofoundation.rosetta.yaciindexer.mapper"})
  public static class BaseMappersConfig {

  }
}
