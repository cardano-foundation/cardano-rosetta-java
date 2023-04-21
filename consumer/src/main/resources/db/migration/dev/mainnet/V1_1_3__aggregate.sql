SET SCHEMA 'mainnet';

INSERT INTO address_tx_balance(tx_id, address, balance)
SELECT CASE
           WHEN a.txid IS NULL THEN b.txid
           ELSE a.txid
           END,
       CASE
           WHEN a.address IS NULL THEN b.address
           ELSE a.address
           END,
       (COALESCE(a.balance, 0) - COALESCE(b.balance, 0))
FROM (SELECT DISTINCT tx_id AS txid, tx_out.address AS address, sum(tx_out.value) AS balance
      FROM tx_out
      GROUP BY address, tx_id) AS a
         FULL JOIN
     (SELECT DISTINCT tx_in_id AS txid, tx_out.address AS address, sum(tx_out.value) AS balance
      FROM tx_in
               INNER JOIN tx_out
                          ON tx_in.tx_out_id = tx_out.tx_id AND tx_in.tx_out_index = tx_out.INDEX
      GROUP BY address, tx_in_id) AS b
     ON a.txid = b.txid AND a.address = b.address;

INSERT INTO address(address, balance, tx_count, address_has_script)
SELECT atb.address, sum(balance), count(tx_id), false
FROM address_tx_balance atb
GROUP BY atb.address
ORDER BY sum(balance) DESC;

UPDATE address_tx_balance
SET "time" = b."time"
    FROM tx
INNER JOIN block b ON b.id = tx.block_id
WHERE address_tx_balance.tx_id = tx.id
