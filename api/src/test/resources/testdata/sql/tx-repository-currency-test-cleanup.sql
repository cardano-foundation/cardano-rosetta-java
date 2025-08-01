-- Clean up address_utxo records
DELETE FROM address_utxo
WHERE tx_hash IN (
    'tx_lovelace_1', 'tx_lovelace_2', 'tx_native_asset_1', 
    'tx_mixed_assets_1', 'tx_policy_specific_1', 'tx_min_token_1'
);

-- Clean up transaction records
DELETE FROM transaction
WHERE tx_hash IN (
    'tx_lovelace_1', 'tx_lovelace_2', 'tx_native_asset_1', 
    'tx_mixed_assets_1', 'tx_policy_specific_1', 'tx_min_token_1'
);

-- Clean up block records
DELETE FROM block
WHERE hash IN ('currency_block_1', 'currency_block_2', 'currency_block_3');