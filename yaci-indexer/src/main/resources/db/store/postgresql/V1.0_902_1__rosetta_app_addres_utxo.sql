DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_address_utxo_owner_addr_block'
        AND tablename = 'address_utxo'
        AND schemaname = current_schema()
    ) THEN
        CREATE INDEX idx_address_utxo_owner_addr_block ON address_utxo (owner_addr, block);
    END IF;
END $$;