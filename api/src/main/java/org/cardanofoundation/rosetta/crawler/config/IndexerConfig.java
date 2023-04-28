package org.cardanofoundation.rosetta.crawler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@ConfigurationProperties
public class IndexerConfig {
    private final String type;
    private final String endpointUrl;
}
