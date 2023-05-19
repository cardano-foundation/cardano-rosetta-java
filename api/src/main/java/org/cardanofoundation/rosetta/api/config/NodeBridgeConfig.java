package org.cardanofoundation.rosetta.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@ConfigurationProperties
public class NodeBridgeConfig {
  private final String type;
  private final String endpointUrl;
}
