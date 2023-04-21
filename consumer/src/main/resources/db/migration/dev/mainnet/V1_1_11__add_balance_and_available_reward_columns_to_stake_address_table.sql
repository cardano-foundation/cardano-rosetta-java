SET SCHEMA 'mainnet';

ALTER TABLE stake_address
    ADD COLUMN balance          numeric(39),
    ADD COLUMN available_reward numeric(39);

UPDATE stake_address sa
SET balance = (SELECT SUM(balance) FROM address WHERE address.stake_address_id = sa.id);

UPDATE stake_address sa
SET available_reward = (select (select coalesce(sum(r.amount), 0)
                                from reward r
                                where r.addr_id = sa.id)
                                   -
                               (select coalesce(sum(w.amount), 0)
                                from withdrawal w
                                where w.addr_id = sa.id));