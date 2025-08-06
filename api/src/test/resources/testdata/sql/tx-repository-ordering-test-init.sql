-- Insert blocks with different block times to test ordering (use slot for ordering since block_time might not be available)
INSERT INTO block (hash, number, slot) VALUES ('blockHash1', 1, 100); -- oldest
INSERT INTO block (hash, number, slot) VALUES ('blockHash2', 2, 200); -- middle  
INSERT INTO block (hash, number, slot) VALUES ('blockHash3', 3, 300); -- newest

-- Insert transactions into blocks (txs in later blocks should appear first due to DESC ordering)
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHash1', 'blockHash1', 1000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHash2', 'blockHash1', 2000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHash3', 'blockHash2', 3000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHash4', 'blockHash3', 4000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHash5', 'blockHash3', 5000000);

-- Insert some address UTXOs for the OR query join
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHash1', 0, 'addr1', 1);
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHash3', 0, 'addr2', 2);
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHash5', 0, 'addr3', 3);