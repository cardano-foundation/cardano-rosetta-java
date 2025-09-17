package org.cardanofoundation.rosetta.api.block.mapper;

import org.assertj.core.api.Assertions;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.CurrencyResponse;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.TokenBundleItem;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionMapperUtilsTest {

  @Mock
  private ProtocolParamService protocolParamService;
  
  @Mock
  private TokenRegistryHttpGateway tokenRegistryHttpGateway;

  private TransactionMapperUtils transactionMapperUtils;

  @BeforeEach
  void setUp() {
    transactionMapperUtils = new TransactionMapperUtils(protocolParamService, tokenRegistryHttpGateway);
    
    // Mock token registry to return empty map (no metadata) - use lenient to avoid unnecessary stubbing errors
    lenient().when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet())).thenReturn(Collections.emptyMap());
  }

  @Test
  void mapToOperationMetaDataTest() {
    // given
    List<Amt> amtList = Arrays.asList(
        newAmt(1, 11, true),
        newAmt(1, 12, false),
        newAmt(1, 13, false),
        newAmt(1, 14, false),
        newAmt(2, 21, false),
        newAmt(2, 22, true),
        newAmt(3, 31, false),
        newAmt(3, 32, true),
        newAmt(3, 33, false),
        newAmt(4, 41, true)
    );
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaData(true, amtList);
    // then
    assertNotNull(operationMetadata);
    List<TokenBundleItem> tokenBundle = operationMetadata.getTokenBundle();
    assertEquals(3, tokenBundle.size());

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId1"))
        .containsExactlyInAnyOrder("assetName12", "assetName13", "assetName14");

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId2"))
        .containsExactlyInAnyOrder("assetName21");

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId3"))
        .containsExactlyInAnyOrder("assetName31", "assetName33");
  }

  @Test
  void mapToOperationMetaDataNegativeTest() {
    // given
    List<Amt> amtList = Arrays.asList(
        newAmt(1, 11, true),
        newAmt(2, 21, true));
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaData(true, amtList);
    // then
    assertNull(operationMetadata);
  }

  @Test
  void mapToOperationMetaDataWithTokenRegistryTest() {
    // given
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    String subject = policyId + "746573744173736574";  // hex encoding of "testAsset"
    
    // Mock token registry response with full metadata
    TokenSubject tokenSubject = new TokenSubject();
    tokenSubject.setSubject(subject);
    
    TokenMetadata metadata = new TokenMetadata();
    metadata.setName(TokenProperty.builder().value("Test Token").source("registry").build());
    metadata.setDescription(TokenProperty.builder().value("Test description").source("registry").build());
    metadata.setTicker(TokenProperty.builder().value("TST").source("registry").build());
    metadata.setUrl(TokenProperty.builder().value("https://test.com").source("registry").build());
    metadata.setLogo(TokenProperty.builder().value("base64logo").source("registry").build());
    metadata.setDecimals(TokenPropertyNumber.builder().value(6L).source("registry").build());
    metadata.setVersion(TokenPropertyNumber.builder().value(1L).source("registry").build());
    
    tokenSubject.setMetadata(metadata);
    
    Map<String, Optional<TokenSubject>> tokenMetadataMap = new HashMap<>();
    tokenMetadataMap.put(subject, Optional.of(tokenSubject));
    
    when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet())).thenReturn(tokenMetadataMap);
    
    List<Amt> amtList = Arrays.asList(
        newAmtWithCustomName(policyId, assetName, false)
    );
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaData(false, amtList);
    
    // then
    assertNotNull(operationMetadata);
    List<TokenBundleItem> tokenBundle = operationMetadata.getTokenBundle();
    assertEquals(1, tokenBundle.size());
    
    TokenBundleItem item = tokenBundle.get(0);
    assertEquals(policyId, item.getPolicyId());
    assertEquals(1, item.getTokens().size());
    
    Amount amount = item.getTokens().get(0);
    assertNotNull(amount.getCurrency());
    
    CurrencyResponse currency = amount.getCurrency();
    assertEquals(assetName, currency.getSymbol());
    assertEquals(6, currency.getDecimals());
    
    // Verify metadata injection
    CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
    assertNotNull(currencyMetadata);
    assertEquals(policyId, currencyMetadata.getPolicyId());
    assertEquals(subject, currencyMetadata.getSubject());
    assertEquals("Test Token", currencyMetadata.getName());
    assertEquals("Test description", currencyMetadata.getDescription());
    assertEquals("TST", currencyMetadata.getTicker());
    assertEquals("https://test.com", currencyMetadata.getUrl());
    assertEquals("base64logo", currencyMetadata.getLogo());
    assertEquals(BigDecimal.valueOf(1L), currencyMetadata.getVersion());
  }

  @Test
  void mapToOperationMetaDataWithoutTokenRegistryTest() {
    // given - no token metadata available
    when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet())).thenReturn(Collections.emptyMap());
    
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    
    List<Amt> amtList = Arrays.asList(
        newAmtWithCustomName(policyId, assetName, false)
    );
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaData(false, amtList);
    
    // then
    assertNotNull(operationMetadata);
    List<TokenBundleItem> tokenBundle = operationMetadata.getTokenBundle();
    assertEquals(1, tokenBundle.size());
    
    TokenBundleItem item = tokenBundle.get(0);
    assertEquals(policyId, item.getPolicyId());
    assertEquals(1, item.getTokens().size());
    
    Amount amount = item.getTokens().get(0);
    assertNotNull(amount.getCurrency());
    
    CurrencyResponse currency = amount.getCurrency();
    assertEquals(assetName, currency.getSymbol());
    assertEquals(0, currency.getDecimals()); // Default when no metadata
    
    // Verify metadata contains only policyId (mandatory field)
    CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
    assertNotNull(currencyMetadata);
    assertEquals(policyId, currencyMetadata.getPolicyId());
    assertNull(currencyMetadata.getSubject());
    assertNull(currencyMetadata.getName());
    assertNull(currencyMetadata.getDescription());
    assertNull(currencyMetadata.getTicker());
    assertNull(currencyMetadata.getUrl());
    assertNull(currencyMetadata.getLogo());
    assertNull(currencyMetadata.getVersion());
  }

  @Test
  void mapToOperationMetaDataSpentAmountTest() {
    // given
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    
    when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet())).thenReturn(Collections.emptyMap());
    
    List<Amt> amtList = Arrays.asList(
        Amt.builder()
            .assetName(assetName)
            .policyId(policyId)
            .quantity(BigInteger.valueOf(1000))
            .unit("testUnit")
            .build()
    );
    
    // when - test spent=true
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaData(true, amtList);
    
    // then
    assertNotNull(operationMetadata);
    Amount amount = operationMetadata.getTokenBundle().get(0).getTokens().get(0);
    assertEquals("-1000", amount.getValue()); // Negative for spent
    
    // when - test spent=false  
    operationMetadata = transactionMapperUtils.mapToOperationMetaData(false, amtList);
    
    // then
    assertNotNull(operationMetadata);
    amount = operationMetadata.getTokenBundle().get(0).getTokens().get(0);
    assertEquals("1000", amount.getValue()); // Positive for received
  }

  private static List<String> getPolicyIdUnits(List<TokenBundleItem> tokenBundle, String policyId) {
    return tokenBundle.stream()
        .filter(t -> t.getPolicyId().equals(policyId))
        .map(TokenBundleItem::getTokens)
        .flatMap(List::stream)
        .map(Amount::getCurrency)
        .map(CurrencyResponse::getSymbol)
        .toList();
  }

  private static Amt newAmt(int policy, int number, boolean isLovelace) {
    return Amt.builder()
        .assetName(isLovelace ? Constants.LOVELACE : "assetName" + number)
        .policyId("policyId" + policy)
        .quantity(BigInteger.ONE)
        .unit("unit" + number)
        .build();
  }

  private static Amt newAmtWithCustomName(String policyId, String assetName, boolean isLovelace) {
    return Amt.builder()
        .assetName(isLovelace ? Constants.LOVELACE : assetName)
        .policyId(policyId)
        .quantity(BigInteger.ONE)
        .unit("customUnit")
        .build();
  }

}
