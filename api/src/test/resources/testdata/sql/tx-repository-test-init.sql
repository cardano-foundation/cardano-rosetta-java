INSERT INTO block (hash, number, slot) VALUES ('blockHash1', 1, 100);
INSERT INTO block (hash, number, slot) VALUES ('blockHash2', 2, 200);

INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHashA', 'blockHash1', 1000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHashB', 'blockHash1', 2000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('txHashC', 'blockHash2', 3000000);

INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHashA', 0, 'addr1', 1);
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHashA', 1, 'addr2', 1);
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHashA', 2, 'addr3', 1);

INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block) VALUES ('txHashB', 0, 'addr4', 1);
