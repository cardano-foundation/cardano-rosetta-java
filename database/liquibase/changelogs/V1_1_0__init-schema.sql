--liquibase formatted sql

--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
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

ALTER SCHEMA ${liquibase.cf_db_schema} OWNER TO ${liquibase.cf_serviceuser_name};

SET default_tablespace = '';

SET default_table_access_method = heap;
