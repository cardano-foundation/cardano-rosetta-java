-- =============================================================================
-- ROSETTA SEARCH API PERFORMANCE INDEXES
-- 
-- This file creates indexes to optimize search queries in the Rosetta API.
-- Each index is mapped to specific query patterns in the codebase.
-- =============================================================================

-- =============================================================================
-- HIGH PRIORITY INDEXES - Transaction Search Optimization
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

-- =============================================================================
-- HIGH PRIORITY INDEXES - Currency Filtering
-- =============================================================================

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

-- =============================================================================
-- MEDIUM PRIORITY INDEXES - Join Optimization
-- =============================================================================

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

-- =============================================================================
-- HIGH PRIORITY INDEXES - Window Function & Coverage Optimization
-- =============================================================================

-- 10. Window Function Count Support
-- Used by: TxRepositoryQueryBuilder.buildTransactionSelectQueryWithCount (line 179)
-- Query pattern: SELECT ..., COUNT(*) OVER() as total_count FROM ... ORDER BY slot DESC
-- Optimizes: Single-query pagination with total count
CREATE INDEX idx_transaction_count_support
    ON transaction(slot DESC)
    INCLUDE (tx_hash, block_hash);

-- 11. Transaction Coverage Index for Currency Search
-- Used by: TxRepositoryPostgreSQLImpl currency searches with window functions
-- Query pattern: SELECT tx_hash, block_hash, inputs, outputs, fee, COUNT(*) OVER() FROM transaction
-- Optimizes: Reduces heap lookups during currency-filtered searches
-- Note: Excludes inputs/outputs JSONB columns due to btree size limitations
CREATE INDEX idx_transaction_currency_search
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

-- =============================================================================
-- USAGE NOTES
-- =============================================================================
-- Total indexes created: 13
-- 
-- Key optimizations:
-- 1. Window function COUNT(*) OVER() for efficient pagination
-- 2. GIN index for JSONB currency filtering
-- 3. Partial index for ADA/lovelace (most common currency)
-- 4. Coverage indexes to reduce heap lookups
-- 5. Hash index for large transaction set joins
--
-- Monitor with: SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';