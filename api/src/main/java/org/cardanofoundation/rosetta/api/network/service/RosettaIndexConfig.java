package org.cardanofoundation.rosetta.api.network.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for Rosetta required database indexes.
 * Binds to cardano.rosetta.db_indexes in application.yaml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta")
public class RosettaIndexConfig {

    /**
     * List of required database index names that must be valid and ready
     * before the system transitions to LIVE state.
     */
    private List<String> dbIndexes;

}
