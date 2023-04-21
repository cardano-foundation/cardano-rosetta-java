create table preprod.address
(
    id                 bigserial
        primary key,
    is_deleted         boolean default false,
    address            varchar(65535) not null,
    address_has_script boolean        not null,
    balance            numeric(39)    not null,
    tx_count           bigint,
    stake_address_id   bigint
        references preprod.stake_address
            on delete cascade
);

alter table preprod.address
    owner to "cardano-master";

create table preprod.address_token
(
    id         bigserial
        primary key,
    is_deleted boolean default false,
    address    varchar(65535) not null,
    balance    numeric(39)  not null,
    ident      bigint       not null
        references preprod.multi_asset
            on delete cascade,
    tx_id      bigint       not null
        references preprod.tx
            on delete cascade
);

alter table preprod.address_token
    owner to "cardano-master";

create table preprod.address_tx_balance
(
    id         bigserial
        primary key,
    is_deleted boolean default false,
    address    varchar(65535) not null,
    balance    numeric(39)  not null,
    time       timestamp,
    tx_id      bigint       not null
        references preprod.tx
            on delete cascade
);

alter table preprod.address_tx_balance
    owner to "cardano-master";

create table preprod.asset_metadata
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
        references preprod.multi_asset
            on delete cascade
);

alter table preprod.asset_metadata
    owner to "cardano-master";
