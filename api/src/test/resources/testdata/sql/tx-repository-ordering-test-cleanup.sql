-- Cleanup test data for ordering tests
DELETE FROM address_utxo WHERE tx_hash IN ('txHash1', 'txHash2', 'txHash3', 'txHash4', 'txHash5');
DELETE FROM transaction WHERE tx_hash IN ('txHash1', 'txHash2', 'txHash3', 'txHash4', 'txHash5');
DELETE FROM block WHERE hash IN ('blockHash1', 'blockHash2', 'blockHash3');