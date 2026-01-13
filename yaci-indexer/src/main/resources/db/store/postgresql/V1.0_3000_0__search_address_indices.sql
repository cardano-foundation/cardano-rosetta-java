-- =============================================================================
-- Missing indexes identified by comparing v2.0 with v1.4.3 databases
-- These indexes exist in v1.4.3 but were missing from v2.0, causing the search
-- endpoint to be ~85,000x slower when querying by stake address
-- Reference: Exchange tooling sync-up 2026-01-13
-- =============================================================================

-- =============================================================================
-- Index 1: Stake Address Lookup (PRIMARY FIX)
-- Used by: AddressHistoryRepositoryPostgresImpl.findCompleteTransactionHistoryByAddress
-- Query pattern: SELECT DISTINCT tx_hash FROM address_utxo WHERE owner_stake_addr = ?
-- Performance: Without this index, queries take ~114 seconds (seq scan) vs ~1.3ms (index scan)
-- This is the critical missing index causing the 2.5min vs 6sec performance gap
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_address_utxo_owner_stake_addr ON address_utxo USING btree (owner_stake_addr);

-- =============================================================================
-- Index 2: Standalone Owner Address Lookup
-- Complements idx_address_utxo_owner_addr_tx_hash for pure address lookups
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_address_utxo_owner_addr ON address_utxo USING btree (owner_addr);

-- =============================================================================
-- Index 3: Payment Credential Lookup
-- Used for queries filtering by payment credential hash
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_address_utxo_owner_paykey_hash ON address_utxo USING btree (owner_payment_credential);

-- =============================================================================
-- Index 4: Stake Credential Lookup
-- Used for queries filtering by stake credential hash
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_address_utxo_owner_stakekey_hash ON address_utxo USING btree (owner_stake_credential);

-- =============================================================================
-- Index 5: Epoch Filtering on Address UTXOs
-- Used for time-based filtering of address history
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_address_utxo_epoch ON address_utxo USING btree (epoch);
