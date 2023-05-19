CREATE TYPE rewardtype AS ENUM (
    'leader',
    'member',
    'reserves',
    'treasury',
    'refund'
    );

CREATE TYPE scriptpurposetype AS ENUM (
    'spend',
    'mint',
    'cert',
    'reward'
    );

CREATE TYPE scripttype AS ENUM (
    'multisig',
    'timelock',
    'plutusV1',
    'plutusV2'
    );

CREATE TYPE syncstatetype AS ENUM (
    'lagging',
    'following'
    );

create table delisted_pool
(
    id         bigserial
        primary key,
    hash_raw   bytea not null
        constraint unique_delisted_pool
            unique,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table epoch
(
    id         bigserial
        primary key,
    blk_count  integer        not null,
    end_time   timestamp      not null,
    fees       numeric(20, 0) not null,
    no         integer        not null
        constraint unique_epoch
            unique,
    out_sum    numeric(39)    not null,
    start_time timestamp      not null,
    tx_count   integer        not null,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table epoch_sync_time
(
    id         bigserial
        primary key,
    no         bigint        not null
        constraint unique_epoch_sync_time
            unique,
    seconds    bigint        not null,
    state      syncstatetype not null,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table meta
(
    id           bigserial
        primary key,
    network_name character varying not null,
    start_time   timestamp         not null
        constraint unique_meta
            unique,
    version      character varying not null,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false
);

create table multi_asset
(
    id          bigserial
        primary key,
    fingerprint character varying   not null,
    policy      bytea               not null,
    name        bytea               not null,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false,
    constraint unique_multi_asset
        unique (policy, name)
);

create table ma_tx_out
(
    id        bigserial
        primary key,
    quantity  numeric(20, 0) not null,
    ident     bigint      not null
        references multi_asset
            on delete cascade,
    tx_out_id  bigint         not null,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_ma_tx_out
        unique (ident, tx_out_id)
);

create table pool_hash
(
    id         bigserial
        primary key,
    hash_raw   bytea             not null
        constraint unique_pool_hash
            unique,
    view       character varying not null,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table reserved_pool_ticker
(
    id         bigserial
        primary key,
    name       character varying not null
        constraint unique_reserved_pool_ticker
            unique,
    pool_hash  bytea             not null,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table schema_version
(
    id          bigserial
        primary key,
    stage_one   bigint not null,
    stage_three bigint not null,
    stage_two   bigint not null,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false
);

create table slot_leader
(
    id           bigserial
        primary key,
    description  character varying not null,
    hash         bytea             not null
        constraint unique_slot_leader
            unique,
    pool_hash_id bigint
        references pool_hash
            on delete cascade,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false
);

create table block
(
    id              bigserial
        primary key,
    block_no        bigint    not null,
    epoch_no        integer,
    epoch_slot_no   integer,
    hash            bytea     not null
        constraint unique_block
            unique,
    op_cert         bytea,
    op_cert_counter bigint,
    proto_major     integer   not null,
    proto_minor     integer   not null,
    size            integer   not null,
    slot_no         bigint,
    time            timestamp not null,
    tx_count        bigint    not null,
    vrf_key         character varying,
    previous_id     bigint
        references block
            on delete cascade,
    slot_leader_id  bigint    not null
        references slot_leader
            on delete cascade,
    created_at      timestamp,
    updated_at      timestamp,
    is_deleted      boolean default false
);

create table ada_pots
(
    id         bigserial
        primary key,
    deposits   numeric(20, 0) not null,
    epoch_no   integer        not null,
    fees       numeric(20, 0) not null,
    reserves   numeric(20, 0) not null,
    rewards    numeric(20, 0) not null,
    slot_no    bigint         not null,
    treasury   numeric(20, 0) not null,
    utxo       numeric(20, 0) not null,
    block_id   bigint         not null
        constraint unique_ada_pots
            unique
        references block
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table cost_model
(
    id         bigserial
        primary key,
    costs      jsonb  not null,
    hash       bytea  not null
        constraint unique_cost_model
            unique,
    block_id   bigint not null
        references block
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);


create table epoch_param
(
    id                    bigserial
        primary key,
    coins_per_utxo_size   numeric(20, 0),
    collateral_percent    integer,
    decentralisation      double precision not null,
    epoch_no              integer          not null,
    extra_entropy         bytea,
    influence             double precision not null,
    key_deposit           numeric(20, 0)   not null,
    max_bh_size           integer          not null,
    max_block_ex_mem      numeric(20, 0),
    max_block_ex_steps    numeric(20, 0),
    max_block_size        integer          not null,
    max_collateral_inputs integer,
    max_epoch             integer          not null,
    max_tx_ex_mem         numeric(20, 0),
    max_tx_ex_steps       numeric(20, 0),
    max_tx_size           integer          not null,
    max_val_size          numeric(20, 0),
    min_fee_a             integer          not null,
    min_fee_b             integer          not null,
    min_pool_cost         numeric(20, 0)   not null,
    min_utxo_value        numeric(20, 0)   not null,
    monetary_expand_rate  double precision not null,
    nonce                 bytea,
    optimal_pool_count    integer          not null,
    pool_deposit          numeric(20, 0)   not null,
    price_mem             double precision,
    price_step            double precision,
    protocol_major        integer          not null,
    protocol_minor        integer          not null,
    treasury_growth_rate  double precision not null,
    block_id              bigint           not null
        references block
            on delete cascade,
    cost_model_id         bigint
        references cost_model
            on delete cascade,
    created_at            timestamp,
    updated_at            timestamp,
    is_deleted            boolean default false,
    constraint unique_epoch_param
        unique (epoch_no, block_id)
);

create table tx
(
    id                bigserial
        primary key,
    block_index       bigint         not null,
    deposit           bigint         not null,
    fee               numeric(20, 0) not null,
    hash              bytea          not null
        constraint unique_tx
            unique,
    invalid_before    numeric(20, 0),
    invalid_hereafter numeric(20, 0),
    out_sum           numeric(20, 0) not null,
    script_size       integer        not null,
    size              integer        not null,
    valid_contract    boolean        not null,
    block_id          bigint         not null
        references block
            on delete cascade,
    created_at        timestamp,
    updated_at        timestamp,
    is_deleted        boolean default false
);

create table collateral_tx_in
(
    id           bigserial
        primary key,
    tx_out_index smallint not null,
    tx_in_id     bigint   not null
        references tx
            on delete cascade,
    tx_out_id    bigint   not null
        references tx
            on delete cascade,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false,
    constraint unique_col_txin
        unique (tx_in_id, tx_out_id, tx_out_index)
);

create table datum
(
    id         bigserial
        primary key,
    bytes      bytea  not null,
    hash       bytea  not null
        constraint unique_datum
            unique,
    value      jsonb,
    tx_id      bigint not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table extra_key_witness
(
    id         bigserial
        primary key,
    hash       bytea  not null
        constraint unique_witness
            unique,
    tx_id      bigint not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table ma_tx_mint
(
    id         bigserial
        primary key,
    quantity   numeric(20, 0) not null,
    ident      bigint         not null
        references multi_asset
            on delete cascade,
    tx_id      bigint         not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_ma_tx_mint
        unique (ident, tx_id)
);

create table param_proposal
(
    id                    bigserial
        primary key,
    coins_per_utxo_size   numeric(20, 0),
    collateral_percent    integer,
    decentralisation      double precision,
    entropy               bytea,
    epoch_no              integer not null,
    influence             double precision,
    key                   bytea   not null,
    key_deposit           numeric(20, 0),
    max_bh_size           numeric(20, 0),
    max_block_ex_mem      numeric(20, 0),
    max_block_ex_steps    numeric(20, 0),
    max_block_size        numeric(20, 0),
    max_collateral_inputs integer,
    max_epoch             numeric(20, 0),
    max_tx_ex_mem         numeric(20, 0),
    max_tx_ex_steps       numeric(20, 0),
    max_tx_size           numeric(20, 0),
    max_val_size          numeric(20, 0),
    min_fee_a             numeric(20, 0),
    min_fee_b             numeric(20, 0),
    min_pool_cost         numeric(20, 0),
    min_utxo_value        numeric(20, 0),
    monetary_expand_rate  double precision,
    optimal_pool_count    numeric(20, 0),
    pool_deposit          numeric(20, 0),
    price_mem             double precision,
    price_step            double precision,
    protocol_major        integer,
    protocol_minor        integer,
    treasury_growth_rate  double precision,
    cost_model_id         bigint
        references cost_model
            on delete cascade,
    registered_tx_id      bigint  not null
        references tx
            on delete cascade,
    created_at            timestamp,
    updated_at            timestamp,
    is_deleted            boolean default false,
    constraint unique_param_proposal
        unique (key, registered_tx_id)
);

create table pool_metadata_ref
(
    id               bigserial
        primary key,
    hash             bytea             not null,
    url              character varying not null,
    pool_id          bigint            not null
        references pool_hash
            on delete cascade,
    registered_tx_id bigint            not null
        references tx
            on delete cascade,
    created_at       timestamp,
    updated_at       timestamp,
    is_deleted       boolean default false,
    constraint unique_pool_metadata_ref
        unique (pool_id, url, hash)
);

create table pool_offline_data
(
    id          bigserial
        primary key,
    bytes       bytea             not null,
    hash        bytea             not null,
    json        jsonb             not null,
    ticker_name character varying not null,
    pool_id     bigint            not null
        references pool_hash
            on delete cascade,
    pmr_id      bigint            not null
        references pool_metadata_ref
            on delete cascade,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false,
    constraint unique_pool_offline_data
        unique (pool_id, hash)
);

create table pool_offline_fetch_error
(
    id          bigserial
        primary key,
    fetch_error character varying not null,
    fetch_time  timestamp         not null,
    retry_count integer           not null,
    pool_id     bigint            not null
        references pool_hash
            on delete cascade,
    pmr_id      bigint            not null
        references pool_metadata_ref
            on delete cascade,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false,
    constraint unique_pool_offline_fetch_error
        unique (pool_id, fetch_time, retry_count)
);

create table pool_retire
(
    id              bigserial
        primary key,
    cert_index      integer not null,
    retiring_epoch  integer not null,
    announced_tx_id bigint  not null
        references tx
            on delete cascade,
    hash_id         bigint  not null
        references pool_hash
            on delete cascade,
    created_at      timestamp,
    updated_at      timestamp,
    is_deleted      boolean default false,
    constraint unique_pool_retiring
        unique (announced_tx_id, cert_index)
);

create table pot_transfer
(
    id         bigserial
        primary key,
    cert_index integer        not null,
    reserves   numeric(20, 0) not null,
    treasury   numeric(20, 0) not null,
    tx_id      bigint         not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_pot_transfer
        unique (tx_id, cert_index)
);

create table redeemer_data
(
    id         bigserial
        primary key,
    bytes      bytea  not null,
    hash       bytea  not null
        constraint unique_redeemer_data
            unique,
    value      jsonb,
    tx_id      bigint not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false
);

create table redeemer
(
    id               bigserial
        primary key,
    fee              numeric(20, 0),
    index            integer           not null,
    purpose          scriptpurposetype not null,
    script_hash      bytea,
    unit_mem         bigint            not null,
    unit_steps       bigint            not null,
    redeemer_data_id bigint            not null
        references redeemer_data
            on delete cascade,
    tx_id            bigint            not null
        references tx
            on delete cascade,
    created_at       timestamp,
    updated_at       timestamp,
    is_deleted       boolean default false,
    constraint unique_redeemer
        unique (tx_id, purpose, index)
);

create table reference_tx_in
(
    id           bigserial
        primary key,
    tx_out_index smallint not null,
    tx_in_id     bigint   not null
        references tx
            on delete cascade,
    tx_out_id    bigint   not null
        references tx
            on delete cascade,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false,
    constraint unique_ref_txin
        unique (tx_in_id, tx_out_id, tx_out_index)
);

create table script
(
    id              bigserial
        primary key,
    bytes           bytea      not null,
    hash            bytea      not null
        constraint unique_script
            unique,
    json            jsonb,
    serialised_size integer    not null,
    type            scripttype not null,
    tx_id           bigint     not null
        references tx
            on delete cascade,
    created_at      timestamp,
    updated_at      timestamp,
    is_deleted      boolean default false
);

create table stake_address
(
    id          bigserial
        primary key,
    hash_raw    bytea             not null
        constraint unique_stake_address
            unique,
    script_hash bytea,
    view        character varying not null,
    tx_id       bigint            not null
        references tx
            on delete cascade,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false
);

create table collateral_tx_out
(
    id                  bigserial
        primary key,
    address             character varying not null,
    address_has_script  boolean           not null,
    address_raw         bytea             not null,
    data_hash           bytea,
    index               smallint          not null,
    multi_assets_descr  character varying not null,
    payment_cred        bytea,
    value               numeric(20, 0)    not null,
    inline_datum_id     bigint
        references datum
            on delete cascade,
    reference_script_id bigint
        references script
            on delete cascade,
    stake_address_id    bigint
        references stake_address
            on delete cascade,
    tx_id               bigint            not null
        references tx
            on delete cascade,
    created_at          timestamp,
    updated_at          timestamp,
    is_deleted          boolean default false,
    constraint unique_col_txout
        unique (tx_id, index)
);

create table delegation
(
    id              bigserial
        primary key,
    active_epoch_no bigint  not null,
    cert_index      integer not null,
    slot_no         bigint  not null,
    addr_id         bigint  not null
        references stake_address
            on delete cascade,
    pool_hash_id    bigint  not null
        references pool_hash
            on delete cascade,
    redeemer_id     bigint
        references redeemer
            on delete cascade,
    tx_id           bigint  not null
        references tx
            on delete cascade,
    created_at      timestamp,
    updated_at      timestamp,
    is_deleted      boolean default false,
    constraint unique_delegation
        unique (tx_id, cert_index)
);

create table epoch_stake
(
    id         bigserial
        primary key,
    amount     numeric(20, 0) not null,
    epoch_no   integer        not null,
    addr_id    bigint         not null
        references stake_address
            on delete cascade,
    pool_id    bigint         not null
        references pool_hash
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_stake
        unique (epoch_no, addr_id, pool_id)
);

create table pool_update
(
    id               bigserial
        primary key,
    active_epoch_no  bigint           not null,
    cert_index       integer          not null,
    fixed_cost       numeric(20, 0)   not null,
    margin           double precision not null,
    pledge           numeric(20, 0)   not null,
    vrf_key_hash     bytea            not null,
    meta_id          bigint
        references pool_metadata_ref
            on delete cascade,
    hash_id          bigint           not null
        references pool_hash
            on delete cascade,
    registered_tx_id bigint           not null
        references tx
            on delete cascade,
    reward_addr_id   bigint           not null
        references stake_address
            on delete cascade,
    created_at       timestamp,
    updated_at       timestamp,
    is_deleted       boolean default false,
    constraint unique_pool_update
        unique (registered_tx_id, cert_index)
);

create table pool_owner
(
    id             bigserial
        primary key,
    pool_update_id bigint not null
        references pool_update
            on delete cascade,
    addr_id        bigint not null
        references stake_address
            on delete cascade,
    created_at     timestamp,
    updated_at     timestamp,
    is_deleted     boolean default false,
    constraint unique_pool_owner
        unique (addr_id, pool_update_id)
);

create table pool_relay
(
    id           bigserial
        primary key,
    dns_name     character varying not null,
    dns_srv_name character varying not null,
    ipv4         character varying not null,
    ipv6         character varying not null,
    port         integer           not null,
    update_id    bigint            not null
        references pool_update
            on delete cascade,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false,
    constraint unique_pool_relay
        unique (update_id, ipv4, ipv6, dns_name)
);

create table reserve
(
    id         bigserial
        primary key,
    amount     numeric(20, 0) not null,
    cert_index integer        not null,
    addr_id    bigint         not null
        references stake_address
            on delete cascade,
    tx_id      bigint         not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_reserves
        unique (addr_id, tx_id, cert_index)
);

create table reward
(
    id              bigserial
        primary key,
    amount          numeric(20, 0) not null,
    earned_epoch    bigint         not null,
    spendable_epoch bigint         not null,
    type            rewardtype     not null,
    addr_id         bigint         not null
        references stake_address
            on delete cascade,
    pool_id         bigint
        references pool_hash
            on delete cascade,
    created_at      timestamp,
    updated_at      timestamp,
    is_deleted      boolean default false,
    constraint unique_reward
        unique (addr_id, type, earned_epoch, pool_id)
);

create table stake_deregistration
(
    id          bigserial
        primary key,
    cert_index  integer not null,
    epoch_no    integer not null,
    addr_id     bigint  not null
        references stake_address
            on delete cascade,
    redeemer_id bigint
        references redeemer
            on delete cascade,
    tx_id       bigint  not null
        references tx
            on delete cascade,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false,
    constraint unique_stake_deregistration
        unique (tx_id, cert_index)
);

create table stake_registration
(
    id         bigserial
        primary key,
    cert_index integer not null,
    epoch_no   integer not null,
    addr_id    bigint  not null
        references stake_address
            on delete cascade,
    tx_id      bigint  not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_stake_registration
        unique (tx_id, cert_index)
);

create table treasury
(
    id         bigserial
        primary key,
    amount     numeric(20, 0) not null,
    cert_index integer        not null,
    addr_id    bigint         not null
        references stake_address
            on delete cascade,
    tx_id      bigint         not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_treasury
        unique (addr_id, tx_id, cert_index)
);

create table tx_in
(
    id           bigserial
        primary key,
    tx_out_index smallint not null,
    redeemer_id  bigint
        references redeemer
            on delete cascade,
    tx_in_id     bigint   not null
        references tx
            on delete cascade,
    tx_out_id    bigint   not null
        references tx
            on delete cascade,
    created_at   timestamp,
    updated_at   timestamp,
    is_deleted   boolean default false,
    constraint unique_txin
        unique (tx_out_id, tx_out_index)
);

create table tx_metadata
(
    id         bigserial
        primary key,
    bytes      bytea          not null,
    json       jsonb,
    key        numeric(20, 0) not null,
    tx_id      bigint         not null
        references tx
            on delete cascade,
    created_at timestamp,
    updated_at timestamp,
    is_deleted boolean default false,
    constraint unique_tx_metadata
        unique (key, tx_id)
);

create table tx_out
(
    id                  bigserial
        primary key,
    address             character varying not null,
    address_has_script  boolean           not null,
    address_raw         bytea             not null,
    data_hash           bytea,
    index               smallint          not null,
    payment_cred        bytea,
    value               numeric(20, 0)    not null,
    inline_datum_id     bigint
        references datum
            on delete cascade,
    reference_script_id bigint
        references script
            on delete cascade,
    stake_address_id    bigint
        references stake_address
            on delete cascade,
    tx_id               bigint            not null
        references tx
            on delete cascade,
    created_at          timestamp,
    updated_at          timestamp,
    is_deleted          boolean default false,
    constraint unique_txout
        unique (tx_id, index)
);

create table withdrawal
(
    id          bigserial
        primary key,
    amount      numeric(20) not null,
    addr_id     bigint      not null
        references stake_address
            on delete cascade,
    redeemer_id bigint
        references redeemer
            on delete cascade,
    tx_id       bigint      not null
        references tx
            on delete cascade,
    created_at  timestamp,
    updated_at  timestamp,
    is_deleted  boolean default false,
    constraint unique_withdrawal
        unique (addr_id, tx_id)
);



