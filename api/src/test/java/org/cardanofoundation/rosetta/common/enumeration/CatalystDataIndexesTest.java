package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalystDataIndexesTest {

  final CatalystDataIndexes[] catalystDataIndexes = CatalystDataIndexes.values();

  @Test
  void findByValue() {
    for (CatalystDataIndexes catalystDataIndex : catalystDataIndexes) {
      CatalystDataIndexes actual = CatalystDataIndexes.findByValue(catalystDataIndex.getValue());
      assertEquals(catalystDataIndex, actual);
      assertEquals(String.valueOf(catalystDataIndex.getValue()), actual.toString());
    }
    assertNull(CatalystDataIndexes.findByValue(0L));
  }
}
