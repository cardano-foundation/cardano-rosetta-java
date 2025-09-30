package org.cardanofoundation.rosetta.api.block.mapper;

import org.assertj.core.api.Assertions;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransactionMapperUtilsTest {

  @Mock
  private ProtocolParamService protocolParamService;

  @Mock
  private TokenRegistryService tokenRegistryService;

  private TransactionMapperUtils transactionMapperUtils;
  private DataMapper dataMapper;

  @BeforeEach
  void setUp() {
    // Create real DataMapper instance with its dependency
    org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapper tokenRegistryMapper =
        new org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapperImpl();
    dataMapper = new DataMapper(tokenRegistryMapper);

    transactionMapperUtils = new TransactionMapperUtils(protocolParamService, dataMapper);

    // Configure TokenRegistryService to return fallback metadata for any asset
    lenient().when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenAnswer(invocation -> {
      Map<Asset, TokenRegistryCurrencyData> result = new HashMap<>();
      @SuppressWarnings("unchecked")
      Set<Asset> assets = (Set<Asset>) invocation.getArgument(0);
      for (Asset asset : assets) {
        result.put(asset, TokenRegistryCurrencyData.builder()
            .policyId(asset.getPolicyId())
            .decimals(0) // Default decimals
            .build());
      }
      return result;
    });
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
    
    // Create metadata map for the cached method
    Map<Asset, TokenRegistryCurrencyData> metadataMap = createMetadataMapForAmounts(amtList);
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(true, amtList, metadataMap);
    // then
    assertNotNull(operationMetadata);
    List<TokenBundleItem> tokenBundle = operationMetadata.getTokenBundle();
    assertEquals(3, tokenBundle.size());

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId1"))
        .containsExactlyInAnyOrder("unit12", "unit13", "unit14");

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId2"))
        .containsExactlyInAnyOrder("unit21");

    Assertions.assertThat(getPolicyIdUnits(tokenBundle, "policyId3"))
        .containsExactlyInAnyOrder("unit31", "unit33");
  }

  @Test
  void mapToOperationMetaDataNegativeTest() {
    // given
    List<Amt> amtList = Arrays.asList(
        newAmt(1, 11, true),
        newAmt(2, 21, true));
    
    // Create metadata map for the cached method
    Map<Asset, TokenRegistryCurrencyData> metadataMap = createMetadataMapForAmounts(amtList);
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(true, amtList, metadataMap);
    // then
    assertNull(operationMetadata);
  }

  @Test
  void mapToOperationMetaDataWithTokenRegistryTest() {
    // given
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    String subject = policyId + "746573744173736574";  // hex encoding of "testAsset"
    
    // Create Asset object for the request
    Asset asset = Asset.builder()
        .policyId(policyId)
        .assetName(assetName)
        .build();
    
    // Mock token registry response with full metadata
    TokenRegistryCurrencyData currencyMetadata = TokenRegistryCurrencyData.builder()
        .policyId(policyId)
        .subject(subject)
        .name("Test Token")
        .description("Test description")
        .ticker("TST")
        .url("https://test.com")
        .logo(TokenRegistryCurrencyData.LogoData.builder().format(TokenRegistryCurrencyData.LogoFormat.BASE64).value("base64logo").build())
        .decimals(6)
        .version(BigDecimal.valueOf(1L))
        .build();

    Map<Asset, TokenRegistryCurrencyData> tokenMetadataMap = new HashMap<>();
    tokenMetadataMap.put(asset, currencyMetadata);
    
    List<Amt> amtList = Arrays.asList(
        newAmtWithCustomName(policyId, assetName, false)
    );
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(false, amtList, tokenMetadataMap);
    
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
    assertEquals("customUnit", currency.getSymbol());
    assertEquals(6, currency.getDecimals());
    
    // Verify metadata injection
    org.openapitools.client.model.CurrencyMetadataResponse responseMetadata = currency.getMetadata();
    assertNotNull(responseMetadata);
    assertEquals(policyId, responseMetadata.getPolicyId());
    assertEquals(subject, responseMetadata.getSubject());
    assertEquals("Test Token", responseMetadata.getName());
    assertEquals("Test description", responseMetadata.getDescription());
    assertEquals("TST", responseMetadata.getTicker());
    assertEquals("https://test.com", responseMetadata.getUrl());
    assertNotNull(responseMetadata.getLogo());
    assertEquals(org.openapitools.client.model.LogoType.FormatEnum.BASE64, responseMetadata.getLogo().getFormat());
    assertEquals("base64logo", responseMetadata.getLogo().getValue());
    assertEquals(BigDecimal.valueOf(1L), responseMetadata.getVersion());
  }

  @Test
  void mapToOperationMetaDataWithoutTokenRegistryTest() {
    // given - token registry returns fallback metadata with only policyId
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    
    Asset asset = Asset.builder()
        .policyId(policyId)
        .assetName(assetName)
        .build();
    
    // Mock service to return fallback metadata (service always returns something now)
    TokenRegistryCurrencyData fallbackMetadata = TokenRegistryCurrencyData.builder()
        .policyId(policyId)
        .decimals(0)
        .build();

    Map<Asset, TokenRegistryCurrencyData> tokenMetadataMap = new HashMap<>();
    tokenMetadataMap.put(asset, fallbackMetadata);
    
    List<Amt> amtList = Arrays.asList(
        newAmtWithCustomName(policyId, assetName, false)
    );
    
    // when
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(false, amtList, tokenMetadataMap);
    
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
    assertEquals("customUnit", currency.getSymbol());
    assertEquals(0, currency.getDecimals()); // Default when no metadata
    
    // Verify fallback metadata is present with at least policyId
    org.openapitools.client.model.CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
    assertNotNull(currencyMetadata);
    assertEquals(policyId, currencyMetadata.getPolicyId());
    // Other fields should be null since this is fallback metadata
    assertNull(currencyMetadata.getName());
    assertNull(currencyMetadata.getDescription());
  }

  @Test
  void mapToOperationMetaDataSpentAmountTest() {
    // given
    String policyId = "testPolicyId";
    String assetName = "testAsset";
    
    Asset asset = Asset.builder()
        .policyId(policyId)
        .assetName(assetName)
        .build();
    
    // Mock service to return fallback metadata
    TokenRegistryCurrencyData fallbackMetadata = TokenRegistryCurrencyData.builder()
        .policyId(policyId)
        .decimals(0)
        .build();

    Map<Asset, TokenRegistryCurrencyData> tokenMetadataMap = new HashMap<>();
    tokenMetadataMap.put(asset, fallbackMetadata);
    
    List<Amt> amtList = Arrays.asList(
        Amt.builder()
            .assetName(assetName)
            .policyId(policyId)
            .quantity(BigInteger.valueOf(1000))
            .unit("testUnit")
            .build()
    );
    
    // when - test spent=true
    OperationMetadata operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(true, amtList, tokenMetadataMap);
    
    // then
    assertNotNull(operationMetadata);
    Amount amount = operationMetadata.getTokenBundle().get(0).getTokens().get(0);
    assertEquals("-1000", amount.getValue()); // Negative for spent
    
    // when - test spent=false  
    operationMetadata = transactionMapperUtils.mapToOperationMetaDataWithCache(false, amtList, createMetadataMapForAmounts(amtList));
    
    // then
    assertNotNull(operationMetadata);
    amount = operationMetadata.getTokenBundle().get(0).getTokens().get(0);
    assertEquals("1000", amount.getValue()); // Positive for received
  }

  private Map<Asset, TokenRegistryCurrencyData> createMetadataMapForAmounts(List<Amt> amtList) {
    Map<Asset, TokenRegistryCurrencyData> metadataMap = new HashMap<>();
    for (Amt amt : amtList) {
      if (!Constants.LOVELACE.equals(amt.getAssetName())) {
        Asset asset = Asset.builder()
            .policyId(amt.getPolicyId())
            .assetName(amt.getAssetName())
            .build();
        TokenRegistryCurrencyData metadata = TokenRegistryCurrencyData.builder()
            .policyId(amt.getPolicyId())
            .decimals(0) // Default decimals
            .build();
        metadataMap.put(asset, metadata);
      }
    }
    return metadataMap;
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
