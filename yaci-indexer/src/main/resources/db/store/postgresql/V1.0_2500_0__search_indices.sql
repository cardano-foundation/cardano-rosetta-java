-- =============================================================================
-- ROSETTA SEARCH API PERFORMANCE INDEXES
--
-- This file creates indexes to optimize search queries in the Rosetta API.
-- Each index is mapped to specific query patterns in the codebase.
-- =============================================================================

-- 1. Transaction Slot-Based Ordering with Hash Lookups
-- Used by: TxRepositoryPostgreSQLImpl.searchTxnEntitiesAND/OR (lines 66, 105)
-- Query pattern: SELECT ... FROM transaction WHERE tx_hash IN (...) ORDER BY slot DESC LIMIT/OFFSET
-- Optimizes: All paginated transaction searches ordered by slot
CREATE INDEX idx_transaction_slot_desc_hash
    ON transaction(slot DESC, tx_hash);

-- 2. Invalid Transaction Status Index
-- Used by: TxRepositoryPostgreSQLImpl.searchTxnEntitiesAND/OR (lines 69, 109)
-- Query pattern: LEFT JOIN invalid_transaction ON transaction.tx_hash = invalid_transaction.tx_hash
-- Optimizes: Transaction status filtering (success/failed)
CREATE INDEX idx_invalid_transaction_hash_slot
    ON invalid_transaction(tx_hash, slot DESC);

-- 3. Block Number Range Index
-- Used by: TxRepositoryQueryBuilder.buildAndConditions (line 73)
-- Query pattern: WHERE block.number <= ? ORDER BY block.number DESC
-- Optimizes: maxBlock filtering in search queries
CREATE INDEX idx_block_number_desc_hash
    ON block(number DESC, hash);

-- 4. JSONB GIN Index for Currency Filtering
-- Used by: PostgreSQLCurrencyConditionBuilder.buildCurrencyCondition (lines 212-227)
-- Query patterns:
--   - WHERE amounts::jsonb @> '[{"policy_id": "..."}]'
--   - WHERE amounts::jsonb @> '[{"asset_name": "..."}]'
--   - WHERE amounts::jsonb @> '[{"unit": "lovelace"}]'
-- Optimizes: All currency-based filtering (native assets and ADA)
CREATE INDEX idx_address_utxo_amounts_gin
    ON address_utxo USING gin(amounts);

-- 5. ADA/Lovelace Optimized Partial Index
-- Used by: PostgreSQLCurrencyConditionBuilder.buildCurrencyCondition (line 223)
-- Query pattern: EXISTS (SELECT 1 FROM address_utxo WHERE tx_hash = transaction.tx_hash AND amounts @> '[{"unit": "lovelace"}]')
-- Optimizes: ADA-specific searches (80%+ of currency queries)
CREATE INDEX idx_address_utxo_tx_hash_lovelace
    ON address_utxo(tx_hash)
    WHERE amounts::jsonb @> '[{"unit": "lovelace"}]';

-- 6. Transaction Hash Lookup
-- Used by: TxRepositoryPostgreSQLImpl with VALUES approach (lines 66, 105)
-- Query pattern: JOIN (VALUES ('hash1'), ('hash2'), ...) AS hash_values ON transaction.tx_hash = hash_values.hash
-- Optimizes: Large hash set joins (>10k hashes)
CREATE INDEX idx_transaction_hash_btree
    ON transaction USING hash(tx_hash);

-- 7. UTXO Transaction Join Index
-- Used by: TxRepositoryPostgreSQLImpl.searchTxnEntitiesOR (line 127)
-- Query pattern: LEFT JOIN address_utxo ON transaction.tx_hash = address_utxo.tx_hash
-- Optimizes: Currency filtering joins in OR queries
CREATE INDEX idx_address_utxo_tx_hash_slot
    ON address_utxo(tx_hash, slot DESC);

-- 8. Block Hash Direct Lookup
-- Used by: TxRepositoryQueryBuilder.buildAndConditions (line 77)
-- Query pattern: WHERE block.hash = ? AND block.number = ?
-- Optimizes: Block-specific searches
CREATE INDEX idx_block_hash_number
    ON block(hash, number);

-- 9. Transaction Size Join Index
-- Used by: TxRepositoryPostgreSQLImpl (lines 74, 126, 143)
-- Query pattern: LEFT JOIN transaction_size ON transaction.tx_hash = transaction_size.tx_hash
-- Optimizes: Transaction size data retrieval
CREATE INDEX idx_transaction_size_hash
    ON transaction_size(tx_hash);

-- 10. Separate Count Query Optimization (UPDATED)
-- Used by: TxRepositoryCustomBase.executeCountQuery and executeOrCountQuery
-- Query pattern: SELECT COUNT(*) FROM transaction WHERE ... (separate from results query)
-- Optimizes: Fast count queries without window functions
CREATE INDEX idx_transaction_count_optimization
    ON transaction(slot DESC)
    INCLUDE (tx_hash, block_hash)
    WHERE slot IS NOT NULL;

-- 11. Transaction Results Query Coverage (UPDATED)
-- Used by: TxRepositoryCustomBase.executeResultsQuery and executeOrResultsQuery
-- Query pattern: SELECT tx_hash, block_hash, inputs, outputs, fee FROM transaction WHERE ... ORDER BY slot DESC LIMIT/OFFSET
-- Optimizes: Reduces heap lookups during paginated result queries
-- Note: Excludes inputs/outputs JSONB columns due to btree size limitations
CREATE INDEX idx_transaction_results_coverage
    ON transaction(slot DESC)
    INCLUDE (tx_hash, block_hash, fee);

-- 12. Address UTXO Slot Ordering
-- Used by: LedgerSearchServiceImpl.searchTransaction via addressUtxoRepository.findTxHashesByOwnerAddr
-- Query pattern: SELECT tx_hash FROM address_utxo WHERE owner_addr = ? ORDER BY slot DESC
-- Optimizes: Address-based transaction searches
CREATE INDEX idx_address_utxo_slot_desc
    ON address_utxo(slot DESC, tx_hash);

-- 13. Transaction-Block Join with Coverage
-- Used by: TxRepositoryPostgreSQLImpl (lines 73, 125, 142)
-- Query pattern: LEFT JOIN block ON transaction.block_hash = block.hash
-- Optimizes: Multi-table joins for transaction queries
CREATE INDEX idx_transaction_block_join
    ON transaction(block_hash, slot DESC)
    INCLUDE (tx_hash);

-- 14. Address Owner Lookup Optimization
-- Used by: LedgerSearchServiceImpl via addressUtxoRepository.findTxHashesByOwnerAddr
-- Query pattern: SELECT tx_hash FROM address_utxo WHERE owner_addr = ?
-- Statistics show 129,669 tuple reads with only 2 index scans - needs optimization
CREATE INDEX IF NOT EXISTS idx_address_utxo_owner_addr_tx_hash
    ON address_utxo(owner_addr, tx_hash);

-- 15. Transaction Slot Ordering for Separate Query Performance (UPDATED)
-- Problem: Separate count and results queries need efficient slot-based access
-- Used by: Both count queries and results queries with slot ordering
-- This partial index optimizes slot-based filtering in separate query approach
CREATE INDEX IF NOT EXISTS idx_transaction_slot_desc_covering
    ON transaction(slot DESC)
   -- INCLUDE (tx_hash, block_hash, fee, inputs, outputs)
   INCLUDE (tx_hash, block_hash, fee)
   WHERE slot IS NOT NULL;

-- 16. Block Hash Index for Transaction-Block Joins
-- Optimizes: transaction LEFT JOIN block ON transaction.block_hash = block.hash
-- This is critical when joining large result sets
CREATE INDEX IF NOT EXISTS idx_block_hash_covering
    ON block(hash)
    INCLUDE (number, slot);

-- 17. Count Query Optimization for Separate Query Pattern (UPDATED)
-- Problem: Separate COUNT(*) queries need efficient execution without full table scans
-- This helps PostgreSQL execute count queries efficiently for slot-filtered queries
CREATE INDEX IF NOT EXISTS idx_transaction_slot_count_optimization
    ON transaction(slot DESC, tx_hash)
    WHERE slot IS NOT NULL;

-- =============================================================================
-- LARGE HASH SET OPTIMIZATION - Separate Query Pattern
-- =============================================================================

-- 18. VALUES Table Join Optimization for PostgreSQL Large Hash Sets (>10,000)
-- Used by: TxRepositoryPostgreSQLImpl.executeCountQueryWithValues and executeResultsQueryWithValues
-- Query pattern: SELECT ... FROM transaction JOIN (VALUES ('hash1'), ('hash2'), ...) AS hash_values(hash) ON ...
-- Optimizes: Large hash set filtering using VALUES table approach with separate count and results queries
-- Note: This index is critical for the VALUES JOIN performance
CREATE INDEX IF NOT EXISTS idx_transaction_hash_values_join
    ON transaction USING hash(tx_hash)
    INCLUDE (slot, block_hash);

-- 19. Large Hash Set Count Query Support
-- Used by: executeCountQueryWithValues for fast counting with hash filtering
-- Query pattern: SELECT COUNT(*) FROM transaction JOIN (VALUES ...) WHERE ...
-- Optimizes: Count queries for large hash sets without full table scans
CREATE INDEX IF NOT EXISTS idx_transaction_large_hashset_count
    ON transaction(tx_hash, slot DESC)
    WHERE slot IS NOT NULL;

-- 20. Gin indices on inputs and outputs
CREATE INDEX idx_transaction_inputs_gin
    ON transaction USING gin(inputs);

CREATE INDEX idx_transaction_outputs_gin
    ON transaction USING gin(outputs);
