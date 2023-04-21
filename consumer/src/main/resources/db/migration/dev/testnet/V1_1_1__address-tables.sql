create table testnet.address
(
    id                 bigserial
        primary key,
    is_deleted         boolean default false,
    address            varchar(65535) not null,
    address_has_script boolean        not null,
    balance            numeric(39)    not null,
    tx_count           bigint,
    stake_address_id   bigint
        references testnet.stake_address
            on delete cascade
);

alter table testnet.address
    owner to "cardano-master";

create table testnet.address_token
(
    id         bigserial
        primary key,
    is_deleted boolean default false,
    address    varchar(65535) not null,
    balance    numeric(39)  not null,
    ident      bigint       not null
        references testnet.multi_asset
            on delete cascade,
    tx_id      bigint       not null
        references testnet.tx
            on delete cascade
);

alter table testnet.address_token
    owner to "cardano-master";

create table testnet.address_tx_balance
(
    id         bigserial
        primary key,
    is_deleted boolean default false,
    address    varchar(65535) not null,
    balance    numeric(39)  not null,
    time       timestamp,
    tx_id      bigint       not null
        references testnet.tx
            on delete cascade
);

alter table testnet.address_tx_balance
    owner to "cardano-master";

create table testnet.asset_metadata
(
    id          bigserial
        primary key,
    is_deleted  boolean default false,
    decimals    integer         not null,
    description varchar(255)    not null,
    logo        varchar(100000) not null,
    name        varchar(255)    not null,
    policy      varchar(255)    not null,
    subject     varchar(255)    not null,
    ticker      varchar(255)    not null,
    url         varchar(255)    not null,
    ident       bigint          not null
        constraint uk_2pffnc56l6g8ll80g7oibr58w
            unique
        references testnet.multi_asset
            on delete cascade
);

alter table testnet.asset_metadata
    owner to "cardano-master";
