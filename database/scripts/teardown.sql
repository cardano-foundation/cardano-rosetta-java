REVOKE CONNECT ON DATABASE :"cf_dbname" FROM :"cf_serviceuser_name";
SELECT pid, pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = :'cf_dbname' AND pid <> pg_backend_pid();
DROP DATABASE IF EXISTS :"cf_dbname";
DROP USER IF EXISTS :"cf_serviceuser_name";