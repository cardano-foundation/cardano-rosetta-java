package org.cardanofoundation.rosetta.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta.networks")
public class NetworkConfig {
  private String id;
  private Long protocolMagic;
  private String nodeVersion;

  public boolean correspondsToRosettaNetworkIdentifier(final String rosettaNetworkIdentifier) {
    return id.equalsIgnoreCase(rosettaNetworkIdentifier) ||
        (protocolMagic != null && String.valueOf(protocolMagic)
            .equalsIgnoreCase(rosettaNetworkIdentifier));
  }

  public final String getSanitizedNetworkId() {
    return NetworkEnum.fromValue(id).getValue();
  }
}
