package org.cardanofoundation.rosetta.config;

import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.openapitools.client.model.NetworkRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "cardano.rosetta")
public class RosettaConfig {
    private String version;
    private String implementationVersion;
    private List<NetworkConfig> networks;
    private String exemptionsFile;

    public Optional<NetworkConfig> networkConfigFromNetworkRequest(final NetworkRequest networkRequest) {
        if (!networkRequest.getNetworkIdentifier().getBlockchain().equalsIgnoreCase(RosettaConstants.BLOCKCHAIN_NAME)) {
            throw new IllegalArgumentException("Invalid blockchain specified in request. Only 'cardano' is supported by this Rosetta endpoint.");
        }

        return networks.stream()
                .filter(networkConfig -> networkConfig.correspondsToRosettaNetworkIdentifier(networkRequest.getNetworkIdentifier().getNetwork()))
                .findFirst();
    }
}
