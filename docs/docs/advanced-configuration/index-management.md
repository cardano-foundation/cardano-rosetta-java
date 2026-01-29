---
sidebar_position: 5
title: Index Management
description: Understanding and customizing database index creation
---

# Index Management

## Overview

Starting from v2.0, Rosetta uses deferred index creation to improve initial deployment time. The `index-applier` container automatically creates required indexes after initial sync completes.

## Sync Stages

The `/network/status` endpoint now reports three distinct stages:

| Stage | Description |
|-------|-------------|
| `SYNCING` | Initial blockchain synchronization in progress |
| `APPLYING_INDEXES` | Sync complete, creating database indexes |
| `LIVE` | Fully operational, all indexes applied and valid |

## Automatic Index Application

The index-applier container:
1. Waits for sync to reach `APPLYING_INDEXES` stage
2. Detects and removes invalid indexes (from failed operations)
3. Creates all required indexes using `CREATE INDEX CONCURRENTLY`
4. Exits when complete (Rosetta transitions to `LIVE`)

**Performance:** Index creation takes approximately 6 hours on mainnet.

## Customizing Indexes

To add custom indexes for your use case:

**1. Edit the configuration file:**
```bash
api/src/main/resources/config/db-indexes.yaml
```

**2. Add your index definition:**
```yaml
cardano:
  rosetta:
    db_indexes:
      - name: idx_my_custom_index
        command: "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_my_custom_index ON my_table (my_column)"
```

**3. Rebuild and restart:**
```bash
docker compose build index-applier
docker compose restart index-applier
```

:::caution
The index-applier runs as a one-shot container. If you add indexes to an already-running system, you'll need to manually trigger it or restart the full stack.
:::

## Monitoring Index Creation

Check index creation progress:

```bash
# View index-applier logs
docker compose logs -f index-applier

# Check PostgreSQL index creation progress
docker compose exec db psql -U rosetta_db_admin -d rosetta-java -c "
  SELECT phase, blocks_done, blocks_total
  FROM pg_stat_progress_create_index"
```