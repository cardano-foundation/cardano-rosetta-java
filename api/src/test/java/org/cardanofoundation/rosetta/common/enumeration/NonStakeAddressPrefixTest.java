package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NonStakeAddressPrefixTest {

  final NonStakeAddressPrefix[] nonStakeAddressPrefixes = NonStakeAddressPrefix.values();

  @Test
  void findByValue() {
    for (NonStakeAddressPrefix nonStakeAddressPrefix : nonStakeAddressPrefixes) {
      NonStakeAddressPrefix actual = NonStakeAddressPrefix.findByValue(
          nonStakeAddressPrefix.getValue());
      assertEquals(nonStakeAddressPrefix, actual);
      assertEquals(String.valueOf(nonStakeAddressPrefix.getValue()), actual.toString());
    }
    assertNull(NonStakeAddressPrefix.findByValue("Invalid"));
  }
}
