CREATE TABLE IF NOT EXISTS transaction_size (
    tx_hash VARCHAR(1000) PRIMARY KEY,
    block_number BIGINT NOT NULL,
    size INT NOT NULL,
    script_size INT NOT NULL
);