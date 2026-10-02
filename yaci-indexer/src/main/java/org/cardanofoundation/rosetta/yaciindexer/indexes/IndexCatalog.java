package org.cardanofoundation.rosetta.yaciindexer.indexes;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta")
@Data
public class IndexCatalog {
    private List<DbIndex> dbIndexes;

    public record DbIndex(String name, String command) {}
}
