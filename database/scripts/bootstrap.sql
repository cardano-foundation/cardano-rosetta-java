CREATE DATABASE :"cf_dbname";
CREATE USER :"cf_serviceuser_name" WITH PASSWORD :'cf_serviceuser_secret';
GRANT CONNECT ON DATABASE :"cf_dbname" TO :"cf_serviceuser_name";
\c :"cf_dbname";
CREATE SCHEMA IF NOT EXISTS :"cf_db_schema";
ALTER DEFAULT PRIVILEGES IN SCHEMA :"cf_db_schema"
    GRANT USAGE, SELECT ON SEQUENCES TO :"cf_serviceuser_name";
ALTER DATABASE :"cf_dbname" SET search_path TO :"cf_db_schema";
