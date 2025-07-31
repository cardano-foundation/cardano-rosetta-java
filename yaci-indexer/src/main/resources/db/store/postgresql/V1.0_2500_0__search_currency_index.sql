-- Index on amounts JSONB for faster currency filtering
CREATE INDEX idx_address_utxo_amounts_gin ON address_utxo USING gin(amounts);