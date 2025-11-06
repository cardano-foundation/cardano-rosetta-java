-- =============================================================================
-- Index 1: Currency/Asset Filtering with JSONB GIN Index
-- Used by: PostgreSQLCurrencyConditionBuilder (TxRepositoryPostgreSQLImpl.java:195-220)
-- Query pattern: EXISTS (...WHERE au.amounts::jsonb @> '[{"policy_id": "...", "asset_name": "..."}]')
-- Optimizes: All currency-based filtering in search transactions API endpoints:
--   - /search/transactions with currency parameter (ADA/lovelace and native assets)
--   - JSONB @> operator queries for policy_id, asset_name, and unit matching
-- Performance: Critical for currency searches - without this, full table scans on address_utxo
-- =============================================================================
CREATE INDEX idx_address_utxo_amounts_gin ON address_utxo USING gin (amounts);

-- =============================================================================
-- Index 2: Address-to-Transaction Hash Mapping
-- Used by: AddressHistoryRepositoryPostgresImpl.findCompleteTransactionHistoryByAddress (lines 35-38)
-- Query pattern: SELECT DISTINCT tx_hash FROM address_utxo WHERE owner_addr = ? OR owner_stake_addr = ?
-- Optimizes: Address-based transaction searches in search transactions API endpoints:
--   - /search/transactions with account_identifier parameter
--   - Address history lookups that feed into transaction search filters
-- Performance: Enables fast owner_addr lookups with tx_hash in covering index
-- =============================================================================
CREATE INDEX idx_address_utxo_owner_addr_tx_hash ON address_utxo USING btree (owner_addr, tx_hash);

-- =============================================================================
-- Index 3: Block Hash Lookup with Covering Data
-- Used by: TxRepositoryCustomBase.buildBaseResultsQuery and buildBaseCountQuery (lines 82, 100)
-- Query pattern: LEFT JOIN block ON transaction.block_hash = block.hash
-- Optimizes: All search transactions API endpoints that need block information:
--   - /search/transactions with block_identifier parameter
--   - Block number and slot data retrieval in transaction results
--   - Block filtering with maxBlock parameter
-- Performance: Covering index avoids heap lookups for number and slot
-- =============================================================================
CREATE INDEX idx_block_hash_covering ON block USING btree (hash) INCLUDE (number, slot);

-- =============================================================================
-- Index 4: Transaction Success/Failure Status Filtering
-- Used by: TxRepositoryCustomBase.buildBaseResultsQuery and buildBaseCountQuery (lines 85, 103)
-- Query pattern: LEFT JOIN invalid_transaction ON transaction.tx_hash = invalid_transaction.tx_hash
-- Optimizes: Search transactions API endpoints with status/success filtering:
--   - /search/transactions with status parameter (success/failure)
--   - Filtering by transaction validity status
-- Performance: Fast JOIN for success condition evaluation (lines 87-91 in TxRepositoryQueryBuilder)
-- =============================================================================
CREATE INDEX idx_invalid_transaction_hash_slot ON invalid_transaction USING btree (tx_hash, slot);

-- =============================================================================
-- Index 5: Transaction Hash Direct Lookups for VALUES Table Optimization  
-- Used by: TxRepositoryPostgreSQLImpl VALUES table approach (lines 160-175)
-- Query pattern: SELECT ... FROM transaction JOIN (VALUES ...) AS hash_values ON transaction.tx_hash = hash_values.hash
-- Optimizes: Search transactions API endpoints with hash-based filtering:
--   - /search/transactions with transaction_identifier parameter
--   - Large hash set filtering using PostgreSQL VALUES tables
--   - Both direct tx hash searches and address-derived hash searches
-- Performance: Critical for hash-based searches - optimizes VALUES table JOINs
-- =============================================================================
CREATE INDEX idx_transaction_hash_values_join ON transaction USING btree (tx_hash);

-- =============================================================================
-- Index 6: Transaction Slot and Index Ordering for Search Pagination
-- Used by: TxRepositoryCustomBase.executeResultsQuery (line 190)
-- Query pattern: SELECT ... FROM transaction WHERE ... ORDER BY slot DESC, tx_index DESC LIMIT/OFFSET
-- Optimizes: All paginated search transactions API endpoints:
--   - /search/transactions pagination with proper chronological and within-slot ordering
--   - Ensures consistent transaction ordering within the same slot
--   - Maintains deterministic pagination order using tx_index (replaces tx_hash)
-- Performance: Essential for correct pagination - maintains transaction order by slot then tx_index
-- Note: tx_index column added in Yaci-Store starting from 2.0.0 for proper within-block ordering
-- =============================================================================
CREATE INDEX idx_transaction_slot_desc_tx_index_desc ON transaction USING btree (slot DESC, tx_index DESC);
