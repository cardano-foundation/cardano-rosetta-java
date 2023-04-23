SET SCHEMA 'testnet';

create table address_token_balance
(
    id         bigserial   not null
        primary key,
    is_deleted boolean default false,
    address_id bigint,
    balance    numeric(39) not null,
    ident      bigint
);

alter table address_token_balance
    OWNER TO "rosetta_db_admin";

insert into address_token_balance(address_id, ident, balance)
select address_id, ident, sum(balance)
from address_token at2
group by address_id, ident;

CREATE INDEX IF NOT EXISTS idx_address_token_balance_address_id ON address_token_balance (address_id);
CREATE INDEX IF NOT EXISTS idx_address_token_balance_ident ON address_token_balance (ident);
CREATE INDEX IF NOT EXISTS idx_address_token_balance_address_id_ident ON address_token_balance (address_id, ident);
CREATE INDEX IF NOT EXISTS idx_address_token_balance_ident_address_id ON address_token_balance (ident, address_id);

ALTER TABLE address_token_balance
    ADD CONSTRAINT address_token__balance_address_id_fk FOREIGN KEY (address_id) REFERENCES address
        ON DELETE CASCADE;

ALTER TABLE address_token_balance
    ADD CONSTRAINT address_token__balance_ident_fkey FOREIGN KEY (ident) REFERENCES multi_asset
        ON DELETE CASCADE;
