SET SCHEMA 'preprod';

ALTER TABLE address_token ADD address_id bigint;

ALTER TABLE address_token
    ADD CONSTRAINT address_token_address_id_fk FOREIGN KEY (address_id) REFERENCES address
        ON DELETE CASCADE;

UPDATE address_token
SET address_id = a.id FROM address a
WHERE a.address = address_token.address;

CREATE INDEX IF NOT EXISTS idx_address_token_address_id ON address_token (address_id);

ALTER TABLE address_token DROP COLUMN address;
ALTER TABLE address_token ALTER COLUMN address_id SET NOT NULL;


ALTER TABLE address_tx_balance ADD address_id bigint;

ALTER TABLE address_tx_balance
    ADD CONSTRAINT address_tx_balance_address_id_fk FOREIGN KEY (address_id) REFERENCES address
        ON DELETE CASCADE;

UPDATE address_tx_balance
SET address_id = a.id FROM address a
WHERE a.address = address_tx_balance.address;

CREATE INDEX IF NOT EXISTS idx_address_tx_balance_address_id ON address_tx_balance (address_id);

ALTER TABLE address_tx_balance DROP COLUMN address;
ALTER TABLE address_tx_balance ALTER COLUMN address_id SET NOT NULL;
