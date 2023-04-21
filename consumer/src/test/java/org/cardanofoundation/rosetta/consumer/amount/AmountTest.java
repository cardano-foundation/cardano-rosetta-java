package org.cardanofoundation.rosetta.consumer.amount;

import com.sotatek.cardano.ledgersync.common.Amount;
import com.sotatek.cardano.ledgersync.common.constant.Constant;
import java.math.BigInteger;
import java.math.BigInteger;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class AmountTest {

  private final byte[] lovelace = "LOVELACE".getBytes();

  @Test
  public void calculateLovelace() {
    List<Amount> amounts = new ArrayList<>();
    amounts.add(
        Amount.builder().quantity(BigInteger.valueOf(1000)).assetName(lovelace).policyId("p1")
            .build());
    amounts.add(
        Amount.builder().quantity(BigInteger.valueOf(2500)).assetName(lovelace).policyId("p2")
            .build());
    amounts.add(
        Amount.builder().quantity(BigInteger.valueOf(3000)).assetName(lovelace).policyId("p3")
            .build());
    amounts.add(Amount.builder().quantity(BigInteger.valueOf(1000)).assetName("nutcoin".getBytes())
        .policyId("p4").build());

    AtomicReference<BigInteger> value = new AtomicReference<BigInteger>(BigInteger.ZERO);
    amounts.forEach(amount -> {
      if (Constant.isLoveLace(amount.getAssetName())) {
        var amountNative = amount.getQuantity();
        amountNative = amountNative.add(value.get());
        value.set(amountNative);
      }
    });

    assertEquals(6500,value.get().intValue());
  }

}
