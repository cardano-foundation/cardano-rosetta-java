-- Clean up invalid_transaction records
DELETE FROM invalid_transaction
WHERE tx_hash IN ('failedTx1', 'failedTx2');

-- Clean up transaction records
DELETE FROM transaction
WHERE tx_hash IN ('successTx1', 'successTx2', 'successTx3', 'failedTx1', 'failedTx2');

-- Clean up block records
DELETE FROM block
WHERE hash IN ('successBlock1', 'successBlock2');