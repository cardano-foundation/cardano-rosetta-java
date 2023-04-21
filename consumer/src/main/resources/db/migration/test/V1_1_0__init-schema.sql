create table test.flyway_schema_history
(
    installed_rank integer                 not null
        constraint flyway_schema_history_pk
            primary key,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null
);

alter table test.flyway_schema_history
    owner to cardano;

create index flyway_schema_history_s_idx
    on test.flyway_schema_history (success);

CREATE SCHEMA dev;


ALTER SCHEMA dev OWNER TO postgres;

SET default_tablespace = '';

--
-- Name: ada_pots; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.ada_pots (
                              id bigint NOT NULL,
                              is_deleted boolean DEFAULT false,
                              deposits numeric(20,0) NOT NULL,
                              epoch_no integer NOT NULL,
                              fees numeric(20,0) NOT NULL,
                              reserves numeric(20,0) NOT NULL,
                              rewards numeric(20,0) NOT NULL,
                              slot_no bigint NOT NULL,
                              treasury numeric(20,0) NOT NULL,
                              utxo numeric(20,0) NOT NULL,
                              block_id bigint NOT NULL
);


ALTER TABLE test.ada_pots OWNER TO postgres;

--
-- Name: ada_pots_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.ada_pots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.ada_pots_id_seq OWNER TO postgres;

--
-- Name: ada_pots_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.ada_pots_id_seq OWNED BY test.ada_pots.id;


--
-- Name: block; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.block (
                           id bigint NOT NULL,
                           is_deleted boolean DEFAULT false,
                           block_no bigint,
                           epoch_no integer,
                           epoch_slot_no integer,
                           hash character varying(64) NOT NULL,
                           op_cert character varying(64),
                           op_cert_counter bigint,
                           proto_major integer,
                           proto_minor integer,
                           size integer,
                           slot_no bigint,
                           "time" timestamp without time zone,
                           tx_count bigint,
                           vrf_key character varying(65535),
                           previous_id bigint,
                           slot_leader_id bigint NOT NULL
);


ALTER TABLE test.block OWNER TO postgres;

--
-- Name: block_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.block_id_seq OWNER TO postgres;

--
-- Name: block_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.block_id_seq OWNED BY test.block.id;


--
-- Name: collateral_tx_in; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.collateral_tx_in (
                                      id bigint NOT NULL,
                                      is_deleted boolean DEFAULT false,
                                      tx_out_index smallint NOT NULL,
                                      tx_in_id bigint NOT NULL,
                                      tx_out_id bigint NOT NULL
);


ALTER TABLE test.collateral_tx_in OWNER TO postgres;

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.collateral_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.collateral_tx_in_id_seq OWNER TO postgres;

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.collateral_tx_in_id_seq OWNED BY test.collateral_tx_in.id;


--
-- Name: collateral_tx_out; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.collateral_tx_out (
                                       id bigint NOT NULL,
                                       is_deleted boolean DEFAULT false,
                                       address character varying(255) NOT NULL,
                                       address_has_script boolean NOT NULL,
                                       address_raw bytea NOT NULL,
                                       data_hash character varying(64),
                                       index smallint NOT NULL,
                                       multi_assets_descr character varying(255) NOT NULL,
                                       payment_cred character varying(56),
                                       value numeric(20,0) NOT NULL,
                                       inline_datum_id bigint,
                                       reference_script_id bigint,
                                       stake_address_id bigint,
                                       tx_id bigint NOT NULL
);


ALTER TABLE test.collateral_tx_out OWNER TO postgres;

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.collateral_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.collateral_tx_out_id_seq OWNER TO postgres;

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.collateral_tx_out_id_seq OWNED BY test.collateral_tx_out.id;


--
-- Name: cost_model; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.cost_model (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                costs character varying(65535) NOT NULL,
                                hash character varying(64) NOT NULL,
                                block_id bigint NOT NULL
);


ALTER TABLE test.cost_model OWNER TO postgres;

--
-- Name: cost_model_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.cost_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.cost_model_id_seq OWNER TO postgres;

--
-- Name: cost_model_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.cost_model_id_seq OWNED BY test.cost_model.id;


--
-- Name: datum; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.datum (
                           id bigint NOT NULL,
                           is_deleted boolean DEFAULT false,
                           bytes bytea NOT NULL,
                           hash character varying(64) NOT NULL,
                           value character varying(65535),
                           tx_id bigint NOT NULL
);


ALTER TABLE test.datum OWNER TO postgres;

--
-- Name: datum_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.datum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.datum_id_seq OWNER TO postgres;

--
-- Name: datum_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.datum_id_seq OWNED BY test.datum.id;


--
-- Name: delegation; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.delegation (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                active_epoch_no bigint NOT NULL,
                                cert_index integer NOT NULL,
                                slot_no bigint NOT NULL,
                                addr_id bigint NOT NULL,
                                pool_hash_id bigint NOT NULL,
                                redeemer_id bigint,
                                tx_id bigint NOT NULL
);


ALTER TABLE test.delegation OWNER TO postgres;

--
-- Name: delegation_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.delegation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.delegation_id_seq OWNER TO postgres;

--
-- Name: delegation_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.delegation_id_seq OWNED BY test.delegation.id;


--
-- Name: delisted_pool; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.delisted_pool (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   hash_raw character varying(56) NOT NULL
);


ALTER TABLE test.delisted_pool OWNER TO postgres;

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.delisted_pool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.delisted_pool_id_seq OWNER TO postgres;

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.delisted_pool_id_seq OWNED BY test.delisted_pool.id;


--
-- Name: epoch; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.epoch (
                           id bigint NOT NULL,
                           is_deleted boolean DEFAULT false,
                           blk_count integer NOT NULL,
                           end_time timestamp without time zone NOT NULL,
                           fees numeric(20,0) NOT NULL,
                           no integer NOT NULL,
                           out_sum numeric(39,0) NOT NULL,
                           start_time timestamp without time zone NOT NULL,
                           tx_count integer NOT NULL
);


ALTER TABLE test.epoch OWNER TO postgres;

--
-- Name: epoch_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.epoch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.epoch_id_seq OWNER TO postgres;

--
-- Name: epoch_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.epoch_id_seq OWNED BY test.epoch.id;


--
-- Name: epoch_param; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.epoch_param (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 coins_per_utxo_size numeric(20,0),
                                 collateral_percent integer,
                                 decentralisation double precision NOT NULL,
                                 epoch_no integer NOT NULL,
                                 extra_entropy character varying(64),
                                 influence double precision NOT NULL,
                                 key_deposit numeric(20,0) NOT NULL,
                                 max_bh_size integer NOT NULL,
                                 max_block_ex_mem numeric(20,0),
                                 max_block_ex_steps numeric(20,0),
                                 max_block_size integer NOT NULL,
                                 max_collateral_inputs integer,
                                 max_epoch integer NOT NULL,
                                 max_tx_ex_mem numeric(20,0),
                                 max_tx_ex_steps numeric(20,0),
                                 max_tx_size integer NOT NULL,
                                 max_val_size numeric(20,0),
                                 min_fee_a integer NOT NULL,
                                 min_fee_b integer NOT NULL,
                                 min_pool_cost numeric(20,0) NOT NULL,
                                 min_utxo_value numeric(20,0) NOT NULL,
                                 monetary_expand_rate double precision NOT NULL,
                                 nonce character varying(64),
                                 optimal_pool_count integer NOT NULL,
                                 pool_deposit numeric(20,0) NOT NULL,
                                 price_mem double precision,
                                 price_step double precision,
                                 protocol_major integer NOT NULL,
                                 protocol_minor integer NOT NULL,
                                 treasury_growth_rate double precision NOT NULL,
                                 block_id bigint NOT NULL,
                                 cost_model_id bigint
);


ALTER TABLE test.epoch_param OWNER TO postgres;

--
-- Name: epoch_param_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.epoch_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.epoch_param_id_seq OWNER TO postgres;

--
-- Name: epoch_param_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.epoch_param_id_seq OWNED BY test.epoch_param.id;


--
-- Name: epoch_stake; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.epoch_stake (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 amount numeric(20,0) NOT NULL,
                                 epoch_no integer NOT NULL,
                                 addr_id bigint NOT NULL,
                                 pool_id bigint NOT NULL
);


ALTER TABLE test.epoch_stake OWNER TO postgres;

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.epoch_stake_id_seq OWNER TO postgres;

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.epoch_stake_id_seq OWNED BY test.epoch_stake.id;


--
-- Name: epoch_sync_time; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.epoch_sync_time (
                                     id bigint NOT NULL,
                                     is_deleted boolean DEFAULT false,
                                     no bigint NOT NULL,
                                     seconds bigint NOT NULL,
                                     state character varying(255) NOT NULL
);


ALTER TABLE test.epoch_sync_time OWNER TO postgres;

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.epoch_sync_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.epoch_sync_time_id_seq OWNER TO postgres;

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.epoch_sync_time_id_seq OWNED BY test.epoch_sync_time.id;


--
-- Name: extra_key_witness; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.extra_key_witness (
                                       id bigint NOT NULL,
                                       is_deleted boolean DEFAULT false,
                                       hash character varying(56) NOT NULL,
                                       tx_id bigint NOT NULL
);


ALTER TABLE test.extra_key_witness OWNER TO postgres;

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.extra_key_witness_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.extra_key_witness_id_seq OWNER TO postgres;

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.extra_key_witness_id_seq OWNED BY test.extra_key_witness.id;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.hibernate_sequence OWNER TO postgres;

--
-- Name: ma_tx_mint; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.ma_tx_mint (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                quantity numeric(20,0) NOT NULL,
                                ident bigint NOT NULL,
                                tx_id bigint NOT NULL
);


ALTER TABLE test.ma_tx_mint OWNER TO postgres;

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.ma_tx_mint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.ma_tx_mint_id_seq OWNER TO postgres;

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.ma_tx_mint_id_seq OWNED BY test.ma_tx_mint.id;


--
-- Name: ma_tx_out; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.ma_tx_out (
                               id bigint NOT NULL,
                               is_deleted boolean DEFAULT false,
                               quantity numeric(20,0) NOT NULL,
                               ident bigint NOT NULL,
                               tx_out_id bigint NOT NULL
);


ALTER TABLE test.ma_tx_out OWNER TO postgres;

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.ma_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.ma_tx_out_id_seq OWNER TO postgres;

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.ma_tx_out_id_seq OWNED BY test.ma_tx_out.id;


--
-- Name: meta; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.meta (
                          id bigint NOT NULL,
                          is_deleted boolean DEFAULT false,
                          network_name character varying(255) NOT NULL,
                          start_time timestamp without time zone NOT NULL,
                          version character varying(255) NOT NULL
);


ALTER TABLE test.meta OWNER TO postgres;

--
-- Name: meta_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.meta_id_seq OWNER TO postgres;

--
-- Name: meta_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.meta_id_seq OWNED BY test.meta.id;


--
-- Name: multi_asset; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.multi_asset (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 fingerprint character varying(255) NOT NULL,
                                 policy character varying(56) NOT NULL,
                                 name character varying(64) NOT NULL
);


ALTER TABLE test.multi_asset OWNER TO postgres;

--
-- Name: multi_asset_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.multi_asset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.multi_asset_id_seq OWNER TO postgres;

--
-- Name: multi_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.multi_asset_id_seq OWNED BY test.multi_asset.id;


--
-- Name: native; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.native
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.native OWNER TO postgres;

--
-- Name: param_proposal; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.param_proposal (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    coins_per_utxo_size numeric(19,2),
                                    collateral_percent integer,
                                    decentralisation double precision,
                                    entropy character varying(64),
                                    epoch_no integer NOT NULL,
                                    influence double precision,
                                    key character varying(56) NOT NULL,
                                    key_deposit numeric(19,2),
                                    max_bh_size numeric(20,0),
                                    max_block_ex_mem numeric(20,0),
                                    max_block_ex_steps numeric(20,0),
                                    max_block_size numeric(20,0),
                                    max_collateral_inputs integer,
                                    max_epoch numeric(20,0),
                                    max_tx_ex_mem numeric(20,0),
                                    max_tx_ex_steps numeric(20,0),
                                    max_tx_size numeric(20,0),
                                    max_val_size numeric(20,0),
                                    min_fee_a numeric(20,0),
                                    min_fee_b numeric(20,0),
                                    min_pool_cost numeric(20,0),
                                    min_utxo_value numeric(20,0),
                                    monetary_expand_rate double precision,
                                    optimal_pool_count numeric(20,0),
                                    pool_deposit numeric(20,0),
                                    price_mem double precision,
                                    price_step double precision,
                                    protocol_major integer,
                                    protocol_minor integer,
                                    treasury_growth_rate double precision,
                                    cost_model_id bigint,
                                    registered_tx_id bigint NOT NULL
);


ALTER TABLE test.param_proposal OWNER TO postgres;

--
-- Name: param_proposal_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.param_proposal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.param_proposal_id_seq OWNER TO postgres;

--
-- Name: param_proposal_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.param_proposal_id_seq OWNED BY test.param_proposal.id;


--
-- Name: pool_hash; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_hash (
                               id bigint NOT NULL,
                               is_deleted boolean DEFAULT false,
                               hash_raw character varying(56) NOT NULL,
                               view character varying(255) NOT NULL
);


ALTER TABLE test.pool_hash OWNER TO postgres;

--
-- Name: pool_hash_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_hash_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_hash_id_seq OWNER TO postgres;

--
-- Name: pool_hash_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_hash_id_seq OWNED BY test.pool_hash.id;


--
-- Name: pool_metadata_ref; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_metadata_ref (
                                       id bigint NOT NULL,
                                       is_deleted boolean DEFAULT false,
                                       hash character varying(64) NOT NULL,
                                       url character varying(255) NOT NULL,
                                       pool_id bigint NOT NULL,
                                       registered_tx_id bigint NOT NULL
);


ALTER TABLE test.pool_metadata_ref OWNER TO postgres;

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_metadata_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_metadata_ref_id_seq OWNER TO postgres;

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_metadata_ref_id_seq OWNED BY test.pool_metadata_ref.id;


--
-- Name: pool_offline_data; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_offline_data (
                                       id bigint NOT NULL,
                                       is_deleted boolean DEFAULT false,
                                       bytes bytea NOT NULL,
                                       hash character varying(64) NOT NULL,
                                       json character varying(65535) NOT NULL,
                                       ticker_name character varying(255) NOT NULL,
                                       pool_id bigint NOT NULL,
                                       pmr_id bigint NOT NULL
);


ALTER TABLE test.pool_offline_data OWNER TO postgres;

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_offline_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_offline_data_id_seq OWNER TO postgres;

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_offline_data_id_seq OWNED BY test.pool_offline_data.id;


--
-- Name: pool_offline_fetch_error; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_offline_fetch_error (
                                              id bigint NOT NULL,
                                              is_deleted boolean DEFAULT false,
                                              fetch_error character varying(65535) NOT NULL,
                                              fetch_time timestamp without time zone NOT NULL,
                                              retry_count integer NOT NULL,
                                              pool_id bigint NOT NULL,
                                              pmr_id bigint NOT NULL
);


ALTER TABLE test.pool_offline_fetch_error OWNER TO postgres;

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_offline_fetch_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_offline_fetch_error_id_seq OWNER TO postgres;

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_offline_fetch_error_id_seq OWNED BY test.pool_offline_fetch_error.id;


--
-- Name: pool_owner; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_owner (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                pool_update_id bigint NOT NULL,
                                addr_id bigint NOT NULL
);


ALTER TABLE test.pool_owner OWNER TO postgres;

--
-- Name: pool_owner_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_owner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_owner_id_seq OWNER TO postgres;

--
-- Name: pool_owner_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_owner_id_seq OWNED BY test.pool_owner.id;


--
-- Name: pool_relay; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_relay (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                dns_name character varying(255) NOT NULL,
                                dns_srv_name character varying(255) NOT NULL,
                                ipv4 character varying(255) NOT NULL,
                                ipv6 character varying(255) NOT NULL,
                                port integer NOT NULL,
                                update_id bigint NOT NULL
);


ALTER TABLE test.pool_relay OWNER TO postgres;

--
-- Name: pool_relay_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_relay_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_relay_id_seq OWNER TO postgres;

--
-- Name: pool_relay_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_relay_id_seq OWNED BY test.pool_relay.id;


--
-- Name: pool_retire; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_retire (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 cert_index integer NOT NULL,
                                 retiring_epoch integer NOT NULL,
                                 announced_tx_id bigint NOT NULL,
                                 hash_id bigint NOT NULL
);


ALTER TABLE test.pool_retire OWNER TO postgres;

--
-- Name: pool_retire_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_retire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_retire_id_seq OWNER TO postgres;

--
-- Name: pool_retire_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_retire_id_seq OWNED BY test.pool_retire.id;


--
-- Name: pool_update; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pool_update (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 active_epoch_no bigint NOT NULL,
                                 cert_index integer NOT NULL,
                                 fixed_cost numeric(20,0) NOT NULL,
                                 margin double precision NOT NULL,
                                 pledge numeric(20,0) NOT NULL,
                                 vrf_key_hash character varying(64) NOT NULL,
                                 meta_id bigint,
                                 hash_id bigint NOT NULL,
                                 registered_tx_id bigint NOT NULL,
                                 reward_addr_id bigint NOT NULL
);


ALTER TABLE test.pool_update OWNER TO postgres;

--
-- Name: pool_update_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pool_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pool_update_id_seq OWNER TO postgres;

--
-- Name: pool_update_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pool_update_id_seq OWNED BY test.pool_update.id;


--
-- Name: pot_transfer; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.pot_transfer (
                                  id bigint NOT NULL,
                                  is_deleted boolean DEFAULT false,
                                  cert_index integer NOT NULL,
                                  reserves numeric(20,0) NOT NULL,
                                  treasury numeric(20,0) NOT NULL,
                                  tx_id bigint NOT NULL
);


ALTER TABLE test.pot_transfer OWNER TO postgres;

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.pot_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.pot_transfer_id_seq OWNER TO postgres;

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.pot_transfer_id_seq OWNED BY test.pot_transfer.id;


--
-- Name: redeemer; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.redeemer (
                              id bigint NOT NULL,
                              is_deleted boolean DEFAULT false,
                              fee numeric(20,0),
                              index integer NOT NULL,
                              purpose character varying(255) NOT NULL,
                              script_hash character varying(56),
                              unit_mem bigint NOT NULL,
                              unit_steps bigint NOT NULL,
                              redeemer_data_id bigint NOT NULL,
                              tx_id bigint NOT NULL
);


ALTER TABLE test.redeemer OWNER TO postgres;

--
-- Name: redeemer_data; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.redeemer_data (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   bytes bytea NOT NULL,
                                   hash character varying(64) NOT NULL,
                                   value character varying(65535),
                                   tx_id bigint NOT NULL
);


ALTER TABLE test.redeemer_data OWNER TO postgres;

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.redeemer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.redeemer_data_id_seq OWNER TO postgres;

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.redeemer_data_id_seq OWNED BY test.redeemer_data.id;


--
-- Name: redeemer_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.redeemer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.redeemer_id_seq OWNER TO postgres;

--
-- Name: redeemer_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.redeemer_id_seq OWNED BY test.redeemer.id;


--
-- Name: reference_tx_in; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.reference_tx_in (
                                     id bigint NOT NULL,
                                     is_deleted boolean DEFAULT false,
                                     tx_out_index smallint NOT NULL,
                                     tx_in_id bigint NOT NULL,
                                     tx_out_id bigint NOT NULL
);


ALTER TABLE test.reference_tx_in OWNER TO postgres;

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.reference_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.reference_tx_in_id_seq OWNER TO postgres;

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.reference_tx_in_id_seq OWNED BY test.reference_tx_in.id;


--
-- Name: reserve; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.reserve (
                             id bigint NOT NULL,
                             is_deleted boolean DEFAULT false,
                             amount numeric(20,0) NOT NULL,
                             cert_index integer NOT NULL,
                             addr_id bigint NOT NULL,
                             tx_id bigint NOT NULL
);


ALTER TABLE test.reserve OWNER TO postgres;

--
-- Name: reserve_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.reserve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.reserve_id_seq OWNER TO postgres;

--
-- Name: reserve_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.reserve_id_seq OWNED BY test.reserve.id;


--
-- Name: reserved_pool_ticker; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.reserved_pool_ticker (
                                          id bigint NOT NULL,
                                          is_deleted boolean DEFAULT false,
                                          name character varying(255) NOT NULL,
                                          pool_hash character varying(56) NOT NULL
);


ALTER TABLE test.reserved_pool_ticker OWNER TO postgres;

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.reserved_pool_ticker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.reserved_pool_ticker_id_seq OWNER TO postgres;

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.reserved_pool_ticker_id_seq OWNED BY test.reserved_pool_ticker.id;


--
-- Name: reward; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.reward (
                            id bigint NOT NULL,
                            is_deleted boolean DEFAULT false,
                            amount numeric(20,0) NOT NULL,
                            earned_epoch bigint NOT NULL,
                            spendable_epoch bigint NOT NULL,
                            type character varying(255) NOT NULL,
                            addr_id bigint NOT NULL,
                            pool_id bigint
);


ALTER TABLE test.reward OWNER TO postgres;

--
-- Name: reward_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.reward_id_seq OWNER TO postgres;

--
-- Name: reward_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.reward_id_seq OWNED BY test.reward.id;


--
-- Name: schema_version; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.schema_version (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    stage_one bigint NOT NULL,
                                    stage_three bigint NOT NULL,
                                    stage_two bigint NOT NULL
);


ALTER TABLE test.schema_version OWNER TO postgres;

--
-- Name: schema_version_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.schema_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.schema_version_id_seq OWNER TO postgres;

--
-- Name: schema_version_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.schema_version_id_seq OWNED BY test.schema_version.id;


--
-- Name: script; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.script (
                            id bigint NOT NULL,
                            is_deleted boolean DEFAULT false,
                            bytes bytea,
                            hash character varying(64) NOT NULL,
                            json character varying(65535),
                            serialised_size integer,
                            type character varying(255) NOT NULL,
                            tx_id bigint NOT NULL
);


ALTER TABLE test.script OWNER TO postgres;

--
-- Name: script_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.script_id_seq OWNER TO postgres;

--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.script_id_seq OWNED BY test.script.id;


--
-- Name: slot_leader; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.slot_leader (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 description character varying(65535) NOT NULL,
                                 hash character varying(56) NOT NULL,
                                 pool_hash_id bigint
);


ALTER TABLE test.slot_leader OWNER TO postgres;

--
-- Name: slot_leader_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.slot_leader_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.slot_leader_id_seq OWNER TO postgres;

--
-- Name: slot_leader_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.slot_leader_id_seq OWNED BY test.slot_leader.id;


--
-- Name: stake_address; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.stake_address (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   hash_raw varchar(59) NOT NULL,
                                   script_hash character varying(56),
                                   view character varying(65535) NOT NULL,
                                   tx_id bigint NOT NULL
);


ALTER TABLE test.stake_address OWNER TO postgres;

--
-- Name: stake_address_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.stake_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.stake_address_id_seq OWNER TO postgres;

--
-- Name: stake_address_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.stake_address_id_seq OWNED BY test.stake_address.id;


--
-- Name: stake_deregistration; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.stake_deregistration (
                                          id bigint NOT NULL,
                                          is_deleted boolean DEFAULT false,
                                          cert_index integer NOT NULL,
                                          epoch_no integer NOT NULL,
                                          addr_id bigint NOT NULL,
                                          redeemer_id bigint,
                                          tx_id bigint NOT NULL
);


ALTER TABLE test.stake_deregistration OWNER TO postgres;

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.stake_deregistration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.stake_deregistration_id_seq OWNER TO postgres;

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.stake_deregistration_id_seq OWNED BY test.stake_deregistration.id;


--
-- Name: stake_registration; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.stake_registration (
                                        id bigint NOT NULL,
                                        is_deleted boolean DEFAULT false,
                                        cert_index integer NOT NULL,
                                        epoch_no integer NOT NULL,
                                        addr_id bigint NOT NULL,
                                        tx_id bigint NOT NULL
);


ALTER TABLE test.stake_registration OWNER TO postgres;

--
-- Name: stake_registration_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.stake_registration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.stake_registration_id_seq OWNER TO postgres;

--
-- Name: stake_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.stake_registration_id_seq OWNED BY test.stake_registration.id;


--
-- Name: treasury; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.treasury (
                              id bigint NOT NULL,
                              is_deleted boolean DEFAULT false,
                              amount numeric(20,0) NOT NULL,
                              cert_index integer NOT NULL,
                              addr_id bigint NOT NULL,
                              tx_id bigint NOT NULL
);


ALTER TABLE test.treasury OWNER TO postgres;

--
-- Name: treasury_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.treasury_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.treasury_id_seq OWNER TO postgres;

--
-- Name: treasury_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.treasury_id_seq OWNED BY test.treasury.id;


--
-- Name: tx; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.tx (
                        id bigint NOT NULL,
                        is_deleted boolean DEFAULT false,
                        block_index bigint,
                        deposit bigint,
                        fee numeric(20,0),
                        hash character varying(64) NOT NULL,
                        invalid_before numeric(20,0),
                        invalid_hereafter numeric(20,0),
                        out_sum numeric(20,0),
                        script_size integer,
                        size integer,
                        valid_contract boolean,
                        block_id bigint NOT NULL
);


ALTER TABLE test.tx OWNER TO postgres;

--
-- Name: tx_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.tx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.tx_id_seq OWNER TO postgres;

--
-- Name: tx_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.tx_id_seq OWNED BY test.tx.id;


--
-- Name: tx_in; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.tx_in (
                           id bigint NOT NULL,
                           is_deleted boolean DEFAULT false,
                           tx_out_index smallint NOT NULL,
                           redeemer_id bigint,
                           tx_in_id bigint NOT NULL,
                           tx_out_id bigint NOT NULL
);


ALTER TABLE test.tx_in OWNER TO postgres;

--
-- Name: tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.tx_in_id_seq OWNER TO postgres;

--
-- Name: tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.tx_in_id_seq OWNED BY test.tx_in.id;


--
-- Name: tx_metadata; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.tx_metadata (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 bytes bytea NOT NULL,
                                 json character varying(65535),
                                 key numeric(20,0) NOT NULL,
                                 tx_id bigint NOT NULL
);


ALTER TABLE test.tx_metadata OWNER TO postgres;

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.tx_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.tx_metadata_id_seq OWNER TO postgres;

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.tx_metadata_id_seq OWNED BY test.tx_metadata.id;


--
-- Name: tx_out; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.tx_out (
                            id bigint NOT NULL,
                            is_deleted boolean DEFAULT false,
                            address character varying(255) NOT NULL,
                            address_has_script boolean NOT NULL,
                            address_raw bytea NOT NULL,
                            data_hash character varying(64),
                            index smallint NOT NULL,
                            payment_cred character varying(56),
                            value numeric(20,0) NOT NULL,
                            inline_datum_id bigint,
                            reference_script_id bigint,
                            stake_address_id bigint,
                            tx_id bigint NOT NULL
);


ALTER TABLE test.tx_out OWNER TO postgres;

--
-- Name: tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.tx_out_id_seq OWNER TO postgres;

--
-- Name: tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.tx_out_id_seq OWNED BY test.tx_out.id;


--
-- Name: withdrawal; Type: TABLE; Schema: dev; Owner: postgres
--

CREATE TABLE test.withdrawal (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                amount numeric(20,0) NOT NULL,
                                addr_id bigint NOT NULL,
                                redeemer_id bigint,
                                tx_id bigint NOT NULL
);


ALTER TABLE test.withdrawal OWNER TO postgres;

--
-- Name: withdrawal_id_seq; Type: SEQUENCE; Schema: dev; Owner: postgres
--

CREATE SEQUENCE test.withdrawal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test.withdrawal_id_seq OWNER TO postgres;

--
-- Name: withdrawal_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: postgres
--

ALTER SEQUENCE test.withdrawal_id_seq OWNED BY test.withdrawal.id;


--
-- Name: ada_pots id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ada_pots ALTER COLUMN id SET DEFAULT nextval('test.ada_pots_id_seq'::regclass);


--
-- Name: block id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.block ALTER COLUMN id SET DEFAULT nextval('test.block_id_seq'::regclass);


--
-- Name: collateral_tx_in id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_in ALTER COLUMN id SET DEFAULT nextval('test.collateral_tx_in_id_seq'::regclass);


--
-- Name: collateral_tx_out id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out ALTER COLUMN id SET DEFAULT nextval('test.collateral_tx_out_id_seq'::regclass);


--
-- Name: cost_model id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.cost_model ALTER COLUMN id SET DEFAULT nextval('test.cost_model_id_seq'::regclass);


--
-- Name: datum id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.datum ALTER COLUMN id SET DEFAULT nextval('test.datum_id_seq'::regclass);


--
-- Name: delegation id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation ALTER COLUMN id SET DEFAULT nextval('test.delegation_id_seq'::regclass);


--
-- Name: delisted_pool id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delisted_pool ALTER COLUMN id SET DEFAULT nextval('test.delisted_pool_id_seq'::regclass);


--
-- Name: epoch id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch ALTER COLUMN id SET DEFAULT nextval('test.epoch_id_seq'::regclass);


--
-- Name: epoch_param id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_param ALTER COLUMN id SET DEFAULT nextval('test.epoch_param_id_seq'::regclass);


--
-- Name: epoch_stake id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_stake ALTER COLUMN id SET DEFAULT nextval('test.epoch_stake_id_seq'::regclass);


--
-- Name: epoch_sync_time id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_sync_time ALTER COLUMN id SET DEFAULT nextval('test.epoch_sync_time_id_seq'::regclass);


--
-- Name: extra_key_witness id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.extra_key_witness ALTER COLUMN id SET DEFAULT nextval('test.extra_key_witness_id_seq'::regclass);


--
-- Name: ma_tx_mint id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_mint ALTER COLUMN id SET DEFAULT nextval('test.ma_tx_mint_id_seq'::regclass);


--
-- Name: ma_tx_out id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_out ALTER COLUMN id SET DEFAULT nextval('test.ma_tx_out_id_seq'::regclass);


--
-- Name: meta id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.meta ALTER COLUMN id SET DEFAULT nextval('test.meta_id_seq'::regclass);


--
-- Name: multi_asset id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.multi_asset ALTER COLUMN id SET DEFAULT nextval('test.multi_asset_id_seq'::regclass);


--
-- Name: param_proposal id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.param_proposal ALTER COLUMN id SET DEFAULT nextval('test.param_proposal_id_seq'::regclass);


--
-- Name: pool_hash id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_hash ALTER COLUMN id SET DEFAULT nextval('test.pool_hash_id_seq'::regclass);


--
-- Name: pool_metadata_ref id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_metadata_ref ALTER COLUMN id SET DEFAULT nextval('test.pool_metadata_ref_id_seq'::regclass);


--
-- Name: pool_offline_data id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_data ALTER COLUMN id SET DEFAULT nextval('test.pool_offline_data_id_seq'::regclass);


--
-- Name: pool_offline_fetch_error id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_fetch_error ALTER COLUMN id SET DEFAULT nextval('test.pool_offline_fetch_error_id_seq'::regclass);


--
-- Name: pool_owner id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_owner ALTER COLUMN id SET DEFAULT nextval('test.pool_owner_id_seq'::regclass);


--
-- Name: pool_relay id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_relay ALTER COLUMN id SET DEFAULT nextval('test.pool_relay_id_seq'::regclass);


--
-- Name: pool_retire id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_retire ALTER COLUMN id SET DEFAULT nextval('test.pool_retire_id_seq'::regclass);


--
-- Name: pool_update id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update ALTER COLUMN id SET DEFAULT nextval('test.pool_update_id_seq'::regclass);


--
-- Name: pot_transfer id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pot_transfer ALTER COLUMN id SET DEFAULT nextval('test.pot_transfer_id_seq'::regclass);


--
-- Name: redeemer id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer ALTER COLUMN id SET DEFAULT nextval('test.redeemer_id_seq'::regclass);


--
-- Name: redeemer_data id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer_data ALTER COLUMN id SET DEFAULT nextval('test.redeemer_data_id_seq'::regclass);


--
-- Name: reference_tx_in id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reference_tx_in ALTER COLUMN id SET DEFAULT nextval('test.reference_tx_in_id_seq'::regclass);


--
-- Name: reserve id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserve ALTER COLUMN id SET DEFAULT nextval('test.reserve_id_seq'::regclass);


--
-- Name: reserved_pool_ticker id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserved_pool_ticker ALTER COLUMN id SET DEFAULT nextval('test.reserved_pool_ticker_id_seq'::regclass);


--
-- Name: reward id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reward ALTER COLUMN id SET DEFAULT nextval('test.reward_id_seq'::regclass);


--
-- Name: schema_version id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.schema_version ALTER COLUMN id SET DEFAULT nextval('test.schema_version_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.script ALTER COLUMN id SET DEFAULT nextval('test.script_id_seq'::regclass);


--
-- Name: slot_leader id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.slot_leader ALTER COLUMN id SET DEFAULT nextval('test.slot_leader_id_seq'::regclass);


--
-- Name: stake_address id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_address ALTER COLUMN id SET DEFAULT nextval('test.stake_address_id_seq'::regclass);


--
-- Name: stake_deregistration id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration ALTER COLUMN id SET DEFAULT nextval('test.stake_deregistration_id_seq'::regclass);


--
-- Name: stake_registration id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_registration ALTER COLUMN id SET DEFAULT nextval('test.stake_registration_id_seq'::regclass);


--
-- Name: treasury id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.treasury ALTER COLUMN id SET DEFAULT nextval('test.treasury_id_seq'::regclass);


--
-- Name: tx id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx ALTER COLUMN id SET DEFAULT nextval('test.tx_id_seq'::regclass);


--
-- Name: tx_in id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in ALTER COLUMN id SET DEFAULT nextval('test.tx_in_id_seq'::regclass);


--
-- Name: tx_metadata id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_metadata ALTER COLUMN id SET DEFAULT nextval('test.tx_metadata_id_seq'::regclass);


--
-- Name: tx_out id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out ALTER COLUMN id SET DEFAULT nextval('test.tx_out_id_seq'::regclass);


--
-- Name: withdrawal id; Type: DEFAULT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.withdrawal ALTER COLUMN id SET DEFAULT nextval('test.withdrawal_id_seq'::regclass);


--
-- Data for Name: ada_pots; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.ada_pots (id, is_deleted, deposits, epoch_no, fees, reserves, rewards, slot_no, treasury, utxo, block_id) FROM stdin;
\.


--
-- Data for Name: block; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.block (id, is_deleted, block_no, epoch_no, epoch_slot_no, hash, op_cert, op_cert_counter, proto_major, proto_minor, size, slot_no, "time", tx_count, vrf_key, previous_id, slot_leader_id) FROM stdin;
\.


--
-- Data for Name: collateral_tx_in; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.collateral_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: collateral_tx_out; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.collateral_tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, index, multi_assets_descr, payment_cred, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
\.


--
-- Data for Name: cost_model; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.cost_model (id, is_deleted, costs, hash, block_id) FROM stdin;
\.


--
-- Data for Name: datum; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.datum (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: delegation; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.delegation (id, is_deleted, active_epoch_no, cert_index, slot_no, addr_id, pool_hash_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: delisted_pool; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.delisted_pool (id, is_deleted, hash_raw) FROM stdin;
\.


--
-- Data for Name: epoch; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.epoch (id, is_deleted, blk_count, end_time, fees, no, out_sum, start_time, tx_count) FROM stdin;
\.


--
-- Data for Name: epoch_param; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.epoch_param (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, epoch_no, extra_entropy, influence, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, nonce, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, block_id, cost_model_id) FROM stdin;
\.


--
-- Data for Name: epoch_stake; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.epoch_stake (id, is_deleted, amount, epoch_no, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: epoch_sync_time; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.epoch_sync_time (id, is_deleted, no, seconds, state) FROM stdin;
\.


--
-- Data for Name: extra_key_witness; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.extra_key_witness (id, is_deleted, hash, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_mint; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.ma_tx_mint (id, is_deleted, quantity, ident, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_out; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.ma_tx_out (id, is_deleted, quantity, ident, tx_out_id) FROM stdin;
\.


--
-- Data for Name: meta; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.meta (id, is_deleted, network_name, start_time, version) FROM stdin;
\.


--
-- Data for Name: multi_asset; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.multi_asset (id, is_deleted, fingerprint, policy, name) FROM stdin;
\.


--
-- Data for Name: param_proposal; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.param_proposal (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, entropy, epoch_no, influence, key, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, cost_model_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_hash; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_hash (id, is_deleted, hash_raw, view) FROM stdin;
\.


--
-- Data for Name: pool_metadata_ref; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_metadata_ref (id, is_deleted, hash, url, pool_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_data; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_offline_data (id, is_deleted, bytes, hash, json, ticker_name, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_fetch_error; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_offline_fetch_error (id, is_deleted, fetch_error, fetch_time, retry_count, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_owner; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_owner (id, is_deleted, pool_update_id, addr_id) FROM stdin;
\.


--
-- Data for Name: pool_relay; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_relay (id, is_deleted, dns_name, dns_srv_name, ipv4, ipv6, port, update_id) FROM stdin;
\.


--
-- Data for Name: pool_retire; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_retire (id, is_deleted, cert_index, retiring_epoch, announced_tx_id, hash_id) FROM stdin;
\.


--
-- Data for Name: pool_update; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pool_update (id, is_deleted, active_epoch_no, cert_index, fixed_cost, margin, pledge, vrf_key_hash, meta_id, hash_id, registered_tx_id, reward_addr_id) FROM stdin;
\.


--
-- Data for Name: pot_transfer; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.pot_transfer (id, is_deleted, cert_index, reserves, treasury, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.redeemer (id, is_deleted, fee, index, purpose, script_hash, unit_mem, unit_steps, redeemer_data_id, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer_data; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.redeemer_data (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: reference_tx_in; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.reference_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: reserve; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.reserve (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: reserved_pool_ticker; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.reserved_pool_ticker (id, is_deleted, name, pool_hash) FROM stdin;
\.


--
-- Data for Name: reward; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.reward (id, is_deleted, amount, earned_epoch, spendable_epoch, type, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.schema_version (id, is_deleted, stage_one, stage_three, stage_two) FROM stdin;
\.


--
-- Data for Name: script; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.script (id, is_deleted, bytes, hash, json, serialised_size, type, tx_id) FROM stdin;
\.


--
-- Data for Name: slot_leader; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.slot_leader (id, is_deleted, description, hash, pool_hash_id) FROM stdin;
\.


--
-- Data for Name: stake_address; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.stake_address (id, is_deleted, hash_raw, script_hash, view, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_deregistration; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.stake_deregistration (id, is_deleted, cert_index, epoch_no, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_registration; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.stake_registration (id, is_deleted, cert_index, epoch_no, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: treasury; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.treasury (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: tx; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.tx (id, is_deleted, block_index, deposit, fee, hash, invalid_before, invalid_hereafter, out_sum, script_size, size, valid_contract, block_id) FROM stdin;
\.


--
-- Data for Name: tx_in; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.tx_in (id, is_deleted, tx_out_index, redeemer_id, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: tx_metadata; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.tx_metadata (id, is_deleted, bytes, json, key, tx_id) FROM stdin;
\.


--
-- Data for Name: tx_out; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, index, payment_cred, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
\.


--
-- Data for Name: withdrawal; Type: TABLE DATA; Schema: dev; Owner: postgres
--

COPY test.withdrawal (id, is_deleted, amount, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Name: ada_pots_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.ada_pots_id_seq', 1, false);


--
-- Name: block_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.block_id_seq', 1, false);


--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.collateral_tx_in_id_seq', 1, false);


--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.collateral_tx_out_id_seq', 1, false);


--
-- Name: cost_model_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.cost_model_id_seq', 1, false);


--
-- Name: datum_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.datum_id_seq', 1, false);


--
-- Name: delegation_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.delegation_id_seq', 1, false);


--
-- Name: delisted_pool_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.delisted_pool_id_seq', 1, false);


--
-- Name: epoch_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.epoch_id_seq', 1, false);


--
-- Name: epoch_param_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.epoch_param_id_seq', 1, false);


--
-- Name: epoch_stake_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.epoch_stake_id_seq', 1, false);


--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.epoch_sync_time_id_seq', 1, false);


--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.extra_key_witness_id_seq', 1, false);


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.hibernate_sequence', 1, false);


--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.ma_tx_mint_id_seq', 1, false);


--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.ma_tx_out_id_seq', 1, false);


--
-- Name: meta_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.meta_id_seq', 1, false);


--
-- Name: multi_asset_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.multi_asset_id_seq', 1, false);


--
-- Name: native; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.native', 1, false);


--
-- Name: param_proposal_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.param_proposal_id_seq', 1, false);


--
-- Name: pool_hash_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_hash_id_seq', 1, false);


--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_metadata_ref_id_seq', 1, false);


--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_offline_data_id_seq', 1, false);


--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_offline_fetch_error_id_seq', 1, false);


--
-- Name: pool_owner_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_owner_id_seq', 1, false);


--
-- Name: pool_relay_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_relay_id_seq', 1, false);


--
-- Name: pool_retire_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_retire_id_seq', 1, false);


--
-- Name: pool_update_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pool_update_id_seq', 1, false);


--
-- Name: pot_transfer_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.pot_transfer_id_seq', 1, false);


--
-- Name: redeemer_data_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.redeemer_data_id_seq', 1, false);


--
-- Name: redeemer_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.redeemer_id_seq', 1, false);


--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.reference_tx_in_id_seq', 1, false);


--
-- Name: reserve_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.reserve_id_seq', 1, false);


--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.reserved_pool_ticker_id_seq', 1, false);


--
-- Name: reward_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.reward_id_seq', 1, false);


--
-- Name: schema_version_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.schema_version_id_seq', 1, false);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.script_id_seq', 1, false);


--
-- Name: slot_leader_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.slot_leader_id_seq', 1, false);


--
-- Name: stake_address_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.stake_address_id_seq', 1, false);


--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.stake_deregistration_id_seq', 1, false);


--
-- Name: stake_registration_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.stake_registration_id_seq', 1, false);


--
-- Name: treasury_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.treasury_id_seq', 1, false);


--
-- Name: tx_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.tx_id_seq', 1, false);


--
-- Name: tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.tx_in_id_seq', 1, false);


--
-- Name: tx_metadata_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.tx_metadata_id_seq', 1, false);


--
-- Name: tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.tx_out_id_seq', 1, false);


--
-- Name: withdrawal_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: postgres
--

SELECT pg_catalog.setval('test.withdrawal_id_seq', 1, false);


--
-- Name: ada_pots ada_pots_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ada_pots
    ADD CONSTRAINT ada_pots_pkey PRIMARY KEY (id);


--
-- Name: block block_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.block
    ADD CONSTRAINT block_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_in collateral_tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_out collateral_tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_pkey PRIMARY KEY (id);


--
-- Name: cost_model cost_model_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.cost_model
    ADD CONSTRAINT cost_model_pkey PRIMARY KEY (id);


--
-- Name: datum datum_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.datum
    ADD CONSTRAINT datum_pkey PRIMARY KEY (id);


--
-- Name: delegation delegation_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);


--
-- Name: delisted_pool delisted_pool_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delisted_pool
    ADD CONSTRAINT delisted_pool_pkey PRIMARY KEY (id);


--
-- Name: epoch_param epoch_param_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_param
    ADD CONSTRAINT epoch_param_pkey PRIMARY KEY (id);


--
-- Name: epoch epoch_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch
    ADD CONSTRAINT epoch_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_sync_time epoch_sync_time_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_sync_time
    ADD CONSTRAINT epoch_sync_time_pkey PRIMARY KEY (id);


--
-- Name: extra_key_witness extra_key_witness_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.extra_key_witness
    ADD CONSTRAINT extra_key_witness_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_mint ma_tx_mint_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_out ma_tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_out
    ADD CONSTRAINT ma_tx_out_pkey PRIMARY KEY (id);


--
-- Name: meta meta_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.meta
    ADD CONSTRAINT meta_pkey PRIMARY KEY (id);


--
-- Name: multi_asset multi_asset_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.multi_asset
    ADD CONSTRAINT multi_asset_pkey PRIMARY KEY (id);


--
-- Name: param_proposal param_proposal_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.param_proposal
    ADD CONSTRAINT param_proposal_pkey PRIMARY KEY (id);


--
-- Name: pool_hash pool_hash_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_hash
    ADD CONSTRAINT pool_hash_pkey PRIMARY KEY (id);


--
-- Name: pool_metadata_ref pool_metadata_ref_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_data pool_offline_data_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pkey PRIMARY KEY (id);


--
-- Name: pool_owner pool_owner_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_owner
    ADD CONSTRAINT pool_owner_pkey PRIMARY KEY (id);


--
-- Name: pool_relay pool_relay_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_relay
    ADD CONSTRAINT pool_relay_pkey PRIMARY KEY (id);


--
-- Name: pool_retire pool_retire_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_retire
    ADD CONSTRAINT pool_retire_pkey PRIMARY KEY (id);


--
-- Name: pool_update pool_update_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT pool_update_pkey PRIMARY KEY (id);


--
-- Name: pot_transfer pot_transfer_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pot_transfer
    ADD CONSTRAINT pot_transfer_pkey PRIMARY KEY (id);


--
-- Name: redeemer_data redeemer_data_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer_data
    ADD CONSTRAINT redeemer_data_pkey PRIMARY KEY (id);


--
-- Name: redeemer redeemer_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer
    ADD CONSTRAINT redeemer_pkey PRIMARY KEY (id);


--
-- Name: reference_tx_in reference_tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reference_tx_in
    ADD CONSTRAINT reference_tx_in_pkey PRIMARY KEY (id);


--
-- Name: reserve reserve_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserve
    ADD CONSTRAINT reserve_pkey PRIMARY KEY (id);


--
-- Name: reserved_pool_ticker reserved_pool_ticker_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserved_pool_ticker
    ADD CONSTRAINT reserved_pool_ticker_pkey PRIMARY KEY (id);


--
-- Name: reward reward_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.schema_version
    ADD CONSTRAINT schema_version_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: slot_leader slot_leader_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.slot_leader
    ADD CONSTRAINT slot_leader_pkey PRIMARY KEY (id);


--
-- Name: stake_address stake_address_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_address
    ADD CONSTRAINT stake_address_pkey PRIMARY KEY (id);


--
-- Name: stake_deregistration stake_deregistration_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration
    ADD CONSTRAINT stake_deregistration_pkey PRIMARY KEY (id);


--
-- Name: stake_registration stake_registration_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_registration
    ADD CONSTRAINT stake_registration_pkey PRIMARY KEY (id);


--
-- Name: treasury treasury_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.treasury
    ADD CONSTRAINT treasury_pkey PRIMARY KEY (id);


--
-- Name: tx_in tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in
    ADD CONSTRAINT tx_in_pkey PRIMARY KEY (id);


--
-- Name: tx_metadata tx_metadata_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_metadata
    ADD CONSTRAINT tx_metadata_pkey PRIMARY KEY (id);


--
-- Name: tx_out tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT tx_out_pkey PRIMARY KEY (id);


--
-- Name: tx tx_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx
    ADD CONSTRAINT tx_pkey PRIMARY KEY (id);


--
-- Name: ada_pots uk_143qflkqxvmvp4cukodhskt43; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ada_pots
    ADD CONSTRAINT uk_143qflkqxvmvp4cukodhskt43 UNIQUE (block_id);


--
-- Name: block unique_block; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.block
    ADD CONSTRAINT unique_block UNIQUE (hash);


--
-- Name: collateral_tx_in unique_col_txin; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_in
    ADD CONSTRAINT unique_col_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: collateral_tx_out unique_col_txout; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT unique_col_txout UNIQUE (tx_id, index);


--
-- Name: cost_model unique_cost_model; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.cost_model
    ADD CONSTRAINT unique_cost_model UNIQUE (hash);


--
-- Name: datum unique_datum; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.datum
    ADD CONSTRAINT unique_datum UNIQUE (hash);


--
-- Name: delegation unique_delegation; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT unique_delegation UNIQUE (tx_id, cert_index);


--
-- Name: delisted_pool unique_delisted_pool; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delisted_pool
    ADD CONSTRAINT unique_delisted_pool UNIQUE (hash_raw);


--
-- Name: epoch unique_epoch; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch
    ADD CONSTRAINT unique_epoch UNIQUE (no);


--
-- Name: epoch_param unique_epoch_param; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_param
    ADD CONSTRAINT unique_epoch_param UNIQUE (epoch_no, block_id);


--
-- Name: epoch_sync_time unique_epoch_sync_time; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_sync_time
    ADD CONSTRAINT unique_epoch_sync_time UNIQUE (no);


--
-- Name: ma_tx_mint unique_ma_tx_mint; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_mint
    ADD CONSTRAINT unique_ma_tx_mint UNIQUE (ident, tx_id);


--
-- Name: ma_tx_out unique_ma_tx_out; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_out
    ADD CONSTRAINT unique_ma_tx_out UNIQUE (ident, tx_out_id);


--
-- Name: meta unique_meta; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.meta
    ADD CONSTRAINT unique_meta UNIQUE (start_time);


--
-- Name: multi_asset unique_multi_asset; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.multi_asset
    ADD CONSTRAINT unique_multi_asset UNIQUE (policy, name);


--
-- Name: param_proposal unique_param_proposal; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.param_proposal
    ADD CONSTRAINT unique_param_proposal UNIQUE (key, registered_tx_id);


--
-- Name: pool_hash unique_pool_hash; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_hash
    ADD CONSTRAINT unique_pool_hash UNIQUE (hash_raw);


--
-- Name: pool_metadata_ref unique_pool_metadata_ref; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_metadata_ref
    ADD CONSTRAINT unique_pool_metadata_ref UNIQUE (pool_id, url, hash);


--
-- Name: pool_offline_data unique_pool_offline_data; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_data
    ADD CONSTRAINT unique_pool_offline_data UNIQUE (pool_id, hash);


--
-- Name: pool_offline_fetch_error unique_pool_offline_fetch_error; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_fetch_error
    ADD CONSTRAINT unique_pool_offline_fetch_error UNIQUE (pool_id, fetch_time, retry_count);


--
-- Name: pool_owner unique_pool_owner; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_owner
    ADD CONSTRAINT unique_pool_owner UNIQUE (addr_id, pool_update_id);


--
-- Name: pool_relay unique_pool_relay; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_relay
    ADD CONSTRAINT unique_pool_relay UNIQUE (update_id, ipv4, ipv6, dns_name);


--
-- Name: pool_retire unique_pool_retiring; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_retire
    ADD CONSTRAINT unique_pool_retiring UNIQUE (announced_tx_id, cert_index);


--
-- Name: pool_update unique_pool_update; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT unique_pool_update UNIQUE (registered_tx_id, cert_index);


--
-- Name: pot_transfer unique_pot_transfer; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pot_transfer
    ADD CONSTRAINT unique_pot_transfer UNIQUE (tx_id, cert_index);


--
-- Name: redeemer unique_redeemer; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer
    ADD CONSTRAINT unique_redeemer UNIQUE (tx_id, purpose, index);


--
-- Name: redeemer_data unique_redeemer_data; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer_data
    ADD CONSTRAINT unique_redeemer_data UNIQUE (hash);


--
-- Name: reference_tx_in unique_ref_txin; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reference_tx_in
    ADD CONSTRAINT unique_ref_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: reserved_pool_ticker unique_reserved_pool_ticker; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserved_pool_ticker
    ADD CONSTRAINT unique_reserved_pool_ticker UNIQUE (name);


--
-- Name: reserve unique_reserves; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserve
    ADD CONSTRAINT unique_reserves UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: reward unique_reward; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);


--
-- Name: script unique_script; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.script
    ADD CONSTRAINT unique_script UNIQUE (hash);


--
-- Name: slot_leader unique_slot_leader; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.slot_leader
    ADD CONSTRAINT unique_slot_leader UNIQUE (hash);


--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);


--
-- Name: stake_address unique_stake_address; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_address
    ADD CONSTRAINT unique_stake_address UNIQUE (hash_raw);


--
-- Name: stake_deregistration unique_stake_deregistration; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration
    ADD CONSTRAINT unique_stake_deregistration UNIQUE (tx_id, cert_index);


--
-- Name: stake_registration unique_stake_registration; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_registration
    ADD CONSTRAINT unique_stake_registration UNIQUE (tx_id, cert_index);


--
-- Name: treasury unique_treasury; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.treasury
    ADD CONSTRAINT unique_treasury UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: tx unique_tx; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx
    ADD CONSTRAINT unique_tx UNIQUE (hash);


--
-- Name: tx_metadata unique_tx_metadata; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_metadata
    ADD CONSTRAINT unique_tx_metadata UNIQUE (key, tx_id);


--
-- Name: tx_in unique_txin; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in
    ADD CONSTRAINT unique_txin UNIQUE (tx_out_id, tx_out_index);


--
-- Name: tx_out unique_txout; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT unique_txout UNIQUE (tx_id, index);


--
-- Name: extra_key_witness unique_witness; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.extra_key_witness
    ADD CONSTRAINT unique_witness UNIQUE (hash);


--
-- Name: withdrawal withdrawal_pkey; Type: CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id);


--
-- Name: ada_pots ada_pots_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ada_pots
    ADD CONSTRAINT ada_pots_block_id_fkey FOREIGN KEY (block_id) REFERENCES test.block(id) ON DELETE CASCADE;


--
-- Name: block block_slot_leader_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.block
    ADD CONSTRAINT block_slot_leader_id_fkey FOREIGN KEY (slot_leader_id) REFERENCES test.slot_leader(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_in collateral_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_in collateral_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES test.datum(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES test.script(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: cost_model cost_model_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.cost_model
    ADD CONSTRAINT cost_model_block_id_fkey FOREIGN KEY (block_id) REFERENCES test.block(id) ON DELETE CASCADE;


--
-- Name: datum datum_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.datum
    ADD CONSTRAINT datum_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT delegation_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT delegation_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT delegation_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES test.redeemer(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.delegation
    ADD CONSTRAINT delegation_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_param
    ADD CONSTRAINT epoch_param_block_id_fkey FOREIGN KEY (block_id) REFERENCES test.block(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_param
    ADD CONSTRAINT epoch_param_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES test.cost_model(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_stake
    ADD CONSTRAINT epoch_stake_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.epoch_stake
    ADD CONSTRAINT epoch_stake_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: extra_key_witness extra_key_witness_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.extra_key_witness
    ADD CONSTRAINT extra_key_witness_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: block fk6gd608i8qbyert1hlfl9be71h; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.block
    ADD CONSTRAINT fk6gd608i8qbyert1hlfl9be71h FOREIGN KEY (previous_id) REFERENCES test.block(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_ident_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_ident_fkey FOREIGN KEY (ident) REFERENCES test.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_ident_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.ma_tx_out
    ADD CONSTRAINT ma_tx_out_ident_fkey FOREIGN KEY (ident) REFERENCES test.multi_asset(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.param_proposal
    ADD CONSTRAINT param_proposal_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES test.cost_model(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.param_proposal
    ADD CONSTRAINT param_proposal_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pmr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES test.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pmr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES test.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_owner
    ADD CONSTRAINT pool_owner_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_pool_update_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_owner
    ADD CONSTRAINT pool_owner_pool_update_id_fkey FOREIGN KEY (pool_update_id) REFERENCES test.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_relay pool_relay_update_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_relay
    ADD CONSTRAINT pool_relay_update_id_fkey FOREIGN KEY (update_id) REFERENCES test.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_announced_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_retire
    ADD CONSTRAINT pool_retire_announced_tx_id_fkey FOREIGN KEY (announced_tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_retire
    ADD CONSTRAINT pool_retire_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT pool_update_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_meta_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT pool_update_meta_id_fkey FOREIGN KEY (meta_id) REFERENCES test.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT pool_update_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pool_update
    ADD CONSTRAINT pool_update_reward_addr_id_fkey FOREIGN KEY (reward_addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: pot_transfer pot_transfer_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.pot_transfer
    ADD CONSTRAINT pot_transfer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer_data redeemer_data_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer_data
    ADD CONSTRAINT redeemer_data_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_redeemer_data_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer
    ADD CONSTRAINT redeemer_redeemer_data_id_fkey FOREIGN KEY (redeemer_data_id) REFERENCES test.redeemer_data(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.redeemer
    ADD CONSTRAINT redeemer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserve
    ADD CONSTRAINT reserve_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reserve
    ADD CONSTRAINT reserve_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: reward reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reward
    ADD CONSTRAINT reward_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: reward reward_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.reward
    ADD CONSTRAINT reward_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: script script_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.script
    ADD CONSTRAINT script_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: slot_leader slot_leader_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.slot_leader
    ADD CONSTRAINT slot_leader_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES test.pool_hash(id) ON DELETE CASCADE;


--
-- Name: stake_address stake_address_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_address
    ADD CONSTRAINT stake_address_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration
    ADD CONSTRAINT stake_deregistration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration
    ADD CONSTRAINT stake_deregistration_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES test.redeemer(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_deregistration
    ADD CONSTRAINT stake_deregistration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_registration
    ADD CONSTRAINT stake_registration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.stake_registration
    ADD CONSTRAINT stake_registration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.treasury
    ADD CONSTRAINT treasury_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.treasury
    ADD CONSTRAINT treasury_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: tx tx_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx
    ADD CONSTRAINT tx_block_id_fkey FOREIGN KEY (block_id) REFERENCES test.block(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in
    ADD CONSTRAINT tx_in_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES test.redeemer(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in
    ADD CONSTRAINT tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_in
    ADD CONSTRAINT tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: tx_metadata tx_metadata_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_metadata
    ADD CONSTRAINT tx_metadata_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES test.datum(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES test.script(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.tx_out
    ADD CONSTRAINT tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.withdrawal
    ADD CONSTRAINT withdrawal_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES test.stake_address(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.withdrawal
    ADD CONSTRAINT withdrawal_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES test.redeemer(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: postgres
--

ALTER TABLE ONLY test.withdrawal
    ADD CONSTRAINT withdrawal_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES test.tx(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--
