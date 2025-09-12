package org.cardanofoundation.rosetta.api.block.mapper;

import org.assertj.core.api.Assertions;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.TokenBundleItem;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;

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

  private static List<String> getPolicyIdUnits(List<TokenBundleItem> tokenBundle, String policyId) {
    return tokenBundle.stream()
        .filter(t -> t.getPolicyId().equals(policyId))
        .map(TokenBundleItem::getTokens)
        .flatMap(List::stream)
        .map(Amount::getCurrency)
        .map(Currency::getSymbol)
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

}
