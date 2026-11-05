package org.cardanofoundation.rosetta.yaciindexer.service;

import org.junit.jupiter.api.Test;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSizeCalculatorImplTest {

  private final TransactionSizeCalculatorImpl calculator = new TransactionSizeCalculatorImpl();

  @Test
  void calculateSize_deeplyNestedMetadata_doesNotOverflowStack() {
    int depth = 20_000;
    DataItem nested = new UnsignedInteger(0);
    for (int i = 0; i < depth; i++) {
      Array wrapper = new Array();
      wrapper.add(nested);
      nested = wrapper;
    }
    Map tx = new Map();
    tx.put(new UnsignedInteger(3), nested);

    assertThat(calculator.calculateSize(tx)).isEqualTo(depth + 3);
  }
}
