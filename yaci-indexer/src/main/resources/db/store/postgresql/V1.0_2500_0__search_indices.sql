
-- =============================================================================
-- HIGH PRIORITY INDEXES - Critical Performance Impact (Implement First)
-- =============================================================================

-- 1. Composite Index for Slot-Based Ordering with Hash Lookups
-- Optimizes: All search queries with pagination (ORDER BY slot DESC) + hash filtering
-- Query pattern: SELECT ... WHERE tx_hash IN (...) ORDER BY slot DESC LIMIT/OFFSET

CREATE INDEX CONCURRENTLY idx_transaction_slot_desc_hash
    ON transaction(slot DESC, tx_hash);

-- 2. Block Join Optimization Index  
-- Optimizes: transaction-block joins which appear in every search query
-- Query pattern: transaction LEFT JOIN block ON transaction.block_hash = block.hash
CREATE INDEX CONCURRENTLY idx_transaction_block_hash_slot
    ON transaction(block_hash, slot DESC);

-- 3a. Success Status Partial Index (Successful Transactions)
-- Optimizes: Filtering successful transactions (most common case, ~95% of queries)
-- Query pattern: WHERE tx_hash NOT IN (SELECT tx_hash FROM invalid_transaction)
CREATE INDEX CONCURRENTLY idx_transaction_successful
    ON transaction(slot DESC, tx_hash)
    WHERE tx_hash NOT IN (SELECT tx_hash FROM invalid_transaction);

-- 3b. Failed Transaction Status Index
-- Optimizes: Filtering failed transactions and joins with invalid_transaction table
-- Query pattern: JOIN invalid_transaction ON transaction.tx_hash = invalid_transaction.tx_hash
CREATE INDEX CONCURRENTLY idx_invalid_transaction_hash_slot
    ON invalid_transaction(tx_hash, slot DESC);

-- 4. Block Number Range Index
-- Optimizes: maxBlock filtering (WHERE block.number <= maxBlock)
-- Query pattern: WHERE block.number <= ? ORDER BY block.number DESC
CREATE INDEX CONCURRENTLY idx_block_number_desc_hash
    ON block(number DESC, hash);

-- =============================================================================
-- HIGH PRIORITY INDEXES - Currency Filtering Enhancement  
-- =============================================================================

-- 5a. Policy ID JSONB Index
-- Optimizes: Currency filtering by policy_id (specific native assets)
-- Query pattern: WHERE amounts::jsonb @> '[{"policy_id": "..."}]'
CREATE INDEX CONCURRENTLY idx_address_utxo_amounts_policy_id
    ON address_utxo USING gin((amounts->>'policy_id'));

-- 5b. Asset Name JSONB Index  
-- Optimizes: Currency filtering by asset_name (token symbol searches)
-- Query pattern: WHERE amounts::jsonb @> '[{"asset_name": "..."}]'
CREATE INDEX CONCURRENTLY idx_address_utxo_amounts_asset_name
    ON address_utxo USING gin((amounts->>'asset_name'));

-- 5c. ADA/Lovelace Partial Index (Most Common Currency)
-- Optimizes: ADA/Lovelace filtering (80%+ of currency searches)
-- Query pattern: WHERE amounts::jsonb @> '[{"unit": "lovelace"}]'
CREATE INDEX CONCURRENTLY idx_address_utxo_amounts_lovelace
    ON address_utxo(tx_hash)
    WHERE amounts::jsonb @> '[{"unit": "lovelace"}]';

-- =============================================================================
-- MEDIUM PRIORITY INDEXES - Large Hash Set & OR Query Optimization
-- =============================================================================

-- 6. Hash Array/VALUES Optimization 
-- Optimizes: Large hash set joins (>10k hashes) using PostgreSQL VALUES approach
-- Query pattern: JOIN (VALUES ('hash1'), ('hash2'), ...) AS hash_values(hash) ON transaction.tx_hash = hash_values.hash
CREATE INDEX CONCURRENTLY idx_transaction_hash_btree
    ON transaction USING hash(tx_hash);

-- 7. UTXO Transaction Join Index
-- Optimizes: OR queries that join transaction with address_utxo for currency filtering
-- Query pattern: JOIN address_utxo ON transaction.tx_hash = address_utxo.tx_hash ORDER BY slot DESC
CREATE INDEX CONCURRENTLY idx_address_utxo_tx_hash_slot
    ON address_utxo(tx_hash, slot DESC);

-- 8. Block Hash Direct Lookup
-- Optimizes: Direct block hash filtering (WHERE block.hash = ?)
-- Query pattern: WHERE block.hash = ? AND block.number = ?
CREATE INDEX CONCURRENTLY idx_block_hash_number
    ON block(hash, number);

-- 9. Transaction Size Join Index
-- Optimizes: transaction_size table joins (present in every query for tx size data)
-- Query pattern: LEFT JOIN transaction_size ON transaction.tx_hash = transaction_size.tx_hash
CREATE INDEX CONCURRENTLY idx_transaction_size_hash
    ON transaction_size(tx_hash);

-- =============================================================================
-- LOW PRIORITY INDEXES - Edge Cases & Specialized Queries
-- =============================================================================

-- 10. Epoch-Based Filtering
-- Optimizes: Epoch-based search queries (rare usage pattern)
-- Query pattern: WHERE block.epoch = ? ORDER BY slot DESC
-- CREATE INDEX CONCURRENTLY idx_block_epoch_slot_desc
--     ON block(epoch, slot DESC);

-- 11. Multi-Currency Composite Index
-- Optimizes: Multiple currency filtering edge cases (queries with multiple asset filters)
-- Query pattern: WHERE amounts has multiple policy_id conditions
-- CREATE INDEX CONCURRENTLY idx_address_utxo_currency_composite
--     ON address_utxo(tx_hash) 
--     WHERE amounts::jsonb ? 'policy_id';

-- 12. Window Function Count Optimization
-- Optimizes: COUNT() OVER() window functions for single-query pagination
-- Query pattern: SELECT ..., COUNT(*) OVER() as total_count FROM ... ORDER BY slot DESC
CREATE INDEX CONCURRENTLY idx_transaction_count_support
    ON transaction(slot DESC)
    INCLUDE (tx_hash, block_hash);
