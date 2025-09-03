-- Insert test blocks
INSERT INTO block (hash, number, slot) VALUES ('currency_block_1', 100, 1000);
INSERT INTO block (hash, number, slot) VALUES ('currency_block_2', 101, 1100);
INSERT INTO block (hash, number, slot) VALUES ('currency_block_3', 102, 1200);

-- Insert test transactions
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_lovelace_1', 'currency_block_1', 1000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_lovelace_2', 'currency_block_1', 2000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_native_asset_1', 'currency_block_2', 3000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_mixed_assets_1', 'currency_block_2', 4000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_policy_specific_1', 'currency_block_3', 5000000);
INSERT INTO transaction (tx_hash, block_hash, fee) VALUES ('tx_min_token_1', 'currency_block_3', 6000000);

-- Insert address_utxo records with JSONB amounts for different currency scenarios

-- Transaction with only lovelace (ADA)
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_lovelace_1', 0, 'addr_lovelace_1', 100, '[{"unit": "lovelace", "quantity": "5000000"}]');

INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_lovelace_2', 0, 'addr_lovelace_2', 100, '[{"unit": "lovelace", "quantity": "3000000"}]');

-- Transaction with native asset (policy ID + asset name)
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_native_asset_1', 0, 'addr_native_1', 101, '[
  {"unit": "lovelace", "quantity": "2000000"},
  {"policy_id": "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6", "asset_name": "MIN", "quantity": "1000000"}
]');

-- Transaction with mixed assets
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_mixed_assets_1', 0, 'addr_mixed_1', 101, '[
  {"unit": "lovelace", "quantity": "1500000"},
  {"policy_id": "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6", "asset_name": "MIN", "quantity": "500000"},
  {"policy_id": "d5e6bf0500378d4f0da4e8dde6becec7621cd8cbf5cbb9b87013d4cc", "asset_name": "537061636542756433343132", "quantity": "100"}
]');

-- Transaction with specific policy ID only assets
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_policy_specific_1', 0, 'addr_policy_1', 102, '[
  {"unit": "lovelace", "quantity": "2500000"},
  {"policy_id": "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6", "asset_name": "TOKEN1", "quantity": "1000"},
  {"policy_id": "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6", "asset_name": "TOKEN2", "quantity": "2000"}
]');

-- Transaction with MIN tokens from different policy
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_min_token_1', 0, 'addr_min_1', 102, '[
  {"unit": "lovelace", "quantity": "1000000"},
  {"policy_id": "a1b2c3d4e5f6789012345678901234567890123456789012345678901234", "asset_name": "MIN", "quantity": "750000"}
]');

-- Additional UTXOs for same transactions (outputs)
INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_native_asset_1', 1, 'addr_native_2', 101, '[
  {"unit": "lovelace", "quantity": "1000000"}
]');

INSERT INTO address_utxo (tx_hash, output_index, owner_addr, block, amounts) 
VALUES ('tx_mixed_assets_1', 1, 'addr_mixed_2', 101, '[
  {"unit": "lovelace", "quantity": "500000"},
  {"policy_id": "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6", "asset_name": "MIN", "quantity": "250000"}
]');