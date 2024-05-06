package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NetworkIdentifierTypeTest {

  final NetworkIdentifierType[] networkIdentifierTypes = NetworkIdentifierType.values();

  @Test
  void find() {
    assertEquals(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, NetworkIdentifierType.find(1));
    assertNull(NetworkIdentifierType.find(100));
  }

  @Test
  void findByName() {
    for (NetworkIdentifierType networkIdentifierType : networkIdentifierTypes) {
      NetworkIdentifierType actual = NetworkIdentifierType.findByName(networkIdentifierType.getNetworkId());
      assertEquals(networkIdentifierType, actual);
    }
    assertNull(NetworkIdentifierType.findByName("Invalid"));
  }
}
