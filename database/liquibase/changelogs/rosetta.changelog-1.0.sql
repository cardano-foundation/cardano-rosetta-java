--liquibase formatted sql

--
-- PostgreSQL database dump
--

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

CREATE SCHEMA IF NOT EXISTS ${liquibase.cf_db_schema};

ALTER SCHEMA ${liquibase.cf_db_schema} OWNER TO ${liquibase.cf_serviceuser_name};

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ada_pots; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.ada_pots (
                                                    id bigint NOT NULL,
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


ALTER TABLE ${liquibase.cf_db_schema}.ada_pots OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ada_pots_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.ada_pots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.ada_pots_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ada_pots_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.ada_pots_id_seq OWNED BY ${liquibase.cf_db_schema}.ada_pots.id;


--
-- Name: block; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.block (
                                                 id bigint NOT NULL,
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
                                                 "time" timestamp(6) without time zone,
                                                 tx_count bigint,
                                                 vrf_key character varying(65535),
                                                 previous_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.block OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: block_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.block_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: block_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.block_id_seq OWNED BY ${liquibase.cf_db_schema}.block.id;


--
-- Name: collateral_tx_in; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.collateral_tx_in (
                                                            id bigint NOT NULL,
                                                            tx_in_id bigint NOT NULL,
                                                            tx_out_id bigint NOT NULL,
                                                            tx_out_index smallint
);


ALTER TABLE ${liquibase.cf_db_schema}.collateral_tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.collateral_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.collateral_tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.collateral_tx_in_id_seq OWNED BY ${liquibase.cf_db_schema}.collateral_tx_in.id;


--
-- Name: collateral_tx_out; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.collateral_tx_out (
                                                             id bigint NOT NULL,
                                                             address oid NOT NULL,
                                                             address_has_script boolean NOT NULL,
                                                             address_raw bytea NOT NULL,
                                                             data_hash character varying(255),
                                                             index smallint,
                                                             inline_datum_id bigint,
                                                             multi_assets_descr oid NOT NULL,
                                                             payment_cred character varying(255),
                                                             reference_script_id bigint,
                                                             stake_address_id bigint,
                                                             tx_id bigint NOT NULL,
                                                             value numeric(20,0) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.collateral_tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.collateral_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.collateral_tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.collateral_tx_out_id_seq OWNED BY ${liquibase.cf_db_schema}.collateral_tx_out.id;


--
-- Name: cost_model; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.cost_model (
                                                      id bigint NOT NULL,
                                                      costs character varying(65535) NOT NULL,
                                                      hash character varying(64) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.cost_model OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: cost_model_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.cost_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.cost_model_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: cost_model_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.cost_model_id_seq OWNED BY ${liquibase.cf_db_schema}.cost_model.id;


--
-- Name: datum; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.datum (
                                                 id bigint NOT NULL,
                                                 bytes bytea,
                                                 hash character varying(64) NOT NULL,
                                                 value character varying(65535),
                                                 tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.datum OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: datum_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.datum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.datum_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: datum_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.datum_id_seq OWNED BY ${liquibase.cf_db_schema}.datum.id;


--
-- Name: delegation; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.delegation (
                                                      id bigint NOT NULL,
                                                      active_epoch_no bigint NOT NULL,
                                                      cert_index integer NOT NULL,
                                                      slot_no bigint NOT NULL,
                                                      addr_id bigint,
                                                      pool_hash_id bigint NOT NULL,
                                                      redeemer_id bigint,
                                                      tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.delegation OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delegation_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.delegation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.delegation_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delegation_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.delegation_id_seq OWNED BY ${liquibase.cf_db_schema}.delegation.id;


--
-- Name: delisted_pool; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.delisted_pool (
                                                         id bigint NOT NULL,
                                                         hash_raw character varying(56) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.delisted_pool OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.delisted_pool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.delisted_pool_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: delisted_pool_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.delisted_pool_id_seq OWNED BY ${liquibase.cf_db_schema}.delisted_pool.id;


--
-- Name: epoch; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.epoch (
                                                 id bigint NOT NULL,
                                                 blk_count integer NOT NULL,
                                                 end_time timestamp(6) without time zone,
                                                 era integer NOT NULL,
                                                 fees numeric(20,0) NOT NULL,
                                                 max_slot integer NOT NULL,
                                                 no integer NOT NULL,
                                                 out_sum numeric(39,0) NOT NULL,
                                                 rewards_distributed numeric(38,0),
                                                 start_time timestamp(6) without time zone,
                                                 tx_count integer NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.epoch OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.epoch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.epoch_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.epoch_id_seq OWNED BY ${liquibase.cf_db_schema}.epoch.id;


--
-- Name: epoch_param; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.epoch_param (
                                                       id bigint NOT NULL,
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


ALTER TABLE ${liquibase.cf_db_schema}.epoch_param OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_param_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.epoch_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.epoch_param_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_param_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.epoch_param_id_seq OWNED BY ${liquibase.cf_db_schema}.epoch_param.id;


--
-- Name: epoch_stake; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.epoch_stake (
                                                       id bigint NOT NULL,
                                                       amount numeric(20,0) NOT NULL,
                                                       epoch_no integer NOT NULL,
                                                       addr_id bigint NOT NULL,
                                                       pool_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.epoch_stake OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.epoch_stake_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.epoch_stake_id_seq OWNED BY ${liquibase.cf_db_schema}.epoch_stake.id;


--
-- Name: epoch_sync_time; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.epoch_sync_time (
                                                           id bigint NOT NULL,
                                                           no bigint NOT NULL,
                                                           seconds bigint NOT NULL,
                                                           state character varying(255) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.epoch_sync_time OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.epoch_sync_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.epoch_sync_time_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.epoch_sync_time_id_seq OWNED BY ${liquibase.cf_db_schema}.epoch_sync_time.id;


--
-- Name: extra_key_witness; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.extra_key_witness (
                                                             id bigint NOT NULL,
                                                             hash character varying(56) NOT NULL,
                                                             tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.extra_key_witness OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.extra_key_witness_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.extra_key_witness_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.extra_key_witness_id_seq OWNED BY ${liquibase.cf_db_schema}.extra_key_witness.id;


--
-- Name: ma_tx_mint; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.ma_tx_mint (
                                                      id bigint NOT NULL,
                                                      ident bigint,
                                                      quantity numeric(20,0) NOT NULL,
                                                      tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.ma_tx_mint OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.ma_tx_mint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.ma_tx_mint_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.ma_tx_mint_id_seq OWNED BY ${liquibase.cf_db_schema}.ma_tx_mint.id;


--
-- Name: ma_tx_out; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.ma_tx_out (
                                                     id bigint NOT NULL,
                                                     ident bigint,
                                                     quantity numeric(20,0) NOT NULL,
                                                     tx_out_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.ma_tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.ma_tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.ma_tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.ma_tx_out_id_seq OWNED BY ${liquibase.cf_db_schema}.ma_tx_out.id;


--
-- Name: meta; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.meta (
                                                id bigint NOT NULL,
                                                network_name character varying(255) NOT NULL,
                                                start_time timestamp(6) without time zone NOT NULL,
                                                version character varying(255) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.meta OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: meta_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.meta_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: meta_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.meta_id_seq OWNED BY ${liquibase.cf_db_schema}.meta.id;


--
-- Name: multi_asset; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.multi_asset (
                                                       id bigint NOT NULL,
                                                       fingerprint character varying(255) NOT NULL,
                                                       name character varying(64) NOT NULL,
                                                       policy character varying(56) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.multi_asset OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: multi_asset_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.multi_asset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.multi_asset_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: multi_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.multi_asset_id_seq OWNED BY ${liquibase.cf_db_schema}.multi_asset.id;


--
-- Name: param_proposal; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.param_proposal (
                                                          id bigint NOT NULL,
                                                          coins_per_utxo_size numeric(38,0),
                                                          collateral_percent integer,
                                                          decentralisation double precision,
                                                          entropy character varying(64),
                                                          epoch_no integer NOT NULL,
                                                          influence double precision,
                                                          key character varying(56) NOT NULL,
                                                          key_deposit numeric(38,0),
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


ALTER TABLE ${liquibase.cf_db_schema}.param_proposal OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: param_proposal_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.param_proposal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.param_proposal_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: param_proposal_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.param_proposal_id_seq OWNED BY ${liquibase.cf_db_schema}.param_proposal.id;


--
-- Name: pool_hash; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_hash (
                                                     id bigint NOT NULL,
                                                     epoch_no integer,
                                                     hash_raw character varying(56) NOT NULL,
                                                     pool_size numeric(20,0) NOT NULL,
                                                     view character varying(255) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_hash OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_hash_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_hash_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_hash_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_hash_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_hash_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_hash.id;


--
-- Name: pool_metadata_ref; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_metadata_ref (
                                                             id bigint NOT NULL,
                                                             hash character varying(64) NOT NULL,
                                                             url character varying(255) NOT NULL,
                                                             pool_id bigint NOT NULL,
                                                             registered_tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_metadata_ref OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_metadata_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_metadata_ref_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_metadata_ref_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_metadata_ref.id;


--
-- Name: pool_offline_data; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_offline_data (
                                                             id bigint NOT NULL,
                                                             bytes bytea,
                                                             hash character varying(64) NOT NULL,
                                                             json character varying(65535) NOT NULL,
                                                             pmr_id bigint,
                                                             pool_id bigint,
                                                             pool_name character varying(255) NOT NULL,
                                                             ticker_name character varying(255) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_offline_data OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_offline_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_offline_data_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_offline_data_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_offline_data.id;


--
-- Name: pool_offline_fetch_error; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_offline_fetch_error (
                                                                    id bigint NOT NULL,
                                                                    fetch_error character varying(65535) NOT NULL,
                                                                    fetch_time timestamp(6) without time zone NOT NULL,
                                                                    retry_count integer NOT NULL,
                                                                    pool_id bigint NOT NULL,
                                                                    pmr_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_offline_fetch_error OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_offline_fetch_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_offline_fetch_error_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_offline_fetch_error_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_offline_fetch_error.id;


--
-- Name: pool_owner; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_owner (
                                                      id bigint NOT NULL,
                                                      pool_update_id bigint,
                                                      addr_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_owner OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_owner_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_owner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_owner_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_owner_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_owner_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_owner.id;


--
-- Name: pool_relay; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_relay (
                                                      id bigint NOT NULL,
                                                      dns_name character varying(255),
                                                      dns_srv_name character varying(255),
                                                      ipv4 character varying(255),
                                                      ipv6 character varying(255),
                                                      port integer,
                                                      update_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_relay OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_relay_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_relay_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_relay_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_relay_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_relay_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_relay.id;


--
-- Name: pool_retire; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_retire (
                                                       id bigint NOT NULL,
                                                       announced_tx_id bigint,
                                                       cert_index integer NOT NULL,
                                                       hash_id bigint,
                                                       retiring_epoch integer NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_retire OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_retire_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_retire_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_retire_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_retire_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_retire_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_retire.id;


--
-- Name: pool_update; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pool_update (
                                                       id bigint NOT NULL,
                                                       active_epoch_no integer NOT NULL,
                                                       cert_index integer NOT NULL,
                                                       fixed_cost numeric(20,0) NOT NULL,
                                                       margin double precision NOT NULL,
                                                       pledge numeric(20,0) NOT NULL,
                                                       hash_id bigint,
                                                       registered_tx_id bigint,
                                                       reward_addr_id bigint,
                                                       vrf_key_hash character varying(64) NOT NULL,
                                                       meta_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.pool_update OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_update_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pool_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pool_update_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pool_update_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pool_update_id_seq OWNED BY ${liquibase.cf_db_schema}.pool_update.id;


--
-- Name: pot_transfer; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.pot_transfer (
                                                        id bigint NOT NULL,
                                                        cert_index integer NOT NULL,
                                                        reserves numeric(20,0) NOT NULL,
                                                        treasury numeric(20,0) NOT NULL,
                                                        tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.pot_transfer OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.pot_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.pot_transfer_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: pot_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.pot_transfer_id_seq OWNED BY ${liquibase.cf_db_schema}.pot_transfer.id;


--
-- Name: redeemer; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.redeemer (
                                                    id bigint NOT NULL,
                                                    fee numeric(20,0),
                                                    index integer NOT NULL,
                                                    purpose character varying(255) NOT NULL,
                                                    script_hash character varying(56),
                                                    unit_mem bigint NOT NULL,
                                                    unit_steps bigint NOT NULL,
                                                    redeemer_data_id bigint NOT NULL,
                                                    tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.redeemer OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.redeemer_data (
                                                         id bigint NOT NULL,
                                                         bytes bytea,
                                                         hash character varying(64) NOT NULL,
                                                         value character varying(65535),
                                                         tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.redeemer_data OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.redeemer_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.redeemer_data_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_data_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.redeemer_data_id_seq OWNED BY ${liquibase.cf_db_schema}.redeemer_data.id;


--
-- Name: redeemer_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.redeemer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.redeemer_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: redeemer_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.redeemer_id_seq OWNED BY ${liquibase.cf_db_schema}.redeemer.id;


--
-- Name: reference_tx_in; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.reference_tx_in (
                                                           id bigint NOT NULL,
                                                           tx_out_index smallint NOT NULL,
                                                           tx_in_id bigint NOT NULL,
                                                           tx_out_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.reference_tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.reference_tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.reference_tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.reference_tx_in_id_seq OWNED BY ${liquibase.cf_db_schema}.reference_tx_in.id;


--
-- Name: reserve; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.reserve (
                                                   id bigint NOT NULL,
                                                   amount numeric(20,0) NOT NULL,
                                                   cert_index integer NOT NULL,
                                                   addr_id bigint NOT NULL,
                                                   tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.reserve OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserve_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.reserve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.reserve_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserve_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.reserve_id_seq OWNED BY ${liquibase.cf_db_schema}.reserve.id;


--
-- Name: reserved_pool_ticker; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.reserved_pool_ticker (
                                                                id bigint NOT NULL,
                                                                name character varying(255) NOT NULL,
                                                                pool_hash character varying(56) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.reserved_pool_ticker OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.reserved_pool_ticker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.reserved_pool_ticker_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.reserved_pool_ticker_id_seq OWNED BY ${liquibase.cf_db_schema}.reserved_pool_ticker.id;


--
-- Name: reverse_index; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.reverse_index (
                                                         id bigint NOT NULL,
                                                         block_id bigint NOT NULL,
                                                         min_ids oid
);


ALTER TABLE ${liquibase.cf_db_schema}.reverse_index OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reverse_index_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.reverse_index_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.reverse_index_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reverse_index_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.reverse_index_id_seq OWNED BY ${liquibase.cf_db_schema}.reverse_index.id;


--
-- Name: reward; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.reward (
                                                  id bigint NOT NULL,
                                                  amount numeric(20,0) NOT NULL,
                                                  earned_epoch integer NOT NULL,
                                                  pool_id bigint,
                                                  spendable_epoch integer NOT NULL,
                                                  addr_id bigint,
                                                  type character varying(255) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.reward OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reward_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.reward_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: reward_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.reward_id_seq OWNED BY ${liquibase.cf_db_schema}.reward.id;


--
-- Name: rollback_history; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.rollback_history (
                                                            id bigint NOT NULL,
                                                            block_hash_end character varying(255) NOT NULL,
                                                            block_hash_start character varying(255) NOT NULL,
                                                            block_no_end bigint NOT NULL,
                                                            block_no_start bigint NOT NULL,
                                                            block_slot_end bigint NOT NULL,
                                                            block_slot_start bigint NOT NULL,
                                                            blocks_deletion_status smallint NOT NULL,
                                                            reason character varying(255),
                                                            rollback_time timestamp(6) without time zone NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.rollback_history OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: rollback_history_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.rollback_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.rollback_history_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: rollback_history_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.rollback_history_id_seq OWNED BY ${liquibase.cf_db_schema}.rollback_history.id;


--
-- Name: schema_version; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.schema_version (
                                                          id bigint NOT NULL,
                                                          stage_one bigint NOT NULL,
                                                          stage_three bigint NOT NULL,
                                                          stage_two bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.schema_version OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: schema_version_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.schema_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.schema_version_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: schema_version_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.schema_version_id_seq OWNED BY ${liquibase.cf_db_schema}.schema_version.id;


--
-- Name: script; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.script (
                                                  id bigint NOT NULL,
                                                  bytes bytea,
                                                  hash character varying(64) NOT NULL,
                                                  json character varying(65535),
                                                  serialised_size integer,
                                                  type character varying(255) NOT NULL,
                                                  tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.script OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: script_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.script_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: script_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.script_id_seq OWNED BY ${liquibase.cf_db_schema}.script.id;


--
-- Name: slot_leader; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.slot_leader (
                                                       id bigint NOT NULL,
                                                       description character varying(65535) NOT NULL,
                                                       hash character varying(56) NOT NULL,
                                                       pool_hash_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.slot_leader OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: slot_leader_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.slot_leader_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.slot_leader_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: slot_leader_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.slot_leader_id_seq OWNED BY ${liquibase.cf_db_schema}.slot_leader.id;


--
-- Name: stake_address; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.stake_address (
                                                         id bigint NOT NULL,
                                                         available_reward numeric(39,0) NOT NULL,
                                                         balance numeric(39,0) NOT NULL,
                                                         hash_raw character varying(255) NOT NULL,
                                                         script_hash character varying(56),
                                                         view character varying(65535) NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.stake_address OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_address_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.stake_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.stake_address_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_address_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.stake_address_id_seq OWNED BY ${liquibase.cf_db_schema}.stake_address.id;


--
-- Name: stake_deregistration; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.stake_deregistration (
                                                                id bigint NOT NULL,
                                                                cert_index integer NOT NULL,
                                                                epoch_no integer NOT NULL,
                                                                addr_id bigint NOT NULL,
                                                                redeemer_id bigint,
                                                                tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.stake_deregistration OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.stake_deregistration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.stake_deregistration_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.stake_deregistration_id_seq OWNED BY ${liquibase.cf_db_schema}.stake_deregistration.id;


--
-- Name: stake_registration; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.stake_registration (
                                                              id bigint NOT NULL,
                                                              cert_index integer NOT NULL,
                                                              epoch_no integer NOT NULL,
                                                              addr_id bigint NOT NULL,
                                                              tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.stake_registration OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_registration_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.stake_registration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.stake_registration_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: stake_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.stake_registration_id_seq OWNED BY ${liquibase.cf_db_schema}.stake_registration.id;


--
-- Name: treasury; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.treasury (
                                                    id bigint NOT NULL,
                                                    amount numeric(20,0) NOT NULL,
                                                    cert_index integer NOT NULL,
                                                    addr_id bigint NOT NULL,
                                                    tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.treasury OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: treasury_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.treasury_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.treasury_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: treasury_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.treasury_id_seq OWNED BY ${liquibase.cf_db_schema}.treasury.id;


--
-- Name: tx; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.tx (
                                              id bigint NOT NULL,
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


ALTER TABLE ${liquibase.cf_db_schema}.tx OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.tx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.tx_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.tx_id_seq OWNED BY ${liquibase.cf_db_schema}.tx.id;


--
-- Name: tx_in; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.tx_in (
                                                 id bigint NOT NULL,
                                                 tx_in_id bigint,
                                                 tx_out_index smallint NOT NULL,
                                                 tx_out_id bigint,
                                                 redeemer_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.tx_in OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_in_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.tx_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.tx_in_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_in_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.tx_in_id_seq OWNED BY ${liquibase.cf_db_schema}.tx_in.id;


--
-- Name: tx_metadata; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.tx_metadata (
                                                       id bigint NOT NULL,
                                                       bytes bytea,
                                                       json character varying(65535),
                                                       key numeric(20,0) NOT NULL,
                                                       tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.tx_metadata OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.tx_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.tx_metadata_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.tx_metadata_id_seq OWNED BY ${liquibase.cf_db_schema}.tx_metadata.id;


--
-- Name: tx_out; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.tx_out (
                                                  id bigint NOT NULL,
                                                  address character varying(65535) NOT NULL,
                                                  address_has_script boolean NOT NULL,
                                                  address_raw bytea NOT NULL,
                                                  data_hash character varying(64),
                                                  index smallint NOT NULL,
                                                  payment_cred character varying(56),
                                                  tx_id bigint,
                                                  value numeric(20,0) NOT NULL,
                                                  inline_datum_id bigint,
                                                  reference_script_id bigint,
                                                  stake_address_id bigint
);


ALTER TABLE ${liquibase.cf_db_schema}.tx_out OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_out_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.tx_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.tx_out_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: tx_out_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.tx_out_id_seq OWNED BY ${liquibase.cf_db_schema}.tx_out.id;


--
-- Name: withdrawal; Type: TABLE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE TABLE ${liquibase.cf_db_schema}.withdrawal (
                                                      id bigint NOT NULL,
                                                      amount numeric(20,0) NOT NULL,
                                                      addr_id bigint,
                                                      redeemer_id bigint,
                                                      tx_id bigint NOT NULL
);


ALTER TABLE ${liquibase.cf_db_schema}.withdrawal OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: withdrawal_id_seq; Type: SEQUENCE; Schema: preprod. Owner: rosetta_db_admin
--

CREATE SEQUENCE ${liquibase.cf_db_schema}.withdrawal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ${liquibase.cf_db_schema}.withdrawal_id_seq OWNER TO ${liquibase.cf_serviceuser_name};

--
-- Name: withdrawal_id_seq; Type: SEQUENCE OWNED BY; Schema: preprod. Owner: rosetta_db_admin
--

ALTER SEQUENCE ${liquibase.cf_db_schema}.withdrawal_id_seq OWNED BY ${liquibase.cf_db_schema}.withdrawal.id;


--
-- Name: ada_pots id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ada_pots ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.ada_pots_id_seq'::regclass);


--
-- Name: block id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.block ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.block_id_seq'::regclass);


--
-- Name: collateral_tx_in id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.collateral_tx_in ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.collateral_tx_in_id_seq'::regclass);


--
-- Name: collateral_tx_out id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.collateral_tx_out ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.collateral_tx_out_id_seq'::regclass);


--
-- Name: cost_model id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.cost_model ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.cost_model_id_seq'::regclass);


--
-- Name: datum id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.datum ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.datum_id_seq'::regclass);


--
-- Name: delegation id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.delegation_id_seq'::regclass);


--
-- Name: delisted_pool id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delisted_pool ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.delisted_pool_id_seq'::regclass);


--
-- Name: epoch id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.epoch_id_seq'::regclass);


--
-- Name: epoch_param id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_param ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.epoch_param_id_seq'::regclass);


--
-- Name: epoch_stake id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_stake ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.epoch_stake_id_seq'::regclass);


--
-- Name: epoch_sync_time id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_sync_time ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.epoch_sync_time_id_seq'::regclass);


--
-- Name: extra_key_witness id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.extra_key_witness ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.extra_key_witness_id_seq'::regclass);


--
-- Name: ma_tx_mint id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_mint ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.ma_tx_mint_id_seq'::regclass);


--
-- Name: ma_tx_out id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_out ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.ma_tx_out_id_seq'::regclass);


--
-- Name: meta id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.meta ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.meta_id_seq'::regclass);


--
-- Name: multi_asset id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.multi_asset ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.multi_asset_id_seq'::regclass);


--
-- Name: param_proposal id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.param_proposal ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.param_proposal_id_seq'::regclass);


--
-- Name: pool_hash id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_hash ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_hash_id_seq'::regclass);


--
-- Name: pool_metadata_ref id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_metadata_ref ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_metadata_ref_id_seq'::regclass);


--
-- Name: pool_offline_data id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_data ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_offline_data_id_seq'::regclass);


--
-- Name: pool_offline_fetch_error id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_fetch_error ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_offline_fetch_error_id_seq'::regclass);


--
-- Name: pool_owner id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_owner ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_owner_id_seq'::regclass);


--
-- Name: pool_relay id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_relay ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_relay_id_seq'::regclass);


--
-- Name: pool_retire id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_retire ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_retire_id_seq'::regclass);


--
-- Name: pool_update id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pool_update_id_seq'::regclass);


--
-- Name: pot_transfer id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pot_transfer ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.pot_transfer_id_seq'::regclass);


--
-- Name: redeemer id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.redeemer_id_seq'::regclass);


--
-- Name: redeemer_data id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer_data ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.redeemer_data_id_seq'::regclass);


--
-- Name: reference_tx_in id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reference_tx_in ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.reference_tx_in_id_seq'::regclass);


--
-- Name: reserve id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserve ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.reserve_id_seq'::regclass);


--
-- Name: reserved_pool_ticker id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserved_pool_ticker ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.reserved_pool_ticker_id_seq'::regclass);


--
-- Name: reverse_index id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reverse_index ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.reverse_index_id_seq'::regclass);


--
-- Name: reward id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reward ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.reward_id_seq'::regclass);


--
-- Name: rollback_history id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.rollback_history ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.rollback_history_id_seq'::regclass);


--
-- Name: schema_version id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.schema_version ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.schema_version_id_seq'::regclass);


--
-- Name: script id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.script ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.script_id_seq'::regclass);


--
-- Name: slot_leader id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.slot_leader ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.slot_leader_id_seq'::regclass);


--
-- Name: stake_address id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_address ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.stake_address_id_seq'::regclass);


--
-- Name: stake_deregistration id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.stake_deregistration_id_seq'::regclass);


--
-- Name: stake_registration id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_registration ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.stake_registration_id_seq'::regclass);


--
-- Name: treasury id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.treasury ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.treasury_id_seq'::regclass);


--
-- Name: tx id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.tx_id_seq'::regclass);


--
-- Name: tx_in id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.tx_in_id_seq'::regclass);


--
-- Name: tx_metadata id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_metadata ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.tx_metadata_id_seq'::regclass);


--
-- Name: tx_out id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.tx_out_id_seq'::regclass);


--
-- Name: withdrawal id; Type: DEFAULT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.withdrawal ALTER COLUMN id SET DEFAULT nextval('${liquibase.cf_db_schema}.withdrawal_id_seq'::regclass);


-- Data for Name: block; Type: TABLE DATA; Schema: preprod. Owner: rosetta_db_admin
--

INSERT INTO ${liquibase.cf_db_schema}.block VALUES (1, NULL, NULL, NULL, '5f20df933584822601f9e3f8c024eb5eb252fe8cefb24d1317dc3d432e940ebb', NULL, NULL, 0, 0, 0, 1, NULL, '2017-09-23 21:44:51', 14505, NULL, NULL);

--
-- Data for Name: slot_leader; Type: TABLE DATA; Schema: preprod. Owner: rosetta_db_admin
--

INSERT INTO ${liquibase.cf_db_schema}.slot_leader VALUES (1, 'Genesis slot leader', '5f20df933584822601f9e3f8c024eb5eb252fe8cefb24d1317dc3d43', NULL);



--
-- Data for Name: tx; Type: TABLE DATA; Schema: mainnet. Owner: rosetta_db_admin
--

INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (1, 1, 0, 0, 0, '927edb96f3386ab91b5f5d85d84cb4253c65b1c2f65fa7df25f81fab1d62987a', NULL, NULL, 538861000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (2, 1, 0, 0, 0, '2d51b929d79a0ac8f360f38e8a38cdcb28ca84139aced314c5d7edc739aa4366', NULL, NULL, 1153846000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (3, 1, 0, 0, 0, '29c8a63ac4ff2bdb630656e9e568c3e526ef316b280b7123b9a9a9719f9ce8d7', NULL, NULL, 455585000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (4, 1, 0, 0, 0, '93cf53cbf2475adf8b846fd2ea97ce352097d26fa88843dc761d2b1bbf79f30b', NULL, NULL, 390975000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (5, 1, 0, 0, 0, 'd922298743b0d4474fa1bfdef782eada6df88434c9b760a8a1dc3bf667843957', NULL, NULL, 384573000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (6, 1, 0, 0, 0, '57c4e22be70567dd9c839f3c7d86ebc47a4ad93bde3b21e643c9b4814a5ad5ca', NULL, NULL, 378507000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (7, 1, 0, 0, 0, '9301d7cf3d996b021ea7a77f7ca89434681cdde50663738297f133d087b2724e', NULL, NULL, 499999000000, 0, 0, true);
INSERT INTO ${liquibase.cf_db_schema}.tx VALUES (8, 1, 0, 0, 0, '0760a9f8e4e03b6c6489c7a79bc9f6676d7f5fc31e0dddbc877bab8bece74f05', NULL, NULL, 411085000000, 0, 0, true);

--
-- Data for Name: tx_out; Type: TABLE DATA; Schema: preprod. Owner: rosetta_db_admin
--

INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (1, 'Ae2tdPwUPEZ9vtyppa1FdJzvqJZkEcXgdHxVYAzTWcPaoNycVq5rc36LC1S', false, '\x82d818582183581c8384f3ec4661544b68c77784f694ce2ad4e2097057b8a2e0ba3b36b9a0021a700c376d', NULL, 0, NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (2, 'Ae2tdPwUPEZGvXJ3ebp4LDgBhbxekAH2oKZgfahKq896fehv8oCJxmGJgLt', false, '\x82d818582183581cc99bf0a4d6e235e2df244b90aa55dd042df43f9098e63ba9f9e5057fa0021a9e6bc255', NULL, 0, NULL, 2, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (3, 'Ae2tdPwUPEYyahAbgfTxhChyWV6xMWKCDQs35Cr9qUXgzVviBrvhJ6NyUzA', false, '\x82d818582183581c1bcec52c55b70baabcbd4b0f2477970fd27fd90c85b3a8d0d86e0bb9a0021a8c67a42f', NULL, 0, NULL, 3, 30000000000000000, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (4, 'Ae2tdPwUPEZLC5VMKspod9dcf5PtUtvobxd3THtoxfR9uuzcqDghef9oiTc', false, '\x82d818582183581cea5cf6acbb664af36e1beb9aec7f75ce8f956a5b6231ce56828f7886a0021ac15a247b', NULL, 0, NULL, 4, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (5, 'Ae2tdPwUPEZ2MMeJhD8sBi3BUt78z9KaAuK6uMANczRkgdQ5jf3AwnanRkD', false, '\x82d818582183581c3791c31f0d7ff10970ee7d58231e11dbed87ab3657c7d5a4a0cc432ca0021a95f32062', NULL, 0, NULL, 5, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (6, 'Ae2tdPwUPEZ415vhqz7i5t8jP5dKsDHUmTpTGf4XxeezzJJzhaubdWm4Aj2', false, '\x82d818582183581c481c5ccf914c0461f817f31bf2a50379f6dc1f1ecf54dc9df6f17379a0021aa36bac21', NULL, 0, NULL, 6, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (7, 'Ae2tdPwUPEZ5GNoihaqR2Q43dTxBu9e9Y8m3daWcVw1bvswn4Y8APVUHqaZ', false, '\x82d818582183581c54c66982a2006ac822bbb1502474c01176744dd6dc8f17146c8eabd6a0021a8a64880a', NULL, 0, NULL, 7, 0, NULL, NULL, NULL);
INSERT INTO ${liquibase.cf_db_schema}.tx_out VALUES (8, 'Ae2tdPwUPEZH6yx1bPBeTVy2ikCuJ2aLmsXpYT622bTw6LEy9UHiu9k8KF5', false, '\x82d818582183581ccb6a9f28b78bbf047d1030cb81e731fa52b1753ccd3db3af51e04056a0021aa7475700', NULL, 0, NULL, 8, 0, NULL, NULL, NULL);


SELECT pg_catalog.setval('${liquibase.cf_db_schema}.ada_pots_id_seq', 1, false);


--
-- Name: block_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.block_id_seq', 2, false);


--
-- Name: collateral_tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.collateral_tx_in_id_seq', 1, false);


--
-- Name: collateral_tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.collateral_tx_out_id_seq', 1, false);


--
-- Name: cost_model_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.cost_model_id_seq', 1, true);


--
-- Name: datum_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.datum_id_seq', 1, false);


--
-- Name: delegation_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.delegation_id_seq', 1, false);


--
-- Name: delisted_pool_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.delisted_pool_id_seq', 1, false);


--
-- Name: epoch_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.epoch_id_seq', 1, false);


--
-- Name: epoch_param_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.epoch_param_id_seq', 1, false);


--
-- Name: epoch_stake_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.epoch_stake_id_seq', 1, false);


--
-- Name: epoch_sync_time_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.epoch_sync_time_id_seq', 1, false);


--
-- Name: extra_key_witness_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.extra_key_witness_id_seq', 1, false);


--
-- Name: ma_tx_mint_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.ma_tx_mint_id_seq', 1, false);


--
-- Name: ma_tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.ma_tx_out_id_seq', 1, false);


--
-- Name: meta_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.meta_id_seq', 1, false);


--
-- Name: multi_asset_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.multi_asset_id_seq', 1, false);


--
-- Name: param_proposal_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.param_proposal_id_seq', 1, false);


--
-- Name: pool_hash_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_hash_id_seq', 1, false);


--
-- Name: pool_metadata_ref_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_metadata_ref_id_seq', 1, false);


--
-- Name: pool_offline_data_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_offline_data_id_seq', 1, false);


--
-- Name: pool_offline_fetch_error_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_offline_fetch_error_id_seq', 1, false);


--
-- Name: pool_owner_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_owner_id_seq', 1, false);


--
-- Name: pool_relay_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_relay_id_seq', 1, false);


--
-- Name: pool_retire_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_retire_id_seq', 1, false);


--
-- Name: pool_update_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pool_update_id_seq', 1, false);


--
-- Name: pot_transfer_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.pot_transfer_id_seq', 1, false);


--
-- Name: redeemer_data_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.redeemer_data_id_seq', 1, false);


--
-- Name: redeemer_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.redeemer_id_seq', 1, false);


--
-- Name: reference_tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.reference_tx_in_id_seq', 1, false);


--
-- Name: reserve_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.reserve_id_seq', 1, false);


--
-- Name: reserved_pool_ticker_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.reserved_pool_ticker_id_seq', 1, false);


--
-- Name: reverse_index_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.reverse_index_id_seq', 1, false);


--
-- Name: reward_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.reward_id_seq', 1, false);


--
-- Name: rollback_history_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.rollback_history_id_seq', 1, false);


--
-- Name: schema_version_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.schema_version_id_seq', 1, false);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.script_id_seq', 1, false);


--
-- Name: slot_leader_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.slot_leader_id_seq', 2, false);


--
-- Name: stake_address_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.stake_address_id_seq', 1, false);


--
-- Name: stake_deregistration_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.stake_deregistration_id_seq', 1, false);


--
-- Name: stake_registration_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.stake_registration_id_seq', 1, false);


--
-- Name: treasury_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.treasury_id_seq', 1, false);


--
-- Name: tx_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.tx_id_seq', 9, false);


--
-- Name: tx_in_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.tx_in_id_seq', 1, false);


--
-- Name: tx_metadata_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.tx_metadata_id_seq', 1, false);


--
-- Name: tx_out_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.tx_out_id_seq', 9, false);


--
-- Name: withdrawal_id_seq; Type: SEQUENCE SET; Schema: preprod. Owner: rosetta_db_admin
--

SELECT pg_catalog.setval('${liquibase.cf_db_schema}.withdrawal_id_seq', 1, false);


--
-- Name: ada_pots ada_pots_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ada_pots
    ADD CONSTRAINT ada_pots_pkey PRIMARY KEY (id);


--
-- Name: block block_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.block
    ADD CONSTRAINT block_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_in collateral_tx_in_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.collateral_tx_in
    ADD CONSTRAINT collateral_tx_in_pkey PRIMARY KEY (id);


--
-- Name: collateral_tx_out collateral_tx_out_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.collateral_tx_out
    ADD CONSTRAINT collateral_tx_out_pkey PRIMARY KEY (id);


--
-- Name: cost_model cost_model_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.cost_model
    ADD CONSTRAINT cost_model_pkey PRIMARY KEY (id);


--
-- Name: datum datum_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.datum
    ADD CONSTRAINT datum_pkey PRIMARY KEY (id);


--
-- Name: delegation delegation_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);


--
-- Name: delisted_pool delisted_pool_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delisted_pool
    ADD CONSTRAINT delisted_pool_pkey PRIMARY KEY (id);


--
-- Name: epoch_param epoch_param_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_param
    ADD CONSTRAINT epoch_param_pkey PRIMARY KEY (id);


--
-- Name: epoch epoch_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch
    ADD CONSTRAINT epoch_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_sync_time epoch_sync_time_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_sync_time
    ADD CONSTRAINT epoch_sync_time_pkey PRIMARY KEY (id);


--
-- Name: extra_key_witness extra_key_witness_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.extra_key_witness
    ADD CONSTRAINT extra_key_witness_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_mint ma_tx_mint_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_pkey PRIMARY KEY (id);


--
-- Name: ma_tx_out ma_tx_out_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_out
    ADD CONSTRAINT ma_tx_out_pkey PRIMARY KEY (id);


--
-- Name: meta meta_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.meta
    ADD CONSTRAINT meta_pkey PRIMARY KEY (id);


--
-- Name: multi_asset multi_asset_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.multi_asset
    ADD CONSTRAINT multi_asset_pkey PRIMARY KEY (id);


--
-- Name: param_proposal param_proposal_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.param_proposal
    ADD CONSTRAINT param_proposal_pkey PRIMARY KEY (id);


--
-- Name: pool_hash pool_hash_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_hash
    ADD CONSTRAINT pool_hash_pkey PRIMARY KEY (id);


--
-- Name: pool_metadata_ref pool_metadata_ref_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_data pool_offline_data_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pkey PRIMARY KEY (id);


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pkey PRIMARY KEY (id);


--
-- Name: pool_owner pool_owner_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_owner
    ADD CONSTRAINT pool_owner_pkey PRIMARY KEY (id);


--
-- Name: pool_relay pool_relay_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_relay
    ADD CONSTRAINT pool_relay_pkey PRIMARY KEY (id);


--
-- Name: pool_retire pool_retire_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_retire
    ADD CONSTRAINT pool_retire_pkey PRIMARY KEY (id);


--
-- Name: pool_update pool_update_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT pool_update_pkey PRIMARY KEY (id);


--
-- Name: pot_transfer pot_transfer_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pot_transfer
    ADD CONSTRAINT pot_transfer_pkey PRIMARY KEY (id);


--
-- Name: redeemer_data redeemer_data_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer_data
    ADD CONSTRAINT redeemer_data_pkey PRIMARY KEY (id);


--
-- Name: redeemer redeemer_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer
    ADD CONSTRAINT redeemer_pkey PRIMARY KEY (id);


--
-- Name: reference_tx_in reference_tx_in_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reference_tx_in
    ADD CONSTRAINT reference_tx_in_pkey PRIMARY KEY (id);


--
-- Name: reserve reserve_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserve
    ADD CONSTRAINT reserve_pkey PRIMARY KEY (id);


--
-- Name: reserved_pool_ticker reserved_pool_ticker_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserved_pool_ticker
    ADD CONSTRAINT reserved_pool_ticker_pkey PRIMARY KEY (id);


--
-- Name: reverse_index reverse_index_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reverse_index
    ADD CONSTRAINT reverse_index_pkey PRIMARY KEY (id);


--
-- Name: reward reward_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);


--
-- Name: rollback_history rollback_history_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.rollback_history
    ADD CONSTRAINT rollback_history_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.schema_version
    ADD CONSTRAINT schema_version_pkey PRIMARY KEY (id);


--
-- Name: script script_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.script
    ADD CONSTRAINT script_pkey PRIMARY KEY (id);


--
-- Name: slot_leader slot_leader_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.slot_leader
    ADD CONSTRAINT slot_leader_pkey PRIMARY KEY (id);


--
-- Name: stake_address stake_address_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_address
    ADD CONSTRAINT stake_address_pkey PRIMARY KEY (id);


--
-- Name: stake_deregistration stake_deregistration_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration
    ADD CONSTRAINT stake_deregistration_pkey PRIMARY KEY (id);


--
-- Name: stake_registration stake_registration_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_registration
    ADD CONSTRAINT stake_registration_pkey PRIMARY KEY (id);


--
-- Name: treasury treasury_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.treasury
    ADD CONSTRAINT treasury_pkey PRIMARY KEY (id);


--
-- Name: tx_in tx_in_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in
    ADD CONSTRAINT tx_in_pkey PRIMARY KEY (id);


--
-- Name: tx_metadata tx_metadata_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_metadata
    ADD CONSTRAINT tx_metadata_pkey PRIMARY KEY (id);


--
-- Name: tx_out tx_out_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT tx_out_pkey PRIMARY KEY (id);


--
-- Name: tx tx_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx
    ADD CONSTRAINT tx_pkey PRIMARY KEY (id);


--
-- Name: ada_pots uk_143qflkqxvmvp4cukodhskt43; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ada_pots
    ADD CONSTRAINT uk_143qflkqxvmvp4cukodhskt43 UNIQUE (block_id);


--
-- Name: block unique_block; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.block
    ADD CONSTRAINT unique_block UNIQUE (hash);


--
-- Name: cost_model unique_cost_model; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.cost_model
    ADD CONSTRAINT unique_cost_model UNIQUE (hash);


--
-- Name: datum unique_datum; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.datum
    ADD CONSTRAINT unique_datum UNIQUE (hash);


--
-- Name: delegation unique_delegation; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT unique_delegation UNIQUE (tx_id, cert_index);


--
-- Name: delisted_pool unique_delisted_pool; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delisted_pool
    ADD CONSTRAINT unique_delisted_pool UNIQUE (hash_raw);


--
-- Name: epoch unique_epoch; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch
    ADD CONSTRAINT unique_epoch UNIQUE (no);


--
-- Name: epoch_param unique_epoch_param; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_param
    ADD CONSTRAINT unique_epoch_param UNIQUE (epoch_no, block_id);


--
-- Name: epoch_sync_time unique_epoch_sync_time; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_sync_time
    ADD CONSTRAINT unique_epoch_sync_time UNIQUE (no);


--
-- Name: ma_tx_mint unique_ma_tx_mint; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_mint
    ADD CONSTRAINT unique_ma_tx_mint UNIQUE (ident, tx_id);


--
-- Name: ma_tx_out unique_ma_tx_out; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_out
    ADD CONSTRAINT unique_ma_tx_out UNIQUE (ident, tx_out_id);


--
-- Name: meta unique_meta; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.meta
    ADD CONSTRAINT unique_meta UNIQUE (start_time);


--
-- Name: multi_asset unique_multi_asset; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.multi_asset
    ADD CONSTRAINT unique_multi_asset UNIQUE (policy, name);


--
-- Name: param_proposal unique_param_proposal; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.param_proposal
    ADD CONSTRAINT unique_param_proposal UNIQUE (key, registered_tx_id);


--
-- Name: pool_hash unique_pool_hash; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_hash
    ADD CONSTRAINT unique_pool_hash UNIQUE (hash_raw);


--
-- Name: pool_metadata_ref unique_pool_metadata_ref; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_metadata_ref
    ADD CONSTRAINT unique_pool_metadata_ref UNIQUE (pool_id, url, hash);


--
-- Name: pool_offline_data unique_pool_offline_data; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_data
    ADD CONSTRAINT unique_pool_offline_data UNIQUE (pool_id, hash);


--
-- Name: pool_offline_fetch_error unique_pool_offline_fetch_error; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_fetch_error
    ADD CONSTRAINT unique_pool_offline_fetch_error UNIQUE (pool_id, fetch_time, retry_count);


--
-- Name: pool_owner unique_pool_owner; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_owner
    ADD CONSTRAINT unique_pool_owner UNIQUE (addr_id, pool_update_id);


--
-- Name: pool_relay unique_pool_relay; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_relay
    ADD CONSTRAINT unique_pool_relay UNIQUE (update_id, ipv4, ipv6, dns_name);


--
-- Name: pool_retire unique_pool_retiring; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_retire
    ADD CONSTRAINT unique_pool_retiring UNIQUE (announced_tx_id, cert_index);


--
-- Name: pool_update unique_pool_update; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT unique_pool_update UNIQUE (registered_tx_id, cert_index);


--
-- Name: pot_transfer unique_pot_transfer; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pot_transfer
    ADD CONSTRAINT unique_pot_transfer UNIQUE (tx_id, cert_index);


--
-- Name: redeemer unique_redeemer; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer
    ADD CONSTRAINT unique_redeemer UNIQUE (tx_id, purpose, index);


--
-- Name: redeemer_data unique_redeemer_data; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer_data
    ADD CONSTRAINT unique_redeemer_data UNIQUE (hash);


--
-- Name: reference_tx_in unique_ref_txin; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reference_tx_in
    ADD CONSTRAINT unique_ref_txin UNIQUE (tx_in_id, tx_out_id, tx_out_index);


--
-- Name: reserved_pool_ticker unique_reserved_pool_ticker; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserved_pool_ticker
    ADD CONSTRAINT unique_reserved_pool_ticker UNIQUE (name);


--
-- Name: reserve unique_reserves; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserve
    ADD CONSTRAINT unique_reserves UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: reward unique_reward; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);


--
-- Name: script unique_script; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.script
    ADD CONSTRAINT unique_script UNIQUE (hash);


--
-- Name: slot_leader unique_slot_leader; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.slot_leader
    ADD CONSTRAINT unique_slot_leader UNIQUE (hash);


--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);


--
-- Name: stake_address unique_stake_address; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_address
    ADD CONSTRAINT unique_stake_address UNIQUE (hash_raw);


--
-- Name: stake_deregistration unique_stake_deregistration; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration
    ADD CONSTRAINT unique_stake_deregistration UNIQUE (tx_id, cert_index);


--
-- Name: stake_registration unique_stake_registration; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_registration
    ADD CONSTRAINT unique_stake_registration UNIQUE (tx_id, cert_index);


--
-- Name: treasury unique_treasury; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.treasury
    ADD CONSTRAINT unique_treasury UNIQUE (addr_id, tx_id, cert_index);


--
-- Name: tx unique_tx; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx
    ADD CONSTRAINT unique_tx UNIQUE (hash);


--
-- Name: tx_metadata unique_tx_metadata; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_metadata
    ADD CONSTRAINT unique_tx_metadata UNIQUE (key, tx_id);


--
-- Name: tx_in unique_txin; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in
    ADD CONSTRAINT unique_txin UNIQUE (tx_out_id, tx_out_index);


--
-- Name: tx_out unique_txout; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT unique_txout UNIQUE (tx_id, index);


--
-- Name: extra_key_witness unique_witness; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.extra_key_witness
    ADD CONSTRAINT unique_witness UNIQUE (hash);


--
-- Name: withdrawal withdrawal_pkey; Type: CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.withdrawal
    ADD CONSTRAINT withdrawal_pkey PRIMARY KEY (id);


--
-- Name: ada_pots ada_pots_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ada_pots
    ADD CONSTRAINT ada_pots_block_id_fkey FOREIGN KEY (block_id) REFERENCES ${liquibase.cf_db_schema}.block(id) ON DELETE CASCADE;


--
-- Name: datum datum_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.datum
    ADD CONSTRAINT datum_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT delegation_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT delegation_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT delegation_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES ${liquibase.cf_db_schema}.redeemer(id) ON DELETE CASCADE;


--
-- Name: delegation delegation_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.delegation
    ADD CONSTRAINT delegation_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_param
    ADD CONSTRAINT epoch_param_block_id_fkey FOREIGN KEY (block_id) REFERENCES ${liquibase.cf_db_schema}.block(id) ON DELETE CASCADE;


--
-- Name: epoch_param epoch_param_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_param
    ADD CONSTRAINT epoch_param_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES ${liquibase.cf_db_schema}.cost_model(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_stake
    ADD CONSTRAINT epoch_stake_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: epoch_stake epoch_stake_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.epoch_stake
    ADD CONSTRAINT epoch_stake_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: extra_key_witness extra_key_witness_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.extra_key_witness
    ADD CONSTRAINT extra_key_witness_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: slot_leader slot_leader_pool_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.slot_leader
    ADD CONSTRAINT slot_leader_pool_hash_id_fkey FOREIGN KEY (pool_hash_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: block block_previous_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.block
    ADD CONSTRAINT block_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES ${liquibase.cf_db_schema}.block(id) ON DELETE CASCADE;


--
-- Name: block block_slot_leader_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.block
    ADD CONSTRAINT block_slot_leader_id_fkey FOREIGN KEY (slot_leader_id) REFERENCES ${liquibase.cf_db_schema}.slot_leader(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_ident_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_ident_fkey FOREIGN KEY (ident) REFERENCES ${liquibase.cf_db_schema}.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_mint ma_tx_mint_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_mint
    ADD CONSTRAINT ma_tx_mint_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_ident_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_out
    ADD CONSTRAINT ma_tx_out_ident_fkey FOREIGN KEY (ident) REFERENCES ${liquibase.cf_db_schema}.multi_asset(id) ON DELETE CASCADE;


--
-- Name: ma_tx_out ma_tx_out_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.ma_tx_out
    ADD CONSTRAINT ma_tx_out_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES ${liquibase.cf_db_schema}.tx_out(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_cost_model_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.param_proposal
    ADD CONSTRAINT param_proposal_cost_model_id_fkey FOREIGN KEY (cost_model_id) REFERENCES ${liquibase.cf_db_schema}.cost_model(id) ON DELETE CASCADE;


--
-- Name: param_proposal param_proposal_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.param_proposal
    ADD CONSTRAINT param_proposal_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_metadata_ref pool_metadata_ref_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_metadata_ref
    ADD CONSTRAINT pool_metadata_ref_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pmr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES ${liquibase.cf_db_schema}.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_data pool_offline_data_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_data
    ADD CONSTRAINT pool_offline_data_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pmr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pmr_id_fkey FOREIGN KEY (pmr_id) REFERENCES ${liquibase.cf_db_schema}.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_offline_fetch_error pool_offline_fetch_error_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_offline_fetch_error
    ADD CONSTRAINT pool_offline_fetch_error_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_owner
    ADD CONSTRAINT pool_owner_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: pool_owner pool_owner_pool_update_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_owner
    ADD CONSTRAINT pool_owner_pool_update_id_fkey FOREIGN KEY (pool_update_id) REFERENCES ${liquibase.cf_db_schema}.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_relay pool_relay_update_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_relay
    ADD CONSTRAINT pool_relay_update_id_fkey FOREIGN KEY (update_id) REFERENCES ${liquibase.cf_db_schema}.pool_update(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_announced_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_retire
    ADD CONSTRAINT pool_retire_announced_tx_id_fkey FOREIGN KEY (announced_tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: pool_retire pool_retire_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_retire
    ADD CONSTRAINT pool_retire_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_hash_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT pool_update_hash_id_fkey FOREIGN KEY (hash_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_meta_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT pool_update_meta_id_fkey FOREIGN KEY (meta_id) REFERENCES ${liquibase.cf_db_schema}.pool_metadata_ref(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_registered_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT pool_update_registered_tx_id_fkey FOREIGN KEY (registered_tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: pool_update pool_update_reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pool_update
    ADD CONSTRAINT pool_update_reward_addr_id_fkey FOREIGN KEY (reward_addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: pot_transfer pot_transfer_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.pot_transfer
    ADD CONSTRAINT pot_transfer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer_data redeemer_data_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer_data
    ADD CONSTRAINT redeemer_data_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_redeemer_data_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer
    ADD CONSTRAINT redeemer_redeemer_data_id_fkey FOREIGN KEY (redeemer_data_id) REFERENCES ${liquibase.cf_db_schema}.redeemer_data(id) ON DELETE CASCADE;


--
-- Name: redeemer redeemer_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.redeemer
    ADD CONSTRAINT redeemer_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: reference_tx_in reference_tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reference_tx_in
    ADD CONSTRAINT reference_tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserve
    ADD CONSTRAINT reserve_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: reserve reserve_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reserve
    ADD CONSTRAINT reserve_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: reward reward_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reward
    ADD CONSTRAINT reward_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: reward reward_pool_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.reward
    ADD CONSTRAINT reward_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES ${liquibase.cf_db_schema}.pool_hash(id) ON DELETE CASCADE;


--
-- Name: script script_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.script
    ADD CONSTRAINT script_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration
    ADD CONSTRAINT stake_deregistration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration
    ADD CONSTRAINT stake_deregistration_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES ${liquibase.cf_db_schema}.redeemer(id) ON DELETE CASCADE;


--
-- Name: stake_deregistration stake_deregistration_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_deregistration
    ADD CONSTRAINT stake_deregistration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_registration
    ADD CONSTRAINT stake_registration_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: stake_registration stake_registration_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.stake_registration
    ADD CONSTRAINT stake_registration_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.treasury
    ADD CONSTRAINT treasury_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: treasury treasury_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.treasury
    ADD CONSTRAINT treasury_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: tx tx_block_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx
    ADD CONSTRAINT tx_block_id_fkey FOREIGN KEY (block_id) REFERENCES ${liquibase.cf_db_schema}.block(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in
    ADD CONSTRAINT tx_in_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES ${liquibase.cf_db_schema}.redeemer(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_in_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in
    ADD CONSTRAINT tx_in_tx_in_id_fkey FOREIGN KEY (tx_in_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: tx_in tx_in_tx_out_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_in
    ADD CONSTRAINT tx_in_tx_out_id_fkey FOREIGN KEY (tx_out_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: tx_metadata tx_metadata_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_metadata
    ADD CONSTRAINT tx_metadata_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_inline_datum_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT tx_out_inline_datum_id_fkey FOREIGN KEY (inline_datum_id) REFERENCES ${liquibase.cf_db_schema}.datum(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_reference_script_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT tx_out_reference_script_id_fkey FOREIGN KEY (reference_script_id) REFERENCES ${liquibase.cf_db_schema}.script(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_stake_address_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT tx_out_stake_address_id_fkey FOREIGN KEY (stake_address_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: tx_out tx_out_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.tx_out
    ADD CONSTRAINT tx_out_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_addr_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.withdrawal
    ADD CONSTRAINT withdrawal_addr_id_fkey FOREIGN KEY (addr_id) REFERENCES ${liquibase.cf_db_schema}.stake_address(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_redeemer_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.withdrawal
    ADD CONSTRAINT withdrawal_redeemer_id_fkey FOREIGN KEY (redeemer_id) REFERENCES ${liquibase.cf_db_schema}.redeemer(id) ON DELETE CASCADE;


--
-- Name: withdrawal withdrawal_tx_id_fkey; Type: FK CONSTRAINT; Schema: preprod. Owner: rosetta_db_admin
--

ALTER TABLE ONLY ${liquibase.cf_db_schema}.withdrawal
    ADD CONSTRAINT withdrawal_tx_id_fkey FOREIGN KEY (tx_id) REFERENCES ${liquibase.cf_db_schema}.tx(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--
