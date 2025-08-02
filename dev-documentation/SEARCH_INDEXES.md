 Primary Performance Indexes

  1. Transaction Table Indexes

  -- Core lookup index (most important)
  CREATE INDEX CONCURRENTLY idx_transaction_tx_hash ON transaction(tx_hash);

  -- Slot-based ordering with hash (for pagination)
  CREATE INDEX CONCURRENTLY idx_transaction_slot_desc_tx_hash ON transaction(slot DESC, tx_hash);

  -- Block-based filtering
  CREATE INDEX CONCURRENTLY idx_transaction_block_hash ON transaction(block_hash);

  -- Combined slot and block for filtered pagination
  CREATE INDEX CONCURRENTLY idx_transaction_block_slot_desc ON transaction(block_hash, slot DESC);

  2. Block Table Indexes

  -- Primary block lookup
  CREATE INDEX CONCURRENTLY idx_block_hash ON block(hash);

  -- Block number filtering (for maxBlock, blockNumber conditions)
  CREATE INDEX CONCURRENTLY idx_block_number ON block(number);

  -- Combined for range queries
  CREATE INDEX CONCURRENTLY idx_block_number_hash ON block(number, hash);

  3. Invalid Transaction Indexes

  -- Success/failure filtering
  CREATE INDEX CONCURRENTLY idx_invalid_transaction_tx_hash ON invalid_transaction(tx_hash);

  4. Transaction Size Indexes

  -- Size data lookup
  CREATE INDEX CONCURRENTLY idx_transaction_size_tx_hash ON transaction_size(tx_hash);

  5. Address UTXO Indexes

  -- Basic UTXO lookup
  CREATE INDEX CONCURRENTLY idx_address_utxo_tx_hash ON address_utxo(tx_hash);

  -- JSONB currency filtering (most important for performance)
  CREATE INDEX CONCURRENTLY idx_address_utxo_amounts_gin ON address_utxo USING GIN (amounts);

  -- Specific JSONB path indexes for common currency searches
  CREATE INDEX CONCURRENTLY idx_address_utxo_policy_id_gin
  ON address_utxo USING GIN ((amounts -> 'policy_id'));

  CREATE INDEX CONCURRENTLY idx_address_utxo_unit_gin
  ON address_utxo USING GIN ((amounts -> 'unit'));

  CREATE INDEX CONCURRENTLY idx_address_utxo_asset_name_gin
  ON address_utxo USING GIN ((amounts -> 'asset_name'));

  Advanced Composite Indexes

  6. Multi-Column Performance Indexes

  -- For complex AND searches with success filtering
  CREATE INDEX CONCURRENTLY idx_transaction_success_slot
  ON transaction(tx_hash, slot DESC)
  WHERE tx_hash NOT IN (SELECT tx_hash FROM invalid_transaction);

  -- For block-based searches with ordering
  CREATE INDEX CONCURRENTLY idx_transaction_block_slot_success
  ON transaction(block_hash, slot DESC, tx_hash);

  -- For maximum block filtering with ordering
  CREATE INDEX CONCURRENTLY idx_block_number_slot_combined
  ON transaction t JOIN block b ON t.block_hash = b.hash (b.number, t.slot DESC);

  7. Partial Indexes for Specific Cases

  -- Only successful transactions (eliminates INVALID_TRANSACTION join)
  CREATE INDEX CONCURRENTLY idx_transaction_successful_only
  ON transaction(tx_hash, slot DESC)
  WHERE tx_hash NOT IN (SELECT tx_hash FROM invalid_transaction);

  -- Only failed transactions
  CREATE INDEX CONCURRENTLY idx_transaction_failed_only
  ON transaction(tx_hash, slot DESC)
  WHERE tx_hash IN (SELECT tx_hash FROM invalid_transaction);

  -- Transactions with UTXO data
  CREATE INDEX CONCURRENTLY idx_transaction_with_utxos
  ON transaction(tx_hash, slot DESC)
  WHERE tx_hash IN (SELECT DISTINCT tx_hash FROM address_utxo);

  Specialized JSONB Indexes

  8. Currency-Specific JSONB Indexes

  -- For ADA/Lovelace searches (most common)
  CREATE INDEX CONCURRENTLY idx_address_utxo_lovelace
  ON address_utxo(tx_hash)
  WHERE amounts @> '[{"unit": "lovelace"}]';

  -- For policy-based token searches
  CREATE INDEX CONCURRENTLY idx_address_utxo_policy_tokens
  ON address_utxo(tx_hash)
  WHERE amounts @> '[{"policy_id": ""}]'::jsonb;

  -- Expression index for policy ID extraction
  CREATE INDEX CONCURRENTLY idx_address_utxo_policy_ids_expr
  ON address_utxo USING GIN ((jsonb_path_query_array(amounts, '$[*].policy_id')));

  Priority Implementation Order

  Tier 1 (Critical):

  - idx_transaction_tx_hash
  - idx_transaction_slot_desc_tx_hash
  - idx_address_utxo_amounts_gin
  - idx_block_hash

  Tier 2 (High Impact):

  - idx_invalid_transaction_tx_hash
  - idx_address_utxo_tx_hash
  - idx_block_number
  - idx_transaction_size_tx_hash

  Tier 3 (Optimization):

  - Partial indexes for success/failure filtering
  - Specialized JSONB path indexes
  - Composite indexes for specific query patterns
