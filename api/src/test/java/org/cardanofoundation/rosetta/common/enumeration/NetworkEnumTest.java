package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NetworkEnumTest {

  final NetworkEnum[] networkEnums = NetworkEnum.values();

  @Test
  void fromValue() {
    for (NetworkEnum networkEnum : networkEnums) {
      NetworkEnum actual = NetworkEnum.findByName(networkEnum.getName());
      assertEquals(networkEnum, actual);
    }
    assertNull(NetworkEnum.findByName("Invalid"));
  }

  @Test
  void fromProtocolMagic() {
    for (NetworkEnum networkEnum : networkEnums) {
      NetworkEnum actual = NetworkEnum.findByProtocolMagic(
          networkEnum.getNetwork().getProtocolMagic());
      assertEquals(networkEnum, actual);
    }
    assertNull(NetworkEnum.findByProtocolMagic(0L));
  }
}
