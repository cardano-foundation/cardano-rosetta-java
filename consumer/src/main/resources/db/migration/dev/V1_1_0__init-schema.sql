--
-- PostgreSQL database dump
--

-- Dumped from database version 14.1
-- Dumped by pg_dump version 14.6 (Ubuntu 14.6-1.pgdg20.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: dev; Type: SCHEMA; Schema: -; Owner: cardano-master
--

CREATE SCHEMA IF NOT EXISTS dev;


ALTER SCHEMA dev OWNER TO "rosetta_db_admin";

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ada_pots; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.ada_pots (
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


ALTER TABLE dev.ada_pots OWNER TO "rosetta_db_admin";

--
-- Name: ada_pots_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.ada_pots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.ada_pots_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: ada_pots_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.ada_pots_id_seq OWNED BY dev.ada_pots.id;


--
-- Name: block; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.block (
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
    slot_leader_id bigint,
    slot_no bigint,
    "time" timestamp without time zone,
    tx_count bigint,
    vrf_key character varying(65535),
    previous_id bigint
);


ALTER TABLE dev.block OWNER TO "rosetta_db_admin";

--
-- Name: block_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.block_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: block_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.block_id_seq OWNED BY dev.block.id;


--
-- Name: collateral_tx_in; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.collateral_tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_out_index smallint NOT NULL,
    tx_in_id bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE dev.collateral_tx_in OWNER TO "rosetta_db_admin";

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.collateral_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.collateral_tx_in_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.collateral_tx_in_id_seq OWNED BY dev.collateral_tx_in.id;


--
-- Name: collateral_tx_out; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.collateral_tx_out (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    address character varying(255) NOT NULL,
    address_has_script boolean NOT NULL,
    address_raw bytea NOT NULL,
    data_hash character varying(64),
    index smallint NOT NULL,
    multi_assets_descr character varying(65535) NOT NULL,
    payment_cred character varying(56),
    value numeric(20,0) NOT NULL,
    inline_datum_id bigint,
    reference_script_id bigint,
    stake_address_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.collateral_tx_out OWNER TO "rosetta_db_admin";

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.collateral_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.collateral_tx_out_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.collateral_tx_out_id_seq OWNED BY dev.collateral_tx_out.id;


--
-- Name: cost_model; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.cost_model (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    costs character varying(65535) NOT NULL,
    hash character varying(64) NOT NULL,
    block_id bigint NOT NULL
);


ALTER TABLE dev.cost_model OWNER TO "rosetta_db_admin";

--
-- Name: cost_model_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.cost_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.cost_model_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: cost_model_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.cost_model_id_seq OWNED BY dev.cost_model.id;


--
-- Name: datum; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.datum (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    value character varying(65535),
    tx_id bigint NOT NULL
);


ALTER TABLE dev.datum OWNER TO "rosetta_db_admin";

--
-- Name: datum_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.datum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.datum_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: datum_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.datum_id_seq OWNED BY dev.datum.id;


--
-- Name: delegation; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.delegation (
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


ALTER TABLE dev.delegation OWNER TO "rosetta_db_admin";

--
-- Name: delegation_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.delegation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.delegation_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: delegation_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.delegation_id_seq OWNED BY dev.delegation.id;


--
-- Name: delisted_pool; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.delisted_pool (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(56) NOT NULL
);


ALTER TABLE dev.delisted_pool OWNER TO "rosetta_db_admin";

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.delisted_pool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.delisted_pool_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.delisted_pool_id_seq OWNED BY dev.delisted_pool.id;


--
-- Name: epoch; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.epoch (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    blk_count integer NOT NULL,
    end_time timestamp without time zone,
    fees numeric(20,0) NOT NULL,
    max_slot integer NOT NULL,
    no integer NOT NULL,
    out_sum numeric(39,0) NOT NULL,
    start_time timestamp without time zone,
    tx_count integer NOT NULL
);


ALTER TABLE dev.epoch OWNER TO "rosetta_db_admin";

--
-- Name: epoch_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.epoch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.epoch_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: epoch_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.epoch_id_seq OWNED BY dev.epoch.id;


--
-- Name: epoch_param; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.epoch_param (
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


ALTER TABLE dev.epoch_param OWNER TO "rosetta_db_admin";

--
-- Name: epoch_param_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.epoch_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.epoch_param_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: epoch_param_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.epoch_param_id_seq OWNED BY dev.epoch_param.id;


--
-- Name: epoch_stake; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.epoch_stake (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    pool_id bigint NOT NULL
);


ALTER TABLE dev.epoch_stake OWNER TO "rosetta_db_admin";

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.epoch_stake_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.epoch_stake_id_seq OWNED BY dev.epoch_stake.id;


--
-- Name: epoch_sync_time; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.epoch_sync_time (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    no bigint NOT NULL,
    seconds bigint NOT NULL,
    state character varying(255) NOT NULL
);


ALTER TABLE dev.epoch_sync_time OWNER TO "rosetta_db_admin";

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.epoch_sync_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.epoch_sync_time_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.epoch_sync_time_id_seq OWNED BY dev.epoch_sync_time.id;


--
-- Name: extra_key_witness; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.extra_key_witness (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash character varying(56) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.extra_key_witness OWNER TO "rosetta_db_admin";

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.extra_key_witness_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.extra_key_witness_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.extra_key_witness_id_seq OWNED BY dev.extra_key_witness.id;


--
-- Name: ma_tx_mint; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.ma_tx_mint (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    quantity numeric(20,0) NOT NULL,
    ident bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.ma_tx_mint OWNER TO "rosetta_db_admin";

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.ma_tx_mint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.ma_tx_mint_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.ma_tx_mint_id_seq OWNED BY dev.ma_tx_mint.id;


--
-- Name: ma_tx_out; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.ma_tx_out (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    quantity numeric(20,0) NOT NULL,
    ident bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE dev.ma_tx_out OWNER TO "rosetta_db_admin";

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.ma_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.ma_tx_out_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.ma_tx_out_id_seq OWNED BY dev.ma_tx_out.id;


--
-- Name: meta; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.meta (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    network_name character varying(255) NOT NULL,
    start_time timestamp without time zone NOT NULL,
    version character varying(255) NOT NULL
);


ALTER TABLE dev.meta OWNER TO "rosetta_db_admin";

--
-- Name: meta_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.meta_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: meta_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.meta_id_seq OWNED BY dev.meta.id;


--
-- Name: multi_asset; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.multi_asset (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    fingerprint character varying(255) NOT NULL,
    name bytea NOT NULL,
    policy character varying(56) NOT NULL,
    supply numeric(23,0),
    tx_count bigint
);


ALTER TABLE dev.multi_asset OWNER TO "rosetta_db_admin";

--
-- Name: multi_asset_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.multi_asset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.multi_asset_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: multi_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.multi_asset_id_seq OWNED BY dev.multi_asset.id;


--
-- Name: param_proposal; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.param_proposal (
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


ALTER TABLE dev.param_proposal OWNER TO "rosetta_db_admin";

--
-- Name: param_proposal_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.param_proposal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.param_proposal_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: param_proposal_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.param_proposal_id_seq OWNED BY dev.param_proposal.id;


--
-- Name: pool_hash; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_hash (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(56) NOT NULL,
    pool_size numeric(19,2) NOT NULL,
    view character varying(255) NOT NULL
);


ALTER TABLE dev.pool_hash OWNER TO "rosetta_db_admin";

--
-- Name: pool_hash_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_hash_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_hash_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_hash_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_hash_id_seq OWNED BY dev.pool_hash.id;


--
-- Name: pool_metadata_ref; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_metadata_ref (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash character varying(64) NOT NULL,
    url character varying(255) NOT NULL,
    pool_id bigint NOT NULL,
    registered_tx_id bigint NOT NULL
);


ALTER TABLE dev.pool_metadata_ref OWNER TO "rosetta_db_admin";

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_metadata_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_metadata_ref_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_metadata_ref_id_seq OWNED BY dev.pool_metadata_ref.id;


--
-- Name: pool_offline_data; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_offline_data (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    json character varying(65535) NOT NULL,
    ticker_name character varying(255) NOT NULL,
    pool_id bigint NOT NULL,
    pmr_id bigint NOT NULL
);


ALTER TABLE dev.pool_offline_data OWNER TO "rosetta_db_admin";

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_offline_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_offline_data_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_offline_data_id_seq OWNED BY dev.pool_offline_data.id;


--
-- Name: pool_offline_fetch_error; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_offline_fetch_error (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    fetch_error character varying(65535) NOT NULL,
    fetch_time timestamp without time zone NOT NULL,
    retry_count integer NOT NULL,
    pool_id bigint NOT NULL,
    pmr_id bigint NOT NULL
);


ALTER TABLE dev.pool_offline_fetch_error OWNER TO "rosetta_db_admin";

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_offline_fetch_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_offline_fetch_error_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_offline_fetch_error_id_seq OWNED BY dev.pool_offline_fetch_error.id;


--
-- Name: pool_owner; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_owner (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    pool_update_id bigint NOT NULL,
    addr_id bigint NOT NULL
);


ALTER TABLE dev.pool_owner OWNER TO "rosetta_db_admin";

--
-- Name: pool_owner_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_owner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_owner_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_owner_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_owner_id_seq OWNED BY dev.pool_owner.id;


--
-- Name: pool_relay; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_relay (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    dns_name character varying(255),
    dns_srv_name character varying(255),
    ipv4 character varying(255),
    ipv6 character varying(255),
    port integer,
    update_id bigint NOT NULL
);


ALTER TABLE dev.pool_relay OWNER TO "rosetta_db_admin";

--
-- Name: pool_relay_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_relay_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_relay_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_relay_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_relay_id_seq OWNED BY dev.pool_relay.id;


--
-- Name: pool_retire; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_retire (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    retiring_epoch integer NOT NULL,
    announced_tx_id bigint NOT NULL,
    hash_id bigint NOT NULL
);


ALTER TABLE dev.pool_retire OWNER TO "rosetta_db_admin";

--
-- Name: pool_retire_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_retire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_retire_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_retire_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_retire_id_seq OWNED BY dev.pool_retire.id;


--
-- Name: pool_stake; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_stake (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(19,2),
    pool_id bigint
);


ALTER TABLE dev.pool_stake OWNER TO "rosetta_db_admin";

--
-- Name: pool_stake_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_stake_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_stake_id_seq OWNED BY dev.pool_stake.id;


--
-- Name: pool_update; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pool_update (
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


ALTER TABLE dev.pool_update OWNER TO "rosetta_db_admin";

--
-- Name: pool_update_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pool_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pool_update_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pool_update_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pool_update_id_seq OWNED BY dev.pool_update.id;


--
-- Name: pot_transfer; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.pot_transfer (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    reserves numeric(20,0) NOT NULL,
    treasury numeric(20,0) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.pot_transfer OWNER TO "rosetta_db_admin";

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.pot_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.pot_transfer_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.pot_transfer_id_seq OWNED BY dev.pot_transfer.id;


--
-- Name: redeemer; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.redeemer (
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


ALTER TABLE dev.redeemer OWNER TO "rosetta_db_admin";

--
-- Name: redeemer_data; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.redeemer_data (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    value character varying(65535),
    tx_id bigint NOT NULL
);


ALTER TABLE dev.redeemer_data OWNER TO "rosetta_db_admin";

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.redeemer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.redeemer_data_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.redeemer_data_id_seq OWNED BY dev.redeemer_data.id;


--
-- Name: redeemer_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.redeemer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.redeemer_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: redeemer_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.redeemer_id_seq OWNED BY dev.redeemer.id;


--
-- Name: reference_tx_in; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.reference_tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_out_index smallint NOT NULL,
    tx_in_id bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE dev.reference_tx_in OWNER TO "rosetta_db_admin";

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.reference_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.reference_tx_in_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.reference_tx_in_id_seq OWNED BY dev.reference_tx_in.id;


--
-- Name: reserve; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.reserve (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    cert_index integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.reserve OWNER TO "rosetta_db_admin";

--
-- Name: reserve_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.reserve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.reserve_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: reserve_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.reserve_id_seq OWNED BY dev.reserve.id;


--
-- Name: reserved_pool_ticker; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.reserved_pool_ticker (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    name character varying(255) NOT NULL,
    pool_hash character varying(56) NOT NULL
);


ALTER TABLE dev.reserved_pool_ticker OWNER TO "rosetta_db_admin";

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.reserved_pool_ticker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.reserved_pool_ticker_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.reserved_pool_ticker_id_seq OWNED BY dev.reserved_pool_ticker.id;


--
-- Name: reward; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.reward (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    earned_epoch bigint NOT NULL,
    spendable_epoch bigint NOT NULL,
    type character varying(255) NOT NULL,
    addr_id bigint NOT NULL,
    pool_id bigint
);


ALTER TABLE dev.reward OWNER TO "rosetta_db_admin";

--
-- Name: reward_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.reward_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: reward_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.reward_id_seq OWNED BY dev.reward.id;


--
-- Name: schema_version; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.schema_version (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    stage_one bigint NOT NULL,
    stage_three bigint NOT NULL,
    stage_two bigint NOT NULL
);


ALTER TABLE dev.schema_version OWNER TO "rosetta_db_admin";

--
-- Name: schema_version_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.schema_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.schema_version_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: schema_version_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.schema_version_id_seq OWNED BY dev.schema_version.id;


--
-- Name: script; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.script (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    json character varying(65535),
    serialised_size integer,
    type character varying(255) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.script OWNER TO "rosetta_db_admin";

--
-- Name: script_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.script_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.script_id_seq OWNED BY dev.script.id;


--
-- Name: slot_leader; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.slot_leader (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    description character varying(65535) NOT NULL,
    hash character varying(56) NOT NULL,
    pool_hash_id bigint
);


ALTER TABLE dev.slot_leader OWNER TO "rosetta_db_admin";

--
-- Name: slot_leader_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.slot_leader_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.slot_leader_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: slot_leader_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.slot_leader_id_seq OWNED BY dev.slot_leader.id;


--
-- Name: stake_address; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.stake_address (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(255) NOT NULL,
    script_hash character varying(56),
    view character varying(65535) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.stake_address OWNER TO "rosetta_db_admin";

--
-- Name: stake_address_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.stake_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.stake_address_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: stake_address_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.stake_address_id_seq OWNED BY dev.stake_address.id;


--
-- Name: stake_deregistration; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.stake_deregistration (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    redeemer_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.stake_deregistration OWNER TO "rosetta_db_admin";

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.stake_deregistration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.stake_deregistration_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.stake_deregistration_id_seq OWNED BY dev.stake_deregistration.id;


--
-- Name: stake_registration; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.stake_registration (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.stake_registration OWNER TO "rosetta_db_admin";

--
-- Name: stake_registration_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.stake_registration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.stake_registration_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: stake_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.stake_registration_id_seq OWNED BY dev.stake_registration.id;


--
-- Name: treasury; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.treasury (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    cert_index integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.treasury OWNER TO "rosetta_db_admin";

--
-- Name: treasury_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.treasury_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.treasury_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: treasury_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.treasury_id_seq OWNED BY dev.treasury.id;


--
-- Name: tx; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.tx (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    block_id bigint,
    block_index bigint,
    deposit bigint,
    fee numeric(20,0),
    hash character varying(64) NOT NULL,
    invalid_before numeric(20,0),
    invalid_hereafter numeric(20,0),
    out_sum numeric(20,0),
    script_size integer,
    size integer,
    valid_contract boolean
);


ALTER TABLE dev.tx OWNER TO "rosetta_db_admin";

--
-- Name: tx_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.tx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.tx_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: tx_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.tx_id_seq OWNED BY dev.tx.id;


--
-- Name: tx_in; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_in_id bigint,
    tx_out_index smallint NOT NULL,
    tx_out_id bigint,
    redeemer_id bigint
);


ALTER TABLE dev.tx_in OWNER TO "rosetta_db_admin";

--
-- Name: tx_in_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.tx_in_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.tx_in_id_seq OWNED BY dev.tx_in.id;


--
-- Name: tx_metadata; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.tx_metadata (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    json character varying(65535),
    key numeric(20,0) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.tx_metadata OWNER TO "rosetta_db_admin";

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.tx_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.tx_metadata_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.tx_metadata_id_seq OWNED BY dev.tx_metadata.id;


--
-- Name: tx_out; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.tx_out (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    address character varying(65535) NOT NULL,
    address_has_script boolean NOT NULL,
    address_raw bytea NOT NULL,
    data_hash character varying(64),
    has_used boolean,
    index smallint NOT NULL,
    payment_cred character varying(56),
    token_type character varying(255),
    tx_out_type character varying(255),
    value numeric(20,0) NOT NULL,
    inline_datum_id bigint,
    reference_script_id bigint,
    stake_address_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.tx_out OWNER TO "rosetta_db_admin";

--
-- Name: tx_out_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.tx_out_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.tx_out_id_seq OWNED BY dev.tx_out.id;


--
-- Name: withdrawal; Type: TABLE; Schema: dev; Owner: cardano-master
--

CREATE TABLE dev.withdrawal (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    addr_id bigint NOT NULL,
    redeemer_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE dev.withdrawal OWNER TO "rosetta_db_admin";

--
-- Name: withdrawal_id_seq; Type: SEQUENCE; Schema: dev; Owner: cardano-master
--

CREATE SEQUENCE dev.withdrawal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dev.withdrawal_id_seq OWNER TO "rosetta_db_admin";

--
-- Name: withdrawal_id_seq; Type: SEQUENCE OWNED BY; Schema: dev; Owner: cardano-master
--

ALTER SEQUENCE dev.withdrawal_id_seq OWNED BY dev.withdrawal.id;


--
-- Name: ada_pots id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ada_pots ALTER COLUMN id SET DEFAULT nextval('dev.ada_pots_id_seq'::regclass);


--
-- Name: block id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.block ALTER COLUMN id SET DEFAULT nextval('dev.block_id_seq'::regclass);


--
-- Name: collateral_tx_in id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_in ALTER COLUMN id SET DEFAULT nextval('dev.collateral_tx_in_id_seq'::regclass);


--
-- Name: collateral_tx_out id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out ALTER COLUMN id SET DEFAULT nextval('dev.collateral_tx_out_id_seq'::regclass);


--
-- Name: cost_model id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.cost_model ALTER COLUMN id SET DEFAULT nextval('dev.cost_model_id_seq'::regclass);


--
-- Name: datum id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.datum ALTER COLUMN id SET DEFAULT nextval('dev.datum_id_seq'::regclass);


--
-- Name: delegation id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation ALTER COLUMN id SET DEFAULT nextval('dev.delegation_id_seq'::regclass);


--
-- Name: delisted_pool id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delisted_pool ALTER COLUMN id SET DEFAULT nextval('dev.delisted_pool_id_seq'::regclass);


--
-- Name: epoch id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch ALTER COLUMN id SET DEFAULT nextval('dev.epoch_id_seq'::regclass);


--
-- Name: epoch_param id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_param ALTER COLUMN id SET DEFAULT nextval('dev.epoch_param_id_seq'::regclass);


--
-- Name: epoch_stake id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_stake ALTER COLUMN id SET DEFAULT nextval('dev.epoch_stake_id_seq'::regclass);


--
-- Name: epoch_sync_time id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_sync_time ALTER COLUMN id SET DEFAULT nextval('dev.epoch_sync_time_id_seq'::regclass);


--
-- Name: extra_key_witness id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.extra_key_witness ALTER COLUMN id SET DEFAULT nextval('dev.extra_key_witness_id_seq'::regclass);


--
-- Name: ma_tx_mint id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_mint ALTER COLUMN id SET DEFAULT nextval('dev.ma_tx_mint_id_seq'::regclass);


--
-- Name: ma_tx_out id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_out ALTER COLUMN id SET DEFAULT nextval('dev.ma_tx_out_id_seq'::regclass);


--
-- Name: meta id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.meta ALTER COLUMN id SET DEFAULT nextval('dev.meta_id_seq'::regclass);


--
-- Name: multi_asset id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.multi_asset ALTER COLUMN id SET DEFAULT nextval('dev.multi_asset_id_seq'::regclass);


--
-- Name: param_proposal id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.param_proposal ALTER COLUMN id SET DEFAULT nextval('dev.param_proposal_id_seq'::regclass);


--
-- Name: pool_hash id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_hash ALTER COLUMN id SET DEFAULT nextval('dev.pool_hash_id_seq'::regclass);


--
-- Name: pool_metadata_ref id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_metadata_ref ALTER COLUMN id SET DEFAULT nextval('dev.pool_metadata_ref_id_seq'::regclass);


--
-- Name: pool_offline_data id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_data ALTER COLUMN id SET DEFAULT nextval('dev.pool_offline_data_id_seq'::regclass);


--
-- Name: pool_offline_fetch_error id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_fetch_error ALTER COLUMN id SET DEFAULT nextval('dev.pool_offline_fetch_error_id_seq'::regclass);


--
-- Name: pool_owner id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_owner ALTER COLUMN id SET DEFAULT nextval('dev.pool_owner_id_seq'::regclass);


--
-- Name: pool_relay id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_relay ALTER COLUMN id SET DEFAULT nextval('dev.pool_relay_id_seq'::regclass);


--
-- Name: pool_retire id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_retire ALTER COLUMN id SET DEFAULT nextval('dev.pool_retire_id_seq'::regclass);


--
-- Name: pool_stake id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_stake ALTER COLUMN id SET DEFAULT nextval('dev.pool_stake_id_seq'::regclass);


--
-- Name: pool_update id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update ALTER COLUMN id SET DEFAULT nextval('dev.pool_update_id_seq'::regclass);


--
-- Name: pot_transfer id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pot_transfer ALTER COLUMN id SET DEFAULT nextval('dev.pot_transfer_id_seq'::regclass);


--
-- Name: redeemer id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer ALTER COLUMN id SET DEFAULT nextval('dev.redeemer_id_seq'::regclass);


--
-- Name: redeemer_data id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer_data ALTER COLUMN id SET DEFAULT nextval('dev.redeemer_data_id_seq'::regclass);


--
-- Name: reference_tx_in id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reference_tx_in ALTER COLUMN id SET DEFAULT nextval('dev.reference_tx_in_id_seq'::regclass);


--
-- Name: reserve id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserve ALTER COLUMN id SET DEFAULT nextval('dev.reserve_id_seq'::regclass);


--
-- Name: reserved_pool_ticker id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserved_pool_ticker ALTER COLUMN id SET DEFAULT nextval('dev.reserved_pool_ticker_id_seq'::regclass);


--
-- Name: reward id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reward ALTER COLUMN id SET DEFAULT nextval('dev.reward_id_seq'::regclass);


--
-- Name: schema_version id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.schema_version ALTER COLUMN id SET DEFAULT nextval('dev.schema_version_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.script ALTER COLUMN id SET DEFAULT nextval('dev.script_id_seq'::regclass);


--
-- Name: slot_leader id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.slot_leader ALTER COLUMN id SET DEFAULT nextval('dev.slot_leader_id_seq'::regclass);


--
-- Name: stake_address id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_address ALTER COLUMN id SET DEFAULT nextval('dev.stake_address_id_seq'::regclass);


--
-- Name: stake_deregistration id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration ALTER COLUMN id SET DEFAULT nextval('dev.stake_deregistration_id_seq'::regclass);


--
-- Name: stake_registration id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_registration ALTER COLUMN id SET DEFAULT nextval('dev.stake_registration_id_seq'::regclass);


--
-- Name: treasury id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.treasury ALTER COLUMN id SET DEFAULT nextval('dev.treasury_id_seq'::regclass);


--
-- Name: tx id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx ALTER COLUMN id SET DEFAULT nextval('dev.tx_id_seq'::regclass);


--
-- Name: tx_in id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_in ALTER COLUMN id SET DEFAULT nextval('dev.tx_in_id_seq'::regclass);


--
-- Name: tx_metadata id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_metadata ALTER COLUMN id SET DEFAULT nextval('dev.tx_metadata_id_seq'::regclass);


--
-- Name: tx_out id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out ALTER COLUMN id SET DEFAULT nextval('dev.tx_out_id_seq'::regclass);


--
-- Name: withdrawal id; Type: DEFAULT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.withdrawal ALTER COLUMN id SET DEFAULT nextval('dev.withdrawal_id_seq'::regclass);


--
-- Data for Name: ada_pots; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.ada_pots (id, is_deleted, deposits, epoch_no, fees, reserves, rewards, slot_no, treasury, utxo, block_id) FROM stdin;
\.


--
-- Data for Name: block; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.block (id, is_deleted, block_no, epoch_no, epoch_slot_no, hash, op_cert, op_cert_counter, proto_major, proto_minor, size, slot_leader_id, slot_no, "time", tx_count, vrf_key, previous_id) FROM stdin;
1	f	\N	\N	\N	96fceff972c2c06bd3bb5243c39215333be6d56aaf4823073dca31afe5038471	\N	\N	0	0	0	1	\N	2019-07-24 20:20:16	207	\N	\N
2	f	\N	0	\N	8f8602837f7c6f8b8867dd1cbc1842cf51a27eaed2c70ef48325d00f8efb320f	\N	\N	0	0	648085	2	\N	2019-07-24 20:20:16	0	\N	1
\.


--
-- Data for Name: collateral_tx_in; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.collateral_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: collateral_tx_out; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.collateral_tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, index, multi_assets_descr, payment_cred, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
\.


--
-- Data for Name: cost_model; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.cost_model (id, is_deleted, costs, hash, block_id) FROM stdin;
\.


--
-- Data for Name: datum; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.datum (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: delegation; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.delegation (id, is_deleted, active_epoch_no, cert_index, slot_no, addr_id, pool_hash_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: delisted_pool; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.delisted_pool (id, is_deleted, hash_raw) FROM stdin;
\.


--
-- Data for Name: epoch; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.epoch (id, is_deleted, blk_count, end_time, fees, max_slot, no, out_sum, start_time, tx_count) FROM stdin;
\.


--
-- Data for Name: epoch_param; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.epoch_param (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, epoch_no, extra_entropy, influence, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, nonce, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, block_id, cost_model_id) FROM stdin;
\.


--
-- Data for Name: epoch_stake; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.epoch_stake (id, is_deleted, amount, epoch_no, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: epoch_sync_time; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.epoch_sync_time (id, is_deleted, no, seconds, state) FROM stdin;
\.


--
-- Data for Name: extra_key_witness; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.extra_key_witness (id, is_deleted, hash, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_mint; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.ma_tx_mint (id, is_deleted, quantity, ident, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_out; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.ma_tx_out (id, is_deleted, quantity, ident, tx_out_id) FROM stdin;
\.


--
-- Data for Name: meta; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.meta (id, is_deleted, network_name, start_time, version) FROM stdin;
\.


--
-- Data for Name: multi_asset; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.multi_asset (id, is_deleted, fingerprint, name, policy, supply, tx_count) FROM stdin;
\.


--
-- Data for Name: param_proposal; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.param_proposal (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, entropy, epoch_no, influence, key, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, cost_model_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_hash; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_hash (id, is_deleted, hash_raw, pool_size, view) FROM stdin;
\.


--
-- Data for Name: pool_metadata_ref; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_metadata_ref (id, is_deleted, hash, url, pool_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_data; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_offline_data (id, is_deleted, bytes, hash, json, ticker_name, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_fetch_error; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_offline_fetch_error (id, is_deleted, fetch_error, fetch_time, retry_count, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_owner; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_owner (id, is_deleted, pool_update_id, addr_id) FROM stdin;
\.


--
-- Data for Name: pool_relay; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_relay (id, is_deleted, dns_name, dns_srv_name, ipv4, ipv6, port, update_id) FROM stdin;
\.


--
-- Data for Name: pool_retire; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_retire (id, is_deleted, cert_index, retiring_epoch, announced_tx_id, hash_id) FROM stdin;
\.


--
-- Data for Name: pool_stake; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_stake (id, is_deleted, amount, pool_id) FROM stdin;
\.


--
-- Data for Name: pool_update; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pool_update (id, is_deleted, active_epoch_no, cert_index, fixed_cost, margin, pledge, vrf_key_hash, meta_id, hash_id, registered_tx_id, reward_addr_id) FROM stdin;
\.


--
-- Data for Name: pot_transfer; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.pot_transfer (id, is_deleted, cert_index, reserves, treasury, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.redeemer (id, is_deleted, fee, index, purpose, script_hash, unit_mem, unit_steps, redeemer_data_id, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer_data; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.redeemer_data (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: reference_tx_in; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.reference_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: reserve; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.reserve (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: reserved_pool_ticker; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.reserved_pool_ticker (id, is_deleted, name, pool_hash) FROM stdin;
\.


--
-- Data for Name: reward; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.reward (id, is_deleted, amount, earned_epoch, spendable_epoch, type, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.schema_version (id, is_deleted, stage_one, stage_three, stage_two) FROM stdin;
\.


--
-- Data for Name: script; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.script (id, is_deleted, bytes, hash, json, serialised_size, type, tx_id) FROM stdin;
\.


--
-- Data for Name: slot_leader; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.slot_leader (id, is_deleted, description, hash, pool_hash_id) FROM stdin;
1	f	Genesis slot leader	96fceff972c2c06bd3bb5243c39215333be6d56aaf4823073dca31af	\N
2	f	Epoch boundary slot leader	00000000000000000000000000000000000000000000000000000000	\N
\.


--
-- Data for Name: stake_address; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.stake_address (id, is_deleted, hash_raw, script_hash, view, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_deregistration; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.stake_deregistration (id, is_deleted, cert_index, epoch_no, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_registration; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.stake_registration (id, is_deleted, cert_index, epoch_no, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: treasury; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.treasury (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: tx; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.tx (id, is_deleted, block_id, block_index, deposit, fee, hash, invalid_before, invalid_hereafter, out_sum, script_size, size, valid_contract) FROM stdin;
1	f	1	0	0	0	64887f4d5a17571af19c0a73495c17d5dd2627951e50e39ecd7e674621f42d2e	\N	\N	20000000000000	0	0	t
2	f	1	0	0	0	3f691a4d66d4fc83efd06853ff0c1d119111b0a7125d47b65e43a4e5ee843a01	\N	\N	20000000000000	0	0	t
3	f	1	0	0	0	c7cf687c1cfd1f73c0588d648a6470803cc15024ae4a8ac3364bfec67021e72b	\N	\N	20000000000000	0	0	t
4	f	1	0	0	0	9e8a9e4822a651272d0d1fe589de0fbc63401214ce699fe8352347d65b9dd280	\N	\N	20000000000000	0	0	t
5	f	1	0	0	0	82e43cc5bb332e4ac7830d4ef4380b20f279c7f0619fbe310cc12a1b559f2191	\N	\N	20000000000000	0	0	t
6	f	1	0	0	0	625fa9bd92c3716f728436369b6adcf099a834fe5d41bea4bfddfd5b7e390312	\N	\N	20000000000000	0	0	t
7	f	1	0	0	0	133e4c47f9b9dcfea64291cf5c630dd3709de9891e8c866cb10de0973e314255	\N	\N	20000000000000	0	0	t
8	f	1	0	0	0	1083cf4e2e3724f70fb19b60564aeebcfd770f9f5ad6359cc57f96bf1ac689b9	\N	\N	20000000000000	0	0	t
9	f	1	0	0	0	ca4cc43006b0aa02934b16ccae0703d08992c4f91506345a4ca93d6340fd612d	\N	\N	20000000000000	0	0	t
10	f	1	0	0	0	c7f970e1aa9a65c08b74710ef5a64096666cb89743f86e36ea4ba7a1b6258e8c	\N	\N	20000000000000	0	0	t
11	f	1	0	0	0	09414de14156ae4e9114a6572dfdbb96ad4d430ec39f4554f9947ab44eb199e2	\N	\N	20000000000000	0	0	t
12	f	1	0	0	0	e528dc9403481a1889561327bc5fe27c427debc459e070b8084b309877fa4851	\N	\N	20000000000000	0	0	t
13	f	1	0	0	0	46c172ade222e6273da30fa040e00768f1a7cc82e4d32174043a6c5ec89cb79c	\N	\N	20000000000000	0	0	t
14	f	1	0	0	0	5011b14a299cb029ed828f9e2fd74e36cd381d04326a3295b2b08a98c23e341a	\N	\N	20000000000000	0	0	t
15	f	1	0	0	0	0d359eb9acd266527965ab62cd618228fb1bc37f1cf06ef74c19d8eb8edc9aea	\N	\N	20000000000000	0	0	t
16	f	1	0	0	0	99725ee3a2c582f0a5cb33df6b21d8185f3ba72b7a49d7ca1c5ad31a32c345d3	\N	\N	20000000000000	0	0	t
17	f	1	0	0	0	fac2e080aad8a75fb79d7b41f7d95935b05fd6e934a288d43781c715fbc615ee	\N	\N	20000000000000	0	0	t
18	f	1	0	0	0	12d36d56902c8574c5967ccd80f865f80877a3c3180a13aae6348400bab4df92	\N	\N	20000000000000	0	0	t
19	f	1	0	0	0	d9573d62abc811f9360e25c30242c79904fdc3954208dd4e53bcf7a0637a2243	\N	\N	20000000000000	0	0	t
20	f	1	0	0	0	548f7eafb747e96a3715b5f00cd26f45e7cf53c1c12c89b645a0105572179930	\N	\N	20000000000000	0	0	t
21	f	1	0	0	0	22c9c6553887ab7d5826e4dbd2b0d62ea8471e0f2ec5ec7a51fb740b5845d030	\N	\N	20000000000000	0	0	t
22	f	1	0	0	0	146a5f91a43a37e87216346c557f35ac8679e791bbb70ca3acf07559583ff2a2	\N	\N	20000000000000	0	0	t
23	f	1	0	0	0	c7bedc64c268bb745c7ff92db85475b1a552c9ab587b592b444107337bf2f73b	\N	\N	20000000000000	0	0	t
24	f	1	0	0	0	7869095410b274a3b75f42d61c7753fe078832bdfdacdc63c747059004e807db	\N	\N	20000000000000	0	0	t
25	f	1	0	0	0	fab36eacb10f0daeab0d53a4ed3697e9b6bbe20726c89dbc0a07cd7c46daf089	\N	\N	20000000000000	0	0	t
26	f	1	0	0	0	8d20ac9310e08986ce1f9bccbec9a1ed274a646613c4e078da1120bb273b01d4	\N	\N	20000000000000	0	0	t
27	f	1	0	0	0	d4b7e351543719e40e65968d73b5d0ef89f65fd29577a0fc942f390fdf2a8167	\N	\N	20000000000000	0	0	t
28	f	1	0	0	0	5ecb01051a43fbb26f917623b1e2887ed64d70a3d399a4bc89b64c220509d620	\N	\N	20000000000000	0	0	t
29	f	1	0	0	0	837fe6b2f2fd21a348af5182184ce540665437daaacc60c4d95753d6766a724e	\N	\N	20000000000000	0	0	t
30	f	1	0	0	0	3363b9be3a1ada2413bd9cf7cb0ee88f2935fe981b22abee62fba234e9938fd1	\N	\N	20000000000000	0	0	t
31	f	1	0	0	0	5a57e0f747d2b902a19752112e53df8dfc4545daff86f48112450b1da5c90bfa	\N	\N	20000000000000	0	0	t
32	f	1	0	0	0	182a898af6328fa9237b968262ae9439e9dfe6fd67660c69be57409cf61002f2	\N	\N	20000000000000	0	0	t
33	f	1	0	0	0	679fa2551a84512ed0c5bcd94fcef94e646471310bd511d1877b9eb5d57cdbd1	\N	\N	20000000000000	0	0	t
34	f	1	0	0	0	d0a1817763cf2c4d687df197783304b1a0f69ef9e87366258a1d021decf2fef5	\N	\N	20000000000000	0	0	t
35	f	1	0	0	0	e1fa92db52c6a17dd76bf804765fb399bec55cff7471c168bdb950e383deff68	\N	\N	20000000000000	0	0	t
36	f	1	0	0	0	4333f85faa08e5a8fd55557ceb23d9cb9595fafb9b2b67329f629375e098614c	\N	\N	20000000000000	0	0	t
37	f	1	0	0	0	5c4c8acabcffb8e2b97add34f82a028407a374f3e4bcb02fd13eb359550889af	\N	\N	20000000000000	0	0	t
38	f	1	0	0	0	f849fab25efe61a09331ecb2be8dc0bef67b7871e1a62c353bd1a51d44f639f4	\N	\N	20000000000000	0	0	t
39	f	1	0	0	0	c0c27ba2e93857643f28ff1952a722cba79f2ced1863784502fbbe6fb93f0276	\N	\N	20000000000000	0	0	t
40	f	1	0	0	0	1037586017b1e2d315e4dfbbb1da1a42fb0e03a1f611182e16c6e93a8eca7c28	\N	\N	20000000000000	0	0	t
41	f	1	0	0	0	67b12eef47ea087f7f37e6ee7a61e71bc581d4c257b31eae1509bb1ec77481c0	\N	\N	20000000000000	0	0	t
42	f	1	0	0	0	1c4ed3563e5a69f96489b7d5a2016ef6a8b9db9c4d0761e9cd484320fcecb4b9	\N	\N	20000000000000	0	0	t
43	f	1	0	0	0	ce8ae0315d67997a561d20a093fc65c9acb1832528126497f43244ba7ec5fb4f	\N	\N	20000000000000	0	0	t
44	f	1	0	0	0	5664bc214c07764d2df117abf6cabdae137eb46262239bf37cf767e0f8f79d90	\N	\N	20000000000000	0	0	t
45	f	1	0	0	0	80f165c20882797808485a91838f536d4ce12e9d007062da59a5a1a3013c2d70	\N	\N	20000000000000	0	0	t
46	f	1	0	0	0	01279dd3e8fc73d5a410d0df70c87e975cf370b49b35e8a5c871e0840eb38a70	\N	\N	20000000000000	0	0	t
47	f	1	0	0	0	bb49fc1294e2cb85b57914c7e1a9712dd2b39ee77b339997fd8c45ec25be7c89	\N	\N	20000000000000	0	0	t
48	f	1	0	0	0	ae4ac0efd8e208e1fcb511b105efdd76eca7d92a643567b554db96ca334ab5ae	\N	\N	20000000000000	0	0	t
49	f	1	0	0	0	9e5d6207933e52bafa17528665a681a43d84026e0717eeeb97517cbb13365719	\N	\N	20000000000000	0	0	t
50	f	1	0	0	0	2726fe3dc191b60ff8d54d23acdf5550bd973fa5d41996d85787deacfccc955e	\N	\N	20000000000000	0	0	t
51	f	1	0	0	0	b329892f5e575a79e20f8774d85355312076bf745536696bc22667072b49947c	\N	\N	20000000000000	0	0	t
52	f	1	0	0	0	6e744c1897498899f55bebf07224da399fe38b32bbdc1aae2da98cf71751cf21	\N	\N	20000000000000	0	0	t
53	f	1	0	0	0	daeb196aa990ffd27787e9d8f02c7987b13202c854173efe11ad280764babe15	\N	\N	20000000000000	0	0	t
54	f	1	0	0	0	8187482f215ac19c7b4816822b232d068690ec1a1161df513ae2b853310ee765	\N	\N	20000000000000	0	0	t
55	f	1	0	0	0	3c626e3014ec4e73d473f754bd593feca618d667735612de62b5be9ac1ad69a7	\N	\N	20000000000000	0	0	t
56	f	1	0	0	0	f087e82a186a37b7b57c04e86b785da7fa30f59a47c193665e0045a06829b617	\N	\N	20000000000000	0	0	t
57	f	1	0	0	0	05b05eb698da0a039c843c1df48925857c570e8e68dd71d99690cf8ec9de17c4	\N	\N	20000000000000	0	0	t
58	f	1	0	0	0	8faed81cf34275e67c0b9380dfb3ea69451a38514e8641811fd23f9e0a287f9f	\N	\N	20000000000000	0	0	t
59	f	1	0	0	0	59e5f7c1c27a86b75e776f6c9f7486e3d13f53c289f2f681192571306b067dc5	\N	\N	20000000000000	0	0	t
60	f	1	0	0	0	e5b77ac8565a4e39049cb4c736f10fbd869e9f8e8e16f1e554ff12e2e6cd1268	\N	\N	20000000000000	0	0	t
61	f	1	0	0	0	1c2a36be8fb41070f8ec9cd65717e7781c20f2bfc0d1592869f4b7d4b2970b90	\N	\N	20000000000000	0	0	t
62	f	1	0	0	0	9aa67957a73a80d5b18e0dc01557d2563ca4d164a5b50161972d15285234b626	\N	\N	20000000000000	0	0	t
63	f	1	0	0	0	626d60ef7c102af2d542428fdb8e34372d784c262099c48fd832ea89b14efaac	\N	\N	20000000000000	0	0	t
64	f	1	0	0	0	c0621cb9de09960952317afc015de2872268e8392e1b143882a9fe16db71d823	\N	\N	20000000000000	0	0	t
65	f	1	0	0	0	7a05788a28ca7e5ed22a35f41cffcfcce9fb3048a00f0cafc325fde2ed4b831c	\N	\N	20000000000000	0	0	t
66	f	1	0	0	0	fce5a5e6879ae7ce9a5e363094842d5d93d02999befda55a614f43c2512deff9	\N	\N	20000000000000	0	0	t
67	f	1	0	0	0	52bf47e7f23a9c61bf58b879c41df7d005fd3eeaefb0341c92c20d043b65b714	\N	\N	20000000000000	0	0	t
68	f	1	0	0	0	e97ae570b36d5d9383f2be891a369ac58cb0b86fd811353a85af279a1aff3d74	\N	\N	20000000000000	0	0	t
69	f	1	0	0	0	6c156bd332768b31e9b293e0d293ab1145dbce9faf633beaad09e34fba0e0ecf	\N	\N	20000000000000	0	0	t
70	f	1	0	0	0	10350fce349474527209d7a90bca45b745628fafee778b565f3b1da3eb9cfd33	\N	\N	20000000000000	0	0	t
71	f	1	0	0	0	c2497cc53a401d94c4a9f9ce46c40a6f4f2cfeb5322243528ef74dc7bbaef245	\N	\N	20000000000000	0	0	t
72	f	1	0	0	0	f6dfa24bc655a08a1e207391d38b8bfffc259b13f4aeaef0ab2d05380c26d852	\N	\N	20000000000000	0	0	t
73	f	1	0	0	0	b3df685a52671912493b74acd05a2098b017e74c8a572b84071ce255ab8b5eeb	\N	\N	20000000000000	0	0	t
74	f	1	0	0	0	8001ddcf8aa4876195b9a420b020de0eeeb74707f6f51be2475290940ecc9f1e	\N	\N	20000000000000	0	0	t
75	f	1	0	0	0	5187941c8ccb20f86b3554ef1fda168d0b1d0bdb4be939c2b8275a75acfedd38	\N	\N	20000000000000	0	0	t
76	f	1	0	0	0	3f6dbc5519851d7d80114d945d9c0da913a7b90abe9474dd08597a5f6b322562	\N	\N	20000000000000	0	0	t
77	f	1	0	0	0	6797dd6e9f51c03857d1dd5b9558df24ad612e9b4e5667142d8d5f0673c27367	\N	\N	20000000000000	0	0	t
78	f	1	0	0	0	95c590568c8084a5ee43ee9bc90cca2365784ab4a1749c1683ab3795089f7c77	\N	\N	20000000000000	0	0	t
79	f	1	0	0	0	bbdc0a9821d3563893c86c177c04d990500702a06d023277dcb9a2ea3c829a52	\N	\N	20000000000000	0	0	t
80	f	1	0	0	0	8d0ab3e7f696d930b0b96101678bee0560af5a52e686c138ac5ea518ac0278c7	\N	\N	20000000000000	0	0	t
81	f	1	0	0	0	3b02287d434a15ee2b890b9d0ba9438f6b7e017e3cd71de70dccb28b166672d1	\N	\N	20000000000000	0	0	t
82	f	1	0	0	0	df5b07a41f7b1f50ec9754a7dda755f20f8d6b81ee7d25b51dad0f6e24869c2f	\N	\N	20000000000000	0	0	t
83	f	1	0	0	0	ef723a0b1f5dada16ab2ea69e8de9608dedadb4ae96b1ea820989ca4e1defc85	\N	\N	20000000000000	0	0	t
84	f	1	0	0	0	182cb4ace4301c7c332a13fdb2a2a799d39e4e740ca25f1371349ef4eec13a7f	\N	\N	20000000000000	0	0	t
85	f	1	0	0	0	0d712f00a600aedb5b725a5e4f391dad44fa7f319655e38629514033ca8df0cc	\N	\N	20000000000000	0	0	t
86	f	1	0	0	0	5d985723ef566c6d3891049e8f1ae9dfe34f659996140f6bb4e2472dfb7e7fc3	\N	\N	20000000000000	0	0	t
87	f	1	0	0	0	ed5ce59e0618d9292a396e59538aec3ead29c44ee0889f63f25d62033b0e2a4d	\N	\N	20000000000000	0	0	t
88	f	1	0	0	0	17d47ddbfda352d3f98283e16b72da110de129bd3ede1a5e611923a792b06110	\N	\N	20000000000000	0	0	t
89	f	1	0	0	0	64c2404b84d87f11832736b8856aff3c0210566b4b60764968652022462f0e0f	\N	\N	20000000000000	0	0	t
90	f	1	0	0	0	6faa2cc8e50454a16b21038ce74e4beb576e4c1b89360347c26361253c749cd9	\N	\N	20000000000000	0	0	t
91	f	1	0	0	0	101bdee3c16da2a649a217fed2ae3f59580ae3447a34d048fe5d7d27837a04d6	\N	\N	20000000000000	0	0	t
92	f	1	0	0	0	6749e51e0cb51c4df48a56efcd510000b174c8f60b95d025edd2ea8db45fe03f	\N	\N	20000000000000	0	0	t
93	f	1	0	0	0	e567f503959afaf8088e8d8b53f19ce968d005ccb9b49b6e1880da643f13a5e8	\N	\N	20000000000000	0	0	t
94	f	1	0	0	0	c0fc25ec318c2e295112c90ebc3865d820acc8052688759f61af0462ab0612de	\N	\N	20000000000000	0	0	t
95	f	1	0	0	0	cbcb5d24b831547d69b2e82106f2735d60752f49e0f70788bac2fd56a240d19d	\N	\N	20000000000000	0	0	t
96	f	1	0	0	0	b377bd72bf833e4a488b0cabcd39a07c4c7d5d2694f6b35f4c91efb3758592cc	\N	\N	20000000000000	0	0	t
97	f	1	0	0	0	65eb25a5d2d73caeb57b978a393dd2a593c4b1ffb40456fb976140dd45e56821	\N	\N	20000000000000	0	0	t
98	f	1	0	0	0	4066d41c8d79c75eb95c883c0a5c2932769242a1bf3f91bab3f072d3028e5606	\N	\N	20000000000000	0	0	t
99	f	1	0	0	0	e41525b19e1e6c2d79cc70ed35fbc5f6c517eb59b2dcc49931039dcdb0737516	\N	\N	20000000000000	0	0	t
100	f	1	0	0	0	d40fecb0cfdfc3dddc20293f5b5a0d89be16cb738091f1ea252602dcaa3c320c	\N	\N	20000000000000	0	0	t
101	f	1	0	0	0	4a0478e3cc85bdd7818b7d94ace936ded7b51ca6323b85ad03d056dcf872c511	\N	\N	19999999999999	0	0	t
102	f	1	0	0	0	18ddf7fa2bc580496331b15b46f1d8e6fb3b4659fce10230dcb0074aafa810fd	\N	\N	5428571428571429	0	0	t
103	f	1	0	0	0	3c6d33de36618d4205bb655d7acbd264dc77b4e7e55e78072bb943836b8c1596	\N	\N	19999999999999	0	0	t
104	f	1	0	0	0	77819823e2bfc81bb71e4bea3c272a6b787171a07b07c75522406917648b1e6c	\N	\N	5428571428571429	0	0	t
105	f	1	0	0	0	4399bc853c6a5d67b3f7211493fc2ab0f852a452fd1bdb9499027127662a5e64	\N	\N	19999999999999	0	0	t
106	f	1	0	0	0	d8239beeeb82340b83af275157511cbf417ac0365b1d02425a67dca603d4dd08	\N	\N	19999999999999	0	0	t
107	f	1	0	0	0	ef39debd9b861ac9694c9e10b991e2c17b20fe69d02bd65ec04d68657fbcd7d6	\N	\N	19999999999999	0	0	t
108	f	1	0	0	0	80969ea317668240006ca07bf806a8f0e874650adf2cc4bb5f63d5ecbb8bf796	\N	\N	19999999999999	0	0	t
109	f	1	0	0	0	14f806c3c25ea753b84cf1b516c6987b9e6c68afadd42f6f4fa6b6e7e4991410	\N	\N	19999999999999	0	0	t
110	f	1	0	0	0	35ddfffa0803c78840f78124211a6834f023dea50e9b1e0d18c0dfa98718acfa	\N	\N	19999999999999	0	0	t
111	f	1	0	0	0	6ca09ac7df1ffe0cdd12ade5a187f6d8ed74f926b0053c86608049885b71096c	\N	\N	19999999999999	0	0	t
112	f	1	0	0	0	a706dfb8c00e1ae4070eae7e83b084d7de97895201810c9a7843f6b7f52a37cb	\N	\N	19999999999999	0	0	t
113	f	1	0	0	0	ec09747618628da46dc39c242bf14da8498fed0a1fcb8e43602e9ac5b65b4746	\N	\N	19999999999999	0	0	t
114	f	1	0	0	0	15c0cc8463efb80af0cf8735453ee92a3df59f1b0e5449d3aeb66c48c3b679f7	\N	\N	19999999999999	0	0	t
115	f	1	0	0	0	466bd2f404788cc76a5fc5462c4abb74abc51302930e20c501d7802b3be1790a	\N	\N	19999999999999	0	0	t
116	f	1	0	0	0	b8a127f286f2e513e9b5f54de7f0f6446beea85fc7189bc35e7843987a809af2	\N	\N	19999999999999	0	0	t
117	f	1	0	0	0	518f8ef30c24242c96de64073fbe6b22efc4d78773fac25b5800756408570bd6	\N	\N	19999999999999	0	0	t
118	f	1	0	0	0	500845cd0b9e51abc2baa783c8c2ad7acb701dc55a7f153771a66e1662e69c05	\N	\N	19999999999999	0	0	t
119	f	1	0	0	0	2c57a000e280f5cd55a34e09429ad51a5d6c6fb490b98d3179a60eb15c240569	\N	\N	19999999999999	0	0	t
120	f	1	0	0	0	3fd3c6ae35c915cb33e30d4fc77d8352345a64c3a5e9c77b575e58e6db041105	\N	\N	19999999999999	0	0	t
121	f	1	0	0	0	686560defdc826125c632e1c33683f4b6e59fe5f32fdc5f4e7b5697345b32b25	\N	\N	19999999999999	0	0	t
122	f	1	0	0	0	1e2c69662fd1581c4707f13b86129faee06c42308c023d1e2479cdc03ef723c6	\N	\N	19999999999999	0	0	t
123	f	1	0	0	0	a8bab21e6876cb95fffda105b2b85568bf6c8d755f1d7ee9dbbdeb8822d969c0	\N	\N	19999999999999	0	0	t
124	f	1	0	0	0	d9fd13b4b9b51a6a4a10fd31199579c842394344d7c2774d9bb60676a4beafb5	\N	\N	19999999999999	0	0	t
125	f	1	0	0	0	c863506efee720a89ab685ea6b0e4e31b7e15a8eddd013fa5e13d7ef5de9a1d2	\N	\N	19999999999999	0	0	t
126	f	1	0	0	0	63f3ab9cfcf44d485cada06b766f7fc73591fd781c5401db96316ebebca4597f	\N	\N	19999999999999	0	0	t
127	f	1	0	0	0	3036ca53d94996fae8cabbb75c1e8e77274ed8beddddb9aad1042d1fccd630c3	\N	\N	19999999999999	0	0	t
128	f	1	0	0	0	66e8087b25e5e55369d68e51449e2875430eb5f893a2a1f4fc2a0399fece125e	\N	\N	19999999999999	0	0	t
129	f	1	0	0	0	0a3eecbba8c47b5d941a4a06b3661a664c7290c071212365bb0fbc2e8ccd4112	\N	\N	5428571428571429	0	0	t
130	f	1	0	0	0	a8bc30cc729d5eb9a32914997461b57bdf240c888e53c30f537cd015adbe9ac2	\N	\N	19999999999999	0	0	t
131	f	1	0	0	0	11396a85a2a7df23da72f90812a698d131ad398381f6718ed7bffc587ae1cad3	\N	\N	19999999999999	0	0	t
132	f	1	0	0	0	f87c94b2f6a5468b51d68d1ad680840ce5856091dad6a8570e32ed333235edb8	\N	\N	19999999999999	0	0	t
133	f	1	0	0	0	218c605a4cf4f07e15246fa4c0227f96d6866841344f9ad21038f32d76bea5e4	\N	\N	19999999999999	0	0	t
134	f	1	0	0	0	c5ce88c82ee64c1a4fb5ff0e99055b45b3af9b6ae661b81b214dd681046d9d86	\N	\N	19999999999999	0	0	t
135	f	1	0	0	0	bce5abeac611c713b9e6f69b94f4bae6b53856ce710a39604fd2a22d816c82a6	\N	\N	19999999999999	0	0	t
136	f	1	0	0	0	cb804ffc38ffae45b45428cfb097d6df9037eda5a9c8e53d858f1c82404b476b	\N	\N	19999999999999	0	0	t
137	f	1	0	0	0	529b97f7c5bb908329e90bb7975c4d4082116ca96bf149addd7a22ebad68b5d1	\N	\N	19999999999999	0	0	t
138	f	1	0	0	0	109702b24276359731a0d468140d3f6dd16b7dc8930df25ddb4428bedf4738f1	\N	\N	19999999999999	0	0	t
139	f	1	0	0	0	71cf47f7d3ba58d1cacc060f0bc924b34fb52e4a73d68de0745e7b5a1a89c591	\N	\N	5428571428571429	0	0	t
140	f	1	0	0	0	dad0619a164c3869fff4e58096d19b91e4cf24daacb393bc25d5c010775ba51a	\N	\N	19999999999999	0	0	t
141	f	1	0	0	0	b3469bf68d168895554c0cc0854d27de94f1c9650d698752aedcd4e74e89ee10	\N	\N	19999999999999	0	0	t
142	f	1	0	0	0	d538efd5da4a1914de8cbb95515adfda6935d71f7b92b8acdf08744772f73b07	\N	\N	19999999999999	0	0	t
143	f	1	0	0	0	0d384300504b409885650ddf45229103686a320c6ee972ea1ea35a9d50c20e86	\N	\N	19999999999999	0	0	t
144	f	1	0	0	0	43cc4e4adc15bb6555acf01506ceaaf203d7329f6425598012406b4c09c74996	\N	\N	19999999999999	0	0	t
145	f	1	0	0	0	8ace72143f38d8716aca464eacb3993551160d1e7815d4ff81ad093fbf911dc8	\N	\N	5428571428571429	0	0	t
146	f	1	0	0	0	d13e153c6df662e565b4d5608d4224de21fb387c5371f499d788e82912069324	\N	\N	19999999999999	0	0	t
147	f	1	0	0	0	4090c3c2f22793742cc4382911a04dd85ec0c02882aa98d5fde00b3755e341b6	\N	\N	19999999999999	0	0	t
148	f	1	0	0	0	91e30a7aedea4e737745df7774e908c89bddf7f858cc79c54902f10a5f0c7ce7	\N	\N	19999999999999	0	0	t
149	f	1	0	0	0	900706397c11562a0f6f2d58598969629fafd6b63e6b42cb562c76417df4214a	\N	\N	19999999999999	0	0	t
150	f	1	0	0	0	8cdd9ed2f59b5d5b6f380b43cf0c99f52f4b0a6f6a144d8aaa5183948a02db30	\N	\N	19999999999999	0	0	t
151	f	1	0	0	0	04ac888be49e1c56ed3372410395fc3c06b96131fe64b42443a7d5892d14070f	\N	\N	19999999999999	0	0	t
152	f	1	0	0	0	b41cbb4ce7a8d944009142274fd0a8490645fe70220d1b1f5cdcfc19b1b94c18	\N	\N	5428571428571429	0	0	t
153	f	1	0	0	0	962a6d56ad99b4cfb15eeaebfdbe30db266023a7bc761f9a111e0a3247c28344	\N	\N	19999999999999	0	0	t
154	f	1	0	0	0	e909604251e02128a2334ce3e993854514808b5a2427193b054effc9752317c8	\N	\N	19999999999999	0	0	t
155	f	1	0	0	0	9b5774d19cfaa78cd3eb644c86c7ab14b2ee17ae6ba9b0c4b6f9162af84ac663	\N	\N	19999999999999	0	0	t
156	f	1	0	0	0	edf2397a389d3b377182488a7b2da321da5488eae95d114974bb4b70f74ca70d	\N	\N	19999999999999	0	0	t
157	f	1	0	0	0	7e77d52caf9436ad6ec21a42885f349ad238725078457ad7d8ea73a35c0772f3	\N	\N	19999999999999	0	0	t
158	f	1	0	0	0	e026f06fbc020b46041cbf3902baf151a5c0f904a17b526914ca9ecfbee89c98	\N	\N	19999999999999	0	0	t
159	f	1	0	0	0	5df163d56de5380d8350d6d8a76856382cb418b7254627f3efe5017259df36d6	\N	\N	19999999999999	0	0	t
160	f	1	0	0	0	4c1d403a0c825509df0fd54f67ba5dfaa73db049963a956506e86ec110a7719f	\N	\N	19999999999999	0	0	t
161	f	1	0	0	0	478031b96fccacb288fa754d52def32ab279fea815ecf11580fffff9618998a2	\N	\N	19999999999999	0	0	t
162	f	1	0	0	0	a814a357b821767b1d18dcd9f5a28bd0b5ffa66df29e09e1f367e33fa932f221	\N	\N	19999999999999	0	0	t
163	f	1	0	0	0	49386bdc878a10673309ca1411e35309a3d7d82f8bcbdef9eb551c74e20ae1cb	\N	\N	19999999999999	0	0	t
164	f	1	0	0	0	6ada3f35ab153a893e2327387c108971ddd7bc527162ba23ab90e6b47ca3ad1c	\N	\N	19999999999999	0	0	t
165	f	1	0	0	0	bf244513e707d61b594d57c2ed96c2da621a08a547f681c53d411007239e1a83	\N	\N	19999999999999	0	0	t
166	f	1	0	0	0	680ae958afc60cd4fab9d50437d7e9b77f39e6fae365f424c5dd1261c1b117b4	\N	\N	19999999999999	0	0	t
167	f	1	0	0	0	71fee906d1ae6dbcb86ba9109361f9e75b6e44ca12a7b6e38eb53caaef18b2cf	\N	\N	19999999999999	0	0	t
168	f	1	0	0	0	e04645681cfb4e4f15f99ea11ef451245ed65c3b0ca7f36c791361b46471ef5c	\N	\N	19999999999999	0	0	t
169	f	1	0	0	0	c629f6703dbcd17e096443dbaf274039c2c0d032aa3ec21da0e371027827d147	\N	\N	19999999999999	0	0	t
170	f	1	0	0	0	5309103b08a5a0e77315d54a20d38c3a3ea0f20dd9f7876c086e43fafed277e0	\N	\N	19999999999999	0	0	t
171	f	1	0	0	0	6b300c36d301746a8c749ad0f168f5c855c857adf8c63035f4481ea88e505644	\N	\N	19999999999999	0	0	t
172	f	1	0	0	0	0cb2831ecd55c4986faee0c9f3bb2c2b12b3ca48e2bb7bcd319f7d65eedc34ea	\N	\N	19999999999999	0	0	t
173	f	1	0	0	0	d39b04c9a8fc5c819a57781aae333a57b8c9556712e3f93a3eb9beb054d61f09	\N	\N	19999999999999	0	0	t
174	f	1	0	0	0	426be020f903b33c8c10661eaaea973d5da1f1817ba97ea43d2bd1dc711116b5	\N	\N	19999999999999	0	0	t
175	f	1	0	0	0	4674eebcd12f75a34115416ff38491ca17047abd8796c212820ed370007c34eb	\N	\N	19999999999999	0	0	t
176	f	1	0	0	0	32ff02ef83a3d2d3e47a9d9beea63013f9971445b1532a639fc16388d2cc7740	\N	\N	19999999999999	0	0	t
177	f	1	0	0	0	d5d4be88edccde34f5093b1d64de2a2d4338c8c4397d617ac6db9fa43d3a0d8b	\N	\N	19999999999999	0	0	t
178	f	1	0	0	0	ed54de7b1bf05423816a44f898c3482976d590d717313a5328f055582f9156e0	\N	\N	19999999999999	0	0	t
179	f	1	0	0	0	7a58c4aa565fd2f316bb277996a3d487374a7efed1147355ee38da02a08a6ca2	\N	\N	19999999999999	0	0	t
180	f	1	0	0	0	8c9e13dbba548f09f6f2095eb32343daf899587268682ecfd3b9176fe139531d	\N	\N	19999999999999	0	0	t
181	f	1	0	0	0	1b92ccd1fc259f21c2b91453bcdd26db39e366897895bab31877d5eb0d68d1dd	\N	\N	19999999999999	0	0	t
182	f	1	0	0	0	38004b65cefb79955b19fdfe7ac07421f357c2cd7fb5c7208c3a8b6bd640d43d	\N	\N	19999999999999	0	0	t
183	f	1	0	0	0	564caec7bd5e257fdf8a584f591c9cdf38eeb9aaa49dd341caf7649b7f0d147c	\N	\N	19999999999999	0	0	t
184	f	1	0	0	0	4577491c69c0e349fb31b88a54beefdad0ca580fb128a89c084caa02c9de1a5a	\N	\N	19999999999999	0	0	t
185	f	1	0	0	0	41ce133cbca203433bc303a5f924da45ffbed4fde77b8911e38d4d66c426f246	\N	\N	19999999999999	0	0	t
186	f	1	0	0	0	4319f29944295e9c643c9a9746bed1c8d3a549f1dc22fe4e3ec00486adf9a5ff	\N	\N	19999999999999	0	0	t
187	f	1	0	0	0	b21bbca2aa1566312259e9ce616b50d1c0085913b4bbf27737c6f35084f6da00	\N	\N	19999999999999	0	0	t
188	f	1	0	0	0	ccacdbc26ca5dc733820d84f272797f7c47b69f97defad266c495bd96debeb7b	\N	\N	19999999999999	0	0	t
189	f	1	0	0	0	fe647d0ff061147cb42f74956e7fc76b0757375048329019df50c4e9525034a4	\N	\N	19999999999999	0	0	t
190	f	1	0	0	0	7c40ea2651858948c1f354f539df2a62f35a6cd68181a0b533f58e857b506d79	\N	\N	19999999999999	0	0	t
191	f	1	0	0	0	80bb8a1211d6e576192577a86786f401ed32e0884b2bd8dc8ecad054b23fa998	\N	\N	19999999999999	0	0	t
192	f	1	0	0	0	060d1a249d8b921da25266aafa8a3085b438e6e31382041033f9c6681dc09d65	\N	\N	19999999999999	0	0	t
193	f	1	0	0	0	2802ea0bd75c8c15b9e1a47ab257e4990e7c6b289881f9a4d3a990f9c5301bb0	\N	\N	19999999999999	0	0	t
194	f	1	0	0	0	aa7ecfd4028048c504cf08123eb9ff9a928ada5655e9f848f57cd5a8e0834961	\N	\N	19999999999999	0	0	t
195	f	1	0	0	0	f26d34884ba13481bf0a896b84237b33afe4022b542f954657d40a0345301f4a	\N	\N	19999999999999	0	0	t
196	f	1	0	0	0	95b86825fdd09d6c0e657d433d3655503bb03cd4610dfb0e62b601d0cbc01434	\N	\N	19999999999999	0	0	t
197	f	1	0	0	0	68f090234c2566c820bcb03d00160e30378f151ad6c7366497dba46805533a46	\N	\N	5428571428571429	0	0	t
198	f	1	0	0	0	be094a62c7411f436cfec8a1a3f99a56e57fe7e678e1ddc042200247a5d85004	\N	\N	19999999999999	0	0	t
199	f	1	0	0	0	89b0c062e91576e2c57baa11d051bc175bbc7ec74585eda9c21a9351429c2da4	\N	\N	19999999999999	0	0	t
200	f	1	0	0	0	0f8e7781437c023989e10f4e033dc165505149e73bb4a7e5941eecc628bfe327	\N	\N	19999999999999	0	0	t
201	f	1	0	0	0	fea301a277c46b8b1fa210405cabd32c804f746d2e469b33f2cf2363c1ca8528	\N	\N	19999999999999	0	0	t
202	f	1	0	0	0	f45e6669c498abf947955ec17a7678fc7a6a4cefb546fc9f3ad2907f7fd6ede8	\N	\N	19999999999999	0	0	t
203	f	1	0	0	0	8e1b93825cd0e5f1ca164631f9c349e4e25384a47943ef731d55c5983be6d100	\N	\N	19999999999999	0	0	t
204	f	1	0	0	0	18e19b010b042eb05a94ed8fa7f5fbb9e3cc14b90e1972fc303968f1c4fb6f7d	\N	\N	19999999999999	0	0	t
205	f	1	0	0	0	82ca963e398cb2ab5e3888fed37bed9e7899fdad31ef10c2189a8c82cc95030e	\N	\N	19999999999999	0	0	t
206	f	1	0	0	0	cf24201c472d9dd755fcb455ea35af8427283ebb708582a0edbca620a1124349	\N	\N	19999999999999	0	0	t
207	f	1	0	0	0	7c4a02389c30d163745d263d96861d70a37e85f6f9c7b576c94db914a86df694	\N	\N	19999999999999	0	0	t
\.


--
-- Data for Name: tx_in; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.tx_in (id, is_deleted, tx_in_id, tx_out_index, tx_out_id, redeemer_id) FROM stdin;
\.


--
-- Data for Name: tx_metadata; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.tx_metadata (id, is_deleted, bytes, json, key, tx_id) FROM stdin;
\.


--
-- Data for Name: tx_out; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, has_used, index, payment_cred, token_type, tx_out_type, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
1	f	2cWKMJemoBakKr71kb6hRACLDFXp5MJA2xD24K7QCLRdZvScK3BCkdsu96cGZ8ENv6Jzx	f	\\x82d818582883581cbf916071e3eeb8f9bc930a7eb8c664bc03769d1cda263c7cc37fb5c9a102451a4170cb17021a3f869e7d	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	1
2	f	2cWKMJemoBajXqjndT8ugw64NyyywNRru3yieT4yV62R6Zje4F9WN3pXbQzwiEF3vcLFQ	f	\\x82d818582883581c900a4b82ce29724b1464db8407fb6aec914ea11e4d4d887fb084a7f8a102451a4170cb17021ac45670f7	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	2
3	f	2cWKMJemoBahti5Nt5otmb6B3sxHEbohkB1aKFkEBiKR1A7ygLoSzTPTKBUXonmiRiwp7	f	\\x82d818582883581c2dc3226fbdfa03f86ddc44641a1330f72ba235f88268a8b513c48df3a102451a4170cb17021a1ad5324c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	3
4	f	2cWKMJemoBaipDqkA5UuCHnJBfcdCwXSZqvpLpiVz1TfkyFXoHDaqWdNPJstVJ6Ad2ERi	f	\\x82d818582883581c650b86dab6e0298abca8199039e8c97439ec7dbf2ba07efcb48dd9d2a102451a4170cb17021aacd58f35	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	4
5	f	2cWKMJemoBajtE9twqWKPUQLtEU8aDDpYiCPFTS9oc8TfD1YNgViGwXUcS7L9MTGcHjo4	f	\\x82d818582883581ca519d09942902a35490124f5563de2fb41e974194a6b9a2df6551311a102451a4170cb17021a0e283ce7	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	5
6	f	2cWKMJemoBakbKK5uBtvfP5tTQATJN1DnXNZbVtKo4PBHqA3gZq1rWzbjzwTfCm8C9B3z	f	\\x82d818582883581ccf8c6be8cef553a7d9d5807eef126f9a004e51a71e66b7f54646caf2a102451a4170cb17021aaed68ce5	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	6
7	f	2cWKMJemoBaiQ52QYSRu7oofN1CbiN6MjXbHjnqizdPuRg6EymzvHKpzXZi4whHfx3Q6N	f	\\x82d818582883581c4c1827baf88b1011f7d14de56fbd46e0f18181efa7909bd1b1159277a102451a4170cb17021a297237f3	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	7
8	f	2cWKMJemoBai63i1gkodr1yrrwmcNLUJZqiAy6MF7Xj8nDG7npMfoEgnB94pFYYTauD3E	f	\\x82d818582883581c3979c535f44c3535bfeafd13257e48997b1e7a09bbc7fbee5f2c806ea102451a4170cb17021a8101d6e1	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	8
9	f	2cWKMJemoBakFqvzBwt9kxmeQAwKqL1ajfzCFy5tY1TZXob2NUENaAN7VxhCJaD3ybhB2	f	\\x82d818582883581cbb6eb9f8c476e9436b6614c1862897647e018e943686931d1f1ef29da102451a4170cb17021a51acbc75	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	9
10	f	2cWKMJemoBamAw2b4CPMpJTVt2Jog1b3q6DmbLGjXFMus7ZfizYWBMjYePLMmGcrmK1PD	f	\\x82d818582883581cf246a31870fece0b80e55761963b3b816ca545dd122fee98a4c2dee2a102451a4170cb17021aa0cdd678	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	10
11	f	2cWKMJemoBakAyWD5Qe7aTftJYWUcJntvnWQiKiQ8MhCEaZY4DD3KStXr73SgCiDqYvhG	f	\\x82d818582883581cb666edd35c93072762ce66a11e3ddaab1a6ef8c6fe0c22f980c1a76ea102451a4170cb17021a00e82fab	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	11
12	f	2cWKMJemoBai9rXSd5wmGzzYktjNffQ6QaHcMUCtPQgzvjJbDtbLYRYzn4PN4wZTthMue	f	\\x82d818582883581c3d68a4e5991fb0dfe489c1efbfd4e75a8c49d9015a6cebf26f664d33a102451a4170cb17021aae9f796d	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	12
13	f	2cWKMJemoBahmj4r84YFn4K7jkJU8FJdicDp1S8JMM49PikC3FqheqEAVwrVgGvB3oN9v	f	\\x82d818582883581c268c64e1b1bfa4b388c7596e29ec42ab15bc4aa7de191b27164ae154a102451a4170cb17021a7a1ab749	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	13
14	f	2cWKMJemoBajYangUhwh3erpxp4rwUXowLmWppr9Ki6SnGLZsWc1RQptQcVqsHS8p9U1U	f	\\x82d818582883581c90ce977b7254d1c9ed85e74399ba0701e9640daace324078a7869195a102451a4170cb17021ae8f71fb7	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	14
15	f	2cWKMJemoBajRGmZJQE4J4o64AMpA1ZeX2x1FQL6tQj2Bk9qv3cNjazq3rGoXir5Qcrpi	f	\\x82d818582883581c89412b5c2fbe4df78ae21e3d6c9cc4d3eb4bff962c2f6cf584128bdda102451a4170cb17021a72ccb3db	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	15
16	f	2cWKMJemoBakPwip4TJHA1W7TsCop7XQ9WqT1cchS46pFzk26tp3ptCTyyYThjVR5paMv	f	\\x82d818582883581cc3ccda11be2da6b8d278589536a590f0616d4415cdacce6360bf3f45a102451a4170cb17021a0b0daef9	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	16
17	f	2cWKMJemoBajKAPoXSAcac4kDiV1qezazvLmtqahzMd4atpadDMzhGGFaYcrgDfA5BjBG	f	\\x82d818582883581c82f14ebe406c74da5481a60d08469010c7f8646a5a4202905f4417f3a102451a4170cb17021a6435672b	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	17
18	f	2cWKMJemoBaiQKewfpuaQbeqjLeRmYdai9Qt7eXUZPbv4VwK594cSLuGK1ErtZoPgwLuN	f	\\x82d818582883581c4c5add343cbbcf5f16d2f4ca02a429d38dadb34b0b00e1a8c650d1a7a102451a4170cb17021a81eeb3a9	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	18
19	f	2cWKMJemoBaksfAxvNzmtzsJ5VeZqUw85FRfCJAwt8tGR5PCoDCxysZTf615JSqZBACRU	f	\\x82d818582883581ce06e6d17ce027264345f46fb14bba0d478d68b4313fdffdccb408ccda102451a4170cb17021a21c4ab9f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	19
20	f	2cWKMJemoBaiEHjmRy9GfsgkjWRsaoJuFsk9pCZdKDopmpwhv2Lg83FPDg73AMmdmoBrh	f	\\x82d818582883581c41fd78017e084ba8801013514f34389ada28bec8f3d8594618eb908ca102451a4170cb17021a10b23d1a	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	20
21	f	2cWKMJemoBaikHaQTAS4cWs8fSfvWZN5spAaf1BLYoRCG3uVDXjEyguugoqmEkCUbXfeX	f	\\x82d818582883581c60fab2a40dd55290abffd9b0cceaa01a4bb29f82b69144692e994237a102451a4170cb17021ae898bdc8	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	21
22	f	2cWKMJemoBahDf8szz42YJFvjZebQ2h183vdYaqqUsqMraTGUT5mK8jDMJYtKr11rMoSC	f	\\x82d818582883581c056b9378131e1f8d0d1ac0509ed68b49b9e4b3a03d6d1b3605a13a5ca102451a4170cb17021abeb093dd	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	22
23	f	2cWKMJemoBaimZz6qZ6pQanUbFr8TVj7pZAa1rjFBa4zdHDDQKF1SBMytmghhiUXR8tpU	f	\\x82d818582883581c624dfb73c29e4bfed80de3c375863a5dd5480059df653d169c5fd50aa102451a4170cb17021ab4389d85	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	23
24	f	2cWKMJemoBahCGNyBrqkxRz5LfgcUMCdv2nJKLbYuwYJSiAkTQPYqSJkuy2Zk96g7L388	f	\\x82d818582883581c03fb580dca8ae81eb1c551efe51957f5c918637a357e456ec731a3d9a102451a4170cb17021aa1b288dd	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	24
25	f	2cWKMJemoBaki6ABk6Ya1sGZxDk7WC9r4gTWLLtpYK2UbnXJBvNo1u1346kLueZ3FnG3k	f	\\x82d818582883581cd68bb422690cf9fda2b1cddaa864a8f44cd03cadb7b13c2f719d12bba102451a4170cb17021a95a89363	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	25
26	f	2cWKMJemoBahx9z1sDpGvRGPYFy5PfPHDRR98HEyfozpPmxPy8rsoAMtENKb5KZRMRPQJ	f	\\x82d818582883581c3152a9b75b8a3895e8585940abd082e069a87c9028cec9d186b256bca102451a4170cb17021a280aa35f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	26
27	f	2cWKMJemoBahn1uzw5RY9gRTGnt1Khj7V1xj1Y3cmpyETdyDpSCBgriEjvMMmHnssFdrv	f	\\x82d818582883581c26d936d6bb66a0c78e4e0413de5dd57fcab268bd55c25c249f1075bda102451a4170cb17021ade16062f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	27
28	f	2cWKMJemoBahbpR281HxvaASJ8E3y2jShZNXMjDigoJWAokgf6zS8DihZ5jMaqR8PN9nn	f	\\x82d818582883581c1c501faa31e081d6bd52f0ab21a5f451de2029f1c3543a2d6b5f0f13a102451a4170cb17021ad01bee87	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	28
29	f	2cWKMJemoBamK3TaNdhVVFZzVz32YMHGkpVG6r9GGtbE6pcVTEXYE2L6eZCAPDs5tiJCT	f	\\x82d818582883581cfaa7af201f3ca8237dbdfd4f4a062bfeeff78dea24df6169267899aea102451a4170cb17021a589e9594	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	29
30	f	2cWKMJemoBai3tmhRyRL3gpDC4z9hLdtnx9DGGRK6qVhCDSp2S3LmAs1FvtVys7b5cA7j	f	\\x82d818582883581c374017610a07661ae6691ce009d25c5f075d0004b0ca2673a51777e7a102451a4170cb17021aa6c41542	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	30
31	f	2cWKMJemoBai7u5Ef84SYC86BVP8SkLKdJFiPi8gEM2HFcghJWzKKdTZmVUWpEmuN2nUx	f	\\x82d818582883581c3b635506a960ebf9673fc7581ce962663845a01471e8ada487add0aca102451a4170cb17021a0a12ef21	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	31
32	f	2cWKMJemoBam45PqN2etHBnn8qecYNCcWdAZJAhiotTpBbtunmVf1NFn7TmTbare4bMuZ	f	\\x82d818582883581ceb3186e466092b0553d20125667cbd7ff2d5dd7f5e397afeda0477c5a102451a4170cb17021a5225e618	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	32
33	f	2cWKMJemoBak48vd7Q8oJGGRtHkMMNUMvZaGtypUY7Nyj3SdZrwdg5bh2iTMqpe8wMxhJ	f	\\x82d818582883581caf56a0abc11eb79173b6510c864a74624d0d72a8e3115476ea717f7fa102451a4170cb17021aeb958c7d	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	33
34	f	2cWKMJemoBahUvuzSWN79hZ4aaZYj86m8q46wmuCfq256u6JNTZH78nZgJPtja5E7RtMk	f	\\x82d818582883581c15328053f31dca2a28d270b7efc740fec35a21d0f928fbd9584beb58a102451a4170cb17021a2443199f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	34
35	f	2cWKMJemoBak8Y3Qxa1JY2qpKK4FTTuh7QSSX36MQqFAAhDnzoFdjnMyNnPufpkeRRHyn	f	\\x82d818582883581cb3e1e5d0d1a6acffeac0c1f5a847e6be241d67e06f6a892ae3f837d0a102451a4170cb17021aa9cca4bd	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	35
36	f	2cWKMJemoBamMfvunWBf4nv7VrQDyxQaVhn5asmytAgfdcuMwhtN5gY3xxBEGUTEUCfxZ	f	\\x82d818582883581cfd5eea400fa8a879be52e8c303b297d25154e36dc5598867dc33e23da102451a4170cb17021a212d2876	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	36
37	f	2cWKMJemoBaipV49L5AyF3DmvCuuZAgHQhAuosUyJs2vNbn9p5XZGeYjHx8AxmCMiGdVb	f	\\x82d818582883581c6550e5d83a9307444379284234bc7265e12cc9fe7223ab0c20531d1ca102451a4170cb17021a631dd7b2	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	37
38	f	2cWKMJemoBak2h1NKX8JcEuASHgfeNAG78ZTYUEB9Z4zNQJZYtHEm9Nu9ABVtnLRXk7cf	f	\\x82d818582883581cadd7fb4d98e43f4ffcf804028dfab360cf49c75396718d77357e5a7ea102451a4170cb17021a67f6f4c4	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	38
39	f	2cWKMJemoBahDVpanWgYSrtrvqCUUZ1xk2FNTpRJbQGKYFofyt4oAVjbENn9wuKfG9Pjf	f	\\x82d818582883581c05411991324d6f1adb0630e8d4448eced31fb24e19fd32e9fb8b0c45a102451a4170cb17021acbb2da32	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	39
40	f	2cWKMJemoBam6eUXYddMwDPMvit9pCahHzLee2RZCB5VENyaiU1aTiHc1czsmtVEZoJfn	f	\\x82d818582883581cedd94c7178224bd58bb5ecf6906e0bf9ecab77a2f11ff454cbe5a0e9a102451a4170cb17021aa67838fd	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	40
41	f	2cWKMJemoBahv88VL9uMsWFSL9mLFnQNjFPAoo6ab5ekrDgz5cw7o2izjL7Hcy4UVh5C4	f	\\x82d818582883581c2f394752fb74724409457b42abc5dd321e88e52c6ce901833031fea1a102451a4170cb17021a4d7414b1	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	41
42	f	2cWKMJemoBakSLBdvTtN6e6svUF45RFPknm4WnBurcVHRRARQqMMkeU3PpdXpcVAzLYLB	f	\\x82d818582883581cc64434cbe7eb8210f478fcf093e39d88a9adccb6881b831492a7138ca102451a4170cb17021aaf29395c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	42
43	f	2cWKMJemoBaiKjeSd6YxkNx6fYfcHpFXhecZrBMZdQqrCiTJf9deFEgv92MvWWZt95rsx	f	\\x82d818582883581c479dee634465b59d7cb68506d64af1ff6a4b1ac1858746abe6481037a102451a4170cb17021ad607feef	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	43
44	f	2cWKMJemoBahGrcUU1zNZYjDcdTYgzvmbCTen9uezUWGHQ8nCFx5GgRgEo14rtkgUrPiC	f	\\x82d818582883581c08b94c95f74e073d8a98d23c5e97bc40490054f7787fa8cfcd4f15f1a102451a4170cb17021a67f1fb65	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	44
45	f	2cWKMJemoBaihHmcXp7qESRXFkHJLbKsamab4Nhb2Si5MdfPMxaWQb8F9cADHa2i784Lf	f	\\x82d818582883581c5de22eb62e4bdd7dcda5e4d4e02b540fa07634dbf8f30be627c39a47a102451a4170cb17021a7708ccb8	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	45
46	f	2cWKMJemoBajb3fWdQn76E3fJz3tK7ZNphTVuoshfJPJ4Pm1gGru6U16Kh9qgYgkZkkM1	f	\\x82d818582883581c935a128328ad0818161b4762f95fdd0440ad73b914aba9149193c9b5a102451a4170cb17021a2272e26c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	46
47	f	2cWKMJemoBahrKFp6wBiya2cFVpWLseEzwNwGqGMgDoSDEkyYtGgRrNF4kDodc4ejEX6R	f	\\x82d818582883581c2b4a264d3a16b81a0081879a91be026f75b6ae1f5e3dd44176d06938a102451a4170cb17021a7054ce7a	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	47
48	f	2cWKMJemoBaithT7S3zV4Ep56h3yV8uaw7wzVSLRQeiovTgB5Euwf3rahBoK9FuUPmfbY	f	\\x82d818582883581c69ab484dfcd3b39fb3414e42ceda1ce5a172e1bfbb3b40c8c05b23a2a102451a4170cb17021a530d9d4b	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	48
49	f	2cWKMJemoBaiXpK1vbRSMM9Senf2zhhxwhANFuW9D89HeYbwE1YQH9GuY5GxY8NpfdKw1	f	\\x82d818582883581c5418c9ed1f000e069cc3005a586a46bee3c761e9b64b2ea5b6217b74a102451a4170cb17021ae2d49ce4	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	49
50	f	2cWKMJemoBam8JA3evvH8sw7cARrSEKzJVZSACFF3HT5i11LAD4eEc1ts1qGQrE8tGoAU	f	\\x82d818582883581cef8d94eac8a18ce23db65acaab757992271a9fa018030ce091ca96c2a102451a4170cb17021a2aa56945	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	50
51	f	2cWKMJemoBajiViwdUnUPXGvwEpYsJfhHcAkrAoExhdEugB2xQyLsaytpxJ4i9oGxddWp	f	\\x82d818582883581c9b0c277052b30aa84a627fdfbabcddb1350a6325204b7c08e120d1afa102451a4170cb17021a21c696c1	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	51
52	f	2cWKMJemoBaiVHxw2zLamWW26HhwfZFVY41gBgqqj48w8Zs6YgS1ALB9CtEj3nqoj4QwY	f	\\x82d818582883581c517d7c7f41249550c9f5f704ccf5c12803f7bc49cd390eafbe3753daa102451a4170cb17021ae0ca96ef	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	52
53	f	2cWKMJemoBai6D3AkYJgMNDEr6uVxKccvbeB64izpwa1ANdCPtSoK4rMbqgneGKNAN5kv	f	\\x82d818582883581c39a4506914d9e0f8b959ac55531b0f6e258761919b7ce9c45272b0f2a102451a4170cb17021a4009807b	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	53
54	f	2cWKMJemoBajv3Gmm49XGdFKGfAfvSGcePFH2NkESZgFU8vUpEnQdK321pGLes4tbnXz8	f	\\x82d818582883581ca6f92110eff0289d56b5bb7acfea25a7ee292e0150c0d202820779e4a102451a4170cb17021a9cb061f1	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	54
55	f	2cWKMJemoBaj5Tj1FiBcp1DinZw2CY4evVQYUH3D1a8PLHLa8htsPspzn2VKbfmbxGL3E	f	\\x82d818582883581c74c9d0e1454fb677345ead96572771e7759be6b2c3a5172db389c062a102451a4170cb17021af7821035	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	55
56	f	2cWKMJemoBajsJ4ZWKEQ5dY7AjKv5EotHbdFWRiKzbkmcMotT4YFDMahGniobitVPsV7z	f	\\x82d818582883581ca4232b3d680958c9c5a8ad9ed622886f5b6433b7f49525eb6902dea1a102451a4170cb17021a5a1dc175	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	56
57	f	2cWKMJemoBaihf7ae3NiSgcHSWDnggDbA2czzidfGmJfFoLPHjhsw1nsh53gDwyge4dLE	f	\\x82d818582883581c5e4381c96fc3f5014a987ade5e6f9f7b14a175e9ad50e500159675c9a102451a4170cb17021aeab25993	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	57
58	f	2cWKMJemoBaksJCP94NDzXeoqKfhXgiBeMVtRHVMRDR2mbUgLb87bMVWvqXmxC3C5sx4m	f	\\x82d818582883581ce00ec8680e10648c2a97d8ed5d21205781422a0eaee41e078c809f16a102451a4170cb17021a60080886	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	58
59	f	2cWKMJemoBaicxU8Dn8tHk4VAqkJdu3VsHEvfrBx82Pb3Nps3wUVZMfeWgcaBeJKwLguA	f	\\x82d818582883581c59684f73ce66bec53345bab8fbefc03d34f0d6dda2fa262af9fe9d32a102451a4170cb17021ad51b0aa5	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	59
60	f	2cWKMJemoBak3fhjNNrU4CoJ3yYKuwXrZwEyyd2xXVpzyKYypf5DKw1kt5ATzseTKhFB9	f	\\x82d818582883581caeda80370fba28e7a6fbac38d492374ce52062e656e7c3c4c26a4656a102451a4170cb17021ad8baf324	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	60
61	f	2cWKMJemoBaiB8n7dS7pKZBs38Z36MiBAk8BqSZHJVvXKJrLUupsNoba6cvKy9L1sBvgv	f	\\x82d818582883581c3ebb3818e2a5f9d4f477fadf32a93cdfb58c27456c81c2bea57713dda102451a4170cb17021aab1926ef	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	61
62	f	2cWKMJemoBakj7PqMWHbe2ovMzo4p5MP4u6Fr2Dsks2T6L5WWs5GZXkBncStkJTdxy74w	f	\\x82d818582883581cd799cd48e05221b5b6707f53988fd252b4c3f37c4a994be47ffb53e2a102451a4170cb17021a7ed59b6c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	62
63	f	2cWKMJemoBahs4T9z3jPgvrLQLcd5p2itrkXsAEFNDaCHhvQBWXVt3gg7uZtRbNr54SpW	f	\\x82d818582883581c2c0f1c4ff2258d44535275bea6654c696ab0f3ee83b4f28d003deee3a102451a4170cb17021aea5f87df	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	63
64	f	2cWKMJemoBakchHy1QmiyBXrfLhH9yp9G9mv1mraNjvqAhkfGSdMwzEXmxHTcUvGp71H9	f	\\x82d818582883581cd0f91d127a8af4da851e89b79a3cbde959a7b7745f74b5ab39d516fda102451a4170cb17021a44a330a8	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	64
65	f	2cWKMJemoBahjDBJduufhWr5cE3AWDxo5y2iizyKtm8mXT1ECTku4D5pq9hbbRqwMWzFa	f	\\x82d818582883581c23f32d9ecb9f4f9411964e211f6e625366bf6530fc425e9d29fe288da102451a4170cb17021ab64519d9	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	65
66	f	2cWKMJemoBahmJRkcux1jYp1LFcPpRdjTyLdbcYWkTXNxqKEVdedTAMz939s467SVapNz	f	\\x82d818582883581c261c0b36bc320f4302cabce17b7291ae2faf2d5277c01d53398996b3a102451a4170cb17021adb15d91f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	66
67	f	2cWKMJemoBakr3jdVuzD9cZ1YoM2Yp1APo3VSCPucHQaxR29pz2T7ccd5s1SFozLgf3Df	f	\\x82d818582883581cdec460ae34413233e021b7add0e319f021431a1bf4641c948ab1317da102451a4170cb17021a571746a6	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	67
68	f	2cWKMJemoBakAmWCQo2mZ99QViTY3YsuSWD774jpirobhrFbLTaz2kn7KSJ51sMjWSEtV	f	\\x82d818582883581cb630360ae5bc24d8a85b7eb9c802b6198ec4ccb1455023f1c13561baa102451a4170cb17021aaa212976	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	68
69	f	2cWKMJemoBakFXjBBUMeuiQnQyJLqQmdQzuMicq7279yKxaSH6n2wUPiBnsB6A3cYfP8U	f	\\x82d818582883581cbb1bb8f2c273e9645da0b56fe8660e3b0b1b35d3dc69576259f8775da102451a4170cb17021aa8ca1289	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	69
70	f	2cWKMJemoBahrbGhXPVWysfUY788WnwnkcvAKaD1xhCwMLTLezXYGtoySMcKHG5cgYZC1	f	\\x82d818582883581c2b932ce17f3bda2c5fa9ce95ddf4c1ef0b204a6cccf619b4e4fa3705a102451a4170cb17021a4e37faa6	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	70
71	f	2cWKMJemoBahDqyUq52QYTgtCRHSPGtf7xfEB7BH1ge4iF47EjSvAu57YSbCnLh67aCeH	f	\\x82d818582883581c059cfe8d7eb858f9020020650c11250adaa4adb1b449e8bb59a271fda102451a4170cb17021abee27f26	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	71
72	f	2cWKMJemoBaj6jnaphLYSTwugNTtKak77t1xRVvrt1GeRdqQqMhMXKSwoZjbWdBkrbKe1	f	\\x82d818582883581c761b84cf531b505a0f42a84be2dd179353b009563b109b7c18741547a102451a4170cb17021a2eb901aa	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	72
73	f	2cWKMJemoBak6VBVeTnAetN6j31T73mGAGCoWWyTyYCMtZBN6tzRBwpGJTeiCZCL4hSug	f	\\x82d818582883581cb1c3ec3966a19077fd655088116b5c047ccabc9f2ab2df65c52edd47a102451a4170cb17021ac934ca83	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	73
74	f	2cWKMJemoBaki7nTDMxnNPjdmEeuMWUnEx7GmoD44yF9qNGNc21Li9UaSNdPAD9aTx3hD	f	\\x82d818582883581cd6931d53b91b749e7a523f51b0474041c882ecccf00a2dd5ffe4a3e9a102451a4170cb17021aa6b1fc3c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	74
75	f	2cWKMJemoBajD3vQRgRamxcV6KMMLvhgqo8tvMY38U8x6gDbgJTi6Ggf4jQuVwJqJXSMT	f	\\x82d818582883581c7ca1008bc6d911faa2649b94393d0f2453205c2bc4cf61b839fe6b1ca102451a4170cb17021a069a8b26	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	75
76	f	2cWKMJemoBahYfornoKUwVU4o5qT2Bt8G4m5JxqQ1GPz5odwUAE7j4j815uziZG8f9cgY	f	\\x82d818582883581c190f7c26c65a96c62cda224dce1b7c383e4829c180c0b9451617d2d1a102451a4170cb17021abb039ea1	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	76
77	f	2cWKMJemoBaktRGQW5vxnvUYViiba13PVrUpfXd9j1DN8iK4rJAbrrmEUV6mwtoCAFsrP	f	\\x82d818582883581ce1377b9f68a2a910fc8700bbd454d248bcc37157350373a25db0c79ca102451a4170cb17021ab9fe56d8	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	77
78	f	2cWKMJemoBajjaNFDF4KNgzxHyZAYJwq8BNgx3hjrdfzkmB6bjFJUknjF6B2fE4JXofnQ	f	\\x82d818582883581c9c29ca8bd9bbe3fafd8f14885bd50d8cf524a14b907417c0154f709ca102451a4170cb17021a792e5391	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	78
79	f	2cWKMJemoBajQGY3HG6G5FB84QceQJzAx2coi6LSN6Z8Zc6Yz4a3Nx9aJE6qHBvNpgWcr	f	\\x82d818582883581c8837a423ccfe85a801b7a2c28de799020a046092218619e4e3194278a102451a4170cb17021ad81bbd9b	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	79
80	f	2cWKMJemoBahfzmkt1zWAAnqCbwhhzrGasr5aibC8C1geVUnL6pYVA4H5pF9xbu6YafVF	f	\\x82d818582883581c20a136a69b8ab597194b3c24fef2d5bec93f2b1e8c0850c8ad49f2d3a102451a4170cb17021a85bcdaf6	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	80
81	f	2cWKMJemoBai5owpyzenZRG9kKH62Dthu4dQSw1zrNLk5cSWm9a66SDHnZwxhPYHHNzX3	f	\\x82d818582883581c393b051d2ddd315a47c98e0019aae3339c66f8c9680abc15be18c125a102451a4170cb17021adf6a32da	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	81
82	f	2cWKMJemoBaiUTqZtwiD3NBAMS8y8JfsGuwrVr6344kdoraf7ZNnxBsGx21mmET3Ak2Qw	f	\\x82d818582883581c50a20a12c2edd8f0f2d3d7393670916c8b1bc4938e848e93f5267f8ba102451a4170cb17021a113c3f18	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	82
83	f	2cWKMJemoBaitJEi3UmuBdbTxWVcCyF7JemsnkV2zqfK6Pt6toTogujvo55QiyeVPKt7s	f	\\x82d818582883581c69416edefe2c0c87058275bef532c6b4bd196cc52a5b1705318a4f94a102451a4170cb17021aa10235aa	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	83
84	f	2cWKMJemoBaiNyqKH4qMizA9yNGAG4Pfh7TsWsW8PYbmo7NQuQV1KWV7KY7kuWMmEbNvb	f	\\x82d818582883581c4af804df15fbf8a9b4db50950d10403851900053aec8190359196e1aa102451a4170cb17021a02b9ea38	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	84
85	f	2cWKMJemoBaiqZvEgXxMiLMF4wM8HK2WMfJnv1MPmVVQo7YdYxiNMSa6hSF8PLtdhBmFL	f	\\x82d818582883581c666f8a580cf6708cddb82f4982f643c8a3de8b43f8dda32bed6f5fd6a102451a4170cb17021a08ed523f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	85
86	f	2cWKMJemoBakheEQF9DutroUDWwYYXXUi5KY3zHTW9PmoH6A5Fy6YeEGGyvKnbx1yJFZu	f	\\x82d818582883581cd6157b477ab97ea9b5994a07253578ea7d8af2d7107ea0b1f876a540a102451a4170cb17021aa35bb954	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	86
87	f	2cWKMJemoBajQ2z4PbTVh7t6UeSxD5Mz7rdvrYQyzY8iVbjbsijXsRt6vixso8qS5T1Jw	f	\\x82d818582883581c87f9d9a6bbd717afb48998ab5cf8615a669052012f5e0a07961217a9a102451a4170cb17021ae9cdc880	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	87
88	f	2cWKMJemoBajurhaao2XSYn1APf7QbeSpxjw5yN7dn3GhzZ984DzyngtJaeWNnygh3hHA	f	\\x82d818582883581ca6c8ec139c025742c5902924752f2b1975142e59ae85b2c9b1ace347a102451a4170cb17021ac3ffa4f9	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	88
89	f	2cWKMJemoBajseY4rJtcxwTzGBYh9vzLzuNg6BZ2onwNESJMfXVLrgwCpyGsN2JWDqStL	f	\\x82d818582883581ca48086bd27319568ba8b7b10fde52acaa384d0f0306bcce4144d4479a102451a4170cb17021a620cd545	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	89
90	f	2cWKMJemoBahXJnt8xZtzXVWwYUGMMNG1ryY9cnyVEErmR8v8YekuwyqTFa2ZtfY7PYsp	f	\\x82d818582883581c17a7301be4de4229d903fa4df26de6252a9250edc67e1dcbbf1718bfa102451a4170cb17021a1447e20f	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	90
91	f	2cWKMJemoBahLWwxnYCjVbUCWKRDdj3m2ffDdU6dhTPgZFFobboEjH74tco2Vv6g2WN5o	f	\\x82d818582883581c0c817f8e3040d759faf5d0c4e39614c5abfa48b322c3700baa63f860a102451a4170cb17021ae065e5c2	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	91
92	f	2cWKMJemoBaiz1d5V62bWPh5S6562oAFCHHZS2Q84rVvutXT4BuSNsaEAketuDjh9gGLP	f	\\x82d818582883581c6f28780e537f3f88cc27d1ffa7071ed19d5bbac144604f632dcb4e04a102451a4170cb17021a89035198	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	92
93	f	2cWKMJemoBajPGoyf4ipCXa8Y4i4E5WiXWpaaKZBZ9gcHdkiJ6s3VcbWEXHTPPyRaVsdK	f	\\x82d818582883581c87306dba0d6f680b722c88e5fabe48ac1d3f0e11b20cd6f2c37ddfd9a102451a4170cb17021afda26ab2	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	93
94	f	2cWKMJemoBahh5zRAn5egJauHckFwzLtccpvQiyrLz72nCohx3Tv77rS5hKFu6UxN9DFZ	f	\\x82d818582883581c21c179183f3e2a0f7787f93a250dbc69110a254b346cd5d8898b9562a102451a4170cb17021a9dc5cf2c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	94
95	f	2cWKMJemoBahWv6nS1yhHnM9TYQ4yK9RGJYWDEpSnzGK7oxcnmZWejMdqKLryioK2DDw9	f	\\x82d818582883581c173fb895d158678b21d48a7f47a26e6417905693f1b297e36424350ca102451a4170cb17021a98a81224	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	95
96	f	2cWKMJemoBaiLmWoFTvBd68uXDrRrhU3xPJaWDQ4UoTSgwG5tgYysm3PjQM2MT3S8HhuS	f	\\x82d818582883581c48aeea4c961b12bea775ad9f51fb0652c648fa3536f16ac0a6019c97a102451a4170cb17021ad177a791	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	96
97	f	2cWKMJemoBajCqAsiGx7c1HHqDcDD6KZskCo4ATJmTHrXnbcx45LjNPXSGdTYhiDXCP2o	f	\\x82d818582883581c7c66dced5fdae332af2a2e860d9b5850ea97cdd9c386e22b8eaa8e01a102451a4170cb17021abf030bd8	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	97
98	f	2cWKMJemoBahqsqXZMytAazDJVZzxbWrfyrokQrNNyqb1XrNefsWHF2yRxSf9zx4F1cgB	f	\\x82d818582883581c2ad63f24467b10efd14d512fb92d8cdebbf47dec136ee96917d1caada102451a4170cb17021a3ad09acc	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	98
99	f	2cWKMJemoBahmes7MXtF4kfFhzxmuGmpGqmNNgfwFBtgDJo6XHXMkLR1imdG2Y3Zxgx13	f	\\x82d818582883581c26793b793f98e5e3aa0e9b524cb578b9b1b0c0246288ab7d407df90da102451a4170cb17021a70018a46	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	99
100	f	2cWKMJemoBajb2NV2UUd7qUgBQnSSR9Vaew7evjNopQfqe8Yqai8jJUSgaTRo6RNi6VfR	f	\\x82d818582883581c93542c85977312b4eb64f9f0ee4ccd9889ce4206adde79d7fe036582a102451a4170cb17021a7ac0e19c	\N	\N	0	\N	\N	\N	20000000000000	\N	\N	\N	100
101	f	37btjrVyb4KAr171Hd3fu65bbtKxqwktHGwY9kNanPYGXQcFKA8d9Hp1RgLrxzU1AdJCJbJDByTnHdDmtA7pm985tKK8hr5JdHPXFfSvkkYZn3kb9G	f	\\x82d818584983581c045b6b72620590db47874059d017ce57ce58a41799fa062e48e315bba201581e581c0eb2c96f499f615d1294d33e8796ae161d20361209c1e0b7d9096cae02451a4170cb17001a8df7ff1f	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	101
102	f	2cWKMJemoBahEJS9xuB3R1ofSgtG621enmfpxfx9Unpo2K26wJPioaA4tizZrNMACNoQb	f	\\x82d818582883581c0615a51961b4d2019a758c38e8a89797d6bddaf380c60bfd9c8da8cea102451a4170cb17001ab5c6ffe8	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	102
103	f	37btjrVyb4KAtY8hCobTmAB36dzosSo644ZrzATKQhP1AsnM6BAVfTWwMX5BGXhigxLm5hk4beodymyjivxrH7ZY6BZjMu3AtafB5guvagxEZM7vJq	f	\\x82d818584983581c065abb15b4f952d30c6fc708d3b62c94ef841ed2f6ccaffd141633cba201581e581ca6095020d661f16e2c2830557a8e9a671830e95a3686ec2c63a8d5bd02451a4170cb17001a4f9f2a6e	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	103
104	f	2cWKMJemoBahLnFCQ8wrTuZ3sMyiCeEkUZDYLNucPiVTJr8UU3BgADsKtqosDYNFXzeiw	f	\\x82d818582883581c0cc73fdb9f488aa2a38d33a4ecedbc9ccc417aace6fdc18272d3c89da102451a4170cb17001a477be9fc	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	104
105	f	37btjrVyb4KB6yr5YozXGqSKemHyZfsgiRQFX3VdJBKx7waoSaScNWc2dNvhNp6HSnXMxUwDtBvicXDWdpoJ7cKLWAwqEYki5azdt1qDP4sHXh8XhJ	f	\\x82d818584983581c1028932ac799a032976350ffc46e975aea8b67ffcf1131237575c3b2a201581e581ceff333ced74ff063b91805ce6a65fb31d6f901ef53cdbf7a783e1a2402451a4170cb17001a9b965db1	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	105
106	f	37btjrVyb4KBEKzyCGqao8GmErVER19oBH3L8xVNCSZs8tCVd53iV5FXZAZ7bNCT67bKoasGeiYZxEoDzGBsKxb4uJxStU1e9wkaPo6BxUEErZG7LD	f	\\x82d818584983581c15f292c0526796d9d1567920d1b81abf826394f201f36b32d9bc2041a201581e581cc9ac148c34cfd4925b53ac0ffdddedd9a9cfc8577900cb409a21c6ad02451a4170cb17001a33d23bea	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	106
107	f	37btjrVyb4KBGJtifVyePJjSHTno1XNBeTcqmSqEhyUznts9KGQTsvCd9Hq6zM2w29njvmJYCtWmNkUXvayfqyv7epN1awWkKK1WUFQKJsjtevSKz7	f	\\x82d818584983581c17821fe2ce8214821c31a5847a40995a3363a5e2aaeceb641974a041a201581e581c75436855f7b2b80078da68accbabf112ed5af5333167f2600bf8822102451a4170cb17001a73fb3f90	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	107
108	f	37btjrVyb4KBPHxFJqCekpPztGnLgbVsUA46Q8Lj2LKbFJL5Nqk5LgP2u28eBJAxkkU2r118ARdXW7fXLPQgctwK22L4N3zc6XeoDqkadGmTd4s8a1	f	\\x82d818584983581c1d02c57d53c17e1761028b56bf9901911dcd528a5babc3b7b40fda05a201581e581c3112385ce3e27dde2af1635e3ec111b84329cae0cd3818fe13de32c002451a4170cb17001a1b96f476	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	108
109	f	37btjrVyb4KBTRHUnz17FQNFzHR8PpoGGwuNQZauAUxmvTb1o7Ragv9Zvyiv6Cb3rnrmYY1PGtVFTmom3TGg4mK5XpkRyf7PnnCG5EQM69i7MViLpU	f	\\x82d818584983581c20431348f382640343979be5023c85d5851922a4075c5de56de1b612a201581e581c7b05e3bdc13ff6e795fc98c9e11e74edde38e2622505b8597db0adc702451a4170cb17001a0d3dc915	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	109
110	f	37btjrVyb4KBUH8nDpwtt3sSc6rJ7AkYjmnqvt1tJ4J8ZKCuqPrEuEioJJZXeD9aohveoB9zhRWru6oM5zyBcgMtkA26HLtTDKsSVwzoqugfftbiPu	f	\\x82d818584983581c20f06f43827b40e5f38545aa124e1dddd21d9ff704b754bcdac3d787a201581e581cb6c02531d0b2ac1959b29b8eef02ab0e938f86d04d72140247c4f3b002451a4170cb17001a5e508434	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	110
111	f	37btjrVyb4KBYe4RSCngNCgVMAeMJkRQqoJs8t6t9e9BHcNvvT6awv4CruMWH2FyiudxcGfZHmjghDvqk39iFrmCt4XE2XDuYzyo97BxwS6MngfeWp	f	\\x82d818584983581c246002bbf89c40ff2acba05b8cec06211d9c5e7571406ea3db8c51bca201581e581cf79e85024e9636649646a2910ad17cdb12c55f2d263496ba1b406ae502451a4170cb17001a3e614bf5	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	111
112	f	37btjrVyb4KBcyGa22cYKz3Xo9UnB2kzZg4nqVX863JCAvUd54ehdg94DWwnBasCv6sUdbKdh9t4tf48oaXokeoms1HDNuegsmRjVntHBX3Z2hnrV7	f	\\x82d818584983581c27c99f075b8a0ad603c6e7befbaa316f30462dfd15b1633191bb29ffa201581e581cb94645b82c497cd1b03cbdd66846d2e4d87e525817fafc869a5c61b902451a4170cb17001afba98a6a	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	112
113	f	37btjrVyb4KBrhG1yaBVY3X1ZoTCjUH7gbiA4qSFsMGgtLUBgwHiMZPiAJ3kQrRiboPV3s7eYXZD9fnd27qRb1cCEMc4oU57aPn2cYcEdAEJHrsyLB	f	\\x82d818584983581c3299c5a807cd2e66286a286219a662701e656d87be2d2c85ccb7c886a201581e581c447bcdb55cb7031ae8c2b82be75486b699d7b053e19218c1ac80447102451a4170cb17001a5c1699d8	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	113
114	f	37btjrVyb4KBsV28Wce4x44WsTVoXa5s4zaBKALWXxMQ2MtfVgB8LJJW34QFpvjrxKmheLtpcLyVqaeu36oB6ZcgQPppFN4oqhdueoKBEpn8dfQUVF	f	\\x82d818584983581c3338e8ec85600c0178818d3b54521fbac14e8184c33d77ebcbfadbbfa201581e581c43ea00059f591f31153ebaffcf996dad8d5dc42f6ed61a3c59b2e36d02451a4170cb17001a49f71aea	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	114
115	f	37btjrVyb4KBtLs3NwnoLkyVx9ZxSoQ7mQx2ZvzanG7PGWWdMA9ksXqZxakLxdj9MAPKw7eQoZJWHxJJCsd7MeWj7ujfXtPsthGhcURT3Hste4Kr4n	f	\\x82d818584983581c33e63eee9cc1ba10409cc91c108e8d06b649793b869b81ceab50adc6a201581e581c7de0f460fed1ea5a0528946250dba6a4179c52e0f55e741874b1c23202451a4170cb17001ac6d24a9f	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	115
116	f	37btjrVyb4KC1L9MsZ8htPomWp4FV4hULeBVT6jf3GGqmDcw3k9tPn6pnfqGTWowQDvqVr7BqtQ5rcQ7gc5z41qh9vyBV7Ds83bRsndDbTAwkEUJ21	f	\\x82d818584983581c3967a8dd0b11cb9bca734f0bfcbf4fccde5d48dfb841b7fe86f30c26a201581e581cef8be4217e688de9dfd45f44bf1c3c7f6bbbb26681da1c70a139f88e02451a4170cb17001a499e0b86	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	116
117	f	37btjrVyb4KC3HyNR82Bj2Sr9o6CF9o3J5hBNycGb9JwrHggTYUHfivi87akkYDv8ayepMkM4mNvxTKvoVdMHFkMnZZgrk5qobwPKM8idSnYYmvTRU	f	\\x82d818584983581c3af38251c902d07d8a33f1ccbb7b5fdbeedb6e06f665e1e787a8ba0ea201581e581c8b4b20da44aa3c1eda318a43580754a6e9bec4df70bf5fb578631bc302451a4170cb17001af0cc39db	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	117
118	f	37btjrVyb4KC4LdZLvrexUgAntpySomDAPVwdMEnz1cP9pZxsxZqVYzM6zPPWAhc7byfwsLdgW8GEMTuTUagYFAKEmYgaDmYxK7cHxtWJG3hiMHxVU	f	\\x82d818584983581c3bc674b4c21a606e81e0f33691d6fa83e3244414bd14790aa70393e1a201581e581ccb1fe6de300a77c5eea6bbf07b145db5080f36a6cb6c4ee8d973eea702451a4170cb17001a1d292b8f	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	118
119	f	37btjrVyb4KCE1qeEoUh9b8CpcZcJ794Di14AxAELGoppJNVdB79nnuKcgRut566MdDkxTqravFaDSD9iwAvDByUHi59xocCY3ButEjmCQeLTLZXQ7	f	\\x82d818584983581c43661fcc1fb76a14fd8020e9ebaf812677d7fddaf734b92e5e4b420ca201581e581c0fc2f46e8acb0eac94207e554f6c0cccc4a00b5d9b4a493e55f87f9102451a4170cb17001ae695bf24	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	119
120	f	37btjrVyb4KCF4JTyyC27XuFmcrn9Nxj1DmM4G5BKnf8F8F8BSpvn3PnsLRNH2RZJoajmg4yqHnwMXpUnbb7sFSuthjXv6YUenX8EWsApQUzAm77Dm	f	\\x82d818584983581c443863bbc6591bbd44aed2ecd25ce6c3d3de703d0e568814268acf9aa201581e581cfbb0c5218a56db2e3c3214bec3544b158d4330748ed1ccc403fd385c02451a4170cb17001a47b270cc	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	120
121	f	37btjrVyb4KCGn7x5G6obn9NPNoTuv25LrBcqK9wHCm3XbrhxqycSQrbPfsDxgDp2M8pqTjCk8cVEG2fRxWrTjfG4q71MtyMo6nt8WG11kJzdQQSNL	f	\\x82d818584983581c45938558d103c26f50107a23a2f3deb8bc6f45b94683a68f1e78b29ca201581e581cf1f6839dd4ac8ce586c538f2ec762f8b2cd8ccef38bc01566be33a0b02451a4170cb17001ae93387a1	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	121
122	f	37btjrVyb4KCHCpiGd1J2GtVjP4KxEWP7RE6K6yxHzE97cbDgFD63fUFygbni8jKw1N37nGsT43KBvBn5w9ee8sVegr6Tg8fAr52mUkhdvTZYJV52T	f	\\x82d818584983581c45e96d822296daafc3fef524ef711a2bd91c714463ca325561ffeb2da201581e581c3c1f18be1ece269a6783abe6974880aaf341a0270519878fef5668ac02451a4170cb17001a112bebb4	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	122
123	f	37btjrVyb4KCLfSBFhCBdrb72YBNJkKcDCqpdxf3k7iwJTj7M3txxiq3fcam7Nyi8sLcJNSfgnUrh8C7RKEMN5wWpku7HLdZqZVuPRjeihhgXmEpCe	f	\\x82d818584983581c48a313edbfffc47f8a160be84db593b2084aa2f98f62ab68c3cf1527a201581e581cf5c43940b7801291ab5463283b9dec534ab1464841b92d0b3e33a7e902451a4170cb17001a8c65d587	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	123
124	f	37btjrVyb4KCR9vZcXetWv4QdP4jPSH4msGeXs8DUBUMVYJHgD62etfv2jiD7gmLbLezCAiGTQu9JvrHd9Wfu74wguKgkX1vCUQkzcWsn4rVWsKCyt	f	\\x82d818584983581c4c2cf5e493c401a19823ef2a77e72e5d80c3cd709f7b5990050c29b7a201581e581c5018a30e7ce95ba096263ec0ae497b6380475b6e8b3d6c37d223952802451a4170cb17001af003daff	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	124
125	f	37btjrVyb4KCRMJDqQ3iK4M19XhWGWpFbCoxjgeDB7ZqUhgW7jSYLEk5oVL4okVPVx5rXCgoK2ND9kAWnNU5QncJp1qvuCngRdJaLrvFwp4boE56VR	f	\\x82d818584983581c4c548351e9c7d4a0c8698d82fd126b06a94b8f89b44adcc57d4b7a69a201581e581c2769e3ee74ec70e701ef931dd738a929e4e5df7112329e599e489b0602451a4170cb17001a1de0ec54	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	125
126	f	37btjrVyb4KCRtni6YrG77RLPosnDqtEYoAD5xLdKYkWgnLqGa8yuXDUQd3psHrfxqaRcvNTsAW4ngUe6bzstbzSUJtwoaKbYaL8zjFAJJsZkQ42ti	f	\\x82d818584983581c4cc206f64ecaf8d37a45c1b1e84a73816c1feb8ee57149734cff2dd8a201581e581c76aec3a184649c1b508a156d043d381ad4d47d9e7e20704963cfd27002451a4170cb17001a5675e9a3	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	126
127	f	37btjrVyb4KCTVE8b2UitH791rYrkSrHG9u449h6JHKotuPWRsdVZQfP1jXrs4ygSxAnG1rM5mGFM6cmUqA44e9fenjbVC1QFYyn3R4CaptVZypKgW	f	\\x82d818584983581c4e037e0adc9a41e57dc4b9873f630a0fa542bcfca210c53956d981c9a201581e581cc6cfeaf98c53226b988bcfd167a6c96b1684d5e1f4170354d396427e02451a4170cb17001a63a565d3	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	127
128	f	37btjrVyb4KCaYYFDdnbHEdBzpDPcL5iH98AGF8J5avuweDHwKChDq9mBvLKVJS6F4YTuAVDigPjMnAcuYUJ2UUbvDPePFZBhLhBrFUKeauoKC8X5z	f	\\x82d818584983581c5392ec9d4d39291f4c758c7836b8d9ea4e9a64bdb785ce2063d201eaa201581e581c1fa181b3f0c2ad410ef1eb3aaab08122d522f06e6bee035338c75a9002451a4170cb17001ae0cc3be1	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	128
129	f	2cWKMJemoBaiXc4dYCHfDyvy3LcqPebJ8zfRJsZZoHDqik2SzK9Be73YQ5W9u5jEiMPXa	f	\\x82d818582883581c53e0f0d85d62c3b279a4c5d874c55f715dfda8c99ea7d2185611fb0ca102451a4170cb17001af2e31c55	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	129
130	f	37btjrVyb4KCdPziHrv1QgXL5zMy3KYxr7zPqoRd96iz3LNrkWbobRmPswTpRKgCQEkZcEnipiNJ5UoULAc33mbR44MchdHT5vLNYT9sPxwzpgNUWj	f	\\x82d818584983581c55d25174689fd5e0bd4e93dfbf1cb673149c2ad0c5fa0a48501a09f5a201581e581cd38739b907ad4c45648b2af3bf68e33796a288892364b3f717c7637d02451a4170cb17001a7fd7bae0	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	130
131	f	37btjrVyb4KCfir7GrvC6Y5kBNjeakZNd5po62AzQQ85SGkBB4QfXibC4fSNK5YvNeVgmPc8WbEeSUHRjoiqhJ4HDtinK2deBHSdCH6Cw8k2u92rdh	f	\\x82d818584983581c57a747df9a1b8fdf864397656d98b985f3b96e3cf559ffb2b8bd1e31a201581e581c1dbbb78e1559c70273981dc59b62c7ff1ff3a83ec18bccafe839817402451a4170cb17001aa5cef41c	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	131
132	f	37btjrVyb4KCgWqZ8sJW46mQGdZS9wKW2TEQesyCoRScUxvisbdvEkfsxYLR49i6wE6uP1BBgPX9eg8cxKHPuNyKpStwf5UVmRCXD2ahotjamotMwV	f	\\x82d818584983581c584737cc938e7eb179db23730ef048471c3564ebf7fef8691ff09b9da201581e581ce177310be74b3073ec94d5b2274fa8145997b6cb7a499d556c3b668b02451a4170cb17001aba8377c0	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	132
133	f	37btjrVyb4KCi8WRzrkFiE1AVYbSiXBzMRTuTLv8DAchfg4tPaDgHRuDN6m4dq4VkA17pkWwCoa2NmvNb5sGeU1ZkjcqFuYrWPK2C5a3TLBfx64uLs	f	\\x82d818584983581c598cf7ac04e7cd63ec3cb5ab5364ee68282e8651fb04615779d5be4da201581e581c5e6031ff5ffb6998d4feca82388e8afeb600c9375c102a9e03fc8fca02451a4170cb17001a92470278	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	133
134	f	37btjrVyb4KCjschbSccsYGDJo1rVBjdVrpH27iRtc5h1q4XRqpQJTma1NA9t9t8PrTsJjFE7WzNCczJHQR1RGXW1jDNEiEqNa6xctAZ4ZBtXKHVtp	f	\\x82d818584983581c5aec9530ae5aa5dd859240028a63974b544afbbbc0ffbd596a514808a201581e581cc5504df299b8b576c518d22ef0b2b520624a98b3d5e8a47c285d074702451a4170cb17001abe589fcd	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	134
135	f	37btjrVyb4KCmCLYttFEWLNQc1MRbV1NyhhssRioZ5CgkqHgYUTT1pPSr2hrfevSe8bSwLiPsLnaCbsxJQc5SWgWYEJDPWuUA1s4AotQxERNbT9ReA	f	\\x82d818584983581c5bf7641ab201bf36a18a036e3887097b4ac486094009638abfbc57b0a201581e581cb1b928d416baddaadcbc8e73cda0ba4e9f0217722389145b4df9d01602451a4170cb17001a32533e2b	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	135
136	f	37btjrVyb4KCqRLQRj8svdZGGLQDZGdXztzeC15Vvt6uZWg23QAdfL1dMc52dpc8jqKquWNj6xjyLnLciVnRxzEq1kiq54ssmc6h7V5xK1cqqKJWBT	f	\\x82d818584983581c5f4b6612271d95caf05c6539421fba13ac9957c5e1bcd17af1ccb148a201581e581c1d748db41fa3b2ae28b25ab7bfa68e3a95cf44c14717aae3ffd8184d02451a4170cb17001acf2b40ba	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	136
137	f	37btjrVyb4KCsozLcUUHR8GyVG7erY6j9zehKTADn3e5xpRJtu1YgfJzSmAyERBHUXa5LGWY2aR2KqcssnRjwugh1bGjxc6U6ZrePJnALYTw2TR3yh	f	\\x82d818584983581c612d95a2661432002a327fa7102ce413c4a39b3be31f2c47c9adfd76a201581e581cf4ad6d265e5b1ce98d7c6f12944058fd97f495c1c45ef6a0606bf5a402451a4170cb17001aefc6afa0	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	137
138	f	37btjrVyb4KD1x4cqHfGxrvBubZ8pSM8Jmw15UiHpy77eMsqpMewGND2GdvAwTBZhf4KA4uypBJnuUPbPYFovpRVJ92BUaMBHfQnAD3i15DAzD8EvL	f	\\x82d818584983581c679746e0563b79839024022a2b8a47ff1fddb7ce035d6af170247d92a201581e581c16add35760ce9dcafad6ad39da3230e07bd08fa94ae97247d5b4a45802451a4170cb17001ab54629a1	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	138
139	f	2cWKMJemoBait15xg1M73WAvWafoieg2GrcykbRk6J1QC2jMUXn7LpXf4mk5RUeu8qYeG	f	\\x82d818582883581c68f33ac888fefa46c5a78abe848287ef9294cb444e2c42f0fb41ad5da102451a4170cb17001ac6b505fd	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	139
140	f	37btjrVyb4KD5zMmtnD3jWpX3TSJnZJ8jMzCFQHYa3HcHNXxdAnK5A88SiWncRpJQxesMDrYgzPHk7SnFNag5teaFELV6hE9opnJpJzMGpVicDDRX4	f	\\x82d818584983581c6ac6117351d146c271b3a9adc2b3cacd04a48cb31e85924ddde7f341a201581e581c057ec5a2169806fe7e176b99dbf56087e1f524fb99af3c05841b151502451a4170cb17001a5f59d0ef	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	140
141	f	37btjrVyb4KD9Z53z9qD7gTWMHr22e8jDcpCHgJFQaGQsvnNkycRehAaxLAnufNRjhLzQ57XVGJnR6mcsk6MorapLpADT77tyTaX9xfUSZyTA32ZAy	f	\\x82d818584983581c6d94eeb4f198f410fcbbd2cba8be42092821a872f01fd497afc319f7a201581e581c742090d8f3e75e8d647ac883d2f26c9d6eca25874c6501b31522027102451a4170cb17001a2683c22a	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	141
142	f	37btjrVyb4KDA9F68PUv9efaoQHTvacu98Dk6Zx3784ADx4SDnwMfDt3uRfJwqBELeVuis5UEqsf9u4zAD9YC82s6YNmQu43avWDqrQq9Z4hHkEVrL	f	\\x82d818584983581c6e0bc5f4e10679e967a32e8a6cce9d0f6c1d0fb83e20ac8662c43a91a201581e581cc37ec8e62a98ffb825bc56570e3c5919f9e0bcab53f9e1b88a137bf902451a4170cb17001a198a6c55	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	142
143	f	37btjrVyb4KDB6sZamEoJWYBLoDWucRdDtRXCuQvLCoVoHPNjrywJKDz8PQg65ZtLvYvREwAK9oLzGGb6UcdAf9zwQcFaKRRHhLzCZxwTsRDVyPkSy	f	\\x82d818584983581c6ecd3aaab5dddd9bf4fee20a9d8a9df172e832ec0f2441b1bbf46f5ca201581e581cd76075cf341881b94b991154681a9a47f22d6fd5a74ba1d263b0dabb02451a4170cb17001aee2ee05e	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	143
144	f	37btjrVyb4KDEX2XToMQoi1No3YdREgZWzrf1xQPbfhbZTZnprFwDsRMiBxqUrA7p4BwjxXHDyqAccPwyX8iWWquz2CrLazJMR4s8AMz2US1D1ffJL	f	\\x82d818584983581c717e55c1feeaa3b81fdd6bfc41891dfb37b52285bb14b97de6cc3ce3a201581e581cd587cdbd1c47ddb75ba9d1ceef94be8e13c9f7c0afa5958072fdd70d02451a4170cb17001ab9df4c35	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	144
145	f	2cWKMJemoBaj34AMeqLspGBgX1PVc7z6VkALK3rtVd8iFgCtMUenNoHhVRnjeGfYVQJM1	f	\\x82d818582883581c724d71a999f361e57443e04fb227e13176ca9e478b176bb615f94a4fa102451a4170cb17001a7c5911a4	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	145
146	f	37btjrVyb4KDHDPBVenqrh8tUTVNYX5ZGwjd4r3svqdnwWicGnMZZV1E7nBJVQsDY69co936H9onHpmA3PYSabYH4ibbULphL1CitDgArH9KknBARc	f	\\x82d818584983581c739e14f823ab6ccbb84f7397daffd009f13c8bd1a95224440d47f352a201581e581c0983fff20723f06961a0d2b17a2336f90690a4fb9e8d8203c3231aa202451a4170cb17001a57595997	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	146
147	f	37btjrVyb4KDHFyvvKb29RD53ebt6N8kpbL41J4VxWpiFC4FnxxybP33M9tBbdqfMXvSvyTQpv4dULXf5B838kEWXSJ24bpHtFgcbRkiHQwqWFQ5du	f	\\x82d818584983581c73a71ee279bec731c1503aa3215462bf628877c216c94116bf097572a201581e581c3705910fa94311075a8e1c0e45082cb0d8e0f298707b86a3c6dfa36d02451a4170cb17001a56907684	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	147
148	f	37btjrVyb4KDHmdZ8ewhythkmGaUzLCwME5pGtWR7nPhE9nCjLMcKstQFyKq1vTSagA2BXtiopfGwtLq2e1jhUVKsw1Z8Me6XTmLm5C9MzRYCZCd3n	f	\\x82d818584983581c740e3a3c6e5d5ff30ed37b977dca675691086316bfa83e26facb3fb0a201581e581cdbe799216bd6d47e1380d4f090da33c30d1ff0981632b2943e4e7b7702451a4170cb17001af5cfe009	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	148
149	f	37btjrVyb4KDJc8Af5dfJY2jcFbtkofFL6qXBxWnk2kHzCE63qAQR9Ynnk1XkfUrcnBrN7EEyvxmUDEFdNFfZHzXKhmkSQht3b6Y5rHHmuFYYoKdZz	f	\\x82d818584983581c74b6de64fa7d37109a17d9fc417452a09a5878a062432b8a9cd332dba201581e581c71aa8c36c9f9acd77fe1023cd42a713935dbe25560612293367e62ab02451a4170cb17001af0653f59	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	149
150	f	37btjrVyb4KDLtM8HUJsBwergjZUj4DcMfkFmbV4bXUFGJk815o9nowX9ndPPVAeSNjAFYqJeFwTiMa9Ka8LqBnqFZgPpacyx9LrQLoXVMjvvLB7DK	f	\\x82d818584983581c7682b0fb217b204829985d106c440a83b20e9fee02cc8c8f712c8088a201581e581c1d44bec12a698858ed986a222b66a2c31031f76aeff4c67c42d12fce02451a4170cb17001ae8f691c2	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	150
151	f	37btjrVyb4KDTABtj2RScLVCLVyFhxcURYUZNVta8CghbH5Edz32XSP79NHc28QTkKLMNUBupRnJXs4zcZt8C2fiPFGZfgSBMqMGMidWc2zo9piRb4	f	\\x82d818584983581c7b73fd9ce6c58d027286950a73306d4b168ce2a6760c912e6b7f5df2a201581e581c0973d6ae810a15b2c44a71c6671698a84d420636ad5c2c2908393a9902451a4170cb17001a98d1ef6f	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	151
152	f	2cWKMJemoBajGgvgVVziaKmUFa4LwJnAHffmuaSJBMDqethwJVQsyBsTSfFhp5jFpkVQM	f	\\x82d818582883581c80632193e03f035bc477c75ae58234a0dd733aa9e11751c8c3219672a102451a4170cb17001a0f28f622	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	152
153	f	37btjrVyb4KDaKs8A6CU8Lrzxpr3WNM1kDdd4CPe66TqSP2ehHexrAuZ3ykMmhkaUZEHUEiq78ELQx5vpSFGHXFKbyGgrWa8rokqamCV8bSKiqsqVt	f	\\x82d818584983581c81198df206c8fdd3a4d3b9f288810c387075fc96cb444771241414fea201581e581c0b684e001b2109677135d2c8cd153ef5e95f0d5a4df229657c9955f602451a4170cb17001abb17f9bb	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	153
154	f	37btjrVyb4KDdgCo1a7URfpQVFoTJEUcn4LqWpAtCeLaW7NeGMJtecsahTJM7886BjLcnhU2CboLSUojCPcab3WNTmXFDrRMHMHdmWefCAyYA7QaaL	f	\\x82d818584983581c83bd644b3028a6ee02a437b1292cc4bebbbdee9f2eedad619e6001d6a201581e581cea1a0c3f56ca17f5ac1e490af0a89b5b8683eb11d25f2363821d6a3202451a4170cb17001a915c6889	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	154
155	f	37btjrVyb4KDec7E64byKc4XjmmCRDaTGQYgHJTPDijZVr7NwZSP8g7ienzTLx5Z1quaQRhJqqAyV8Z2QdkzXvjTTRiVDCqps78uGp3uuth4wEJKP9	f	\\x82d818584983581c8478d96d1877f68de3b0ad2dc93347793de990e7f4490986fb6f545fa201581e581c0aebb8716e0715058768b0076c749b7977ad08d55b8e43ad4a8a9d0c02451a4170cb17001a04560984	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	155
156	f	37btjrVyb4KDfwNJcNMYsyEDVHWrGrNAPBwF9FGEnm5j2XFs4BeGdiSPqPtqCjWCvcRYBfY5EoDjRBhjsTrr2HjB1jw8XZ9Hy8wd9gz4KkCbMugSgf	f	\\x82d818584983581c858587ac68209ac3695b1170c4abade806a6c09ee8733b7c6e21d6bfa201581e581c0f1b9cfe58e1fe7c344e9bb40631525da69a171254db3f3fb89a86b602451a4170cb17001a3f711058	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	156
157	f	37btjrVyb4KDjPpuxBuJM1Ma5NGBqriSEAcozZMqYkWEkoJ3GgR8MAy3Zeb8q5BvtsWGEpSFQ69znPuaX4kVCcnEiEMDp91A6EL8A5YM12cpYYogLm	f	\\x82d818584983581c883ea62908e84ef4fdf2b0c629b837c1635ebd7b833e1b3b43abbe04a201581e581c0a847123c7753567b462abf11c178a8bf1612a4b95cdd6d9532fda3102451a4170cb17001a577629b6	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	157
158	f	37btjrVyb4KDmCmpc6FutA3PbQmWxcsbzZsFEdMKhHxrJVtGkmaiWd18dKmiaRA6o4Y2sAjDwjFozKzNzQg3dp8CXVSPpWgDLAXaozoRyPanW7UM8B	f	\\x82d818584983581c89ab9d1256982064a6654c10b02201e2415b76a3e1c7daa867529f47a201581e581cdd0dbe97231098043fe3d628e58872bd0dcff4280e97f076740e450202451a4170cb17001a054be5e8	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	158
159	f	37btjrVyb4KDpppzxzoaPgnstPejNxGaSZ3Vh22Qd2DWGrwfLJ2tizs33Y5Yjya1U6TXPzVX2PT5g1PXMy4jR4aWZRGZqYJk4Uw9p1h6BhEa189eiN	f	\\x82d818584983581c8c861a9c85f4693f98a2ef812d4895bcc08c10d67bcecb835ba30ae1a201581e581cf76df016205d86858c067304a7e01a2e1bfdea99b7490fc3bb1d192a02451a4170cb17001a9bfed303	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	159
160	f	37btjrVyb4KDsFS7rbQjZQGX6Fz53u76NF2hF9iqRhfbx8ePmjJKCsv5rZV1hhtP6DHdKwLAf5zkVH9FE51xYuCvJzNppSn1tgExVQJpuTwKmyTekT	f	\\x82d818584983581c8e6f137fef429d876d70484bca97f82b4acfafec119b088c06324a8aa201581e581ce12593273e5bc238283be8516b6ead778949cba71653aca16c24656d02451a4170cb17001a5758289c	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	160
161	f	37btjrVyb4KDsi9fc3RfExWLumjkp2YrcMjZpew19Z92kZnjPy5Xa84KY2WZw6xmjJA7AXFJCBWtrF9RFw1BjCewEqK77CVYj2s7bk9aAA7yyZARRz	f	\\x82d818584983581c8ecbfc41d24c4b6f5c9a029c18d93d61b0a6ac63dc34d41702b8867ba201581e581c13cc583d5cc026c91e0eb079c31a2f3ea6b230830728334efcd4955002451a4170cb17001a4d758d71	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	161
162	f	37btjrVyb4KDvKgSbCTx1gwwZFGe5DZaXyGwTYGGnJNCQ9C61XP4n1pKFQtNbYEowGeRoKHCGUvuU8Ebz2vQwN7YhcJ9bSb5oNpAoCe8UxX5KK5C3e	f	\\x82d818584983581c90daf373d89f0d14b172f82d9a05ffbcca7d7b99a3720969814e7d11a201581e581ce62a9c86970abe9b9d714303e45eddd6e471e94593b48db312bdd42502451a4170cb17001a3e95e725	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	162
163	f	37btjrVyb4KDxRyqtP9nWyEd7sfzdB8Xgh2egCATJAVtvxM2LhKLp1ALCE714vMCsbQZ5SwvVgiAvmieJTkae865ycwU39JN4pgt27pqEuB8uvi947	f	\\x82d818584983581c928443871ee0d45d1471e5677e8c020fa8b2c3e2c95d7a1f8f328e56a201581e581c86874fa1f2374d7a58fa309a8c6c916511082b5726e204f47f608c0302451a4170cb17001ae9de25cc	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	163
164	f	37btjrVyb4KDz8QGqk9LJ9kSsSd1zKgqfTuiTKbL92b4aDSXmRPyrFZp3VPEwhMyEmwCiSkpd7KQztirmU6CGwiphhiHoXbbZjkbfiHN5Mq1y7fmJM	f	\\x82d818584983581c93da8900b1525561249d94b2ac6ff5fd526265771cb7b0d55562ae05a201581e581c1bee0e5d6c069965be9930e28ce93077d4acb60cc791af6efe535e5902451a4170cb17001a8869e96e	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	164
165	f	37btjrVyb4KEC4vC63KNqRBD7RX1KBwWDdPDE6oXGxP8x5aKrbVTALaq4XBdak8F47Kt9VcsQvVsKZfAit8vBtZpG2mc6VKUXCFv8pTYWwQMnABckB	f	\\x82d818584983581c9d42d73b79bcaa9f539c2a83ea1718f32551873de487f1d7ddbd9039a201581e581cd5575d6539a4ac88132a5c756c8681663d3faa47f498f7b87a3e0cc902451a4170cb17001ade93b074	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	165
166	f	37btjrVyb4KEDBSAmNtUBy6pfXesvTvtrDZQsSYcyo7SUwjLkhoSaPDCsNqMmoGbqzFQyEe9DNwK59BMudtdkzFPBpbgiEWx5SZr6vVMbpe86qsQJV	f	\\x82d818584983581c9e2334a9865ba25993cc8dede2b04be49897a1e7becf70e5329084eaa201581e581c5eaf4b263070efa8b91d4ae5a0c5dc5bcefa696d0700f3ea92751a0a02451a4170cb17001ac26ce062	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	166
167	f	37btjrVyb4KEW3PG2LAJNwmok2H6i149HetvT6fYrPsqPGpUkucNkA4b5TQmv896EF44UmcCAXDycfxFB7GcVefBCgk9cZffVUdX3kri3i5TEqSjKm	f	\\x82d818584983581cab6c244bac336fa6e174095a276e219256ec42ce7e417eea16c859f0a201581e581cabbd4c5c87526d0540ab0da342a51cafadf618d1c924f09421fde5b102451a4170cb17001a41916bb0	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	167
168	f	37btjrVyb4KEXaPVoMuKpnVEKKLMFuGSvYL7YMAD529yxeK8Y1zqnbh5FQ8GMYpJwARugWmzaXdJ1gopgsxziC4e5wgjf3zkp7RH41KTJ73xLyfrFP	f	\\x82d818584983581caca1a9fe0feeaa7e4ce8a6272df1bbc75a9a65eec18346b11d9f816ba201581e581c8f0fe96db0db8ea1e89e0985d76a5cd090fd933aa33cf99868c66f5a02451a4170cb17001adc6e4436	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	168
169	f	37btjrVyb4KEZWqFxGYhFuLE23i92B8BiLRwwFUdnjmM24KHCNuWik5dc6MJqz7GxupgKGK5zzbYSXJyA8DQVDszyFmJuoxgzPn2GSnfBoREZ82ZdF	f	\\x82d818584983581cae28b3ae5a7977c01c3019d9448cd10ed38ff7fdc5416ef25ec7124ca201581e581c6cd4c56c15fe35f2c45884d42d914b9f40822ff901fe50a082fc768b02451a4170cb17001a6259c58e	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	169
170	f	37btjrVyb4KEbo96SLroMfZB11rEvztPswgdqC5ESkHXc4EaFdAhsDQ24vJK23XsjTJzpPS3ZcDWiHiFVtmp5wkJrcnRcMe74s88eTy8YLvLL7EBRj	f	\\x82d818584983581caff4d11fe3576c886ed622a0b7ee1f8a006171b3481f18419bb56860a201581e581ca078c7c3264ceb44e7a4c1707b24a319d606efca14599bb1d035b77d02451a4170cb17001a85ec5e8a	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	170
171	f	37btjrVyb4KEcCa8yc1d8Zrne1hRtedHQQGPbkRJcvHak2ygcCndPfSqWwb2L59ERxhtqsMJLdS1fPQPs2vcJcQCvB52tCm4rGCDwUmRG51PUzjSVh	f	\\x82d818584983581cb0464dbfc408183e81605d633bb641fbaea84e9cf991830f1680c3e3a201581e581c98e15fccd8c6862f22530bcc0313f75a4d2a09f97555f1ee8d3090ac02451a4170cb17001a7de8ad84	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	171
172	f	37btjrVyb4KEdwV1MS3Pjek1HjLN2CSq3SJZGFBbZctkGLz569i9RWN15bAvCcZ8R5dgEi8iYjpmMVKioufoGv3issZQvVtzPz38pWHBViyRK2how5	f	\\x82d818584983581cb1a53c47229d05cafca3e88afd5e17c1421a6fcad63f2a14a189e768a201581e581c3511e0843c4925ce7b461c414b3b32087125b27e3474e09e4ef83c3602451a4170cb17001a0167cc48	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	172
173	f	37btjrVyb4KEg6anTcJ9E4EAvYtNV9xXL6LNpA15YLhgvm9zJ1D2jwme574HikZ36rKdTwaUmpEicCoL1bDw4CtH5PNcFnTRGQNaFd5ai6Wvo6CZsi	f	\\x82d818584983581cb35848bdf518e7354b992d5f7e744eff51cef8c1badea258626e4ddca201581e581cb134ac3a644ecaeb1bc572d874c959e5ac4e636359101ef8d8c1947402451a4170cb17001a24dce285	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	173
174	f	37btjrVyb4KEgvmzjLT7R9xHg1vNob6vCf999UFuG4qfTpHGZufdhbkUogSFJXtQXnCcJDHJ6xuZt92H6VgxGdhSLQ9gmWZy4zCJEVu8Nj8NashVQY	f	\\x82d818584983581cb3ffe1ce218383c5cc6f025853fbc9915995e333fcf46463eee66a72a201581e581c269562d0187a688938da2e5778ca1db807779ca1b96eb931176b927d02451a4170cb17001a6e03b705	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	174
175	f	37btjrVyb4KEhnv3cCqP8jzRBbwE5v6ymPkBjTexHCcCgYJarjHHaxipJvz4aaXc5Xmp5KXxnC3SoE1oqR8sdGHobyfsAqJy7DwejZWpkkoYD7LJsS	f	\\x82d818584983581cb4ae3edfd04760a8f218ebb8fc596827731c7fbeed852606e2892cd1a201581e581ce7bc3ff57c8032f379a7bd22ce86558fdbad5cbceca23139abdea94502451a4170cb17001af959ff69	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	175
176	f	37btjrVyb4KEhvykuoQZKWdNKFgTrugRUSV66yr8GCXREuaX2PfVQJeuXS4h1bNP8SeUxHAG2J6RfK35YnsZj5qkWQCWV7tVYMVokU5bw9y8CcmqPy	f	\\x82d818584983581cb4ca4a03f19036b1cffa9ca0adaf8c850e5ee74ee50cbe35e2a8e7b9a201581e581cc0c67c14fa550172fbe53fd207a972b8b169021c44b41e4056ff84f602451a4170cb17001a5cb805a4	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	176
177	f	37btjrVyb4KEkSeCVx985rXc38DCud2AW4LdasNmyoPLbtDGcDCyYVdf8BzxvDnzPehv4kyVBkzThjVEkSpGTv8PGQs4yRUgiCaKa7PTtBY4ohNGqR	f	\\x82d818584983581cb6c4d94085ffb9bb36e763bfa6de273facc294d6bc21d8ef217a31efa201581e581cb346299a4517a821f5365ff77d678b7d03a7c5c2de97eed92a74fe0a02451a4170cb17001a907fcadc	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	177
178	f	37btjrVyb4KEwqVLa1yaRPdDUctf525DovPsxoXZfqNp26eXQnmTSFwY3Q3rgPsjRfTKHKtjFpxPy6XYvjzscccsZYfXSaL9MmgrKAaeQgQhCtoXih	f	\\x82d818584983581cbfbf05de3bda1554473e61ef256b74a2e352dec69942ed42c285740aa201581e581c626c835c299c704956f32de8de796a30501083501ef442915d9cf00502451a4170cb17001a1dba836a	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	178
179	f	37btjrVyb4KF3MxxJBeJqCPFgHyUsSDkrDqoctSSVi9h7F4Wj9zFKcPVuVju76KYhdp7nJhy44512Wjhw7WH4sed3MMSh1HYnKUfjZXGkoZXMajaye	f	\\x82d818584983581cc419843c120fe98e6a48cb7f416b1df8b7f2da92e167e79ee87c2065a201581e581c7b70ae6e616594136ac9b159ab0d34992a8bd9f228622a37df74155602451a4170cb17001a42e90c19	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	179
180	f	37btjrVyb4KF3ZQhHTvVH3L7jNYoZ3XWK6SWpqZKiu9AGz6qNtxoxhAmmJpenFMA6fedYDT3Lt7cihgc1q4pE2GJXvPuknAkjvESmPxhhzkBzuiRis	f	\\x82d818584983581cc441505227f487b532bc93fa158d3f7de1bac5a9f3f83ce78e639f6ea201581e581cbb611fd27988c75270fe8faaec7c3e9a81e9f997ca3186422f80b51f02451a4170cb17001acf685024	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	180
181	f	37btjrVyb4KF5R1LEsaQgjWFWXwbgJ51naDEaCRG23KiAN3UtGzaT5PvUANtFBgjmcCtPLMBYMTGL4S8px4HyQMLAyF4fakYoFAJC3PkxCWMUatGWD	f	\\x82d818584983581cc5b77ee6159180e0150a680fb50ffdbb46cd7e014cec299cc7525784a201581e581cf1d40d3c3c86b1d8d29cf0355d00ea4bc5276c2d644c6d3bc4a42a0902451a4170cb17001a097bc322	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	181
182	f	37btjrVyb4KF9xNLdS47pRBLB8e8bQLuvrBiGJbnHPNCbKU4wrMerJztEtYJdHayvaoUEmJ1vc3aiq9Z3UgP83Y1b4rpiyrGeYjzQhhDgB6DW8sWJm	f	\\x82d818584983581cc94b5e69b2bdb75d007d9c95b9c5248b83628fdc95560e274badc269a201581e581c6c206dc307c1c30e238b7f4a63c70a05d1cf23a473a33eb449a96d2902451a4170cb17001abf123afa	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	182
183	f	37btjrVyb4KFB5Tmw1wsLmuv17Q6y8i6HGpVxbW8k4bevmob3DcdbH6jzrAtUrBpKgfTGgPMpLAbJcpaByGGJErkXQWFwrNMW35S79hxFvAN2GTXVQ	f	\\x82d818584983581cca2dbd6c09b53f7ab70bc252b5292115eb1508029c945f9855d834d6a201581e581c804ae072e66a03672bdc3ba73fa334d76eb56ac4585c50fd7ed5d03202451a4170cb17001a67547107	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	183
184	f	37btjrVyb4KFGS7upvgJHtmp7y7EFB67utzaHf7PM8y8U4tNkpmARNwiD7seN4NSAceHmj64KLGgh9qn1BpYF49NyWxocBHn1N533qBUYfhQar9ceu	f	\\x82d818584983581cce6616909f2cdc064698d589537d61489595f244d64156ddaff2ff03a201581e581ca98349d3037755b50d876b38c19b4d5574a1b378cadb065b970ba60f02451a4170cb17001a1f130b32	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	184
185	f	37btjrVyb4KFGV8HUDv7S5E8CSBV3pQLgGFt5HXa2jb9ofbAo3gTxcaQ4So84mHsNk9mhAybq6miH2VZU3iz7cqCd74gPMyn3zdUsrF1u2rib7HSXq	f	\\x82d818584983581cce708b1a77ce2f11eae3c4df0ee2aeb43de6b1b7ede2f8b8b7f4f365a201581e581cb482475e8f5f900926f7fce692c71f7f7916dc83d6a153ddcc2ca7d602451a4170cb17001a23918b60	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	185
186	f	37btjrVyb4KFNpxUYvzqFKsRfRYJKHjEuFfgy6rpoGq29dcMrNhbKTvqNr719U282rp9PcnFonAENkUdv2nE36wZmyNkj8JVQJL1TLu25SKjGFNzVs	f	\\x82d818584983581cd36fb7fce0dcd049bc6fa9aec085939b2e0df6ebdd062973338eef3da201581e581cd5adb7603e4d1f3c7efdf77b91b5f99b21302f0ab3ccb6e0e7ec93f802451a4170cb17001abb1bac96	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	186
187	f	37btjrVyb4KFPbetkmdvqD8nLRFsUVL4HsVUYmgaZhAmBXcr78M3XoZkptjuszd2T1FNr1fGZApkZFZXikGtyhCc7jH5JYD1q8csTNSWQn4Us3nzX9	f	\\x82d818584983581cd40b28435f5e679d5cfbe96e354c1b2d5392c510e0dd11b088cd815ea201581e581c83c4612477ada33e170a4a15ef261e2507519211cb9af661d1a433ee02451a4170cb17001a68661780	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	187
188	f	37btjrVyb4KFTKCoqtZBdbh7LtJ9mRR1nbkX7ggP6a7AwvkSDxUN6U5GJWfuRXnL3a5x5e16uQwyjC6PoPVQ7VLdJXr8Kd3eFknLu6NDf2ey4AaJo2	f	\\x82d818584983581cd6f8c23f5df3e300fe32a787a174ba0425b51eb132a947d866a0ee1ea201581e581caf1f86a53547806aaf393dcb5ba320da17e5f5277e1a4f9c42e0321702451a4170cb17001aaf28a6e9	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	188
189	f	37btjrVyb4KFV3TiBqtLDN3oHQr5NrA8ouAAqasFU4ZuB9W13xgcgWsSy5fUtbNL4imCruQz19hjzBzykxGxCAarrviCUBh3sxWbvvTTHdvpyWGgXc	f	\\x82d818584983581cd8556a043d08e2739a4a2bbde504536d3e7219acf78017abeb2ba0cea201581e581c2cfdaa74463d626ba21287d7f420dd88a624e36d3404b34ea49711a502451a4170cb17001a77b9c3d3	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	189
190	f	37btjrVyb4KFYnqsKFfMwj4S3PcCxwxutoiUubHazLfw4wJc5bfrQgpNEpRnGCS2UUfzvMWRRV2vy4wzPKE7tshLS2YEW219R6QfAenXzktMoXCmuU	f	\\x82d818584983581cdb4962c3134b77b46969347ae2c3cf4b461b999c6f75b1daf4324d01a201581e581c407a75e8087db36c65e175e2956b6ed8a3bbe461f2a774dd14dc140b02451a4170cb17001a319751eb	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	190
191	f	37btjrVyb4KFf5NQ1DuNoAP4phRomqdEUmtFb6sWcDHkizGj56dwn54LfKrfWa6Er5sxDXYrzpWwS56PKmKaBjJtn1JqN67K3CihFXXospn8B2TDz2	f	\\x82d818584983581ce03d11bbf01a292874d6978e8e6710460b19c2c5df30de02e1cb9262a201581e581c98928050a3eb91b9f20ac57ca15c2d8da475d6ec678dd850d6e2242402451a4170cb17001a1663873b	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	191
192	f	37btjrVyb4KFfXPG5GDEc4tLyVUSepKD9GGXZcSuP1xtLLRQnQr4wSXawT62bSJsiPzS25kdADKh94V3iDksm9nq5fhV4jixCnpNjsn7k2hSkwrAUa	f	\\x82d818584983581ce0978a02d153a5d3cf2a6d2ff6bd7b09e3cdda2a082e6290ad7e2581a201581e581c00e6cda4ee6095de21024a5da504eb857a0d0bc1984994b6c6e6f78702451a4170cb17001af84160ab	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	192
193	f	37btjrVyb4KFfgfRyETGNXNXm6gAxNpTQSxr6bM7T6yZE1ibiZBovxG52PioVmLRnPYxs4wYddAfgTH4mbmtLFnwuZYSBh2eNsdLdc6vWb4AdJNNym	f	\\x82d818584983581ce0b7cea7639a8b4f4246ba9284e76f33d5ead44bef6d81301436e592a201581e581c36d80f3aa9d1ad92c6a5d002e0ca0851d7b27145e8c758063effebca02451a4170cb17001a66c66668	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	193
194	f	37btjrVyb4KFfnmiGpxNSGQVMfmFpAFrEAEhZwGPQDutSHnZqQXPhcXxDNcbdoKiztyQHpTA3jSmVowZCxcaJMS1k1wu8U6nXTzejgh7wYZjycamU6	f	\\x82d818584983581ce0cd0cafd3937615a01ffdaf7a66b0a2aa1fb591d5d36700ed43fd6ea201581e581cd0cb8577c9b344009dbe36bb9d329b3a9927f7f566723c25e7550ce802451a4170cb17001ab456452b	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	194
195	f	37btjrVyb4KFh7jhHCtWxW942ceq7Xhxay8FZ7GkEBezGyFm3wJcVBGy1YYJDZ4Z7GbrFZmHLSe47zFs8Rjxk8rveoRpo1s43HXrMrhd4ijim4jJVP	f	\\x82d818584983581ce1d8b4887710ffbd5b9d3d1f0c7a12a4f0ad89c81e07f60ad2c04da1a201581e581c3a61beb9528ff83869aea8180b7a97774a246989dac50fa7c7241afd02451a4170cb17001aca979f52	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	195
196	f	37btjrVyb4KFhYgC9Lr4Se7C1gL39d5WBVADyUyQZz2BfG4BZxczyW827JRQR5enyWaoj6NnA5NyKsheV6Eb7WvQtbN8D6116HTknHhEb5jh1yUU6Z	f	\\x82d818584983581ce22f6f87a5e929d9463d0070682c85ffad83ee029259aeae30a40d74a201581e581c35387d383a63abf271ba1f7897b77cf5f0f4f8deeb296b41dec5e78502451a4170cb17001aeabbc0c6	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	196
197	f	2cWKMJemoBam1WPtZrz3Fi4EMUDao74k9Xn4fhyVYLqK4o1VRzoxPFU91QBRToLbVyrjX	f	\\x82d818582883581ce88a20e63d549e6d22178712831ff491424f10d58c5a19d8d2ded587a102451a4170cb17001ae6a25706	\N	\N	0	\N	\N	\N	5428571428571429	\N	\N	\N	197
198	f	37btjrVyb4KFtDHT2vDtMvQbLBgfH5hnpyVTTqqpPsieykukuxrDShHNccAEEj7M87UuV2GJ5pPA7YJ4JPjSokA99XaDgLmeaAumhZPHMwzg2Laspr	f	\\x82d818584983581cea96b7b537888bfd4e7714a1a9e10cebd634196517a8b76fdafaf539a201581e581c7f1dc0cefc4c237f63a41d2c0694a523f18dfc34475a9aa7cefcad6e02451a4170cb17001ab398e117	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	198
199	f	37btjrVyb4KFtfVixJcJdD27YZfXfRM5diZFj4TEazkCAhF8KyztTbe67zQpBc1RTjruHNvefdJ9Jtr7u5rebR71tGrGtKioSSGKy2hMKd6GUVFkEg	f	\\x82d818584983581ceaf1df242150f12b7862297f41053a6269be40b1984fc9e8121dc458a201581e581cc6fb8edc60089718d0999d543e49fcf01715cdfc44e0bb53d45393e002451a4170cb17001a08ba6635	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	199
200	f	37btjrVyb4KG5ZZfwwiQuhAGWiNJ2FhXP3oAuiq75qknCz4CZWNMVY4B9BmiHRHnWfhUbkLHUqfabCYASUk2V1qGuDw97x1gdf871aFY7Lpz3N1NvT	f	\\x82d818584983581cf387f6884a0d9149affe7303b8a673f5bd934d97928f6f99b2645649a201581e581c019c95c66013e4bcce7af22d45790031fd53a906735869e0d908ff9e02451a4170cb17001a1ae77a60	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	200
201	f	37btjrVyb4KG5vwKTPpCSQ14a7Cv4TESosSVoFVRHsw1vHj7Tpb7N4j1U5dtFcY7L3MWsH9BJqQjU9NxwaHpbHCJAhmsLi2mC2BE8k8Rj7zcjiiVhR	f	\\x82d818584983581cf3d24a790c15c50d4a3993a1536fdce3f3b86d2e19ccdc9473e87526a201581e581cf41f1d35e5f5e9be3b9f046e1203e3c5fbdf93bc23b48dd6683dd5e302451a4170cb17001a3dd4d090	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	201
202	f	37btjrVyb4KGAExHTQjLUHJBksSXGTomjgNsw8a4KepCgQYk4gxacKb84vGpPSv9Pjt3gdgMjA1nB67Pq3XyJpTDk8kLcXpJawCe6SCJf5jUowvAz8	f	\\x82d818584983581cf737c0da1cae25b5c03c8c62b81b43c0008d828e67b94c9050975e5ea201581e581c1c0c777bcb83e298791679fc6268849b8cc0e50e1fcc3cdffbe9dadd02451a4170cb17001a28091f5d	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	202
203	f	37btjrVyb4KGAZJWCpicgRkb9ijP3Jnv6y9EYvnpMqkPdBZ2d6fdnCa9C97HUmfHLWF846AKjViPvnY7MbSM8mTM3VDx5RazBFxA7C7mZ9CyM48AW9	f	\\x82d818584983581cf77790146f4a435f65a290568c4e61063b5813e6b71ba4e3b22ec2aaa201581e581c33095a9d31ee647bab472a40cc31a2bf5251e55cb690031533829f8502451a4170cb17001a141dd526	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	203
204	f	37btjrVyb4KGDMix4Uj5opvbMDgjZYUjeARAqTEFEbgLUH3qyju9gkBpcm2fVWgkcNgK3xFsQgWm1w8zxqvm9P6xJj9mHqLeMJPwDMUKUGPcDyUaDS	f	\\x82d818584983581cf9ac6716a146a073154e91e598df924a3dcd9e108701d199b83b994fa201581e581ce9a39f1a8798d60679c924b506e2479657e36e62fa4111407ce547bf02451a4170cb17001aab9da46d	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	204
205	f	37btjrVyb4KGGSGD8KgQD6qUBaSjxy5JRtsmMSHEGGAZqA29ULGwci8TcM16vBhywuBw54izQtpAqXeyUnbjh56hCgoqGZp9tHTMLLkEgLzwxVCZ4N	f	\\x82d818584983581cfc194b02fbeab78c4ee49fd954467cb5736eb8e572a3795b507aae94a201581e581c181935e6bbd58018b8d6834becde971a5e919d934b44eda6032153a402451a4170cb17001a1928851b	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	205
206	f	37btjrVyb4KGLRpX3uQfSeLovpMTcWfVZSM5RCufYvy2tyCMwrLXyHKCM9VqQh8dCQA6WcTrViaxqpvBSeKreHFL4CftfJU1z7CjHAze236NLesbL8	f	\\x82d818584983581cff3e8f5cf5c22421e6b203bbdc434e5967f7b4988a0a5212bbb7fe24a201581e581c97f8c1e1964c1a097392009cafc848d4b2d8c9d0d0ba2d27d787fc0402451a4170cb17001aa844fcdd	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	206
207	f	37btjrVyb4KGM5rFFreGtZAs4PFB2Drb37uXRHebh8rCeVWFkW8De8XAbYqvfQrAqVthfJp9Qy2YzbzNhWSiUGY3D7yJkRkChyMveKCWT8qUTNEu6e	f	\\x82d818584983581cffc2d026b80fc689a77eec4ff610090c9459ab65d94c318f3ab80b0ca201581e581c303563e9bd68846756d6b72f775b25b54ba895afb336fc4e53240a3d02451a4170cb17001ad1749cef	\N	\N	0	\N	\N	\N	19999999999999	\N	\N	\N	207
\.


--
-- Data for Name: withdrawal; Type: TABLE DATA; Schema: dev; Owner: cardano-master
--

COPY dev.withdrawal (id, is_deleted, amount, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Name: ada_pots_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.ada_pots_id_seq', 1, false);


--
-- Name: block_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.block_id_seq', 2, true);


--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.collateral_tx_in_id_seq', 1, false);


--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.collateral_tx_out_id_seq', 1, false);


--
-- Name: cost_model_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.cost_model_id_seq', 1, false);


--
-- Name: datum_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.datum_id_seq', 1, false);


--
-- Name: delegation_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.delegation_id_seq', 1, false);


--
-- Name: delisted_pool_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.delisted_pool_id_seq', 1, false);


--
-- Name: epoch_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.epoch_id_seq', 1, false);


--
-- Name: epoch_param_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.epoch_param_id_seq', 1, false);


--
-- Name: epoch_stake_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.epoch_stake_id_seq', 1, false);


--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.epoch_sync_time_id_seq', 1, false);


--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.extra_key_witness_id_seq', 1, false);


--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.ma_tx_mint_id_seq', 1, false);


--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.ma_tx_out_id_seq', 1, false);


--
-- Name: meta_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.meta_id_seq', 1, false);


--
-- Name: multi_asset_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.multi_asset_id_seq', 1, false);


--
-- Name: param_proposal_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.param_proposal_id_seq', 1, false);


--
-- Name: pool_hash_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_hash_id_seq', 1, false);


--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_metadata_ref_id_seq', 1, false);


--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_offline_data_id_seq', 1, false);


--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_offline_fetch_error_id_seq', 1, false);


--
-- Name: pool_owner_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_owner_id_seq', 1, false);


--
-- Name: pool_relay_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_relay_id_seq', 1, false);


--
-- Name: pool_retire_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_retire_id_seq', 1, false);


--
-- Name: pool_stake_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_stake_id_seq', 1, false);


--
-- Name: pool_update_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pool_update_id_seq', 1, false);


--
-- Name: pot_transfer_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.pot_transfer_id_seq', 1, false);


--
-- Name: redeemer_data_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.redeemer_data_id_seq', 1, false);


--
-- Name: redeemer_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.redeemer_id_seq', 1, false);


--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.reference_tx_in_id_seq', 1, false);


--
-- Name: reserve_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.reserve_id_seq', 1, false);


--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.reserved_pool_ticker_id_seq', 1, false);


--
-- Name: reward_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.reward_id_seq', 1, false);


--
-- Name: schema_version_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.schema_version_id_seq', 1, false);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.script_id_seq', 1, false);


--
-- Name: slot_leader_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.slot_leader_id_seq', 2, true);


--
-- Name: stake_address_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.stake_address_id_seq', 1, false);


--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.stake_deregistration_id_seq', 1, false);


--
-- Name: stake_registration_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.stake_registration_id_seq', 1, false);


--
-- Name: treasury_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.treasury_id_seq', 1, false);


--
-- Name: tx_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.tx_id_seq', 207, true);


--
-- Name: tx_in_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.tx_in_id_seq', 1, false);


--
-- Name: tx_metadata_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.tx_metadata_id_seq', 1, false);


--
-- Name: tx_out_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.tx_out_id_seq', 207, true);


--
-- Name: withdrawal_id_seq; Type: SEQUENCE SET; Schema: dev; Owner: cardano-master
--

SELECT pg_catalog.setval('dev.withdrawal_id_seq', 1, false);


--
-- Name: ada_pots ada_pots_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ada_pots
    ADD CONSTRAINT ada_pots_pkey PRIMARY KEY (id);


--
-- Name: block block_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.block
    ADD CONSTRAINT block_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_in collateral_tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_out collateral_tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_pkey PRIMARY KEY (id);


--
-- Name: cost_model cost_model_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.cost_model
    ADD CONSTRAINT cost_model_pkey PRIMARY KEY (id);


--
-- Name: datum datum_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.datum
    ADD CONSTRAINT datum_pkey PRIMARY KEY (id);


--
-- Name: delegation delegation_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);


--
-- Name: delisted_pool delisted_pool_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delisted_pool
    ADD CONSTRAINT delisted_pool_pkey PRIMARY KEY (id);


--
-- Name: epoch_param epoch_param_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_param
    ADD CONSTRAINT epoch_param_pkey PRIMARY KEY (id);


--
-- Name: epoch epoch_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch
    ADD CONSTRAINT epoch_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_sync_time epoch_sync_time_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_sync_time
    ADD CONSTRAINT epoch_sync_time_pkey PRIMARY KEY (id);


--
-- Name: extra_key_witness extra_key_witness_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.extra_key_witness
    ADD CONSTRAINT extra_key_witness_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_mint ma_tx_mint_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_out ma_tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_out
    ADD CONSTRAINT ma_tx_out_pkey PRIMARY KEY (id);


--
-- Name: meta meta_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.meta
    ADD CONSTRAINT meta_pkey PRIMARY KEY (id);


--
-- Name: multi_asset multi_asset_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.multi_asset
    ADD CONSTRAINT multi_asset_pkey PRIMARY KEY (id);


--
-- Name: param_proposal param_proposal_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.param_proposal
    ADD CONSTRAINT param_proposal_pkey PRIMARY KEY (id);


--
-- Name: pool_hash pool_hash_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_hash
    ADD CONSTRAINT pool_hash_pkey PRIMARY KEY (id);


--
-- Name: pool_metadata_ref pool_metadata_ref_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_data pool_offline_data_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pkey PRIMARY KEY (id);


--
-- Name: pool_owner pool_owner_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_owner
    ADD CONSTRAINT pool_owner_pkey PRIMARY KEY (id);


--
-- Name: pool_relay pool_relay_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_relay
    ADD CONSTRAINT pool_relay_pkey PRIMARY KEY (id);


--
-- Name: pool_retire pool_retire_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_retire
    ADD CONSTRAINT pool_retire_pkey PRIMARY KEY (id);


--
-- Name: pool_stake pool_stake_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_stake
    ADD CONSTRAINT pool_stake_pkey PRIMARY KEY (id);


--
-- Name: pool_update pool_update_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT pool_update_pkey PRIMARY KEY (id);


--
-- Name: pot_transfer pot_transfer_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pot_transfer
    ADD CONSTRAINT pot_transfer_pkey PRIMARY KEY (id);


--
-- Name: redeemer_data redeemer_data_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer_data
    ADD CONSTRAINT redeemer_data_pkey PRIMARY KEY (id);


--
-- Name: redeemer redeemer_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer
    ADD CONSTRAINT redeemer_pkey PRIMARY KEY (id);


--
-- Name: reference_tx_in reference_tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reference_tx_in
    ADD CONSTRAINT reference_tx_in_pkey PRIMARY KEY (id);


--
-- Name: reserve reserve_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserve
    ADD CONSTRAINT reserve_pkey PRIMARY KEY (id);


--
-- Name: reserved_pool_ticker reserved_pool_ticker_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserved_pool_ticker
    ADD CONSTRAINT reserved_pool_ticker_pkey PRIMARY KEY (id);


--
-- Name: reward reward_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.schema_version
    ADD CONSTRAINT schema_version_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: slot_leader slot_leader_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.slot_leader
    ADD CONSTRAINT slot_leader_pkey PRIMARY KEY (id);


--
-- Name: stake_address stake_address_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_address
    ADD CONSTRAINT stake_address_pkey PRIMARY KEY (id);


--
-- Name: stake_deregistration stake_deregistration_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration
    ADD CONSTRAINT stake_deregistration_pkey PRIMARY KEY (id);


--
-- Name: stake_registration stake_registration_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_registration
    ADD CONSTRAINT stake_registration_pkey PRIMARY KEY (id);


--
-- Name: treasury treasury_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.treasury
    ADD CONSTRAINT treasury_pkey PRIMARY KEY (id);


--
-- Name: tx_in tx_in_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_in
    ADD CONSTRAINT tx_in_pkey PRIMARY KEY (id);


--
-- Name: tx_metadata tx_metadata_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_metadata
    ADD CONSTRAINT tx_metadata_pkey PRIMARY KEY (id);


--
-- Name: tx_out tx_out_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT tx_out_pkey PRIMARY KEY (id);


--
-- Name: tx tx_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx
    ADD CONSTRAINT tx_pkey PRIMARY KEY (id);


--
-- Name: ada_pots uk_143qflkqxvmvp4cukodhskt43; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ada_pots
    ADD CONSTRAINT uk_143qflkqxvmvp4cukodhskt43 UNIQUE (block_id);


--
-- Name: pool_stake uni_pool_id; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_stake
    ADD CONSTRAINT uni_pool_id UNIQUE (pool_id);


--
-- Name: block unique_block; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.block
    ADD CONSTRAINT unique_block UNIQUE (hash);


--
-- Name: collateral_tx_in unique_col_txin; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_in
    ADD CONSTRAINT unique_col_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: collateral_tx_out unique_col_txout; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT unique_col_txout UNIQUE (tx_id, index);


--
-- Name: cost_model unique_cost_model; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.cost_model
    ADD CONSTRAINT unique_cost_model UNIQUE (hash);


--
-- Name: datum unique_datum; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.datum
    ADD CONSTRAINT unique_datum UNIQUE (hash);


--
-- Name: delegation unique_delegation; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT unique_delegation UNIQUE (tx_id, cert_index);


--
-- Name: delisted_pool unique_delisted_pool; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delisted_pool
    ADD CONSTRAINT unique_delisted_pool UNIQUE (hash_raw);


--
-- Name: epoch unique_epoch; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch
    ADD CONSTRAINT unique_epoch UNIQUE (no);


--
-- Name: epoch_param unique_epoch_param; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_param
    ADD CONSTRAINT unique_epoch_param UNIQUE (epoch_no, block_id);


--
-- Name: epoch_sync_time unique_epoch_sync_time; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_sync_time
    ADD CONSTRAINT unique_epoch_sync_time UNIQUE (no);


--
-- Name: ma_tx_mint unique_ma_tx_mint; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_mint
    ADD CONSTRAINT unique_ma_tx_mint UNIQUE (ident, tx_id);


--
-- Name: ma_tx_out unique_ma_tx_out; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_out
    ADD CONSTRAINT unique_ma_tx_out UNIQUE (ident, tx_out_id);


--
-- Name: meta unique_meta; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.meta
    ADD CONSTRAINT unique_meta UNIQUE (start_time);


--
-- Name: multi_asset unique_multi_asset; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.multi_asset
    ADD CONSTRAINT unique_multi_asset UNIQUE (policy, name);


--
-- Name: param_proposal unique_param_proposal; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.param_proposal
    ADD CONSTRAINT unique_param_proposal UNIQUE (key, registered_tx_id);


--
-- Name: pool_hash unique_pool_hash; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_hash
    ADD CONSTRAINT unique_pool_hash UNIQUE (hash_raw);


--
-- Name: pool_metadata_ref unique_pool_metadata_ref; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_metadata_ref
    ADD CONSTRAINT unique_pool_metadata_ref UNIQUE (pool_id, url, hash);


--
-- Name: pool_offline_data unique_pool_offline_data; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_data
    ADD CONSTRAINT unique_pool_offline_data UNIQUE (pool_id, hash);


--
-- Name: pool_offline_fetch_error unique_pool_offline_fetch_error; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_fetch_error
    ADD CONSTRAINT unique_pool_offline_fetch_error UNIQUE (pool_id, fetch_time, retry_count);


--
-- Name: pool_owner unique_pool_owner; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_owner
    ADD CONSTRAINT unique_pool_owner UNIQUE (addr_id, pool_update_id);


--
-- Name: pool_relay unique_pool_relay; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_relay
    ADD CONSTRAINT unique_pool_relay UNIQUE (update_id, ipv4, ipv6, dns_name);


--
-- Name: pool_retire unique_pool_retiring; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_retire
    ADD CONSTRAINT unique_pool_retiring UNIQUE (announced_tx_id, cert_index);


--
-- Name: pool_update unique_pool_update; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT unique_pool_update UNIQUE (registered_tx_id, cert_index);


--
-- Name: pot_transfer unique_pot_transfer; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pot_transfer
    ADD CONSTRAINT unique_pot_transfer UNIQUE (tx_id, cert_index);


--
-- Name: redeemer unique_redeemer; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer
    ADD CONSTRAINT unique_redeemer UNIQUE (tx_id, purpose, index);


--
-- Name: redeemer_data unique_redeemer_data; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer_data
    ADD CONSTRAINT unique_redeemer_data UNIQUE (hash);


--
-- Name: reference_tx_in unique_ref_txin; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reference_tx_in
    ADD CONSTRAINT unique_ref_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: reserved_pool_ticker unique_reserved_pool_ticker; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserved_pool_ticker
    ADD CONSTRAINT unique_reserved_pool_ticker UNIQUE (name);


--
-- Name: reserve unique_reserves; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserve
    ADD CONSTRAINT unique_reserves UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: reward unique_reward; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);


--
-- Name: script unique_script; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.script
    ADD CONSTRAINT unique_script UNIQUE (hash);


--
-- Name: slot_leader unique_slot_leader; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.slot_leader
    ADD CONSTRAINT unique_slot_leader UNIQUE (hash);


--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);


--
-- Name: stake_address unique_stake_address; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_address
    ADD CONSTRAINT unique_stake_address UNIQUE (hash_raw);


--
-- Name: stake_deregistration unique_stake_deregistration; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration
    ADD CONSTRAINT unique_stake_deregistration UNIQUE (tx_id, cert_index);


--
-- Name: stake_registration unique_stake_registration; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_registration
    ADD CONSTRAINT unique_stake_registration UNIQUE (tx_id, cert_index);


--
-- Name: treasury unique_treasury; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.treasury
    ADD CONSTRAINT unique_treasury UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: tx unique_tx; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx
    ADD CONSTRAINT unique_tx UNIQUE (hash);


--
-- Name: tx_metadata unique_tx_metadata; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_metadata
    ADD CONSTRAINT unique_tx_metadata UNIQUE (key, tx_id);


--
-- Name: tx_out unique_txout; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT unique_txout UNIQUE (tx_id, index);


--
-- Name: extra_key_witness unique_witness; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.extra_key_witness
    ADD CONSTRAINT unique_witness UNIQUE (hash);


--
-- Name: withdrawal withdrawal_pkey; Type: CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id);


--
-- Name: ada_pots ada_pots_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ada_pots
    ADD CONSTRAINT ada_pots_block_id_fkey FOREIGN KEY (block_id) REFERENCES dev.block(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_in collateral_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_in collateral_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES dev.datum(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES dev.script(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: collateral_tx_out collateral_tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: cost_model cost_model_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.cost_model
    ADD CONSTRAINT cost_model_block_id_fkey FOREIGN KEY (block_id) REFERENCES dev.block(id) ON DELETE CASCADE;


--
-- Name: datum datum_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.datum
    ADD CONSTRAINT datum_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT delegation_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT delegation_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT delegation_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES dev.redeemer(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.delegation
    ADD CONSTRAINT delegation_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_param
    ADD CONSTRAINT epoch_param_block_id_fkey FOREIGN KEY (block_id) REFERENCES dev.block(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_param
    ADD CONSTRAINT epoch_param_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES dev.cost_model(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_stake
    ADD CONSTRAINT epoch_stake_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.epoch_stake
    ADD CONSTRAINT epoch_stake_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: extra_key_witness extra_key_witness_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.extra_key_witness
    ADD CONSTRAINT extra_key_witness_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: slot_leader fk23db3v8n6dl1gf0njxgpceviq; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.slot_leader
    ADD CONSTRAINT fk23db3v8n6dl1gf0njxgpceviq FOREIGN KEY (pool_hash_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: block fk6gd608i8qbyert1hlfl9be71h; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.block
    ADD CONSTRAINT fk6gd608i8qbyert1hlfl9be71h FOREIGN KEY (previous_id) REFERENCES dev.block(id) ON DELETE CASCADE;


--
-- Name: block fke45qmlh5if6ghcyj0mhumjhk1; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.block
    ADD CONSTRAINT fke45qmlh5if6ghcyj0mhumjhk1 FOREIGN KEY (slot_leader_id) REFERENCES dev.slot_leader(id) ON DELETE CASCADE;


--
-- Name: pool_stake fknipbg0qr651vyttfseohc94ql; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_stake
    ADD CONSTRAINT fknipbg0qr651vyttfseohc94ql FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id);


--
-- Name: ma_tx_mint ma_tx_mint_ident_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_ident_fkey FOREIGN KEY (ident) REFERENCES dev.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_ident_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.ma_tx_out
    ADD CONSTRAINT ma_tx_out_ident_fkey FOREIGN KEY (ident) REFERENCES dev.multi_asset(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.param_proposal
    ADD CONSTRAINT param_proposal_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES dev.cost_model(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.param_proposal
    ADD CONSTRAINT param_proposal_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pmr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES dev.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pmr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES dev.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_owner
    ADD CONSTRAINT pool_owner_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_pool_update_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_owner
    ADD CONSTRAINT pool_owner_pool_update_id_fkey FOREIGN KEY (pool_update_id) REFERENCES dev.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_relay pool_relay_update_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_relay
    ADD CONSTRAINT pool_relay_update_id_fkey FOREIGN KEY (update_id) REFERENCES dev.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_announced_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_retire
    ADD CONSTRAINT pool_retire_announced_tx_id_fkey FOREIGN KEY (announced_tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_retire
    ADD CONSTRAINT pool_retire_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_hash_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT pool_update_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_meta_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT pool_update_meta_id_fkey FOREIGN KEY (meta_id) REFERENCES dev.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT pool_update_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pool_update
    ADD CONSTRAINT pool_update_reward_addr_id_fkey FOREIGN KEY (reward_addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: pot_transfer pot_transfer_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.pot_transfer
    ADD CONSTRAINT pot_transfer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer_data redeemer_data_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer_data
    ADD CONSTRAINT redeemer_data_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_redeemer_data_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer
    ADD CONSTRAINT redeemer_redeemer_data_id_fkey FOREIGN KEY (redeemer_data_id) REFERENCES dev.redeemer_data(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.redeemer
    ADD CONSTRAINT redeemer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserve
    ADD CONSTRAINT reserve_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reserve
    ADD CONSTRAINT reserve_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: reward reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reward
    ADD CONSTRAINT reward_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: reward reward_pool_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.reward
    ADD CONSTRAINT reward_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES dev.pool_hash(id) ON DELETE CASCADE;


--
-- Name: script script_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.script
    ADD CONSTRAINT script_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: stake_address stake_address_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_address
    ADD CONSTRAINT stake_address_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration
    ADD CONSTRAINT stake_deregistration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration
    ADD CONSTRAINT stake_deregistration_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES dev.redeemer(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_deregistration
    ADD CONSTRAINT stake_deregistration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_registration
    ADD CONSTRAINT stake_registration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.stake_registration
    ADD CONSTRAINT stake_registration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.treasury
    ADD CONSTRAINT treasury_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.treasury
    ADD CONSTRAINT treasury_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: tx tx_block_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx
    ADD CONSTRAINT tx_block_id_fkey FOREIGN KEY (block_id) REFERENCES dev.block(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_in
    ADD CONSTRAINT tx_in_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES dev.redeemer(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_in
    ADD CONSTRAINT tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_in
    ADD CONSTRAINT tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: tx_metadata tx_metadata_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_metadata
    ADD CONSTRAINT tx_metadata_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES dev.datum(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES dev.script(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.tx_out
    ADD CONSTRAINT tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_addr_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.withdrawal
    ADD CONSTRAINT withdrawal_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES dev.stake_address(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.withdrawal
    ADD CONSTRAINT withdrawal_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES dev.redeemer(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_tx_id_fkey; Type: FK CONSTRAINT; Schema: dev; Owner: cardano-master
--

ALTER TABLE ONLY dev.withdrawal
    ADD CONSTRAINT withdrawal_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES dev.tx(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

