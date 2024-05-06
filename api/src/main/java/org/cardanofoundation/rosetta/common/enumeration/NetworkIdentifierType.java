package org.cardanofoundation.rosetta.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NetworkIdentifierType {
  CARDANO_MAINNET_NETWORK("mainnet", 1, 764824073L),
  CARDANO_TESTNET_NETWORK("testnet", 0, 1097911063L),
  CARDANO_PREPROD_NETWORK("preprod", 0, 1);

  private final String networkId;
  private final int value;
  private final long protocolMagic;

  NetworkIdentifierType(String networkId, int value, long protocolMagic) {
    this.networkId = networkId;
    this.value = value;
    this.protocolMagic = protocolMagic;
  }

  /**
   * This method will return the NetworkIdentifierType based on the network value
   * Thus, if the network value is 0,
   * then it will return CARDANO_TESTNET_NETWORK or CARDANO_PREPROD_NETWORK
   * (as both of them have the same network value)
   *
   * @param network - network value identifier
   * @return a NetworkIdentifierType Enum instance
   */
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
