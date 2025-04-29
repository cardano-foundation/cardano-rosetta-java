---
sidebar_position: 1
title: Database Tuning
description: Database performance tuning for Cardano Rosetta Java
---

In order to diagnose Postgres SQL queries

Enter docker container:
`docker exec -ti cardano-rosetta-java-db-1 /bin/bash`

Connect to psql inside of docker container:
`PGPASSWORD=weakpwd#123_d psql -h localhost -p 5432 -d rosetta-java  -U rosetta_db_admin`

**Postgres SQL Performance Monitoring Options**

Yes! You can **monitor all queries** in PostgreSQL and check whether they are using indexes using **pg_stat_statements**, logging, and EXPLAIN tools.

---

## **1. Use `pg_stat_statements` (Best for Monitoring)**

PostgreSQL provides a built-in extension `pg_stat_statements` that tracks query performance, including index usage.

### **Enable `pg_stat_statements`**

1. Edit `postgresql.conf` (inside your container):
   ```ini
   shared_preload_libraries = 'pg_stat_statements'
   track_activity_query_size = 2048
   ```
2. Restart PostgreSQL:
   ```bash
   docker restart <postgres_container_name>
   ```
3. Run inside `psql`:
   ```sql
   CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
   ```

### **Query to Check Queries Using Indexes**

```sql
SELECT query, calls, total_exec_time, rows, shared_blks_hit, shared_blks_read
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;
```

- **`shared_blks_hit` > `shared_blks_read`** → More index usage.
- If `shared_blks_read` is high, queries are scanning more blocks, possibly missing indexes.

---

## **2. Log Queries That Don't Use Indexes**

If you want to log queries that perform **sequential scans**, enable logging.

### **Edit `postgresql.conf`**

```ini
log_min_duration_statement = 500  # Log queries taking more than 500ms
log_checkpoints = on
log_connections = on
log_disconnections = on
log_lock_waits = on
log_statement = 'all'
log_temp_files = 0
log_autovacuum_min_duration = 0
```

Then restart PostgreSQL.

### **Filter Queries Using Sequential Scan**

Once logging is enabled, find queries **not using indexes**:

```bash
cat /var/lib/postgresql/data/log/postgresql.log | grep 'Seq Scan'
```

If many queries are using `Seq Scan`, they are not hitting indexes.

---

## **3. Use `EXPLAIN (ANALYZE, BUFFERS)` Automatically**

To analyze all queries dynamically:

```sql
CREATE OR REPLACE FUNCTION log_explain() RETURNS event_trigger AS $$
BEGIN
  RAISE NOTICE '%', (SELECT json_agg(row_to_json(t))
    FROM (
      SELECT query, json_agg(row_to_json(p)) AS plan
      FROM pg_stat_statements p, LATERAL (EXPLAIN (ANALYZE, BUFFERS) p.query) q
    ) t);
END;
$$ LANGUAGE plpgsql;
```

Then attach it to queries.

---

## **4. Use `auto_explain` (Automatic Logging of Query Plans)**

This extension logs execution plans automatically.

### **Enable `auto_explain`**

1. Edit `postgresql.conf`:
   ```ini
   shared_preload_libraries = 'auto_explain'
   auto_explain.log_min_duration = 500  # Log queries taking more than 500ms
   auto_explain.log_analyze = true
   auto_explain.log_buffers = true
   auto_explain.log_nested_statements = true
   ```
2. Restart PostgreSQL:
   ```bash
   docker restart <postgres_container_name>
   ```
3. Run in `psql`:
   ```sql
   LOAD 'auto_explain';
   ```

Now, PostgreSQL will log all slow queries with `EXPLAIN ANALYZE`, allowing you to detect if indexes are used.

---

## **5. Third-Party Monitoring Tools**

If you want a UI-based approach, tools like **pgAdmin**, **pganalyze**, or **pgwatch2** can track index usage visually.

---

### **Final Thoughts**

For **real-time monitoring**, use **`pg_stat_statements`**.  
For **logging slow queries**, enable **`auto_explain`**.  
For **detailed analysis**, filter logs for **`Seq Scan`**.
