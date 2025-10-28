package org.cardanofoundation.rosetta.api;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
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
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { BaseMappersConfig.class})
public class BaseMapperSetup {

  @MockitoBean
  protected ProtocolParamService protocolParamService;

  @MockitoBean
  protected TokenRegistryService tokenRegistryService;

  @Mock
  ProtocolParams protocolParams;

  @BeforeEach
  public void before() {
    when(protocolParamService.findProtocolParameters()).thenReturn(protocolParams);
    when(protocolParams.getPoolDeposit()).thenReturn(new BigInteger("500"));
    
    // Configure TokenRegistryService to return fallback metadata for any asset
    when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenAnswer(invocation -> {
      Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();
      @SuppressWarnings("unchecked")
      java.util.Set<AssetFingerprint> assetFingerprints = (java.util.Set<AssetFingerprint>) invocation.getArgument(0);
      for (AssetFingerprint assetFingerprint : assetFingerprints) {
        result.put(assetFingerprint, TokenRegistryCurrencyData.builder()
            .policyId(assetFingerprint.getPolicyId())
            .decimals(0) // Default decimals
            .build());
      }
      return result;
    });
  }

  @TestConfiguration
  @ComponentScan(basePackages = {
      "org.cardanofoundation.rosetta.api.block.mapper",
      "org.cardanofoundation.rosetta.api.account.mapper",
      "org.cardanofoundation.rosetta.api.search.mapper",
      "org.cardanofoundation.rosetta.api.common.mapper",
      "org.cardanofoundation.rosetta.common.mapper"})
  public static class BaseMappersConfig {

    @Bean
    public JsonNullableModule jsonNullableModule() {
      return new JsonNullableModule();
    }
  }
}
