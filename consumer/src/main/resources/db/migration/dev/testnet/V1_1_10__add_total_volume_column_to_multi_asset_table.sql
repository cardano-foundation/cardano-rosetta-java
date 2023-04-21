SET SCHEMA 'testnet';

ALTER TABLE multi_asset
    ADD COLUMN total_volume numeric(40);

UPDATE multi_asset ma
SET total_volume = (SELECT SUM(balance)
                    FROM address_token atk
                    WHERE atk.ident = ma.id AND atk.balance > 0)
