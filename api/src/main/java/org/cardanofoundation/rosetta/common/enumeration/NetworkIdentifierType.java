package org.cardanofoundation.rosetta.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NetworkIdentifierType {
  CARDANO_MAINNET_NETWORK("mainnet", 1, 764824073L),
  CARDANO_PREPROD_NETWORK("preprod", 0, 1),
  CARDANO_PREVIEW_NETWORK("preview", 0, 2),
  CARDANO_SANCHONET_NETWORK("sanchonet", 0, 4);

  private final String networkId;
  private final int value;
  private final long protocolMagic;

  NetworkIdentifierType(String networkId, int value, long protocolMagic) {
    this.networkId = networkId;
    this.value = value;
    this.protocolMagic = protocolMagic;
  }

  /**
   * This method will return the NetworkIdentifierType based on the network protocol magic
   *
   * @param protocolMagic - network protocolMagic identifier
   * @return a NetworkIdentifierType Enum instance
   */
  public static NetworkIdentifierType findByProtocolMagic(Long protocolMagic) {
    for (NetworkIdentifierType networkIdentifierType : NetworkIdentifierType.values()) {
      if (protocolMagic == networkIdentifierType.getProtocolMagic()) {
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
