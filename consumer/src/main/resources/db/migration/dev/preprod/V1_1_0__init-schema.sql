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
-- Name: preprod; Type: SCHEMA; Schema: -; Owner: cardano-master
--

CREATE SCHEMA IF NOT EXISTS preprod;


ALTER SCHEMA preprod OWNER TO "cardano-master";

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ada_pots; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.ada_pots (
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


ALTER TABLE preprod.ada_pots OWNER TO "cardano-master";

--
-- Name: ada_pots_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.ada_pots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.ada_pots_id_seq OWNER TO "cardano-master";

--
-- Name: ada_pots_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.ada_pots_id_seq OWNED BY preprod.ada_pots.id;


--
-- Name: block; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.block (
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


ALTER TABLE preprod.block OWNER TO "cardano-master";

--
-- Name: block_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.block_id_seq OWNER TO "cardano-master";

--
-- Name: block_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.block_id_seq OWNED BY preprod.block.id;


--
-- Name: cost_model; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.cost_model (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    costs character varying(65535) NOT NULL,
    hash character varying(64) NOT NULL,
    block_id bigint NOT NULL
);


ALTER TABLE preprod.cost_model OWNER TO "cardano-master";

--
-- Name: cost_model_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.cost_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.cost_model_id_seq OWNER TO "cardano-master";

--
-- Name: cost_model_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.cost_model_id_seq OWNED BY preprod.cost_model.id;


--
-- Name: datum; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.datum (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    value character varying(65535),
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.datum OWNER TO "cardano-master";

--
-- Name: datum_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.datum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.datum_id_seq OWNER TO "cardano-master";

--
-- Name: datum_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.datum_id_seq OWNED BY preprod.datum.id;


--
-- Name: delegation; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.delegation (
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


ALTER TABLE preprod.delegation OWNER TO "cardano-master";

--
-- Name: delegation_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.delegation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.delegation_id_seq OWNER TO "cardano-master";

--
-- Name: delegation_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.delegation_id_seq OWNED BY preprod.delegation.id;


--
-- Name: delisted_pool; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.delisted_pool (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(56) NOT NULL
);


ALTER TABLE preprod.delisted_pool OWNER TO "cardano-master";

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.delisted_pool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.delisted_pool_id_seq OWNER TO "cardano-master";

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.delisted_pool_id_seq OWNED BY preprod.delisted_pool.id;


--
-- Name: epoch; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.epoch (
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


ALTER TABLE preprod.epoch OWNER TO "cardano-master";

--
-- Name: epoch_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.epoch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.epoch_id_seq OWNER TO "cardano-master";

--
-- Name: epoch_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.epoch_id_seq OWNED BY preprod.epoch.id;


--
-- Name: epoch_param; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.epoch_param (
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


ALTER TABLE preprod.epoch_param OWNER TO "cardano-master";

--
-- Name: epoch_param_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.epoch_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.epoch_param_id_seq OWNER TO "cardano-master";

--
-- Name: epoch_param_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.epoch_param_id_seq OWNED BY preprod.epoch_param.id;


--
-- Name: epoch_stake; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.epoch_stake (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    pool_id bigint NOT NULL
);


ALTER TABLE preprod.epoch_stake OWNER TO "cardano-master";

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.epoch_stake_id_seq OWNER TO "cardano-master";

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.epoch_stake_id_seq OWNED BY preprod.epoch_stake.id;


--
-- Name: epoch_sync_time; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.epoch_sync_time (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    no bigint NOT NULL,
    seconds bigint NOT NULL,
    state character varying(255) NOT NULL
);


ALTER TABLE preprod.epoch_sync_time OWNER TO "cardano-master";

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.epoch_sync_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.epoch_sync_time_id_seq OWNER TO "cardano-master";

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.epoch_sync_time_id_seq OWNED BY preprod.epoch_sync_time.id;


--
-- Name: extra_key_witness; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.extra_key_witness (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash character varying(56) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.extra_key_witness OWNER TO "cardano-master";

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.extra_key_witness_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.extra_key_witness_id_seq OWNER TO "cardano-master";

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.extra_key_witness_id_seq OWNED BY preprod.extra_key_witness.id;


--
-- Name: failed_tx_out; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.failed_tx_out (
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


ALTER TABLE preprod.failed_tx_out OWNER TO "cardano-master";

--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.failed_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.failed_tx_out_id_seq OWNER TO "cardano-master";

--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.failed_tx_out_id_seq OWNED BY preprod.failed_tx_out.id;


--
-- Name: ma_tx_mint; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.ma_tx_mint (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    quantity numeric(20,0) NOT NULL,
    ident bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.ma_tx_mint OWNER TO "cardano-master";

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.ma_tx_mint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.ma_tx_mint_id_seq OWNER TO "cardano-master";

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.ma_tx_mint_id_seq OWNED BY preprod.ma_tx_mint.id;


--
-- Name: ma_tx_out; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.ma_tx_out (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    quantity numeric(20,0) NOT NULL,
    ident bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE preprod.ma_tx_out OWNER TO "cardano-master";

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.ma_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.ma_tx_out_id_seq OWNER TO "cardano-master";

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.ma_tx_out_id_seq OWNED BY preprod.ma_tx_out.id;


--
-- Name: meta; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.meta (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    network_name character varying(255) NOT NULL,
    start_time timestamp without time zone NOT NULL,
    version character varying(255) NOT NULL
);


ALTER TABLE preprod.meta OWNER TO "cardano-master";

--
-- Name: meta_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.meta_id_seq OWNER TO "cardano-master";

--
-- Name: meta_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.meta_id_seq OWNED BY preprod.meta.id;


--
-- Name: multi_asset; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.multi_asset (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    fingerprint character varying(255) NOT NULL,
    name bytea NOT NULL,
    policy character varying(56) NOT NULL,
    supply numeric(23,0),
    tx_count bigint,
    "time" timestamp without time zone
);


ALTER TABLE preprod.multi_asset OWNER TO "cardano-master";

--
-- Name: multi_asset_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.multi_asset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.multi_asset_id_seq OWNER TO "cardano-master";

--
-- Name: multi_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.multi_asset_id_seq OWNED BY preprod.multi_asset.id;


--
-- Name: param_proposal; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.param_proposal (
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


ALTER TABLE preprod.param_proposal OWNER TO "cardano-master";

--
-- Name: param_proposal_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.param_proposal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.param_proposal_id_seq OWNER TO "cardano-master";

--
-- Name: param_proposal_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.param_proposal_id_seq OWNED BY preprod.param_proposal.id;


--
-- Name: pool_hash; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_hash (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(56) NOT NULL,
    pool_size numeric(20,0) NOT NULL,
    epoch_no integer NOT NULL,
    view character varying(255) NOT NULL
);


ALTER TABLE preprod.pool_hash OWNER TO "cardano-master";

--
-- Name: pool_hash_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_hash_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_hash_id_seq OWNER TO "cardano-master";

--
-- Name: pool_hash_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_hash_id_seq OWNED BY preprod.pool_hash.id;


--
-- Name: pool_metadata_ref; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_metadata_ref (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash character varying(64) NOT NULL,
    url character varying(255) NOT NULL,
    pool_id bigint NOT NULL,
    registered_tx_id bigint NOT NULL
);


ALTER TABLE preprod.pool_metadata_ref OWNER TO "cardano-master";

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_metadata_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_metadata_ref_id_seq OWNER TO "cardano-master";

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_metadata_ref_id_seq OWNED BY preprod.pool_metadata_ref.id;


--
-- Name: pool_offline_data; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_offline_data (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    json character varying(65535) NOT NULL,
    ticker_name character varying(255) NOT NULL,
    pool_id bigint NOT NULL,
    pmr_id bigint NOT NULL
);


ALTER TABLE preprod.pool_offline_data OWNER TO "cardano-master";

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_offline_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_offline_data_id_seq OWNER TO "cardano-master";

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_offline_data_id_seq OWNED BY preprod.pool_offline_data.id;


--
-- Name: pool_offline_fetch_error; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_offline_fetch_error (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    fetch_error character varying(65535) NOT NULL,
    fetch_time timestamp without time zone NOT NULL,
    retry_count integer NOT NULL,
    pool_id bigint NOT NULL,
    pmr_id bigint NOT NULL
);


ALTER TABLE preprod.pool_offline_fetch_error OWNER TO "cardano-master";

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_offline_fetch_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_offline_fetch_error_id_seq OWNER TO "cardano-master";

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_offline_fetch_error_id_seq OWNED BY preprod.pool_offline_fetch_error.id;


--
-- Name: pool_owner; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_owner (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    pool_update_id bigint NOT NULL,
    addr_id bigint NOT NULL
);


ALTER TABLE preprod.pool_owner OWNER TO "cardano-master";

--
-- Name: pool_owner_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_owner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_owner_id_seq OWNER TO "cardano-master";

--
-- Name: pool_owner_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_owner_id_seq OWNED BY preprod.pool_owner.id;


--
-- Name: pool_relay; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_relay (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    dns_name character varying(255),
    dns_srv_name character varying(255),
    ipv4 character varying(255),
    ipv6 character varying(255),
    port integer,
    update_id bigint NOT NULL
);


ALTER TABLE preprod.pool_relay OWNER TO "cardano-master";

--
-- Name: pool_relay_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_relay_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_relay_id_seq OWNER TO "cardano-master";

--
-- Name: pool_relay_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_relay_id_seq OWNED BY preprod.pool_relay.id;


--
-- Name: pool_retire; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_retire (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    retiring_epoch integer NOT NULL,
    announced_tx_id bigint NOT NULL,
    hash_id bigint NOT NULL
);


ALTER TABLE preprod.pool_retire OWNER TO "cardano-master";

--
-- Name: pool_retire_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_retire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_retire_id_seq OWNER TO "cardano-master";

--
-- Name: pool_retire_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_retire_id_seq OWNED BY preprod.pool_retire.id;


--
-- Name: pool_stake; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_stake (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(19,2),
    pool_id bigint
);


ALTER TABLE preprod.pool_stake OWNER TO "cardano-master";

--
-- Name: pool_stake_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_stake_id_seq OWNER TO "cardano-master";

--
-- Name: pool_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_stake_id_seq OWNED BY preprod.pool_stake.id;


--
-- Name: pool_update; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pool_update (
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


ALTER TABLE preprod.pool_update OWNER TO "cardano-master";

--
-- Name: pool_update_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pool_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pool_update_id_seq OWNER TO "cardano-master";

--
-- Name: pool_update_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pool_update_id_seq OWNED BY preprod.pool_update.id;


--
-- Name: pot_transfer; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.pot_transfer (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    reserves numeric(20,0) NOT NULL,
    treasury numeric(20,0) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.pot_transfer OWNER TO "cardano-master";

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.pot_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.pot_transfer_id_seq OWNER TO "cardano-master";

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.pot_transfer_id_seq OWNED BY preprod.pot_transfer.id;


--
-- Name: redeemer; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.redeemer (
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


ALTER TABLE preprod.redeemer OWNER TO "cardano-master";

--
-- Name: redeemer_data; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.redeemer_data (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    value character varying(65535),
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.redeemer_data OWNER TO "cardano-master";

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.redeemer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.redeemer_data_id_seq OWNER TO "cardano-master";

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.redeemer_data_id_seq OWNED BY preprod.redeemer_data.id;


--
-- Name: redeemer_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.redeemer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.redeemer_id_seq OWNER TO "cardano-master";

--
-- Name: redeemer_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.redeemer_id_seq OWNED BY preprod.redeemer.id;


--
-- Name: reference_tx_in; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.reference_tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_out_index smallint NOT NULL,
    tx_in_id bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE preprod.reference_tx_in OWNER TO "cardano-master";

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.reference_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.reference_tx_in_id_seq OWNER TO "cardano-master";

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.reference_tx_in_id_seq OWNED BY preprod.reference_tx_in.id;


--
-- Name: reserve; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.reserve (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    cert_index integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.reserve OWNER TO "cardano-master";

--
-- Name: reserve_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.reserve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.reserve_id_seq OWNER TO "cardano-master";

--
-- Name: reserve_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.reserve_id_seq OWNED BY preprod.reserve.id;


--
-- Name: reserved_pool_ticker; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.reserved_pool_ticker (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    name character varying(255) NOT NULL,
    pool_hash character varying(56) NOT NULL
);


ALTER TABLE preprod.reserved_pool_ticker OWNER TO "cardano-master";

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.reserved_pool_ticker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.reserved_pool_ticker_id_seq OWNER TO "cardano-master";

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.reserved_pool_ticker_id_seq OWNED BY preprod.reserved_pool_ticker.id;


--
-- Name: reward; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.reward (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    earned_epoch bigint NOT NULL,
    spendable_epoch bigint NOT NULL,
    type character varying(255) NOT NULL,
    addr_id bigint NOT NULL,
    pool_id bigint
);


ALTER TABLE preprod.reward OWNER TO "cardano-master";

--
-- Name: reward_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.reward_id_seq OWNER TO "cardano-master";

--
-- Name: reward_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.reward_id_seq OWNED BY preprod.reward.id;


--
-- Name: schema_version; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.schema_version (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    stage_one bigint NOT NULL,
    stage_three bigint NOT NULL,
    stage_two bigint NOT NULL
);


ALTER TABLE preprod.schema_version OWNER TO "cardano-master";

--
-- Name: schema_version_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.schema_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.schema_version_id_seq OWNER TO "cardano-master";

--
-- Name: schema_version_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.schema_version_id_seq OWNED BY preprod.schema_version.id;


--
-- Name: script; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.script (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    hash character varying(64) NOT NULL,
    json character varying(65535),
    serialised_size integer,
    type character varying(255) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.script OWNER TO "cardano-master";

--
-- Name: script_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.script_id_seq OWNER TO "cardano-master";

--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.script_id_seq OWNED BY preprod.script.id;


--
-- Name: slot_leader; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.slot_leader (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    description character varying(65535) NOT NULL,
    hash character varying(56) NOT NULL,
    pool_hash_id bigint
);


ALTER TABLE preprod.slot_leader OWNER TO "cardano-master";

--
-- Name: slot_leader_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.slot_leader_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.slot_leader_id_seq OWNER TO "cardano-master";

--
-- Name: slot_leader_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.slot_leader_id_seq OWNED BY preprod.slot_leader.id;


--
-- Name: stake_address; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.stake_address (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    hash_raw character varying(255) NOT NULL,
    script_hash character varying(56),
    view character varying(65535) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.stake_address OWNER TO "cardano-master";

--
-- Name: stake_address_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.stake_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.stake_address_id_seq OWNER TO "cardano-master";

--
-- Name: stake_address_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.stake_address_id_seq OWNED BY preprod.stake_address.id;


--
-- Name: stake_deregistration; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.stake_deregistration (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    redeemer_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.stake_deregistration OWNER TO "cardano-master";

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.stake_deregistration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.stake_deregistration_id_seq OWNER TO "cardano-master";

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.stake_deregistration_id_seq OWNED BY preprod.stake_deregistration.id;


--
-- Name: stake_registration; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.stake_registration (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    cert_index integer NOT NULL,
    epoch_no integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.stake_registration OWNER TO "cardano-master";

--
-- Name: stake_registration_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.stake_registration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.stake_registration_id_seq OWNER TO "cardano-master";

--
-- Name: stake_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.stake_registration_id_seq OWNED BY preprod.stake_registration.id;


--
-- Name: treasury; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.treasury (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    cert_index integer NOT NULL,
    addr_id bigint NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.treasury OWNER TO "cardano-master";

--
-- Name: treasury_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.treasury_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.treasury_id_seq OWNER TO "cardano-master";

--
-- Name: treasury_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.treasury_id_seq OWNED BY preprod.treasury.id;


--
-- Name: tx; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.tx (
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


ALTER TABLE preprod.tx OWNER TO "cardano-master";

--
-- Name: tx_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.tx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.tx_id_seq OWNER TO "cardano-master";

--
-- Name: tx_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.tx_id_seq OWNED BY preprod.tx.id;


--
-- Name: tx_in; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_in_id bigint,
    tx_out_index smallint NOT NULL,
    tx_out_id bigint,
    redeemer_id bigint
);


ALTER TABLE preprod.tx_in OWNER TO "cardano-master";

--
-- Name: tx_in_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.tx_in_id_seq OWNER TO "cardano-master";

--
-- Name: tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.tx_in_id_seq OWNED BY preprod.tx_in.id;


--
-- Name: tx_metadata; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.tx_metadata (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    bytes bytea,
    json character varying(65535),
    key numeric(20,0) NOT NULL,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.tx_metadata OWNER TO "cardano-master";

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.tx_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.tx_metadata_id_seq OWNER TO "cardano-master";

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.tx_metadata_id_seq OWNED BY preprod.tx_metadata.id;


--
-- Name: tx_out; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.tx_out (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    address character varying(65535) NOT NULL,
    address_has_script boolean NOT NULL,
    address_raw bytea NOT NULL,
    data_hash character varying(64),
    has_used boolean,
    index smallint NOT NULL,
    payment_cred character varying(56),
    token_type integer NOT NULL,
    value numeric(20,0) NOT NULL,
    inline_datum_id bigint,
    reference_script_id bigint,
    stake_address_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.tx_out OWNER TO "cardano-master";

--
-- Name: tx_out_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.tx_out_id_seq OWNER TO "cardano-master";

--
-- Name: tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.tx_out_id_seq OWNED BY preprod.tx_out.id;


--
-- Name: unconsume_tx_in; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.unconsume_tx_in (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    tx_out_index smallint NOT NULL,
    tx_in_id bigint NOT NULL,
    tx_out_id bigint NOT NULL
);


ALTER TABLE preprod.unconsume_tx_in OWNER TO "cardano-master";

--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.unconsume_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.unconsume_tx_in_id_seq OWNER TO "cardano-master";

--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.unconsume_tx_in_id_seq OWNED BY preprod.unconsume_tx_in.id;


--
-- Name: withdrawal; Type: TABLE; Schema: preprod; Owner: cardano-master
--

CREATE TABLE preprod.withdrawal (
    id bigint NOT NULL,
    is_deleted boolean DEFAULT false,
    amount numeric(20,0) NOT NULL,
    addr_id bigint NOT NULL,
    redeemer_id bigint,
    tx_id bigint NOT NULL
);


ALTER TABLE preprod.withdrawal OWNER TO "cardano-master";

--
-- Name: withdrawal_id_seq; Type: SEQUENCE; Schema: preprod; Owner: cardano-master
--

CREATE SEQUENCE preprod.withdrawal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preprod.withdrawal_id_seq OWNER TO "cardano-master";

--
-- Name: withdrawal_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod; Owner: cardano-master
--

ALTER SEQUENCE preprod.withdrawal_id_seq OWNED BY preprod.withdrawal.id;


--
-- Name: ada_pots id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ada_pots ALTER COLUMN id SET DEFAULT nextval('preprod.ada_pots_id_seq'::regclass);


--
-- Name: block id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.block ALTER COLUMN id SET DEFAULT nextval('preprod.block_id_seq'::regclass);


--
-- Name: cost_model id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.cost_model ALTER COLUMN id SET DEFAULT nextval('preprod.cost_model_id_seq'::regclass);


--
-- Name: datum id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.datum ALTER COLUMN id SET DEFAULT nextval('preprod.datum_id_seq'::regclass);


--
-- Name: delegation id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation ALTER COLUMN id SET DEFAULT nextval('preprod.delegation_id_seq'::regclass);


--
-- Name: delisted_pool id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delisted_pool ALTER COLUMN id SET DEFAULT nextval('preprod.delisted_pool_id_seq'::regclass);


--
-- Name: epoch id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch ALTER COLUMN id SET DEFAULT nextval('preprod.epoch_id_seq'::regclass);


--
-- Name: epoch_param id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_param ALTER COLUMN id SET DEFAULT nextval('preprod.epoch_param_id_seq'::regclass);


--
-- Name: epoch_stake id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_stake ALTER COLUMN id SET DEFAULT nextval('preprod.epoch_stake_id_seq'::regclass);


--
-- Name: epoch_sync_time id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_sync_time ALTER COLUMN id SET DEFAULT nextval('preprod.epoch_sync_time_id_seq'::regclass);


--
-- Name: extra_key_witness id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.extra_key_witness ALTER COLUMN id SET DEFAULT nextval('preprod.extra_key_witness_id_seq'::regclass);


--
-- Name: failed_tx_out id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out ALTER COLUMN id SET DEFAULT nextval('preprod.failed_tx_out_id_seq'::regclass);


--
-- Name: ma_tx_mint id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_mint ALTER COLUMN id SET DEFAULT nextval('preprod.ma_tx_mint_id_seq'::regclass);


--
-- Name: ma_tx_out id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_out ALTER COLUMN id SET DEFAULT nextval('preprod.ma_tx_out_id_seq'::regclass);


--
-- Name: meta id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.meta ALTER COLUMN id SET DEFAULT nextval('preprod.meta_id_seq'::regclass);


--
-- Name: multi_asset id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.multi_asset ALTER COLUMN id SET DEFAULT nextval('preprod.multi_asset_id_seq'::regclass);


--
-- Name: param_proposal id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.param_proposal ALTER COLUMN id SET DEFAULT nextval('preprod.param_proposal_id_seq'::regclass);


--
-- Name: pool_hash id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_hash ALTER COLUMN id SET DEFAULT nextval('preprod.pool_hash_id_seq'::regclass);


--
-- Name: pool_metadata_ref id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_metadata_ref ALTER COLUMN id SET DEFAULT nextval('preprod.pool_metadata_ref_id_seq'::regclass);


--
-- Name: pool_offline_data id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_data ALTER COLUMN id SET DEFAULT nextval('preprod.pool_offline_data_id_seq'::regclass);


--
-- Name: pool_offline_fetch_error id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_fetch_error ALTER COLUMN id SET DEFAULT nextval('preprod.pool_offline_fetch_error_id_seq'::regclass);


--
-- Name: pool_owner id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_owner ALTER COLUMN id SET DEFAULT nextval('preprod.pool_owner_id_seq'::regclass);


--
-- Name: pool_relay id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_relay ALTER COLUMN id SET DEFAULT nextval('preprod.pool_relay_id_seq'::regclass);


--
-- Name: pool_retire id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_retire ALTER COLUMN id SET DEFAULT nextval('preprod.pool_retire_id_seq'::regclass);


--
-- Name: pool_stake id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_stake ALTER COLUMN id SET DEFAULT nextval('preprod.pool_stake_id_seq'::regclass);


--
-- Name: pool_update id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update ALTER COLUMN id SET DEFAULT nextval('preprod.pool_update_id_seq'::regclass);


--
-- Name: pot_transfer id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pot_transfer ALTER COLUMN id SET DEFAULT nextval('preprod.pot_transfer_id_seq'::regclass);


--
-- Name: redeemer id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer ALTER COLUMN id SET DEFAULT nextval('preprod.redeemer_id_seq'::regclass);


--
-- Name: redeemer_data id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer_data ALTER COLUMN id SET DEFAULT nextval('preprod.redeemer_data_id_seq'::regclass);


--
-- Name: reference_tx_in id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reference_tx_in ALTER COLUMN id SET DEFAULT nextval('preprod.reference_tx_in_id_seq'::regclass);


--
-- Name: reserve id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserve ALTER COLUMN id SET DEFAULT nextval('preprod.reserve_id_seq'::regclass);


--
-- Name: reserved_pool_ticker id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserved_pool_ticker ALTER COLUMN id SET DEFAULT nextval('preprod.reserved_pool_ticker_id_seq'::regclass);


--
-- Name: reward id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reward ALTER COLUMN id SET DEFAULT nextval('preprod.reward_id_seq'::regclass);


--
-- Name: schema_version id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.schema_version ALTER COLUMN id SET DEFAULT nextval('preprod.schema_version_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.script ALTER COLUMN id SET DEFAULT nextval('preprod.script_id_seq'::regclass);


--
-- Name: slot_leader id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.slot_leader ALTER COLUMN id SET DEFAULT nextval('preprod.slot_leader_id_seq'::regclass);


--
-- Name: stake_address id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_address ALTER COLUMN id SET DEFAULT nextval('preprod.stake_address_id_seq'::regclass);


--
-- Name: stake_deregistration id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration ALTER COLUMN id SET DEFAULT nextval('preprod.stake_deregistration_id_seq'::regclass);


--
-- Name: stake_registration id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_registration ALTER COLUMN id SET DEFAULT nextval('preprod.stake_registration_id_seq'::regclass);


--
-- Name: treasury id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.treasury ALTER COLUMN id SET DEFAULT nextval('preprod.treasury_id_seq'::regclass);


--
-- Name: tx id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx ALTER COLUMN id SET DEFAULT nextval('preprod.tx_id_seq'::regclass);


--
-- Name: tx_in id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in ALTER COLUMN id SET DEFAULT nextval('preprod.tx_in_id_seq'::regclass);


--
-- Name: tx_metadata id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_metadata ALTER COLUMN id SET DEFAULT nextval('preprod.tx_metadata_id_seq'::regclass);


--
-- Name: tx_out id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out ALTER COLUMN id SET DEFAULT nextval('preprod.tx_out_id_seq'::regclass);


--
-- Name: unconsume_tx_in id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.unconsume_tx_in ALTER COLUMN id SET DEFAULT nextval('preprod.unconsume_tx_in_id_seq'::regclass);


--
-- Name: withdrawal id; Type: DEFAULT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.withdrawal ALTER COLUMN id SET DEFAULT nextval('preprod.withdrawal_id_seq'::regclass);


--
-- Data for Name: ada_pots; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.ada_pots (id, is_deleted, deposits, epoch_no, fees, reserves, rewards, slot_no, treasury, utxo, block_id) FROM stdin;
\.


--
-- Data for Name: block; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.block (id, is_deleted, block_no, epoch_no, epoch_slot_no, hash, op_cert, op_cert_counter, proto_major, proto_minor, size, slot_leader_id, slot_no, "time", tx_count, vrf_key, previous_id) FROM stdin;
1	f	\N	\N	\N	d4b8de7a11d929a323373cbab6c1a9bdc931beffff11db111cf9d57356ee1937	\N	\N	0	0	0	1	\N	2022-06-01 00:00:00	8	\N	\N
\.


--
-- Data for Name: cost_model; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.cost_model (id, is_deleted, costs, hash, block_id) FROM stdin;
\.


--
-- Data for Name: datum; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.datum (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: delegation; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.delegation (id, is_deleted, active_epoch_no, cert_index, slot_no, addr_id, pool_hash_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: delisted_pool; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.delisted_pool (id, is_deleted, hash_raw) FROM stdin;
\.


--
-- Data for Name: epoch; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.epoch (id, is_deleted, blk_count, end_time, fees, max_slot, no, out_sum, start_time, tx_count) FROM stdin;
\.


--
-- Data for Name: epoch_param; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.epoch_param (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, epoch_no, extra_entropy, influence, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, nonce, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, block_id, cost_model_id) FROM stdin;
\.


--
-- Data for Name: epoch_stake; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.epoch_stake (id, is_deleted, amount, epoch_no, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: epoch_sync_time; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.epoch_sync_time (id, is_deleted, no, seconds, state) FROM stdin;
\.


--
-- Data for Name: extra_key_witness; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.extra_key_witness (id, is_deleted, hash, tx_id) FROM stdin;
\.


--
-- Data for Name: failed_tx_out; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.failed_tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, index, multi_assets_descr, payment_cred, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_mint; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.ma_tx_mint (id, is_deleted, quantity, ident, tx_id) FROM stdin;
\.


--
-- Data for Name: ma_tx_out; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.ma_tx_out (id, is_deleted, quantity, ident, tx_out_id) FROM stdin;
\.


--
-- Data for Name: meta; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.meta (id, is_deleted, network_name, start_time, version) FROM stdin;
\.


--
-- Data for Name: multi_asset; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.multi_asset (id, is_deleted, fingerprint, name, policy, supply, tx_count) FROM stdin;
\.


--
-- Data for Name: param_proposal; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.param_proposal (id, is_deleted, coins_per_utxo_size, collateral_percent, decentralisation, entropy, epoch_no, influence, key, key_deposit, max_bh_size, max_block_ex_mem, max_block_ex_steps, max_block_size, max_collateral_inputs, max_epoch, max_tx_ex_mem, max_tx_ex_steps, max_tx_size, max_val_size, min_fee_a, min_fee_b, min_pool_cost, min_utxo_value, monetary_expand_rate, optimal_pool_count, pool_deposit, price_mem, price_step, protocol_major, protocol_minor, treasury_growth_rate, cost_model_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_hash; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_hash (id, is_deleted, hash_raw, pool_size, view) FROM stdin;
\.


--
-- Data for Name: pool_metadata_ref; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_metadata_ref (id, is_deleted, hash, url, pool_id, registered_tx_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_data; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_offline_data (id, is_deleted, bytes, hash, json, ticker_name, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_offline_fetch_error; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_offline_fetch_error (id, is_deleted, fetch_error, fetch_time, retry_count, pool_id, pmr_id) FROM stdin;
\.


--
-- Data for Name: pool_owner; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_owner (id, is_deleted, pool_update_id, addr_id) FROM stdin;
\.


--
-- Data for Name: pool_relay; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_relay (id, is_deleted, dns_name, dns_srv_name, ipv4, ipv6, port, update_id) FROM stdin;
\.


--
-- Data for Name: pool_retire; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_retire (id, is_deleted, cert_index, retiring_epoch, announced_tx_id, hash_id) FROM stdin;
\.


--
-- Data for Name: pool_stake; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_stake (id, is_deleted, amount, pool_id) FROM stdin;
\.


--
-- Data for Name: pool_update; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pool_update (id, is_deleted, active_epoch_no, cert_index, fixed_cost, margin, pledge, vrf_key_hash, meta_id, hash_id, registered_tx_id, reward_addr_id) FROM stdin;
\.


--
-- Data for Name: pot_transfer; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.pot_transfer (id, is_deleted, cert_index, reserves, treasury, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.redeemer (id, is_deleted, fee, index, purpose, script_hash, unit_mem, unit_steps, redeemer_data_id, tx_id) FROM stdin;
\.


--
-- Data for Name: redeemer_data; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.redeemer_data (id, is_deleted, bytes, hash, value, tx_id) FROM stdin;
\.


--
-- Data for Name: reference_tx_in; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.reference_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: reserve; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.reserve (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: reserved_pool_ticker; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.reserved_pool_ticker (id, is_deleted, name, pool_hash) FROM stdin;
\.


--
-- Data for Name: reward; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.reward (id, is_deleted, amount, earned_epoch, spendable_epoch, type, addr_id, pool_id) FROM stdin;
\.


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.schema_version (id, is_deleted, stage_one, stage_three, stage_two) FROM stdin;
\.


--
-- Data for Name: script; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.script (id, is_deleted, bytes, hash, json, serialised_size, type, tx_id) FROM stdin;
\.


--
-- Data for Name: slot_leader; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.slot_leader (id, is_deleted, description, hash, pool_hash_id) FROM stdin;
1	f	Genesis slot leader	d4b8de7a11d929a323373cbab6c1a9bdc931beffff11db111cf9d573	\N
\.


--
-- Data for Name: stake_address; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.stake_address (id, is_deleted, hash_raw, script_hash, view, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_deregistration; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.stake_deregistration (id, is_deleted, cert_index, epoch_no, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Data for Name: stake_registration; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.stake_registration (id, is_deleted, cert_index, epoch_no, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: treasury; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.treasury (id, is_deleted, amount, cert_index, addr_id, tx_id) FROM stdin;
\.


--
-- Data for Name: tx; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.tx (id, is_deleted, block_id, block_index, deposit, fee, hash, invalid_before, invalid_hereafter, out_sum, script_size, size, valid_contract) FROM stdin;
1	f	1	0	0	0	8e0280beebc3d12626e87b182f4205d75e49981042f54081cd35f3a4a85630b0	\N	\N	0	0	0	t
2	f	1	0	0	0	02e9a39dec6fad8a889a619b71a3cf06fab8475a6d19ea6e949846b5e0ba8747	\N	\N	0	0	0	t
3	f	1	0	0	0	5526b1373acfc774794a62122f95583ff17febb2ca8a0fe948d097e29cf99099	\N	\N	30000000000000000	0	0	t
4	f	1	0	0	0	21184177437666b3d3229e118d000e9eb0e625063610ee707921493bc215fc01	\N	\N	0	0	0	t
5	f	1	0	0	0	84e79ec2eea95464106f80f70989a29c6247119174d16d505d574ab600e7249a	\N	\N	0	0	0	t
6	f	1	0	0	0	91fb5ef738503676d0e5e91565ba3907c30c8a4d9a99cddc9c84205cfb7f3cbe	\N	\N	0	0	0	t
7	f	1	0	0	0	c0290a97a0c64e5dcc65b7c1eb89fa80d4b94218d15fc938ea5c899ba9d27d6f	\N	\N	0	0	0	t
8	f	1	0	0	0	b731574b44de062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45	\N	\N	0	0	0	t
\.


--
-- Data for Name: tx_in; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.tx_in (id, is_deleted, tx_in_id, tx_out_index, tx_out_id, redeemer_id) FROM stdin;
\.


--
-- Data for Name: tx_metadata; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.tx_metadata (id, is_deleted, bytes, json, key, tx_id) FROM stdin;
\.


--
-- Data for Name: tx_out; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.tx_out (id, is_deleted, address, address_has_script, address_raw, data_hash, has_used, index, payment_cred, token_type, value, inline_datum_id, reference_script_id, stake_address_id, tx_id) FROM stdin;
1	f	FHnt4NL7yPXhCzCHVywZLqVsvwuG3HvwmjKXQJBrXh3h2aigv6uxkePbpzRNV8q	f	\\x82d818582483581c056d8907b4530dabec0ab77456a2b5c7e695150d7534380a8093091ea1024101001ae0af87de	\N	\N	0	\N	0	0	\N	\N	\N	1
2	f	FHnt4NL7yPXuJGViM3KwSPwrwECD9q5vNetX3QJDYRWgiX3RHi5i5VV32dnETDK	f	\\x82d818582483581c5729ef95916cb82d3d7a3ecacd337c7faa7ce69d34397cbee385b55ba1024101001a89394a0a	\N	\N	0	\N	0	0	\N	\N	\N	2
3	f	FHnt4NL7yPXuYUxBF33VX5dZMBDAab2kvSNLRzCskvuKNCSDknzrQvKeQhGUw5a	f	\\x82d818582483581c58d2114184c5ed784cc419eb680a7d33c698d0ec963e2e805e83e260a1024101001a74c02a09	\N	\N	0	\N	0	30000000000000000	\N	\N	\N	3
4	f	FHnt4NL7yPY8exfnuJ8ACyoU7xCN93tKXSv357UrTp1nddGbkWxJpQfrt62xYFX	f	\\x82d818582483581cb174bf6c706e69f7b4d39f661efd4fa07b95dd13aa929eb82f9cce3ca1024101001aac4c43ae	\N	\N	0	\N	0	0	\N	\N	\N	4
5	f	FHnt4NL7yPYFpVcAXZADrKdsqCAFvcRFYkTcqkn2guGmj8akQMiMVjhSUECvD1F	f	\\x82d818582483581ce1e32ec7ea08628646f7a62c842bdcc126fdbcb4c70bd8208d9aa170a1024101001a9034cc16	\N	\N	0	\N	0	0	\N	\N	\N	5
6	f	FHnt4NL7yPYH2vP2FLEfH2pt3K6meM7fgtjRiLBidaqpP5ogPzxLNsZy68e1KdW	f	\\x82d818582483581cea188b7cf0fc4499a701a1508796eaccc12844a6190883d803ad9751a1024101001aeb4eeabd	\N	\N	0	\N	0	0	\N	\N	\N	6
7	f	FHnt4NL7yPYHrcxPtPufYYFWLhqvHGnZ5NFSz2KZpWQgSq4VLsUgWnkEmfUtd1E	f	\\x82d818582483581cefa7c6aa55ddcaa2ef1cf06e6419968d5b456b806ce4e1a4fbed6f90a1024101001ac1dffa05	\N	\N	0	\N	0	0	\N	\N	\N	7
8	f	FHnt4NL7yPYJiN5Y8VsQr6LP6YgN51BHBPegNjVwKkq6AooCkbTpfZ2bqkVkfXU	f	\\x82d818582483581cf573fe1e0f8de40d39632ee6da6d7dc5a4e43d29776eb90349fe630ca1024101001abe027097	\N	\N	0	\N	0	0	\N	\N	\N	8
\.


--
-- Data for Name: unconsume_tx_in; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.unconsume_tx_in (id, is_deleted, tx_out_index, tx_in_id, tx_out_id) FROM stdin;
\.


--
-- Data for Name: withdrawal; Type: TABLE DATA; Schema: preprod; Owner: cardano-master
--

COPY preprod.withdrawal (id, is_deleted, amount, addr_id, redeemer_id, tx_id) FROM stdin;
\.


--
-- Name: ada_pots_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.ada_pots_id_seq', 1, false);


--
-- Name: block_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.block_id_seq', 1, true);


--
-- Name: cost_model_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.cost_model_id_seq', 1, false);


--
-- Name: datum_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.datum_id_seq', 1, false);


--
-- Name: delegation_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.delegation_id_seq', 1, false);


--
-- Name: delisted_pool_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.delisted_pool_id_seq', 1, false);


--
-- Name: epoch_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.epoch_id_seq', 1, false);


--
-- Name: epoch_param_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.epoch_param_id_seq', 1, false);


--
-- Name: epoch_stake_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.epoch_stake_id_seq', 1, false);


--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.epoch_sync_time_id_seq', 1, false);


--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.extra_key_witness_id_seq', 1, false);


--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.failed_tx_out_id_seq', 1, false);


--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.ma_tx_mint_id_seq', 1, false);


--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.ma_tx_out_id_seq', 1, false);


--
-- Name: meta_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.meta_id_seq', 1, false);


--
-- Name: multi_asset_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.multi_asset_id_seq', 1, false);


--
-- Name: param_proposal_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.param_proposal_id_seq', 1, false);


--
-- Name: pool_hash_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_hash_id_seq', 1, false);


--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_metadata_ref_id_seq', 1, false);


--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_offline_data_id_seq', 1, false);


--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_offline_fetch_error_id_seq', 1, false);


--
-- Name: pool_owner_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_owner_id_seq', 1, false);


--
-- Name: pool_relay_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_relay_id_seq', 1, false);


--
-- Name: pool_retire_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_retire_id_seq', 1, false);


--
-- Name: pool_stake_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_stake_id_seq', 1, false);


--
-- Name: pool_update_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pool_update_id_seq', 1, false);


--
-- Name: pot_transfer_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.pot_transfer_id_seq', 1, false);


--
-- Name: redeemer_data_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.redeemer_data_id_seq', 1, false);


--
-- Name: redeemer_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.redeemer_id_seq', 1, false);


--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.reference_tx_in_id_seq', 1, false);


--
-- Name: reserve_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.reserve_id_seq', 1, false);


--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.reserved_pool_ticker_id_seq', 1, false);


--
-- Name: reward_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.reward_id_seq', 1, false);


--
-- Name: schema_version_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.schema_version_id_seq', 1, false);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.script_id_seq', 1, false);


--
-- Name: slot_leader_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.slot_leader_id_seq', 1, true);


--
-- Name: stake_address_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.stake_address_id_seq', 1, false);


--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.stake_deregistration_id_seq', 1, false);


--
-- Name: stake_registration_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.stake_registration_id_seq', 1, false);


--
-- Name: treasury_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.treasury_id_seq', 1, false);


--
-- Name: tx_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.tx_id_seq', 8, true);


--
-- Name: tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.tx_in_id_seq', 1, false);


--
-- Name: tx_metadata_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.tx_metadata_id_seq', 1, false);


--
-- Name: tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.tx_out_id_seq', 8, true);


--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.unconsume_tx_in_id_seq', 1, false);


--
-- Name: withdrawal_id_seq; Type: SEQUENCE SET; Schema: preprod; Owner: cardano-master
--

SELECT pg_catalog.setval('preprod.withdrawal_id_seq', 1, false);


--
-- Name: ada_pots ada_pots_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ada_pots
    ADD CONSTRAINT ada_pots_pkey PRIMARY KEY (id);


--
-- Name: block block_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.block
    ADD CONSTRAINT block_pkey PRIMARY KEY (id);


--
-- Name: cost_model cost_model_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.cost_model
    ADD CONSTRAINT cost_model_pkey PRIMARY KEY (id);


--
-- Name: datum datum_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.datum
    ADD CONSTRAINT datum_pkey PRIMARY KEY (id);


--
-- Name: delegation delegation_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);


--
-- Name: delisted_pool delisted_pool_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delisted_pool
    ADD CONSTRAINT delisted_pool_pkey PRIMARY KEY (id);


--
-- Name: epoch_param epoch_param_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_param
    ADD CONSTRAINT epoch_param_pkey PRIMARY KEY (id);


--
-- Name: epoch epoch_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch
    ADD CONSTRAINT epoch_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_sync_time epoch_sync_time_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_sync_time
    ADD CONSTRAINT epoch_sync_time_pkey PRIMARY KEY (id);


--
-- Name: extra_key_witness extra_key_witness_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.extra_key_witness
    ADD CONSTRAINT extra_key_witness_pkey PRIMARY KEY (id);


--
-- Name: failed_tx_out failed_tx_out_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT failed_tx_out_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_mint ma_tx_mint_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_out ma_tx_out_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_out
    ADD CONSTRAINT ma_tx_out_pkey PRIMARY KEY (id);


--
-- Name: meta meta_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.meta
    ADD CONSTRAINT meta_pkey PRIMARY KEY (id);


--
-- Name: multi_asset multi_asset_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.multi_asset
    ADD CONSTRAINT multi_asset_pkey PRIMARY KEY (id);


--
-- Name: param_proposal param_proposal_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.param_proposal
    ADD CONSTRAINT param_proposal_pkey PRIMARY KEY (id);


--
-- Name: pool_hash pool_hash_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_hash
    ADD CONSTRAINT pool_hash_pkey PRIMARY KEY (id);


--
-- Name: pool_metadata_ref pool_metadata_ref_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_data pool_offline_data_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pkey PRIMARY KEY (id);


--
-- Name: pool_owner pool_owner_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_owner
    ADD CONSTRAINT pool_owner_pkey PRIMARY KEY (id);


--
-- Name: pool_relay pool_relay_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_relay
    ADD CONSTRAINT pool_relay_pkey PRIMARY KEY (id);


--
-- Name: pool_retire pool_retire_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_retire
    ADD CONSTRAINT pool_retire_pkey PRIMARY KEY (id);


--
-- Name: pool_stake pool_stake_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_stake
    ADD CONSTRAINT pool_stake_pkey PRIMARY KEY (id);


--
-- Name: pool_update pool_update_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT pool_update_pkey PRIMARY KEY (id);


--
-- Name: pot_transfer pot_transfer_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pot_transfer
    ADD CONSTRAINT pot_transfer_pkey PRIMARY KEY (id);


--
-- Name: redeemer_data redeemer_data_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer_data
    ADD CONSTRAINT redeemer_data_pkey PRIMARY KEY (id);


--
-- Name: redeemer redeemer_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer
    ADD CONSTRAINT redeemer_pkey PRIMARY KEY (id);


--
-- Name: reference_tx_in reference_tx_in_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reference_tx_in
    ADD CONSTRAINT reference_tx_in_pkey PRIMARY KEY (id);


--
-- Name: reserve reserve_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserve
    ADD CONSTRAINT reserve_pkey PRIMARY KEY (id);


--
-- Name: reserved_pool_ticker reserved_pool_ticker_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserved_pool_ticker
    ADD CONSTRAINT reserved_pool_ticker_pkey PRIMARY KEY (id);


--
-- Name: reward reward_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.schema_version
    ADD CONSTRAINT schema_version_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: slot_leader slot_leader_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.slot_leader
    ADD CONSTRAINT slot_leader_pkey PRIMARY KEY (id);


--
-- Name: stake_address stake_address_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_address
    ADD CONSTRAINT stake_address_pkey PRIMARY KEY (id);


--
-- Name: stake_deregistration stake_deregistration_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration
    ADD CONSTRAINT stake_deregistration_pkey PRIMARY KEY (id);


--
-- Name: stake_registration stake_registration_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_registration
    ADD CONSTRAINT stake_registration_pkey PRIMARY KEY (id);


--
-- Name: treasury treasury_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.treasury
    ADD CONSTRAINT treasury_pkey PRIMARY KEY (id);


--
-- Name: tx_in tx_in_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in
    ADD CONSTRAINT tx_in_pkey PRIMARY KEY (id);


--
-- Name: tx_metadata tx_metadata_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_metadata
    ADD CONSTRAINT tx_metadata_pkey PRIMARY KEY (id);


--
-- Name: tx_out tx_out_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT tx_out_pkey PRIMARY KEY (id);


--
-- Name: tx tx_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx
    ADD CONSTRAINT tx_pkey PRIMARY KEY (id);


--
-- Name: ada_pots uk_143qflkqxvmvp4cukodhskt43; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ada_pots
    ADD CONSTRAINT uk_143qflkqxvmvp4cukodhskt43 UNIQUE (block_id);


--
-- Name: unconsume_tx_in unconsume_tx_in_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.unconsume_tx_in
    ADD CONSTRAINT unconsume_tx_in_pkey PRIMARY KEY (id);


--
-- Name: pool_stake uni_pool_id; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_stake
    ADD CONSTRAINT uni_pool_id UNIQUE (pool_id);


--
-- Name: ada_pots unique_ada_pots; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ada_pots
    ADD CONSTRAINT unique_ada_pots UNIQUE (block_id);


--
-- Name: block unique_block; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.block
    ADD CONSTRAINT unique_block UNIQUE (hash);


--
-- Name: failed_tx_out unique_col_failed_txout; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT unique_col_failed_txout UNIQUE (tx_id, index);


--
-- Name: unconsume_tx_in unique_col_txin; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.unconsume_tx_in
    ADD CONSTRAINT unique_col_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: cost_model unique_cost_model; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.cost_model
    ADD CONSTRAINT unique_cost_model UNIQUE (hash);


--
-- Name: datum unique_datum; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.datum
    ADD CONSTRAINT unique_datum UNIQUE (hash);


--
-- Name: delegation unique_delegation; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT unique_delegation UNIQUE (tx_id, cert_index);


--
-- Name: delisted_pool unique_delisted_pool; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delisted_pool
    ADD CONSTRAINT unique_delisted_pool UNIQUE (hash_raw);


--
-- Name: epoch unique_epoch; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch
    ADD CONSTRAINT unique_epoch UNIQUE (no);


--
-- Name: epoch_param unique_epoch_param; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_param
    ADD CONSTRAINT unique_epoch_param UNIQUE (epoch_no, block_id);


--
-- Name: epoch_sync_time unique_epoch_sync_time; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_sync_time
    ADD CONSTRAINT unique_epoch_sync_time UNIQUE (no);


--
-- Name: ma_tx_mint unique_ma_tx_mint; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_mint
    ADD CONSTRAINT unique_ma_tx_mint UNIQUE (ident, tx_id);


--
-- Name: ma_tx_out unique_ma_tx_out; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_out
    ADD CONSTRAINT unique_ma_tx_out UNIQUE (ident, tx_out_id);


--
-- Name: meta unique_meta; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.meta
    ADD CONSTRAINT unique_meta UNIQUE (start_time);


--
-- Name: multi_asset unique_multi_asset; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.multi_asset
    ADD CONSTRAINT unique_multi_asset UNIQUE (policy, name);


--
-- Name: param_proposal unique_param_proposal; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.param_proposal
    ADD CONSTRAINT unique_param_proposal UNIQUE (key, registered_tx_id);


--
-- Name: pool_hash unique_pool_hash; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_hash
    ADD CONSTRAINT unique_pool_hash UNIQUE (hash_raw);


--
-- Name: pool_metadata_ref unique_pool_metadata_ref; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_metadata_ref
    ADD CONSTRAINT unique_pool_metadata_ref UNIQUE (pool_id, url, hash);


--
-- Name: pool_offline_data unique_pool_offline_data; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_data
    ADD CONSTRAINT unique_pool_offline_data UNIQUE (pool_id, hash);


--
-- Name: pool_offline_fetch_error unique_pool_offline_fetch_error; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_fetch_error
    ADD CONSTRAINT unique_pool_offline_fetch_error UNIQUE (pool_id, fetch_time, retry_count);


--
-- Name: pool_owner unique_pool_owner; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_owner
    ADD CONSTRAINT unique_pool_owner UNIQUE (addr_id, pool_update_id);


--
-- Name: pool_relay unique_pool_relay; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_relay
    ADD CONSTRAINT unique_pool_relay UNIQUE (update_id, ipv4, ipv6, dns_name);


--
-- Name: pool_retire unique_pool_retiring; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_retire
    ADD CONSTRAINT unique_pool_retiring UNIQUE (announced_tx_id, cert_index);


--
-- Name: pool_update unique_pool_update; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT unique_pool_update UNIQUE (registered_tx_id, cert_index);


--
-- Name: pot_transfer unique_pot_transfer; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pot_transfer
    ADD CONSTRAINT unique_pot_transfer UNIQUE (tx_id, cert_index);


--
-- Name: redeemer unique_redeemer; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer
    ADD CONSTRAINT unique_redeemer UNIQUE (tx_id, purpose, index);


--
-- Name: redeemer_data unique_redeemer_data; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer_data
    ADD CONSTRAINT unique_redeemer_data UNIQUE (hash);


--
-- Name: reference_tx_in unique_ref_txin; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reference_tx_in
    ADD CONSTRAINT unique_ref_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: reserved_pool_ticker unique_reserved_pool_ticker; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserved_pool_ticker
    ADD CONSTRAINT unique_reserved_pool_ticker UNIQUE (name);


--
-- Name: reserve unique_reserves; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserve
    ADD CONSTRAINT unique_reserves UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: reward unique_reward; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);


--
-- Name: script unique_script; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.script
    ADD CONSTRAINT unique_script UNIQUE (hash);


--
-- Name: slot_leader unique_slot_leader; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.slot_leader
    ADD CONSTRAINT unique_slot_leader UNIQUE (hash);


--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);


--
-- Name: stake_address unique_stake_address; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_address
    ADD CONSTRAINT unique_stake_address UNIQUE (hash_raw);


--
-- Name: stake_deregistration unique_stake_deregistration; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration
    ADD CONSTRAINT unique_stake_deregistration UNIQUE (tx_id, cert_index);


--
-- Name: stake_registration unique_stake_registration; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_registration
    ADD CONSTRAINT unique_stake_registration UNIQUE (tx_id, cert_index);


--
-- Name: treasury unique_treasury; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.treasury
    ADD CONSTRAINT unique_treasury UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: tx unique_tx; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx
    ADD CONSTRAINT unique_tx UNIQUE (hash);


--
-- Name: tx_metadata unique_tx_metadata; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_metadata
    ADD CONSTRAINT unique_tx_metadata UNIQUE (key, tx_id);


--
-- Name: tx_in unique_txin; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in
    ADD CONSTRAINT unique_txin UNIQUE (tx_out_id, tx_out_index);


--
-- Name: tx_out unique_txout; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT unique_txout UNIQUE (tx_id, index);


--
-- Name: extra_key_witness unique_witness; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.extra_key_witness
    ADD CONSTRAINT unique_witness UNIQUE (hash);


--
-- Name: withdrawal withdrawal_pkey; Type: CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id);


--
-- Name: ada_pots ada_pots_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ada_pots
    ADD CONSTRAINT ada_pots_block_id_fkey FOREIGN KEY (block_id) REFERENCES preprod.block(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES preprod.datum(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES preprod.script(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: cost_model cost_model_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.cost_model
    ADD CONSTRAINT cost_model_block_id_fkey FOREIGN KEY (block_id) REFERENCES preprod.block(id) ON DELETE CASCADE;


--
-- Name: datum datum_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.datum
    ADD CONSTRAINT datum_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT delegation_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT delegation_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT delegation_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES preprod.redeemer(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.delegation
    ADD CONSTRAINT delegation_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_param
    ADD CONSTRAINT epoch_param_block_id_fkey FOREIGN KEY (block_id) REFERENCES preprod.block(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_param
    ADD CONSTRAINT epoch_param_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES preprod.cost_model(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_stake
    ADD CONSTRAINT epoch_stake_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.epoch_stake
    ADD CONSTRAINT epoch_stake_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: extra_key_witness extra_key_witness_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.extra_key_witness
    ADD CONSTRAINT extra_key_witness_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out failed_tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.failed_tx_out
    ADD CONSTRAINT failed_tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: slot_leader fk23db3v8n6dl1gf0njxgpceviq; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.slot_leader
    ADD CONSTRAINT fk23db3v8n6dl1gf0njxgpceviq FOREIGN KEY (pool_hash_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: block fk6gd608i8qbyert1hlfl9be71h; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.block
    ADD CONSTRAINT fk6gd608i8qbyert1hlfl9be71h FOREIGN KEY (previous_id) REFERENCES preprod.block(id) ON DELETE CASCADE;


--
-- Name: block fke45qmlh5if6ghcyj0mhumjhk1; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.block
    ADD CONSTRAINT fke45qmlh5if6ghcyj0mhumjhk1 FOREIGN KEY (slot_leader_id) REFERENCES preprod.slot_leader(id) ON DELETE CASCADE;


--
-- Name: pool_stake fknipbg0qr651vyttfseohc94ql; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_stake
    ADD CONSTRAINT fknipbg0qr651vyttfseohc94ql FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id);


--
-- Name: ma_tx_mint ma_tx_mint_ident_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_ident_fkey FOREIGN KEY (ident) REFERENCES preprod.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_ident_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_out
    ADD CONSTRAINT ma_tx_out_ident_fkey FOREIGN KEY (ident) REFERENCES preprod.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.ma_tx_out
    ADD CONSTRAINT ma_tx_out_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES preprod.tx_out(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.param_proposal
    ADD CONSTRAINT param_proposal_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES preprod.cost_model(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.param_proposal
    ADD CONSTRAINT param_proposal_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pmr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES preprod.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pmr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES preprod.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_owner
    ADD CONSTRAINT pool_owner_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_pool_update_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_owner
    ADD CONSTRAINT pool_owner_pool_update_id_fkey FOREIGN KEY (pool_update_id) REFERENCES preprod.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_relay pool_relay_update_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_relay
    ADD CONSTRAINT pool_relay_update_id_fkey FOREIGN KEY (update_id) REFERENCES preprod.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_announced_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_retire
    ADD CONSTRAINT pool_retire_announced_tx_id_fkey FOREIGN KEY (announced_tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_retire
    ADD CONSTRAINT pool_retire_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT pool_update_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_meta_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT pool_update_meta_id_fkey FOREIGN KEY (meta_id) REFERENCES preprod.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT pool_update_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pool_update
    ADD CONSTRAINT pool_update_reward_addr_id_fkey FOREIGN KEY (reward_addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: pot_transfer pot_transfer_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.pot_transfer
    ADD CONSTRAINT pot_transfer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer_data redeemer_data_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer_data
    ADD CONSTRAINT redeemer_data_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_redeemer_data_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer
    ADD CONSTRAINT redeemer_redeemer_data_id_fkey FOREIGN KEY (redeemer_data_id) REFERENCES preprod.redeemer_data(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.redeemer
    ADD CONSTRAINT redeemer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserve
    ADD CONSTRAINT reserve_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reserve
    ADD CONSTRAINT reserve_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: reward reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reward
    ADD CONSTRAINT reward_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: reward reward_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.reward
    ADD CONSTRAINT reward_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES preprod.pool_hash(id) ON DELETE CASCADE;


--
-- Name: script script_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.script
    ADD CONSTRAINT script_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: stake_address stake_address_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_address
    ADD CONSTRAINT stake_address_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration
    ADD CONSTRAINT stake_deregistration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration
    ADD CONSTRAINT stake_deregistration_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES preprod.redeemer(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_deregistration
    ADD CONSTRAINT stake_deregistration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_registration
    ADD CONSTRAINT stake_registration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.stake_registration
    ADD CONSTRAINT stake_registration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.treasury
    ADD CONSTRAINT treasury_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.treasury
    ADD CONSTRAINT treasury_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: tx tx_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx
    ADD CONSTRAINT tx_block_id_fkey FOREIGN KEY (block_id) REFERENCES preprod.block(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in
    ADD CONSTRAINT tx_in_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES preprod.redeemer(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in
    ADD CONSTRAINT tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_in
    ADD CONSTRAINT tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: tx_metadata tx_metadata_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_metadata
    ADD CONSTRAINT tx_metadata_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES preprod.datum(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES preprod.script(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.tx_out
    ADD CONSTRAINT tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: unconsume_tx_in unsconume_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.unconsume_tx_in
    ADD CONSTRAINT unsconume_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: unconsume_tx_in unsconume_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.unconsume_tx_in
    ADD CONSTRAINT unsconume_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.withdrawal
    ADD CONSTRAINT withdrawal_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES preprod.stake_address(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.withdrawal
    ADD CONSTRAINT withdrawal_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES preprod.redeemer(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod; Owner: cardano-master
--

ALTER TABLE ONLY preprod.withdrawal
    ADD CONSTRAINT withdrawal_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES preprod.tx(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

