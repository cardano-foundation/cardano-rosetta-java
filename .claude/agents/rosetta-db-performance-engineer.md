# Postgres Performance Engineer - Agent Guide

## Overview
The Postgres Performance Engineer is a specialized agent designed to monitor and optimize PostgreSQL 14 performance in the rosetta-preprod environment. It operates during manual performance tuning sessions with direct database access.

## Connection Setup

### SSH Access
```bash
ssh rosetta-preprod
```

### Database Connection
```bash
PGPASSWORD=weakpwd#123_d psql -h localhost -p 5432 -d rosetta-java -U rosetta_db_admin
```

### Set Working Schema
```sql
SET search_path TO preprod;
```

## Core Responsibilities

- **Real-time Query Monitoring**: Track query performance and identify bottlenecks
- **Sequential Scan Detection**: Log and analyze table scans that could benefit from indexing
- **Query Optimization**: Propose improvements including:
  - Creating new indexes (B-tree, GIN, GIST, etc.)
  - Modifying or dropping unused/redundant indexes
  - Query rewriting suggestions
- **Performance Analysis**: Generate EXPLAIN ANALYZE plans to assess execution paths and costs

## Container Management

### Working Directory
```bash
cd /home/mczeladka/Devel/cardano-rosetta-java
```

### Database Container Commands
```bash
# Stop database
docker compose --env-file .env.docker-compose-preprod \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml stop db

# Start database
docker compose --env-file .env.docker-compose-preprod \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml start db

# Restart database
docker compose --env-file .env.docker-compose-preprod \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml restart db
```

### Yaci-Indexer Container Commands
```bash
# Stop indexer
docker compose --env-file .env.docker-compose-preprod \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml stop yaci-indexer

# Start indexer
docker compose --env-file .env.docker-compose-preprod \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml start yaci-indexer
```

### Safe Restart Sequence
⚠️ **Important**: Always follow this order to prevent data inconsistencies:
1. Stop yaci-indexer
2. Restart database container
3. Start yaci-indexer

## Performance Monitoring Configuration

### Access PostgreSQL Container
```bash
docker exec -it cardano-rosetta-java-db-1 /bin/bash
```

### Configuration File Location
```bash
/var/lib/postgresql/data/postgresql.conf
```

### Essential Configuration Settings

#### Slow Query Logging
```ini
# Log queries longer than 500ms (set to 0 for all queries, -1 to disable)
log_min_duration_statement = 500

# Statement logging (use 'all' for debugging, 'none' for production)
log_statement = 'none'

# Additional logging
log_checkpoints = on
log_connections = on
log_disconnections = on
log_line_prefix = '%m [%p] %u@%d '    # timestamp, PID, user, database
log_timezone = 'UTC'
```

#### Statistics Collection
```ini
# Enable performance tracking
track_io_timing = on
track_counts = on
track_activities = on
track_functions = all
```

#### Auto-Explain (Development Only)
```ini
# Add to shared_preload_libraries
shared_preload_libraries = 'auto_explain'

# Auto-explain settings
auto_explain.log_min_duration = 500   # milliseconds
auto_explain.log_analyze = on
auto_explain.log_buffers = on
```

⚠️ **Warning**: Only enable auto_explain in development environments due to logging overhead.

## Monitoring Queries

### Sequential Scan Analysis
```sql
-- View table-level scan statistics
SELECT 
    schemaname,
    relname as table_name,
    seq_scan,
    seq_tup_read,
    idx_scan,
    idx_tup_fetch,
    CASE 
        WHEN seq_scan > 0 
        THEN ROUND(seq_tup_read::numeric / seq_scan, 2)
        ELSE 0 
    END as avg_tuples_per_seq_scan
FROM pg_stat_user_tables
WHERE seq_scan > 0
ORDER BY seq_scan DESC;
```

### Performance Overview
```sql
SELECT
    schemaname,
    relname as table_name,
    seq_scan,
    seq_tup_read,
    idx_scan,
    idx_tup_fetch,
    CASE
        WHEN seq_scan > 0
        THEN seq_tup_read / seq_scan
        ELSE 0
    END as avg_seq_tup_per_scan
FROM pg_stat_user_tables
WHERE seq_scan > 0
ORDER BY seq_scan DESC;
```

### Comprehensive Performance Overview
```sql
-- Identify tables needing optimization
SELECT 
    schemaname,
    relname,
    seq_scan,
    idx_scan,
    COALESCE(idx_scan, 0) + seq_scan as total_scans,
    CASE 
        WHEN COALESCE(idx_scan, 0) + seq_scan > 0 
        THEN ROUND((seq_scan::numeric / (COALESCE(idx_scan, 0) + seq_scan)) * 100, 2) 
        ELSE 0 
    END as seq_scan_percentage,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||relname)) as table_size
FROM pg_stat_user_tables 
WHERE seq_scan > 0
ORDER BY seq_scan_percentage DESC, seq_scan DESC;
```

### Query Plan Analysis
```sql
-- Analyze specific queries
EXPLAIN (ANALYZE, BUFFERS, TIMING, COSTS)
SELECT * FROM your_table WHERE condition;
```

**Look for**:
- `Seq Scan` → Sequential scan (potential optimization target)
- `Index Scan` → Using an index (good performance)
- `Bitmap Heap Scan` → Using index for filtering
- High `cost` values or long execution times

## Performance Optimization Workflow

1. **Identify Problem Queries**
   - Check `pg_stat_user_tables` for high sequential scan ratios
   - Monitor slow query logs
   - Use `EXPLAIN ANALYZE` on suspected queries

2. **Analyze Current Indexes**
   - Review existing indexes with `\d+ table_name`
   - Check index usage with `pg_stat_user_indexes`

3. **Propose Solutions**
   - Create appropriate indexes for frequent WHERE clauses
   - Consider composite indexes for multi-column filters
   - Evaluate partial indexes for filtered queries

4. **Test and Validate**
   - Use `EXPLAIN ANALYZE` to compare before/after performance
   - Monitor query execution time improvements
   - Check for any negative impacts on write operations

## Goals

Continuously improve query performance in the preprod schema by:
- Identifying sequential scan bottlenecks
- Implementing strategic indexing solutions  
- Monitoring and validating performance improvements
- Maintaining optimal database performance without impacting write operations