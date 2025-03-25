DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_address_utxo_tx_hash'
        AND tablename = 'address_utxo'
        AND schemaname = current_schema()
    ) THEN
        CREATE INDEX idx_address_utxo_tx_hash ON address_utxo USING btree (tx_hash);
    END IF;
END $$;