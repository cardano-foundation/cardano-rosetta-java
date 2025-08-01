package org.cardanofoundation.rosetta.api.block.model.repository.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for managing temporary tables with transaction hashes.
 * Provides database-agnostic temporary table operations that work with both PostgreSQL and H2.
 * 
 * Key features:
 * - Uses portable SQL syntax that works on both PostgreSQL and H2
 * - Automatic cleanup via ON COMMIT DROP
 * - Thread-safe temporary table name generation
 * - Efficient batch insert operations
 * - JOOQ integration for type-safe queries
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TxHashTempTableManager {

    private final DSLContext dsl;
    
    // Batch size for efficient INSERT operations
    private static final int BATCH_SIZE = 1000;
    
    // Counter for unique table names within the same transaction
    private static final AtomicInteger TABLE_COUNTER = new AtomicInteger(0);

    /**
     * Creates a temporary table for storing transaction hashes.
     * Uses portable SQL that works on both PostgreSQL and H2.
     * 
     * @param tableName the name of the temporary table to create
     * @return the created Table reference for JOOQ queries
     */
    public Table<?> createTempTable(String tableName) {
        log.debug("Creating temporary table: {}", tableName);
        
        // Use portable CREATE TEMPORARY TABLE syntax
        // ON COMMIT DROP ensures automatic cleanup at transaction end
        String createTableSql = String.format(
            "CREATE TEMPORARY TABLE %s (tx_hash VARCHAR(64) PRIMARY KEY) ON COMMIT DROP",
            tableName
        );
        
        dsl.execute(createTableSql);
        
        // Return a JOOQ table reference
        return DSL.table(DSL.name(tableName));
    }

    /**
     * Populates a temporary table with transaction hashes using batch operations.
     * 
     * @param tableName the name of the temporary table
     * @param txHashes the set of transaction hashes to insert
     */
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
        
        // Process in batches to avoid memory issues
        var hashList = txHashes.stream().toList();
        for (int i = 0; i < hashList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, hashList.size());
            var batch = hashList.subList(i, endIndex);
            
            // Build VALUES clause for this batch
            var insertQuery = insertBase;
            for (String txHash : batch) {
                insertQuery = insertQuery.values(txHash);
            }
            
            // Execute the batch
            insertQuery.execute();
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
     * Creates and populates a temporary table with transaction hashes in one operation.
     * This is a convenience method that combines createTempTable and populateTempTable.
     * 
     * @param txHashes the set of transaction hashes to store
     * @return the name of the created and populated temporary table
     */
    public String createAndPopulateTempTable(Set<String> txHashes) {
        String tableName = generateTempTableName();
        createTempTable(tableName);
        populateTempTable(tableName, txHashes);
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

}
