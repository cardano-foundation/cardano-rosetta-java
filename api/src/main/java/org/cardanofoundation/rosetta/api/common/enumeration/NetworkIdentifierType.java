package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NetworkIdentifierType {
  CARDANO_MAINNET_NETWORK("mainnet", 1, 764824073L),
  CARDANO_TESTNET_NETWORK("testnet", 0, 1097911063L),
  CARDANO_PREPROD_NETWORK("preprod", 0, 1097911063L);

  private String networkId;
  private int value;
  private long protocolMagic;

  NetworkIdentifierType(String networkId, int value, long protocolMagic) {
    this.networkId = networkId;
    this.value = value;
    this.protocolMagic = protocolMagic;
  }

  // TODO EPAM: Usage?
  NetworkIdentifierType() {
  }

  // TODO EPAM: Find by id?
  public static NetworkIdentifierType find(int network) {
    for (NetworkIdentifierType networkIdentifierType : NetworkIdentifierType.values()) {
      if (network == networkIdentifierType.getValue()) {
        return networkIdentifierType;
      }
    }
    return null;
  }

  public static NetworkIdentifierType findByName(String networkId) {
    for (NetworkIdentifierType networkIdentifierType : NetworkIdentifierType.values()) {
      if (networkId.equals(networkIdentifierType.getNetworkId())) {
        return networkIdentifierType;
      }
    }
    return null;
  }

  @JsonValue
  public String getNetworkId() {
    return networkId;
  }

  @JsonValue
  public int getValue() {
    return value;
  }

  @JsonValue
  public long getProtocolMagic() {
    return protocolMagic;
  }

}
