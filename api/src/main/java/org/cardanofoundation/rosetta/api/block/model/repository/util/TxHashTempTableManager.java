package org.cardanofoundation.rosetta.api.block.model.repository.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for managing temporary tables with transaction hashes.
 * Provides database-agnostic temporary table operations that work with both PostgreSQL and H2.
 * 
 * Key features:
 * - Ensures all operations happen within the same transaction/connection context
 * - Uses database-specific temporary table strategies for optimal performance
 * - Thread-safe temporary table name generation
 * - Efficient batch insert operations
 * - JOOQ integration for type-safe queries
 * - Proper transaction propagation to maintain connection consistency
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TxHashTempTableManager {

    private final DSLContext dsl;
    private final Environment environment;
    
    // Batch size for efficient INSERT operations
    private static final int BATCH_SIZE = 1000;
    
    // Counter for unique table names within the same transaction
    private static final AtomicInteger TABLE_COUNTER = new AtomicInteger(0);

    /**
     * Creates a temporary table for storing transaction hashes.
     * Uses database-specific strategies for optimal performance and reliability.
     * 
     * @param tableName the name of the temporary table to create
     * @return the created Table reference for JOOQ queries
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Table<?> createTempTable(String tableName) {
        log.debug("Creating temporary table: {} using dialect: {}", tableName, dsl.dialect());
        
        String createTableSql;
        
        // Check for h2 profile first, as JOOQ might still be configured for PostgreSQL in application-h2.yaml
        boolean isH2Profile = environment.acceptsProfiles(Profiles.of("test-integration", "h2"));
        boolean isPostgreSQL = !isH2Profile && dsl.configuration().dialect().family() == SQLDialect.POSTGRES;
        
        if (isPostgreSQL) {
            // PostgreSQL: Use session-scoped temporary tables that persist across transactions
            // but are automatically dropped at session end
            createTableSql = String.format(
                "CREATE TEMPORARY TABLE %s (tx_hash VARCHAR(64) PRIMARY KEY) ON COMMIT PRESERVE ROWS",
                tableName
            );
        } else {
            // H2: Use standard temporary table with transaction scope
            // H2 handles temporary tables differently and ON COMMIT DROP works reliably
            createTableSql = String.format(
                "CREATE TEMPORARY TABLE %s (tx_hash VARCHAR(64) PRIMARY KEY) ON COMMIT DROP",
                tableName
            );
        }
        
        dsl.execute(createTableSql);
        log.debug("Successfully created temporary table: {}", tableName);
        
        // Return a JOOQ table reference
        return DSL.table(DSL.name(tableName));
    }

    /**
     * Populates a temporary table with transaction hashes using batch operations.
     * Must be called within the same transaction as createTempTable.
     * 
     * @param tableName the name of the temporary table
     * @param txHashes the set of transaction hashes to insert
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void populateTempTable(String tableName, Set<String> txHashes) {
        if (txHashes.isEmpty()) {
            log.debug("No transaction hashes to populate in temp table: {}", tableName);
            return;
        }
        
        log.debug("Populating temp table {} with {} transaction hashes", tableName, txHashes.size());
        
        Table<?> tempTable = DSL.table(DSL.name(tableName));
        var txHashField = DSL.field("tx_hash", String.class);
        
        // Use VALUES clause approach for better H2/PostgreSQL compatibility
        var insertBase = dsl.insertInto(tempTable).columns(txHashField);
        
        // Process in batches to avoid memory issues and SQL size limits
        var hashList = txHashes.stream().toList();
        int totalBatches = (int) Math.ceil((double) hashList.size() / BATCH_SIZE);
        
        for (int i = 0; i < hashList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, hashList.size());
            var batch = hashList.subList(i, endIndex);
            int currentBatch = (i / BATCH_SIZE) + 1;
            
            log.debug("Processing batch {}/{} with {} hashes for temp table {}", 
                     currentBatch, totalBatches, batch.size(), tableName);
            
            // Build VALUES clause for this batch
            var insertQuery = insertBase;
            for (String txHash : batch) {
                insertQuery = insertQuery.values(txHash);
            }
            
            // Execute the batch
            int inserted = insertQuery.execute();
            log.debug("Inserted {} rows in batch {}/{} for temp table {}", 
                     inserted, currentBatch, totalBatches, tableName);
        }
        
        log.debug("Successfully populated temp table {} with {} hashes", tableName, txHashes.size());
    }

    /**
     * Returns a JOOQ Table reference for an existing temporary table.
     * 
     * @param tableName the name of the temporary table
     * @return Table reference for use in JOOQ queries
     */
    public Table<?> getTempTable(String tableName) {
        return DSL.table(DSL.name(tableName));
    }

    /**
     * Generates a unique temporary table name that's safe for SQL usage.
     * Uses UUID and counter to ensure uniqueness within the same transaction.
     * 
     * @return a unique temporary table name
     */
    public String generateTempTableName() {
        // Generate a name that's both unique and SQL-safe
        String uuid = UUID.randomUUID().toString().replace("-", "");
        int counter = TABLE_COUNTER.incrementAndGet();
        
        // Prefix with "temp_tx_" for clarity and limit length for database compatibility
        return String.format("temp_tx_%s_%d", uuid.substring(0, 8), counter);
    }

    /**
     * Creates and populates a temporary table with transaction hashes in one atomic operation.
     * This method ensures both operations happen within the same transaction context.
     * 
     * @param txHashes the set of transaction hashes to store
     * @return the name of the created and populated temporary table
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String createAndPopulateTempTable(Set<String> txHashes) {
        if (txHashes == null || txHashes.isEmpty()) {
            throw new IllegalArgumentException("Transaction hashes cannot be null or empty");
        }
        
        String tableName = generateTempTableName();
        log.debug("Creating and populating temporary table {} with {} transaction hashes", 
                 tableName, txHashes.size());
        
        // Both operations must happen in the same transaction/connection
        createTempTable(tableName);
        populateTempTable(tableName, txHashes);
        
        log.debug("Successfully created and populated temporary table {} with {} hashes", 
                 tableName, txHashes.size());
        return tableName;
    }

    /**
     * Gets the tx_hash field reference for a temporary table.
     * This provides a type-safe way to reference the tx_hash column in JOOQ queries.
     * 
     * @param tempTable the temporary table
     * @return the tx_hash field reference
     */
    public org.jooq.Field<String> getTxHashField(Table<?> tempTable) {
        return tempTable.field("tx_hash", String.class);
    }

    /**
     * Explicitly drops a temporary table if it exists.
     * This is primarily needed for PostgreSQL tables created with ON COMMIT PRESERVE ROWS.
     * H2 tables with ON COMMIT DROP are automatically cleaned up.
     * 
     * @param tableName the name of the temporary table to drop
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void dropTempTableIfExists(String tableName) {
        try {
            // Check for h2 profile first
            boolean isH2Profile = environment.acceptsProfiles(Profiles.of("test-integration", "h2"));
            boolean isPostgreSQL = !isH2Profile && dsl.configuration().dialect().family() == SQLDialect.POSTGRES;
            
            if (isPostgreSQL) {
                // PostgreSQL: Explicitly drop the temporary table
                String dropTableSql = String.format("DROP TABLE IF EXISTS %s", tableName);
                dsl.execute(dropTableSql);
                log.debug("Explicitly dropped temporary table: {}", tableName);
            }
            // H2 tables with ON COMMIT DROP are automatically cleaned up, no action needed
        } catch (Exception e) {
            // Log but don't fail - temporary tables are session-scoped anyway
            log.warn("Failed to explicitly drop temporary table {}: {}", tableName, e.getMessage());
        }
    }

}
