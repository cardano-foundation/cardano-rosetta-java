--liquibase formatted sql

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