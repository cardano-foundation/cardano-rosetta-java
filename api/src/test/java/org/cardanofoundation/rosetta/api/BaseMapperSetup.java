package org.cardanofoundation.rosetta.api;

import java.math.BigInteger;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.Mock;
import org.openapitools.jackson.nullable.JsonNullableModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.BaseMapperSetup.BaseMappersConfig;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { BaseMappersConfig.class})
public class BaseMapperSetup {

  @MockitoBean
  protected ProtocolParamService protocolParamService;

  @MockitoBean
  protected TokenRegistryHttpGateway tokenRegistryHttpGateway;

  @Mock
  ProtocolParams protocolParams;

  @BeforeEach
  public void before() {
    when(protocolParamService.findProtocolParameters()).thenReturn(protocolParams);
    when(protocolParams.getPoolDeposit()).thenReturn(new BigInteger("500"));
  }

  @TestConfiguration
  @ComponentScan(basePackages = {
      "org.cardanofoundation.rosetta.api.block.mapper",
      "org.cardanofoundation.rosetta.api.account.mapper",
      "org.cardanofoundation.rosetta.api.search.mapper",
      "org.cardanofoundation.rosetta.common.mapper"})
  public static class BaseMappersConfig {

    @Bean
    public JsonNullableModule jsonNullableModule() {
      return new JsonNullableModule();
    }
  }
}
