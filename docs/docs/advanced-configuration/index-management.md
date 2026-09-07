---
sidebar_position: 5
title: Index Management
description: Understanding and customizing database index creation
---

# Index Management

## Overview

Starting from v2.3.0, Rosetta manages the required database indexes natively inside `yaci-indexer`. After blockchain sync reaches tip, the indexer automatically creates all required indexes without any external sidecar container.

:::info Upgrading from v2.2.x or earlier
Older releases applied indexes through a separate `index-applier` container (a shell
script looping over `psql`). That container, its `docker-compose-index-applier.yaml`
file, and the Helm `indexApplier` job no longer exist. No action is needed on upgrade —
`yaci-indexer` detects the already-existing indexes on startup and reports `LIVE`
immediately. If your own tooling referenced the `index-applier` container or its compose
file, remove those references.
:::

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

**Crash recovery:** if `yaci-indexer` is restarted mid-build, any index left behind in an
`INVALID` or half-built `BUILDING` state is detected on the next run, dropped
(schema-qualified `DROP INDEX CONCURRENTLY`), and rebuilt automatically. No manual
cleanup is required.

**Performance:** Index creation takes approximately 6 hours on mainnet.

## Health Probes

While indexes are being created, the indexer's Spring Actuator health groups behave as follows:

| Probe | Endpoint | Behavior |
|-------|----------|----------|
| Readiness | `/actuator/health/readiness` | `DOWN` until blockchain sync reaches tip **and** all required indexes are `READY` |
| Liveness | `/actuator/health/liveness` | `DOWN` only when a build stalls — no per-index progress for `INDEX_STALL_TIMEOUT_MINUTES` while in `APPLYING` |

A `FAILED` index build keeps readiness `DOWN` but liveness `UP`. This is intentional: a
stalled build warrants a container restart, while a permanently failed index (for example
a permissions or disk-space problem) does not — restarting would not fix it. Diagnose the
per-index `errorMessage` via `/actuator/rosetta-indexes` and see the retry settings below.

## Failure Retries and Timeouts

If an index build fails, the per-index state is reported as `FAILED` at `/actuator/rosetta-indexes`.
`yaci-indexer` retries failed index creation with a configurable delay and retry cap:

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `INDEX_FAILED_RETRY_MAX_ATTEMPTS` | `3` | Maximum retry attempts after an index build enters `FAILED` |
| `INDEX_FAILED_RETRY_DELAY_MINUTES` | `5` | Delay before retrying `FAILED -> APPLYING` |
| `INDEX_QUERY_TIMEOUT_SECONDS` | `21600` | JDBC statement timeout for each `CREATE/DROP INDEX CONCURRENTLY` statement |
| `INDEX_STALL_TIMEOUT_MINUTES` | `360` | Liveness stall timeout while an index build is `APPLYING` |

Keep `INDEX_STALL_TIMEOUT_MINUTES` greater than the longest expected single index build.
For mainnet, keep both the stall timeout and query timeout high enough for the largest GIN index build.
If retries are exhausted, fix the underlying database issue (for example permissions or disk space) and restart `yaci-indexer` or redeploy to begin a fresh retry cycle.

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
