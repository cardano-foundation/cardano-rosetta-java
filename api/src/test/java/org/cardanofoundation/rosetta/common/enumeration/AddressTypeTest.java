package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddressTypeTest {

  final AddressType[] addressTypes = AddressType.values();

  @Test
  void findByValue() {
    for (AddressType addressType : addressTypes) {
      AddressType actual = AddressType.findByValue(addressType.getValue());
      assertEquals(addressType, actual);
      // check toString() method to return the value
      assertEquals(addressType.getValue(), actual.toString());
    }
    assertNull(AddressType.findByValue("Invalid"));
  }
}
