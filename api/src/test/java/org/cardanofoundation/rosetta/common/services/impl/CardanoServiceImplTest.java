package org.cardanofoundation.rosetta.common.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.cardanofoundation.rosetta.common.services.CardanoService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.Test;

public class CardanoServiceImplTest {

  CardanoService cardanoService = new CardanoServiceImpl(null);
  @Test
  public void calculateFeeTest() {
    List<BigInteger> inputAmounts = List.of(BigInteger.valueOf(5L));
    List<BigInteger> outputAmounts = List.of(BigInteger.valueOf(2L));
    List<BigInteger> withdrawalAmounts = List.of(BigInteger.valueOf(-5L));
    Map<String, Double> depositsSumMap = Map.of(Constants.KEY_REFUNDS_SUM, Double.valueOf(6L),
        Constants.KEY_DEPOSITS_SUM, Double.valueOf(2L), Constants.POOL_DEPOSITS_SUM, Double.valueOf(2L));
    Long l = cardanoService.calculateFee(inputAmounts, outputAmounts, withdrawalAmounts,
        depositsSumMap);

    assertEquals(0L, l);
  }

}
