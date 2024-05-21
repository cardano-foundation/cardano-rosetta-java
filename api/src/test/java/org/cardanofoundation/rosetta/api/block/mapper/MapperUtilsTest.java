package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.TokenBundleItem;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.common.mapper.util.MapperUtils;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MapperUtilsTest {

  final private MapperUtils mapperUtils = Mockito.mock(
      MapperUtils.class,
      Mockito.CALLS_REAL_METHODS);

  @Test
  public void mapToOperationMetaDataTest() {
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
    OperationMetadata operationMetadata = mapperUtils.mapToOperationMetaData(true, amtList);
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
  public void mapToOperationMetaDataNegativeTest() {
    // given
    List<Amt> amtList = Arrays.asList(
        newAmt(1, 11, true),
        newAmt(2, 21, true));
    // when
    OperationMetadata operationMetadata = mapperUtils.mapToOperationMetaData(true, amtList);
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
