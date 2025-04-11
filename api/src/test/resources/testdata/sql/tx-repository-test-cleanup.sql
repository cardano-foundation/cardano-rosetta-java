DELETE FROM address_utxo
WHERE (tx_hash = 'txHashA' AND output_index IN (0, 1, 2))
   OR (tx_hash = 'txHashB' AND output_index = 0);

DELETE FROM transaction
WHERE tx_hash IN ('txHashA', 'txHashB', 'txHashC');

DELETE FROM block
WHERE hash IN ('blockHash1', 'blockHash2');