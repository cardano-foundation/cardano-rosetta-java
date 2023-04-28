--liquibase formatted sql
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

--TODO This is just a dummy table inserted from another project. Please replace accordingly
--changeset cf:1
create table "metadata" (
	"subject" varchar(255) primary key,
	"policy" text,
  "name" varchar(255),
  "ticker" varchar(32),
  "url" varchar(255),
  "description" text,
  "decimals" integer,
	"updated" timestamp,
	"updated_by" varchar(255),
  "properties" jsonb
);
--rollback drop table "metadata" cascade;

create index "idx_metadata_defaultfields" on "metadata"("subject", "policy", "name", "ticker", "url", "description", "decimals", "updated" desc, "updated_by");
--rollback drop index "idx_metadata_defaultfields";

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.1
-- Dumped by pg_dump version 14.6 (Ubuntu 14.6-1.pgdg20.04+1)
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: cardano-master
--

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ada_pots; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.ada_pots (
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


ALTER TABLE public.ada_pots OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ada_pots_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.ada_pots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ada_pots_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ada_pots_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.ada_pots_id_seq OWNED BY public.ada_pots.id;


--
-- Name: block; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.block (
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


ALTER TABLE public.block OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: block_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.block_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: block_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.block_id_seq OWNED BY public.block.id;


--
-- Name: cost_model; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.cost_model (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   costs character varying(65535) NOT NULL,
                                   hash character varying(64) NOT NULL,
                                   block_id bigint NOT NULL
);


ALTER TABLE public.cost_model OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: cost_model_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.cost_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cost_model_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: cost_model_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.cost_model_id_seq OWNED BY public.cost_model.id;


--
-- Name: datum; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.datum (
                              id bigint NOT NULL,
                              is_deleted boolean DEFAULT false,
                              bytes bytea,
                              hash character varying(64) NOT NULL,
                              value character varying(65535),
                              tx_id bigint NOT NULL
);


ALTER TABLE public.datum OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: datum_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.datum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.datum_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: datum_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.datum_id_seq OWNED BY public.datum.id;


--
-- Name: delegation; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.delegation (
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


ALTER TABLE public.delegation OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delegation_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.delegation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delegation_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delegation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.delegation_id_seq OWNED BY public.delegation.id;


--
-- Name: delisted_pool; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.delisted_pool (
                                      id bigint NOT NULL,
                                      is_deleted boolean DEFAULT false,
                                      hash_raw character varying(56) NOT NULL
);


ALTER TABLE public.delisted_pool OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.delisted_pool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delisted_pool_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.delisted_pool_id_seq OWNED BY public.delisted_pool.id;


--
-- Name: epoch; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.epoch (
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


ALTER TABLE public.epoch OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.epoch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.epoch_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.epoch_id_seq OWNED BY public.epoch.id;


--
-- Name: epoch_param; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.epoch_param (
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


ALTER TABLE public.epoch_param OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_param_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.epoch_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.epoch_param_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_param_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.epoch_param_id_seq OWNED BY public.epoch_param.id;


--
-- Name: epoch_stake; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.epoch_stake (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    amount numeric(20,0) NOT NULL,
                                    epoch_no integer NOT NULL,
                                    addr_id bigint NOT NULL,
                                    pool_id bigint NOT NULL
);


ALTER TABLE public.epoch_stake OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.epoch_stake_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.epoch_stake_id_seq OWNED BY public.epoch_stake.id;


--
-- Name: epoch_sync_time; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.epoch_sync_time (
                                        id bigint NOT NULL,
                                        is_deleted boolean DEFAULT false,
                                        no bigint NOT NULL,
                                        seconds bigint NOT NULL,
                                        state character varying(255) NOT NULL
);


ALTER TABLE public.epoch_sync_time OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.epoch_sync_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.epoch_sync_time_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.epoch_sync_time_id_seq OWNED BY public.epoch_sync_time.id;


--
-- Name: extra_key_witness; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.extra_key_witness (
                                          id bigint NOT NULL,
                                          is_deleted boolean DEFAULT false,
                                          hash character varying(56) NOT NULL,
                                          tx_id bigint NOT NULL
);


ALTER TABLE public.extra_key_witness OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.extra_key_witness_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.extra_key_witness_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.extra_key_witness_id_seq OWNED BY public.extra_key_witness.id;

--
-- Name: failed_tx_out; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.failed_tx_out (
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


ALTER TABLE public.failed_tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.failed_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.failed_tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.failed_tx_out_id_seq OWNED BY public.failed_tx_out.id;


--
-- Name: ma_tx_mint; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.ma_tx_mint (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   quantity numeric(20,0) NOT NULL,
                                   ident bigint NOT NULL,
                                   tx_id bigint NOT NULL
);


ALTER TABLE public.ma_tx_mint OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.ma_tx_mint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ma_tx_mint_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.ma_tx_mint_id_seq OWNED BY public.ma_tx_mint.id;


--
-- Name: ma_tx_out; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.ma_tx_out (
                                  id bigint NOT NULL,
                                  is_deleted boolean DEFAULT false,
                                  quantity numeric(20,0) NOT NULL,
                                  ident bigint NOT NULL,
                                  tx_out_id bigint NOT NULL
);


ALTER TABLE public.ma_tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.ma_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ma_tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.ma_tx_out_id_seq OWNED BY public.ma_tx_out.id;


--
-- Name: meta; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.meta (
                             id bigint NOT NULL,
                             is_deleted boolean DEFAULT false,
                             network_name character varying(255) NOT NULL,
                             start_time timestamp without time zone NOT NULL,
                             version character varying(255) NOT NULL
);


ALTER TABLE public.meta OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: meta_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.meta_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: meta_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.meta_id_seq OWNED BY public.meta.id;


--
-- Name: multi_asset; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.multi_asset (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    fingerprint character varying(255) NOT NULL,
                                    name bytea NOT NULL,
                                    policy character varying(56) NOT NULL,
                                    supply numeric(23,0),
                                    tx_count bigint,
                                    "time" timestamp without time zone
);


ALTER TABLE public.multi_asset OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: multi_asset_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.multi_asset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.multi_asset_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: multi_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.multi_asset_id_seq OWNED BY public.multi_asset.id;


--
-- Name: param_proposal; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.param_proposal (
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


ALTER TABLE public.param_proposal OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: param_proposal_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.param_proposal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.param_proposal_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: param_proposal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.param_proposal_id_seq OWNED BY public.param_proposal.id;


--
-- Name: pool_hash; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_hash (
                                  id bigint NOT NULL,
                                  is_deleted boolean DEFAULT false,
                                  hash_raw character varying(56) NOT NULL,
                                  pool_size numeric(20,0) NOT NULL,
                                  epoch_no integer NOT NULL,
                                  view character varying(255) NOT NULL
);


ALTER TABLE public.pool_hash OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_hash_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_hash_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_hash_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_hash_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_hash_id_seq OWNED BY public.pool_hash.id;


--
-- Name: pool_metadata_ref; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_metadata_ref (
                                          id bigint NOT NULL,
                                          is_deleted boolean DEFAULT false,
                                          hash character varying(64) NOT NULL,
                                          url character varying(255) NOT NULL,
                                          pool_id bigint NOT NULL,
                                          registered_tx_id bigint NOT NULL
);


ALTER TABLE public.pool_metadata_ref OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_metadata_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_metadata_ref_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_metadata_ref_id_seq OWNED BY public.pool_metadata_ref.id;


--
-- Name: pool_offline_data; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_offline_data (
                                          id bigint NOT NULL,
                                          is_deleted boolean DEFAULT false,
                                          bytes bytea,
                                          hash character varying(64) NOT NULL,
                                          json character varying(65535) NOT NULL,
                                          ticker_name character varying(255) NOT NULL,
                                          pool_id bigint NOT NULL,
                                          pmr_id bigint NOT NULL
);


ALTER TABLE public.pool_offline_data OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_offline_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_offline_data_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_offline_data_id_seq OWNED BY public.pool_offline_data.id;


--
-- Name: pool_offline_fetch_error; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_offline_fetch_error (
                                                 id bigint NOT NULL,
                                                 is_deleted boolean DEFAULT false,
                                                 fetch_error character varying(65535) NOT NULL,
                                                 fetch_time timestamp without time zone NOT NULL,
                                                 retry_count integer NOT NULL,
                                                 pool_id bigint NOT NULL,
                                                 pmr_id bigint NOT NULL
);


ALTER TABLE public.pool_offline_fetch_error OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_offline_fetch_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_offline_fetch_error_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_offline_fetch_error_id_seq OWNED BY public.pool_offline_fetch_error.id;


--
-- Name: pool_owner; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_owner (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   pool_update_id bigint NOT NULL,
                                   addr_id bigint NOT NULL
);


ALTER TABLE public.pool_owner OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_owner_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_owner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_owner_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_owner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_owner_id_seq OWNED BY public.pool_owner.id;


--
-- Name: pool_relay; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_relay (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   dns_name character varying(255),
                                   dns_srv_name character varying(255),
                                   ipv4 character varying(255),
                                   ipv6 character varying(255),
                                   port integer,
                                   update_id bigint NOT NULL
);


ALTER TABLE public.pool_relay OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_relay_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_relay_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_relay_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_relay_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_relay_id_seq OWNED BY public.pool_relay.id;


--
-- Name: pool_retire; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_retire (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    cert_index integer NOT NULL,
                                    retiring_epoch integer NOT NULL,
                                    announced_tx_id bigint NOT NULL,
                                    hash_id bigint NOT NULL
);


ALTER TABLE public.pool_retire OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_retire_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_retire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_retire_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_retire_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_retire_id_seq OWNED BY public.pool_retire.id;


--
-- Name: pool_stake; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_stake (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   amount numeric(19,2),
                                   pool_id bigint
);


ALTER TABLE public.pool_stake OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_stake_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_stake_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_stake_id_seq OWNED BY public.pool_stake.id;


--
-- Name: pool_update; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pool_update (
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


ALTER TABLE public.pool_update OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_update_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pool_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pool_update_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_update_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pool_update_id_seq OWNED BY public.pool_update.id;


--
-- Name: pot_transfer; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.pot_transfer (
                                     id bigint NOT NULL,
                                     is_deleted boolean DEFAULT false,
                                     cert_index integer NOT NULL,
                                     reserves numeric(20,0) NOT NULL,
                                     treasury numeric(20,0) NOT NULL,
                                     tx_id bigint NOT NULL
);


ALTER TABLE public.pot_transfer OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.pot_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pot_transfer_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.pot_transfer_id_seq OWNED BY public.pot_transfer.id;


--
-- Name: redeemer; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.redeemer (
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


ALTER TABLE public.redeemer OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.redeemer_data (
                                      id bigint NOT NULL,
                                      is_deleted boolean DEFAULT false,
                                      bytes bytea,
                                      hash character varying(64) NOT NULL,
                                      value character varying(65535),
                                      tx_id bigint NOT NULL
);


ALTER TABLE public.redeemer_data OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.redeemer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.redeemer_data_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.redeemer_data_id_seq OWNED BY public.redeemer_data.id;


--
-- Name: redeemer_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.redeemer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.redeemer_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.redeemer_id_seq OWNED BY public.redeemer.id;


--
-- Name: reference_tx_in; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.reference_tx_in (
                                        id bigint NOT NULL,
                                        is_deleted boolean DEFAULT false,
                                        tx_out_index smallint NOT NULL,
                                        tx_in_id bigint NOT NULL,
                                        tx_out_id bigint NOT NULL
);


ALTER TABLE public.reference_tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.reference_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reference_tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.reference_tx_in_id_seq OWNED BY public.reference_tx_in.id;


--
-- Name: reserve; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.reserve (
                                id bigint NOT NULL,
                                is_deleted boolean DEFAULT false,
                                amount numeric(20,0) NOT NULL,
                                cert_index integer NOT NULL,
                                addr_id bigint NOT NULL,
                                tx_id bigint NOT NULL
);


ALTER TABLE public.reserve OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserve_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.reserve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reserve_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserve_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.reserve_id_seq OWNED BY public.reserve.id;


--
-- Name: reserved_pool_ticker; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.reserved_pool_ticker (
                                             id bigint NOT NULL,
                                             is_deleted boolean DEFAULT false,
                                             name character varying(255) NOT NULL,
                                             pool_hash character varying(56) NOT NULL
);


ALTER TABLE public.reserved_pool_ticker OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.reserved_pool_ticker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reserved_pool_ticker_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.reserved_pool_ticker_id_seq OWNED BY public.reserved_pool_ticker.id;


--
-- Name: reward; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.reward (
                               id bigint NOT NULL,
                               is_deleted boolean DEFAULT false,
                               amount numeric(20,0) NOT NULL,
                               earned_epoch bigint NOT NULL,
                               spendable_epoch bigint NOT NULL,
                               type character varying(255) NOT NULL,
                               addr_id bigint NOT NULL,
                               pool_id bigint
);


ALTER TABLE public.reward OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reward_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reward_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reward_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.reward_id_seq OWNED BY public.reward.id;


--
-- Name: schema_version; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.schema_version (
                                       id bigint NOT NULL,
                                       is_deleted boolean DEFAULT false,
                                       stage_one bigint NOT NULL,
                                       stage_three bigint NOT NULL,
                                       stage_two bigint NOT NULL
);


ALTER TABLE public.schema_version OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: schema_version_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.schema_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.schema_version_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: schema_version_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.schema_version_id_seq OWNED BY public.schema_version.id;


--
-- Name: script; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.script (
                               id bigint NOT NULL,
                               is_deleted boolean DEFAULT false,
                               bytes bytea,
                               hash character varying(64) NOT NULL,
                               json character varying(65535),
                               serialised_size integer,
                               type character varying(255) NOT NULL,
                               tx_id bigint NOT NULL
);


ALTER TABLE public.script OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: script_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.script_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.script_id_seq OWNED BY public.script.id;


--
-- Name: slot_leader; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.slot_leader (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    description character varying(65535) NOT NULL,
                                    hash character varying(56) NOT NULL,
                                    pool_hash_id bigint
);


ALTER TABLE public.slot_leader OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: slot_leader_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.slot_leader_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.slot_leader_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: slot_leader_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.slot_leader_id_seq OWNED BY public.slot_leader.id;


--
-- Name: stake_address; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.stake_address (
                                      id bigint NOT NULL,
                                      is_deleted boolean DEFAULT false,
                                      hash_raw character varying(255) NOT NULL,
                                      script_hash character varying(56),
                                      view character varying(65535) NOT NULL,
                                      tx_id bigint NOT NULL
);


ALTER TABLE public.stake_address OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_address_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.stake_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stake_address_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.stake_address_id_seq OWNED BY public.stake_address.id;


--
-- Name: stake_deregistration; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.stake_deregistration (
                                             id bigint NOT NULL,
                                             is_deleted boolean DEFAULT false,
                                             cert_index integer NOT NULL,
                                             epoch_no integer NOT NULL,
                                             addr_id bigint NOT NULL,
                                             redeemer_id bigint,
                                             tx_id bigint NOT NULL
);


ALTER TABLE public.stake_deregistration OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.stake_deregistration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stake_deregistration_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.stake_deregistration_id_seq OWNED BY public.stake_deregistration.id;


--
-- Name: stake_registration; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.stake_registration (
                                           id bigint NOT NULL,
                                           is_deleted boolean DEFAULT false,
                                           cert_index integer NOT NULL,
                                           epoch_no integer NOT NULL,
                                           addr_id bigint NOT NULL,
                                           tx_id bigint NOT NULL
);


ALTER TABLE public.stake_registration OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_registration_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.stake_registration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stake_registration_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.stake_registration_id_seq OWNED BY public.stake_registration.id;


--
-- Name: treasury; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.treasury (
                                 id bigint NOT NULL,
                                 is_deleted boolean DEFAULT false,
                                 amount numeric(20,0) NOT NULL,
                                 cert_index integer NOT NULL,
                                 addr_id bigint NOT NULL,
                                 tx_id bigint NOT NULL
);


ALTER TABLE public.treasury OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: treasury_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.treasury_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.treasury_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: treasury_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.treasury_id_seq OWNED BY public.treasury.id;


--
-- Name: tx; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.tx (
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


ALTER TABLE public.tx OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.tx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tx_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.tx_id_seq OWNED BY public.tx.id;


--
-- Name: tx_in; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.tx_in (
                              id bigint NOT NULL,
                              is_deleted boolean DEFAULT false,
                              tx_in_id bigint,
                              tx_out_index smallint NOT NULL,
                              tx_out_id bigint,
                              redeemer_id bigint
);


ALTER TABLE public.tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_in_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.tx_in_id_seq OWNED BY public.tx_in.id;


--
-- Name: tx_metadata; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.tx_metadata (
                                    id bigint NOT NULL,
                                    is_deleted boolean DEFAULT false,
                                    bytes bytea,
                                    json character varying(65535),
                                    key numeric(20,0) NOT NULL,
                                    tx_id bigint NOT NULL
);


ALTER TABLE public.tx_metadata OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.tx_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tx_metadata_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.tx_metadata_id_seq OWNED BY public.tx_metadata.id;


--
-- Name: tx_out; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.tx_out (
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


ALTER TABLE public.tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_out_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.tx_out_id_seq OWNED BY public.tx_out.id;


--
-- Name: unconsume_tx_in; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.unconsume_tx_in (
                                        id bigint NOT NULL,
                                        is_deleted boolean DEFAULT false,
                                        tx_out_index smallint NOT NULL,
                                        tx_in_id bigint NOT NULL,
                                        tx_out_id bigint NOT NULL
);


ALTER TABLE public.unconsume_tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.unconsume_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.unconsume_tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.unconsume_tx_in_id_seq OWNED BY public.unconsume_tx_in.id;


--
-- Name: withdrawal; Type: TABLE; Schema: public; Owner: cardano-master
--

CREATE TABLE public.withdrawal (
                                   id bigint NOT NULL,
                                   is_deleted boolean DEFAULT false,
                                   amount numeric(20,0) NOT NULL,
                                   addr_id bigint NOT NULL,
                                   redeemer_id bigint,
                                   tx_id bigint NOT NULL
);


ALTER TABLE public.withdrawal OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: withdrawal_id_seq; Type: SEQUENCE; Schema: public; Owner: cardano-master
--

CREATE SEQUENCE public.withdrawal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.withdrawal_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: withdrawal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cardano-master
--

ALTER SEQUENCE public.withdrawal_id_seq OWNED BY public.withdrawal.id;


--
-- Name: ada_pots id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ada_pots ALTER COLUMN id SET DEFAULT nextval('public.ada_pots_id_seq'::regclass);


--
-- Name: block id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.block ALTER COLUMN id SET DEFAULT nextval('public.block_id_seq'::regclass);


--
-- Name: cost_model id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.cost_model ALTER COLUMN id SET DEFAULT nextval('public.cost_model_id_seq'::regclass);


--
-- Name: datum id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.datum ALTER COLUMN id SET DEFAULT nextval('public.datum_id_seq'::regclass);


--
-- Name: delegation id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation ALTER COLUMN id SET DEFAULT nextval('public.delegation_id_seq'::regclass);


--
-- Name: delisted_pool id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delisted_pool ALTER COLUMN id SET DEFAULT nextval('public.delisted_pool_id_seq'::regclass);


--
-- Name: epoch id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch ALTER COLUMN id SET DEFAULT nextval('public.epoch_id_seq'::regclass);


--
-- Name: epoch_param id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_param ALTER COLUMN id SET DEFAULT nextval('public.epoch_param_id_seq'::regclass);


--
-- Name: epoch_stake id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_stake ALTER COLUMN id SET DEFAULT nextval('public.epoch_stake_id_seq'::regclass);


--
-- Name: epoch_sync_time id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_sync_time ALTER COLUMN id SET DEFAULT nextval('public.epoch_sync_time_id_seq'::regclass);


--
-- Name: extra_key_witness id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.extra_key_witness ALTER COLUMN id SET DEFAULT nextval('public.extra_key_witness_id_seq'::regclass);


--
-- Name: failed_tx_out id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out ALTER COLUMN id SET DEFAULT nextval('public.failed_tx_out_id_seq'::regclass);


--
-- Name: ma_tx_mint id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_mint ALTER COLUMN id SET DEFAULT nextval('public.ma_tx_mint_id_seq'::regclass);


--
-- Name: ma_tx_out id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_out ALTER COLUMN id SET DEFAULT nextval('public.ma_tx_out_id_seq'::regclass);


--
-- Name: meta id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.meta ALTER COLUMN id SET DEFAULT nextval('public.meta_id_seq'::regclass);


--
-- Name: multi_asset id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.multi_asset ALTER COLUMN id SET DEFAULT nextval('public.multi_asset_id_seq'::regclass);


--
-- Name: param_proposal id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.param_proposal ALTER COLUMN id SET DEFAULT nextval('public.param_proposal_id_seq'::regclass);


--
-- Name: pool_hash id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_hash ALTER COLUMN id SET DEFAULT nextval('public.pool_hash_id_seq'::regclass);


--
-- Name: pool_metadata_ref id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_metadata_ref ALTER COLUMN id SET DEFAULT nextval('public.pool_metadata_ref_id_seq'::regclass);


--
-- Name: pool_offline_data id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_data ALTER COLUMN id SET DEFAULT nextval('public.pool_offline_data_id_seq'::regclass);


--
-- Name: pool_offline_fetch_error id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_fetch_error ALTER COLUMN id SET DEFAULT nextval('public.pool_offline_fetch_error_id_seq'::regclass);


--
-- Name: pool_owner id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_owner ALTER COLUMN id SET DEFAULT nextval('public.pool_owner_id_seq'::regclass);


--
-- Name: pool_relay id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_relay ALTER COLUMN id SET DEFAULT nextval('public.pool_relay_id_seq'::regclass);


--
-- Name: pool_retire id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_retire ALTER COLUMN id SET DEFAULT nextval('public.pool_retire_id_seq'::regclass);


--
-- Name: pool_stake id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_stake ALTER COLUMN id SET DEFAULT nextval('public.pool_stake_id_seq'::regclass);


--
-- Name: pool_update id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update ALTER COLUMN id SET DEFAULT nextval('public.pool_update_id_seq'::regclass);


--
-- Name: pot_transfer id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pot_transfer ALTER COLUMN id SET DEFAULT nextval('public.pot_transfer_id_seq'::regclass);


--
-- Name: redeemer id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer ALTER COLUMN id SET DEFAULT nextval('public.redeemer_id_seq'::regclass);


--
-- Name: redeemer_data id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer_data ALTER COLUMN id SET DEFAULT nextval('public.redeemer_data_id_seq'::regclass);


--
-- Name: reference_tx_in id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reference_tx_in ALTER COLUMN id SET DEFAULT nextval('public.reference_tx_in_id_seq'::regclass);


--
-- Name: reserve id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserve ALTER COLUMN id SET DEFAULT nextval('public.reserve_id_seq'::regclass);


--
-- Name: reserved_pool_ticker id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserved_pool_ticker ALTER COLUMN id SET DEFAULT nextval('public.reserved_pool_ticker_id_seq'::regclass);


--
-- Name: reward id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reward ALTER COLUMN id SET DEFAULT nextval('public.reward_id_seq'::regclass);


--
-- Name: schema_version id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.schema_version ALTER COLUMN id SET DEFAULT nextval('public.schema_version_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.script ALTER COLUMN id SET DEFAULT nextval('public.script_id_seq'::regclass);


--
-- Name: slot_leader id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.slot_leader ALTER COLUMN id SET DEFAULT nextval('public.slot_leader_id_seq'::regclass);


--
-- Name: stake_address id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_address ALTER COLUMN id SET DEFAULT nextval('public.stake_address_id_seq'::regclass);


--
-- Name: stake_deregistration id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration ALTER COLUMN id SET DEFAULT nextval('public.stake_deregistration_id_seq'::regclass);


--
-- Name: stake_registration id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_registration ALTER COLUMN id SET DEFAULT nextval('public.stake_registration_id_seq'::regclass);


--
-- Name: treasury id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.treasury ALTER COLUMN id SET DEFAULT nextval('public.treasury_id_seq'::regclass);


--
-- Name: tx id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx ALTER COLUMN id SET DEFAULT nextval('public.tx_id_seq'::regclass);


--
-- Name: tx_in id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in ALTER COLUMN id SET DEFAULT nextval('public.tx_in_id_seq'::regclass);


--
-- Name: tx_metadata id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_metadata ALTER COLUMN id SET DEFAULT nextval('public.tx_metadata_id_seq'::regclass);


--
-- Name: tx_out id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out ALTER COLUMN id SET DEFAULT nextval('public.tx_out_id_seq'::regclass);


--
-- Name: unconsume_tx_in id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.unconsume_tx_in ALTER COLUMN id SET DEFAULT nextval('public.unconsume_tx_in_id_seq'::regclass);


--
-- Name: withdrawal id; Type: DEFAULT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.withdrawal ALTER COLUMN id SET DEFAULT nextval('public.withdrawal_id_seq'::regclass);

--
-- Name: ada_pots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.ada_pots_id_seq', 1, false);


--
-- Name: block_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.block_id_seq', 1, true);


--
-- Name: cost_model_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.cost_model_id_seq', 1, false);


--
-- Name: datum_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.datum_id_seq', 1, false);


--
-- Name: delegation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.delegation_id_seq', 1, false);


--
-- Name: delisted_pool_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.delisted_pool_id_seq', 1, false);


--
-- Name: epoch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.epoch_id_seq', 1, false);


--
-- Name: epoch_param_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.epoch_param_id_seq', 1, false);


--
-- Name: epoch_stake_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.epoch_stake_id_seq', 1, false);


--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.epoch_sync_time_id_seq', 1, false);


--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.extra_key_witness_id_seq', 1, false);


--
-- Name: failed_tx_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.failed_tx_out_id_seq', 1, false);


--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.ma_tx_mint_id_seq', 1, false);


--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.ma_tx_out_id_seq', 1, false);


--
-- Name: meta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.meta_id_seq', 1, false);


--
-- Name: multi_asset_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.multi_asset_id_seq', 1, false);


--
-- Name: param_proposal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.param_proposal_id_seq', 1, false);


--
-- Name: pool_hash_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_hash_id_seq', 1, false);


--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_metadata_ref_id_seq', 1, false);


--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_offline_data_id_seq', 1, false);


--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_offline_fetch_error_id_seq', 1, false);


--
-- Name: pool_owner_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_owner_id_seq', 1, false);


--
-- Name: pool_relay_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_relay_id_seq', 1, false);


--
-- Name: pool_retire_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_retire_id_seq', 1, false);


--
-- Name: pool_stake_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_stake_id_seq', 1, false);


--
-- Name: pool_update_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pool_update_id_seq', 1, false);


--
-- Name: pot_transfer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.pot_transfer_id_seq', 1, false);


--
-- Name: redeemer_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.redeemer_data_id_seq', 1, false);


--
-- Name: redeemer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.redeemer_id_seq', 1, false);


--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.reference_tx_in_id_seq', 1, false);


--
-- Name: reserve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.reserve_id_seq', 1, false);


--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.reserved_pool_ticker_id_seq', 1, false);


--
-- Name: reward_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.reward_id_seq', 1, false);


--
-- Name: schema_version_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.schema_version_id_seq', 1, false);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.script_id_seq', 1, false);


--
-- Name: slot_leader_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.slot_leader_id_seq', 1, true);


--
-- Name: stake_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.stake_address_id_seq', 1, false);


--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.stake_deregistration_id_seq', 1, false);


--
-- Name: stake_registration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.stake_registration_id_seq', 1, false);


--
-- Name: treasury_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.treasury_id_seq', 1, false);


--
-- Name: tx_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.tx_id_seq', 8, true);


--
-- Name: tx_in_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.tx_in_id_seq', 1, false);


--
-- Name: tx_metadata_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.tx_metadata_id_seq', 1, false);


--
-- Name: tx_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.tx_out_id_seq', 8, true);


--
-- Name: unconsume_tx_in_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.unconsume_tx_in_id_seq', 1, false);


--
-- Name: withdrawal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cardano-master
--

SELECT pg_catalog.setval('public.withdrawal_id_seq', 1, false);


--
-- Name: ada_pots ada_pots_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ada_pots
    ADD CONSTRAINT ada_pots_pkey PRIMARY KEY (id);


--
-- Name: block block_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.block
    ADD CONSTRAINT block_pkey PRIMARY KEY (id);


--
-- Name: cost_model cost_model_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.cost_model
    ADD CONSTRAINT cost_model_pkey PRIMARY KEY (id);


--
-- Name: datum datum_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.datum
    ADD CONSTRAINT datum_pkey PRIMARY KEY (id);


--
-- Name: delegation delegation_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);


--
-- Name: delisted_pool delisted_pool_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delisted_pool
    ADD CONSTRAINT delisted_pool_pkey PRIMARY KEY (id);


--
-- Name: epoch_param epoch_param_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_param
    ADD CONSTRAINT epoch_param_pkey PRIMARY KEY (id);


--
-- Name: epoch epoch_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch
    ADD CONSTRAINT epoch_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_sync_time epoch_sync_time_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_sync_time
    ADD CONSTRAINT epoch_sync_time_pkey PRIMARY KEY (id);


--
-- Name: extra_key_witness extra_key_witness_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.extra_key_witness
    ADD CONSTRAINT extra_key_witness_pkey PRIMARY KEY (id);


--
-- Name: failed_tx_out failed_tx_out_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT failed_tx_out_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_mint ma_tx_mint_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_out ma_tx_out_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_out
    ADD CONSTRAINT ma_tx_out_pkey PRIMARY KEY (id);


--
-- Name: meta meta_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.meta
    ADD CONSTRAINT meta_pkey PRIMARY KEY (id);


--
-- Name: multi_asset multi_asset_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.multi_asset
    ADD CONSTRAINT multi_asset_pkey PRIMARY KEY (id);


--
-- Name: param_proposal param_proposal_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.param_proposal
    ADD CONSTRAINT param_proposal_pkey PRIMARY KEY (id);


--
-- Name: pool_hash pool_hash_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_hash
    ADD CONSTRAINT pool_hash_pkey PRIMARY KEY (id);


--
-- Name: pool_metadata_ref pool_metadata_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_data pool_offline_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pkey PRIMARY KEY (id);


--
-- Name: pool_owner pool_owner_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_owner
    ADD CONSTRAINT pool_owner_pkey PRIMARY KEY (id);


--
-- Name: pool_relay pool_relay_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_relay
    ADD CONSTRAINT pool_relay_pkey PRIMARY KEY (id);


--
-- Name: pool_retire pool_retire_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_retire
    ADD CONSTRAINT pool_retire_pkey PRIMARY KEY (id);


--
-- Name: pool_stake pool_stake_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_stake
    ADD CONSTRAINT pool_stake_pkey PRIMARY KEY (id);


--
-- Name: pool_update pool_update_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT pool_update_pkey PRIMARY KEY (id);


--
-- Name: pot_transfer pot_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pot_transfer
    ADD CONSTRAINT pot_transfer_pkey PRIMARY KEY (id);


--
-- Name: redeemer_data redeemer_data_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer_data
    ADD CONSTRAINT redeemer_data_pkey PRIMARY KEY (id);


--
-- Name: redeemer redeemer_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer
    ADD CONSTRAINT redeemer_pkey PRIMARY KEY (id);


--
-- Name: reference_tx_in reference_tx_in_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reference_tx_in
    ADD CONSTRAINT reference_tx_in_pkey PRIMARY KEY (id);


--
-- Name: reserve reserve_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserve
    ADD CONSTRAINT reserve_pkey PRIMARY KEY (id);


--
-- Name: reserved_pool_ticker reserved_pool_ticker_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserved_pool_ticker
    ADD CONSTRAINT reserved_pool_ticker_pkey PRIMARY KEY (id);


--
-- Name: reward reward_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.schema_version
    ADD CONSTRAINT schema_version_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: slot_leader slot_leader_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.slot_leader
    ADD CONSTRAINT slot_leader_pkey PRIMARY KEY (id);


--
-- Name: stake_address stake_address_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_address
    ADD CONSTRAINT stake_address_pkey PRIMARY KEY (id);


--
-- Name: stake_deregistration stake_deregistration_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration
    ADD CONSTRAINT stake_deregistration_pkey PRIMARY KEY (id);


--
-- Name: stake_registration stake_registration_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_registration
    ADD CONSTRAINT stake_registration_pkey PRIMARY KEY (id);


--
-- Name: treasury treasury_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.treasury
    ADD CONSTRAINT treasury_pkey PRIMARY KEY (id);


--
-- Name: tx_in tx_in_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in
    ADD CONSTRAINT tx_in_pkey PRIMARY KEY (id);


--
-- Name: tx_metadata tx_metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_metadata
    ADD CONSTRAINT tx_metadata_pkey PRIMARY KEY (id);


--
-- Name: tx_out tx_out_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT tx_out_pkey PRIMARY KEY (id);


--
-- Name: tx tx_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx
    ADD CONSTRAINT tx_pkey PRIMARY KEY (id);


--
-- Name: ada_pots uk_143qflkqxvmvp4cukodhskt43; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ada_pots
    ADD CONSTRAINT uk_143qflkqxvmvp4cukodhskt43 UNIQUE (block_id);


--
-- Name: unconsume_tx_in unconsume_tx_in_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.unconsume_tx_in
    ADD CONSTRAINT unconsume_tx_in_pkey PRIMARY KEY (id);


--
-- Name: pool_stake uni_pool_id; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_stake
    ADD CONSTRAINT uni_pool_id UNIQUE (pool_id);


--
-- Name: ada_pots unique_ada_pots; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ada_pots
    ADD CONSTRAINT unique_ada_pots UNIQUE (block_id);


--
-- Name: block unique_block; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.block
    ADD CONSTRAINT unique_block UNIQUE (hash);


--
-- Name: failed_tx_out unique_col_failed_txout; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT unique_col_failed_txout UNIQUE (tx_id, index);


--
-- Name: unconsume_tx_in unique_col_txin; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.unconsume_tx_in
    ADD CONSTRAINT unique_col_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: cost_model unique_cost_model; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.cost_model
    ADD CONSTRAINT unique_cost_model UNIQUE (hash);


--
-- Name: datum unique_datum; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.datum
    ADD CONSTRAINT unique_datum UNIQUE (hash);


--
-- Name: delegation unique_delegation; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT unique_delegation UNIQUE (tx_id, cert_index);


--
-- Name: delisted_pool unique_delisted_pool; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delisted_pool
    ADD CONSTRAINT unique_delisted_pool UNIQUE (hash_raw);


--
-- Name: epoch unique_epoch; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch
    ADD CONSTRAINT unique_epoch UNIQUE (no);


--
-- Name: epoch_param unique_epoch_param; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_param
    ADD CONSTRAINT unique_epoch_param UNIQUE (epoch_no, block_id);


--
-- Name: epoch_sync_time unique_epoch_sync_time; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_sync_time
    ADD CONSTRAINT unique_epoch_sync_time UNIQUE (no);


--
-- Name: ma_tx_mint unique_ma_tx_mint; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_mint
    ADD CONSTRAINT unique_ma_tx_mint UNIQUE (ident, tx_id);


--
-- Name: ma_tx_out unique_ma_tx_out; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_out
    ADD CONSTRAINT unique_ma_tx_out UNIQUE (ident, tx_out_id);


--
-- Name: meta unique_meta; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.meta
    ADD CONSTRAINT unique_meta UNIQUE (start_time);


--
-- Name: multi_asset unique_multi_asset; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.multi_asset
    ADD CONSTRAINT unique_multi_asset UNIQUE (policy, name);


--
-- Name: param_proposal unique_param_proposal; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.param_proposal
    ADD CONSTRAINT unique_param_proposal UNIQUE (key, registered_tx_id);


--
-- Name: pool_hash unique_pool_hash; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_hash
    ADD CONSTRAINT unique_pool_hash UNIQUE (hash_raw);


--
-- Name: pool_metadata_ref unique_pool_metadata_ref; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_metadata_ref
    ADD CONSTRAINT unique_pool_metadata_ref UNIQUE (pool_id, url, hash);


--
-- Name: pool_offline_data unique_pool_offline_data; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_data
    ADD CONSTRAINT unique_pool_offline_data UNIQUE (pool_id, hash);


--
-- Name: pool_offline_fetch_error unique_pool_offline_fetch_error; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_fetch_error
    ADD CONSTRAINT unique_pool_offline_fetch_error UNIQUE (pool_id, fetch_time, retry_count);


--
-- Name: pool_owner unique_pool_owner; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_owner
    ADD CONSTRAINT unique_pool_owner UNIQUE (addr_id, pool_update_id);


--
-- Name: pool_relay unique_pool_relay; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_relay
    ADD CONSTRAINT unique_pool_relay UNIQUE (update_id, ipv4, ipv6, dns_name);


--
-- Name: pool_retire unique_pool_retiring; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_retire
    ADD CONSTRAINT unique_pool_retiring UNIQUE (announced_tx_id, cert_index);


--
-- Name: pool_update unique_pool_update; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT unique_pool_update UNIQUE (registered_tx_id, cert_index);


--
-- Name: pot_transfer unique_pot_transfer; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pot_transfer
    ADD CONSTRAINT unique_pot_transfer UNIQUE (tx_id, cert_index);


--
-- Name: redeemer unique_redeemer; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer
    ADD CONSTRAINT unique_redeemer UNIQUE (tx_id, purpose, index);


--
-- Name: redeemer_data unique_redeemer_data; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer_data
    ADD CONSTRAINT unique_redeemer_data UNIQUE (hash);


--
-- Name: reference_tx_in unique_ref_txin; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reference_tx_in
    ADD CONSTRAINT unique_ref_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: reserved_pool_ticker unique_reserved_pool_ticker; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserved_pool_ticker
    ADD CONSTRAINT unique_reserved_pool_ticker UNIQUE (name);


--
-- Name: reserve unique_reserves; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserve
    ADD CONSTRAINT unique_reserves UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: reward unique_reward; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);


--
-- Name: script unique_script; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.script
    ADD CONSTRAINT unique_script UNIQUE (hash);


--
-- Name: slot_leader unique_slot_leader; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.slot_leader
    ADD CONSTRAINT unique_slot_leader UNIQUE (hash);


--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);


--
-- Name: stake_address unique_stake_address; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_address
    ADD CONSTRAINT unique_stake_address UNIQUE (hash_raw);


--
-- Name: stake_deregistration unique_stake_deregistration; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration
    ADD CONSTRAINT unique_stake_deregistration UNIQUE (tx_id, cert_index);


--
-- Name: stake_registration unique_stake_registration; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_registration
    ADD CONSTRAINT unique_stake_registration UNIQUE (tx_id, cert_index);


--
-- Name: treasury unique_treasury; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.treasury
    ADD CONSTRAINT unique_treasury UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: tx unique_tx; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx
    ADD CONSTRAINT unique_tx UNIQUE (hash);


--
-- Name: tx_metadata unique_tx_metadata; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_metadata
    ADD CONSTRAINT unique_tx_metadata UNIQUE (key, tx_id);


--
-- Name: tx_in unique_txin; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in
    ADD CONSTRAINT unique_txin UNIQUE (tx_out_id, tx_out_index);


--
-- Name: tx_out unique_txout; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT unique_txout UNIQUE (tx_id, index);


--
-- Name: extra_key_witness unique_witness; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.extra_key_witness
    ADD CONSTRAINT unique_witness UNIQUE (hash);


--
-- Name: withdrawal withdrawal_pkey; Type: CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id);


--
-- Name: ada_pots ada_pots_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ada_pots
    ADD CONSTRAINT ada_pots_block_id_fkey FOREIGN KEY (block_id) REFERENCES public.block(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES public.datum(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES public.script(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out collateral_tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT collateral_tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: cost_model cost_model_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.cost_model
    ADD CONSTRAINT cost_model_block_id_fkey FOREIGN KEY (block_id) REFERENCES public.block(id) ON DELETE CASCADE;


--
-- Name: datum datum_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.datum
    ADD CONSTRAINT datum_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT delegation_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT delegation_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT delegation_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES public.redeemer(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.delegation
    ADD CONSTRAINT delegation_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_param
    ADD CONSTRAINT epoch_param_block_id_fkey FOREIGN KEY (block_id) REFERENCES public.block(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_param
    ADD CONSTRAINT epoch_param_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES public.cost_model(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_stake
    ADD CONSTRAINT epoch_stake_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.epoch_stake
    ADD CONSTRAINT epoch_stake_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: extra_key_witness extra_key_witness_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.extra_key_witness
    ADD CONSTRAINT extra_key_witness_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: failed_tx_out failed_tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.failed_tx_out
    ADD CONSTRAINT failed_tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: slot_leader fk23db3v8n6dl1gf0njxgpceviq; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.slot_leader
    ADD CONSTRAINT fk23db3v8n6dl1gf0njxgpceviq FOREIGN KEY (pool_hash_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: block fk6gd608i8qbyert1hlfl9be71h; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.block
    ADD CONSTRAINT fk6gd608i8qbyert1hlfl9be71h FOREIGN KEY (previous_id) REFERENCES public.block(id) ON DELETE CASCADE;


--
-- Name: block fke45qmlh5if6ghcyj0mhumjhk1; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.block
    ADD CONSTRAINT fke45qmlh5if6ghcyj0mhumjhk1 FOREIGN KEY (slot_leader_id) REFERENCES public.slot_leader(id) ON DELETE CASCADE;


--
-- Name: pool_stake fknipbg0qr651vyttfseohc94ql; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_stake
    ADD CONSTRAINT fknipbg0qr651vyttfseohc94ql FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id);


--
-- Name: ma_tx_mint ma_tx_mint_ident_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_ident_fkey FOREIGN KEY (ident) REFERENCES public.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_ident_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_out
    ADD CONSTRAINT ma_tx_out_ident_fkey FOREIGN KEY (ident) REFERENCES public.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.ma_tx_out
    ADD CONSTRAINT ma_tx_out_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES public.tx_out(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.param_proposal
    ADD CONSTRAINT param_proposal_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES public.cost_model(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.param_proposal
    ADD CONSTRAINT param_proposal_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pmr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES public.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pmr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES public.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_owner
    ADD CONSTRAINT pool_owner_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_pool_update_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_owner
    ADD CONSTRAINT pool_owner_pool_update_id_fkey FOREIGN KEY (pool_update_id) REFERENCES public.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_relay pool_relay_update_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_relay
    ADD CONSTRAINT pool_relay_update_id_fkey FOREIGN KEY (update_id) REFERENCES public.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_announced_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_retire
    ADD CONSTRAINT pool_retire_announced_tx_id_fkey FOREIGN KEY (announced_tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_hash_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_retire
    ADD CONSTRAINT pool_retire_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_hash_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT pool_update_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_meta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT pool_update_meta_id_fkey FOREIGN KEY (meta_id) REFERENCES public.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT pool_update_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pool_update
    ADD CONSTRAINT pool_update_reward_addr_id_fkey FOREIGN KEY (reward_addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: pot_transfer pot_transfer_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.pot_transfer
    ADD CONSTRAINT pot_transfer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer_data redeemer_data_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer_data
    ADD CONSTRAINT redeemer_data_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_redeemer_data_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer
    ADD CONSTRAINT redeemer_redeemer_data_id_fkey FOREIGN KEY (redeemer_data_id) REFERENCES public.redeemer_data(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.redeemer
    ADD CONSTRAINT redeemer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserve
    ADD CONSTRAINT reserve_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reserve
    ADD CONSTRAINT reserve_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: reward reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reward
    ADD CONSTRAINT reward_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: reward reward_pool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.reward
    ADD CONSTRAINT reward_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES public.pool_hash(id) ON DELETE CASCADE;


--
-- Name: script script_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.script
    ADD CONSTRAINT script_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: stake_address stake_address_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_address
    ADD CONSTRAINT stake_address_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration
    ADD CONSTRAINT stake_deregistration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration
    ADD CONSTRAINT stake_deregistration_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES public.redeemer(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_deregistration
    ADD CONSTRAINT stake_deregistration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_registration
    ADD CONSTRAINT stake_registration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.stake_registration
    ADD CONSTRAINT stake_registration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.treasury
    ADD CONSTRAINT treasury_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.treasury
    ADD CONSTRAINT treasury_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: tx tx_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx
    ADD CONSTRAINT tx_block_id_fkey FOREIGN KEY (block_id) REFERENCES public.block(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in
    ADD CONSTRAINT tx_in_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES public.redeemer(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in
    ADD CONSTRAINT tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_in
    ADD CONSTRAINT tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: tx_metadata tx_metadata_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_metadata
    ADD CONSTRAINT tx_metadata_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES public.datum(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES public.script(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.tx_out
    ADD CONSTRAINT tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: unconsume_tx_in unsconume_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.unconsume_tx_in
    ADD CONSTRAINT unsconume_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: unconsume_tx_in unsconume_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.unconsume_tx_in
    ADD CONSTRAINT unsconume_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_addr_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.withdrawal
    ADD CONSTRAINT withdrawal_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES public.stake_address(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.withdrawal
    ADD CONSTRAINT withdrawal_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES public.redeemer(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_tx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cardano-master
--

ALTER TABLE ONLY public.withdrawal
    ADD CONSTRAINT withdrawal_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES public.tx(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

