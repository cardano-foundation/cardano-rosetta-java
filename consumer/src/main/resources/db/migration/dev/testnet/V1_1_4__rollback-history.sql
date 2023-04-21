create table testnet.rollback_history
(
    id                     bigserial
        primary key,
    is_deleted             boolean default false,
    block_hash_end         varchar(255) not null,
    block_hash_start       varchar(255) not null,
    block_no_end           bigint       not null,
    block_no_start         bigint       not null,
    block_slot_end         bigint       not null,
    block_slot_start       bigint       not null,
    blocks_deletion_status integer      not null,
    reason                 varchar(255),
    rollback_time          timestamp    not null
);

alter table testnet.rollback_history
    owner to "cardano-master";

