package org.cardanofoundation.rosetta.api.network.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for Rosetta required database indexes.
 * Binds to cardano.rosetta.db_indexes in db-indexes.yaml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cardano.rosetta")
public class RosettaIndexConfig {

    /**
     * List of required database indexes with their names and creation commands.
     * Each index must be valid and ready before the system transitions to LIVE state.
     */
    private List<DbIndex> dbIndexes;

    /**
     * Gets the list of index names only (useful for monitoring).
     *
     * @return List of index names
     */
    public List<String> getIndexNames() {
        if (dbIndexes == null) {
            return List.of();
        }
        return dbIndexes.stream()
            .map(DbIndex::name)
            .toList();
    }

    /**
     * Gets the list of index creation commands (useful for applying indices).
     *
     * @return List of SQL commands to create indices
     */
    public List<String> getIndexCommands() {
        if (dbIndexes == null) {
            return List.of();
        }
        return dbIndexes.stream()
            .map(DbIndex::command)
            .toList();
    }

    /**
     * Record representing a database index with its name and creation command.
     *
     * @param name    The name of the index (e.g., "idx_address_utxo_amounts_gin")
     * @param command The SQL command to create the index (e.g., "CREATE INDEX CONCURRENTLY...")
     */
    public record DbIndex(String name, String command) {}

}
