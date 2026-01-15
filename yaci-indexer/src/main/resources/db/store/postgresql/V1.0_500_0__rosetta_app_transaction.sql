-- Transaction table - Block lookups
CREATE INDEX idx_transaction_block ON transaction(block);

-- Transaction table - Block hash lookups
CREATE INDEX idx_transaction_block_hash ON transaction(block_hash);
