package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkEnumTest {

  final NetworkEnum[] networkEnums = NetworkEnum.values();

  @Test
  void fromValue() {
    for (NetworkEnum networkEnum : networkEnums) {
      NetworkEnum actual = NetworkEnum.findByName(networkEnum.getName()).orElseThrow();
      assertEquals(networkEnum, actual);
    }
    assertTrue(NetworkEnum.findByName("Invalid").isEmpty());
  }

  @Test
  void fromProtocolMagic() {
    for (NetworkEnum networkEnum : networkEnums) {
      NetworkEnum actual = NetworkEnum.findByProtocolMagic(
          networkEnum.getNetwork().getProtocolMagic()).orElseThrow();

      assertEquals(networkEnum, actual);
    }
    assertTrue(NetworkEnum.findByProtocolMagic(0L).isEmpty());
  }
}
