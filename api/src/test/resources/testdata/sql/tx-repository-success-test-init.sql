-- Insert blocks for testing
INSERT INTO block (hash, number, slot, block_time) VALUES ('successBlock1', 1, 100, 1234567890);
INSERT INTO block (hash, number, slot, block_time) VALUES ('successBlock2', 2, 200, 1234567900);

-- Insert successful transactions (not in invalid_transaction table)
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('successTx1', 'successBlock1', 1000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('successTx2', 'successBlock1', 2000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('successTx3', 'successBlock2', 3000000);

-- Insert failed transactions (will be in invalid_transaction table)
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('failedTx1', 'successBlock1', 1500000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('failedTx2', 'successBlock2', 2500000);

-- Mark failed transactions as invalid
INSERT INTO invalid_transaction (tx_hash, slot, block_hash) VALUES ('failedTx1', 100, 'successBlock1');
INSERT INTO invalid_transaction (tx_hash, slot, block_hash) VALUES ('failedTx2', 200, 'successBlock2');