---
sidebar_position: 5
title: Index Management
description: Understanding and customizing database index creation
---

# Index Management

## Overview

Starting from v2.1, Rosetta manages the required database indexes natively inside `yaci-indexer`. After blockchain sync reaches tip, the indexer automatically creates all required indexes without any external sidecar container.

## Sync Stages

The `/network/status` endpoint reports three distinct stages:

| Stage | Description |
|-------|-------------|
| `SYNCING` | Initial blockchain synchronization in progress |
| `APPLYING_INDEXES` | Sync complete, creating database indexes |
| `LIVE` | Fully operational, all indexes applied and valid |

## Index Lifecycle

The `yaci-indexer` manages indexes through the following states:

1. Detects on startup whether all required indexes already exist (skips directly to LIVE if so)
2. After reaching sync tip, creates all required indexes using `CREATE INDEX CONCURRENTLY`
3. Continues building remaining indexes even when individual ones fail
4. Gates readiness probe on index completion — the API stays ready once indexes are `READY`

**Performance:** Index creation takes approximately 6 hours on mainnet.

## Monitoring Index Creation

Check index creation progress via the actuator endpoint:

```bash
curl -s http://localhost:9095/actuator/rosetta-indexes | jq
```

Example response:

```json
{
  "overallState": "APPLYING",
  "lastProgressAt": "2026-04-22T10:31:05Z",
  "totalRequired": 16,
  "totalReady": 9,
  "totalMissing": 6,
  "totalFailed": 1,
  "indexes": [
    { "name": "idx_address_utxo_amounts_gin", "state": "READY",   "errorMessage": null },
    { "name": "idx_tx_input_tx_hash",         "state": "BUILDING", "errorMessage": null },
    { "name": "idx_block_hash",               "state": "FAILED",   "errorMessage": "permission denied for table block" }
  ]
}
```

Check PostgreSQL-level progress during active index builds:

```bash
docker compose exec db psql -U rosetta_db_admin -d rosetta-java -c "
  SELECT phase, blocks_done, blocks_total
  FROM pg_stat_progress_create_index"
```

## Customizing Indexes

To add custom indexes, edit the catalog YAML in both modules (the parity test enforces they stay identical):

```
api/src/main/resources/config/db-indexes.yaml
yaci-indexer/src/main/resources/config/db-indexes.yaml
```

Add your index definition in both files:

```yaml
cardano:
  rosetta:
    db_indexes:
      - name: idx_my_custom_index
        command: "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_my_custom_index ON my_table (my_column)"
```

Then rebuild and restart yaci-indexer:

```bash
docker compose build yaci-indexer
docker compose restart yaci-indexer
```

:::caution
The indexer detects existing indexes on startup and only creates missing ones. If you add an index to a running system, restarting `yaci-indexer` is sufficient — no full stack restart is needed.
:::
