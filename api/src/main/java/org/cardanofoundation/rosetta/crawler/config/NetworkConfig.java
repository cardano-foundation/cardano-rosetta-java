package org.cardanofoundation.rosetta.crawler.config;

import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.crawler.util.CardanoNetworks;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta.networks")
public class NetworkConfig {
  private String id;
  private Long protocolMagic;
  private String nodeVersion;
  private IndexerConfig indexer;
  private NodeBridgeConfig nodeBridge;

  public boolean correspondsToRosettaNetworkIdentifier(final String rosettaNetworkIdentifier) {
    return id.equalsIgnoreCase(rosettaNetworkIdentifier) ||
        (protocolMagic != null && String.valueOf(protocolMagic)
            .equalsIgnoreCase(rosettaNetworkIdentifier));
  }

  public final String getSanitizedNetworkId() {
    return CardanoNetworks.sanitizeNetworkId(id, Optional.ofNullable(protocolMagic));
  }
}
