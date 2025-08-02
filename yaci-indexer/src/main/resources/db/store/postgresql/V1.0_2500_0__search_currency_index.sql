-- Index on amounts JSONB for faster currency filtering
CREATE INDEX idx_address_utxo_amounts_gin ON address_utxo USING gin(amounts);

-- CREATE INDEX CONCURRENTLY idx_transaction_slot_hash
--     ON transaction(slot DESC, tx_hash);

 -- For filtering by success status
--  CREATE INDEX CONCURRENTLY idx_tx_hash_success
--  ON transaction(tx_hash) WHERE tx_hash NOT IN (SELECT tx_hash FROM invalid_transaction);