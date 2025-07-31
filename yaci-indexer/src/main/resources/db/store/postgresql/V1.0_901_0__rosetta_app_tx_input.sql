DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_input_tx_out_spent_block'
        AND tablename = 'tx_input'
        AND schemaname = current_schema()
    ) THEN
        CREATE INDEX idx_input_tx_out_spent_block ON tx_input (tx_hash, output_index, spent_at_block);
    END IF;
END $$;