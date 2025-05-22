CREATE TABLE IF NOT EXISTS transaction_size (
    tx_hash TEXT PRIMARY KEY,
    block_number BIGINT NOT NULL,
    size INTEGER NOT NULL,
    script_size INTEGER NOT NULL
);