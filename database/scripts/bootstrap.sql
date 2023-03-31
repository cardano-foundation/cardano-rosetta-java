CREATE DATABASE :"cf_dbname";
CREATE USER :"cf_serviceuser_name" WITH PASSWORD :'cf_serviceuser_secret';
GRANT CONNECT ON DATABASE :"cf_dbname" TO :"cf_serviceuser_name";