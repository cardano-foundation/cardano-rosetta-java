package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta")
@Data
public class RosettaIndexConfig {
    private List<DbIndex> dbIndexes;

    public record DbIndex(String name, String command) {}
}
