package org.cardanofoundation.rosetta.api.construction.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalystLabelsTest {

  final CatalystLabels[] catalystLabels = CatalystLabels.values();

  @Test
  void findByValue() {
    for (CatalystLabels catalystLabel : catalystLabels) {
      CatalystLabels actual = CatalystLabels.findByValue(catalystLabel.getLabel());
      assertEquals(catalystLabel, actual);
      assertEquals(catalystLabel.getLabel(), actual.toString());
    }
    assertNull(CatalystLabels.findByValue("Invalid"));
  }
}
