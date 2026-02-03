---
sidebar_position: 3
title: Environment Variables
description: Configuration environment variables for Cardano Rosetta Java
---

# Environment variables

Environment variables configure the behavior of the application. This page documents all available environment variables, their default values, and version history.

## Configuration Files

The root folder contains several example `.env` files for different deployment scenarios:

**Standard Deployments:**
- `.env.docker-compose` - Standard docker compose setup for mainnet (copy and adjust for your needs)
- `.env.docker-compose-preprod` - Pre-configured for preprod network
- `.env.h2` - Local development with H2 in-memory database
- `.env.h2-testdata` - H2 database with test data

**Hardware Performance Profiles:**
- `.env.docker-compose-profile-entry-level` - Minimal hardware requirements
- `.env.docker-compose-profile-mid-level` - Moderate hardware (recommended baseline)
- `.env.docker-compose-profile-advanced-level` - High-performance hardware

**Testing:**
- `.env.IntegrationTest` - Integration tests with yaci devkit

:::note
Hardware profile files should be used **in combination** with a base `.env.docker-compose` file to apply performance tuning settings. See the Hardware Profile Variables section below.
:::

## Main Environment Variables

<div class="env-vars-table">

| Variable                                      | Description                                                           | Default                                | Notes                   |
|-----------------------------------------------|-----------------------------------------------------------------------|----------------------------------------|-------------------------|
| `LOG`                                         | Log level                                                             | INFO                                   | added in release 1.0.0  |
| `NETWORK`                                     | Network                                                               | mainnet                                | added in release 1.0.0  |
| `MITHRIL_SYNC`                                | Sync from Mithril snapshot                                            | true                                   | added in release 1.0.0  |
| `PROTOCOL_MAGIC`                              | Cardano protocol magic                                                | 764824073                              | added in release 1.0.0  |
| `PG_VERSION_TAG`                              | Postgres version tag for building from source                         | REL_14_11                              | added in release 1.2.9  |
| `DB_NAME`                                     | Postgres database                                                     | rosetta-java                           | added in release 1.0.0  |
| `DB_USER`                                     | Postgres admin user                                                   | rosetta_db_admin                       | added in release 1.0.0  |
| `DB_SECRET`                                   | Postgres admin secret                                                 | weakpwd#123_d                          | added in release 1.0.0  |
| `DB_HOST`                                     | Postgres host                                                         | db                                     | added in release 1.0.0  |
| `DB_PORT`                                     | Postgres port                                                         | 5432                                   | added in release 1.0.0  |
| `DB_SCHEMA`                                   | Database schema                                                       | mainnet                                | added in release 1.0.0  |
| `DB_PATH`                                     | Database path                                                         | data                                   | added in release 1.0.0  |
| `CARDANO_NODE_HOST`                           | Cardano node host                                                     | cardano-node                           | added in release 1.0.0  |
| `CARDANO_NODE_PORT`                           | Cardano node port                                                     | 3001                                   | added in release 1.0.0  |
| `CARDANO_NODE_VERSION`                        | Cardano node version                                                  | 10.5.3                                 | added in release 1.0.0  |
| `CARDANO_NODE_SUBMIT_HOST`                    | Cardano node submit API host                                          | cardano-submit-api                     | added in release 1.0.0  |
| `NODE_SUBMIT_API_PORT`                        | Cardano node submit API port                                          | 8090                                   | added in release 1.0.0  |
| `CARDANO_NODE_DIR`                            | Cardano node base directory                                           | /node                                  | added in release 1.0.0  |
| `CARDANO_NODE_SOCKET_PATH`                    | Cardano node socket file path                                         | /node/node.socket                      | added in release 1.0.0  |
| `CARDANO_NODE_DB`                             | Cardano node db path                                                  | /node/db                               | added in release 1.0.0  |
| `CARDANO_CONFIG`                              | Cardano node config path (host side)                                  | ./config/node/mainnet                  | added in release 1.0.0  |
| `CARDANO_CONFIG_CONTAINER_PATH`               | Cardano node config path inside container                             | /config                                | added in release 2.0.0  |
| `MITHRIL_VERSION`                             | Mithril client version                                                | 2524.0                                 | added in release 1.2.9  |
| `SNAPSHOT_DIGEST`                             | Mithril snapshot digest                                               | latest                                 | added in release 1.0.0  |
| `AGGREGATOR_ENDPOINT`                         | Mithril aggregator endpoint (uses default if not set)                 | (empty)                                | added in release 1.0.0  |
| `GENESIS_VERIFICATION_KEY`                    | Mithril genesis verification key (uses default if not set)            | (empty)                                | added in release 1.0.0  |
| `ANCILLARY_VERIFICATION_KEY`                  | Mithril ancillary verification key (uses default if not set)          | (empty)                                | added in release 1.2.9  |
| `RELEASE_VERSION`                             | Docker image tag for API and Indexer images                           | 2.0.0                                  | added in release 2.0.0  |
| `API_SPRING_PROFILES_ACTIVE`                  | API spring profile                                                    | staging                                | added in release 1.0.0  |
| `API_PORT`                                    | Rosetta API exposed port                                              | 8082                                   | added in release 1.0.0  |
| `PRINT_EXCEPTION`                             | Print stack traces in error responses                                 | true                                   | added in release 1.0.0  |
| `TOPOLOGY_FILEPATH`                           | Topology file path                                                    | /config/topology.json                  | added in release 1.0.0  |
| `GENESIS_SHELLEY_PATH`                        | Genesis file path                                                     | /config/shelley-genesis.json           | added in release 1.0.0  |
| `GENESIS_BYRON_PATH`                          | Genesis file path                                                     | /config/byron-genesis.json             | added in release 1.0.0  |
| `GENESIS_ALONZO_PATH`                         | Genesis file path                                                     | /config/alonzo-genesis.json            | added in release 1.0.0  |
| `GENESIS_CONWAY_PATH`                         | Genesis file path                                                     | /config/conway-genesis.json            | added in release 1.0.0  |
| `SEARCH_LIMIT`                                | Search limit used in search                                           | 100                                    | added in release 1.3.2  |
| `REMOVE_SPENT_UTXOS`                          | If pruning should be enabled                                          | true                                   | added in release 1.0.0  |
| `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT`  | Number of safe blocks to keep in the store (~30 days)                 | 129600                                 | added in release 1.2.4  |
| `REMOVE_SPENT_UTXOS_BATCH_SIZE`               | Batch size for UTXO removal operations                                | 3000                                   | added in release 1.4.0  |
| `BLOCK_TRANSACTION_API_TIMEOUT_SECS`          | Timeout for fetching blocks in seconds                                | 5                                      | added in release 1.2.11 |
| `YACI_SPRING_PROFILES`                        | Yaci indexer spring profile (postgres, n2c-socket)                    | postgres,n2c-socket                    | added in release 1.0.0  |
| `MEMPOOL_ENABLED`                             | Enable mempool functionality                                          | false                                  | added in release 1.0.0  |
| `DEVKIT_ENABLED`                              | Devkit enabled                                                        | false                                  | added in release 1.0.0  |
| `DEVKIT_URL`                                  | Devkit URL                                                            | yaci-cli                               | added in release 1.0.0  |
| `DEVKIT_PORT`                                 | Devkit port                                                           | 3333                                   | added in release 1.0.0  |
| `LOG_FILE_PATH`                               | Log file directory path                                               | /var/log/rosetta-java                  | added in release 1.0.0  |
| `LOG_FILE_NAME`                               | Log file name with path                                               | /var/log/rosetta-java/rosetta-java.log | added in release 1.0.0  |
| `LOG_FILE_MAX_SIZE`                           | Maximum size per log file                                             | 10MB                                   | added in release 1.0.0  |
| `LOG_FILE_MAX_HISTORY`                        | Number of log files to retain                                         | 10                                     | added in release 1.0.0  |
| `SYNC`                                        | Enable online mode (DB/indexer start after node reaches tip)          | true                                   | added in release 1.2.0  |
| `SYNC_GRACE_SLOTS_COUNT`                      | Grace period in slots for sync status                                 | 100                                    | added in release 1.2.9  |
| `YACI_HTTP_BASE_URL`                          | Yaci Indexer's URL                                                    | http://yaci-indexer:9095/api/v1        | added in release 1.2.1  |
| `YACI_INDEXER_PORT`                           | Yaci Indexer's port                                                   | 9095                                   | added in release 1.2.1  |
| `HTTP_CONNECT_TIMEOUT_SECONDS`                | Yaci connection timeout in seconds                                    | 5                                      | added in release 1.2.1  |
| `HTTP_REQUEST_TIMEOUT_SECONDS`                | Yaci request timeout in seconds                                       | 5                                      | added in release 1.2.1  |
| `API_DB_POOL_MAX_LIFETIME_MS`                 | Maximum lifetime of connection in pool (milliseconds)                 | 2000000                                | added in release 1.2.5  |
| `API_DB_POOL_CONNECTION_TIMEOUT_MS`           | Connection timeout in milliseconds                                    | 100000                                 | added in release 1.2.5  |
| `API_DB_KEEP_ALIVE_MS`                        | Keep alive in milliseconds                                            | 60000                                  | added in release 1.2.5  |
| `API_DB_LEAK_CONNECTIONS_WARNING_MS`          | Leak connection warning threshold (ms)                                | 60000                                  | added in release 1.2.5  |
| `API_DB_MONITOR_PERFORMANCE`                  | Monitor performance enable?                                           | false                                  | added in release 1.2.5  |
| `API_DB_SHOW_SQL`                             | Show formatted SQL queries in logs for debugging                      | false                                  | added in release 1.2.5  |
| `CONTINUE_PARSING_ON_ERROR`                   | Continue processing failed to parse blocks                            | true                                   | added in release 1.3.0  |
| `PROMETHEUS_PORT`                             | Prometheus metrics port                                               | 9090                                   | added in release 1.3.0  |
| `GRAFANA_PORT`                                | Grafana dashboard port                                                | 3000                                   | added in release 1.3.0  |
| `POSTGRESQL_EXPORTER_PORT`                    | PostgreSQL exporter port for Prometheus                               | 9187                                   | added in release 1.3.0  |
| `PEER_DISCOVERY`                              | Enable peer discovery job for automatic peer refreshing               | false                                  | added in release 1.3.2  |
| `TOKEN_REGISTRY_ENABLED`                      | Enable token registry integration for native token metadata           | false                                  | added in release 1.4.0  |
| `TOKEN_REGISTRY_BASE_URL`                     | Base URL for the token registry API                                   | (empty)                                | added in release 1.4.0  |
| `TOKEN_REGISTRY_CACHE_TTL_HOURS`              | Cache TTL for token metadata in hours                                 | 12                                     | added in release 1.4.0  |
| `TOKEN_REGISTRY_LOGO_FETCH`                   | Enable fetching token logos from registry (increases response size)   | false                                  | added in release 1.4.0  |
| `TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS`      | Token registry request timeout in seconds                             | 2                                      | added in release 1.4.0  |

</div>

## Hardware Profile Variables

The following variables are available only in hardware profile configuration files (`.env.docker-compose-profile-*-level`).

<div class="env-vars-table">

| Variable                                      | Description                                                           | Default (mid-level profile)            | Notes                   |
|-----------------------------------------------|-----------------------------------------------------------------------|----------------------------------------|-------------------------|
| `API_DB_POOL_MIN_COUNT`                       | Minimum number of connections API↔DB                                  | 150                                    | added in release 1.2.5  |
| `API_DB_POOL_MAX_COUNT`                       | Maximum number of connections API↔DB                                  | 150                                    | added in release 1.2.5  |
| `DB_POSTGRES_MAX_CONNECTIONS`                 | Maximum concurrent database connections                               | 300                                    | added in release 1.2.5  |
| `DB_POSTGRES_SHARED_BUFFERS`                  | Memory for caching                                                    | 4GB                                    | added in release 1.2.6  |
| `DB_POSTGRES_EFFECTIVE_CACHE_SIZE`            | Disk cache size                                                       | 8GB                                    | added in release 1.2.6  |
| `DB_POSTGRES_WORK_MEM`                        | Memory per operation for query processing                             | 64MB                                   | added in release 1.2.6  |
| `DB_POSTGRES_MAINTENANCE_WORK_MEM`            | Memory for maintenance tasks like index creation                      | 512MB                                  | added in release 1.2.6  |
| `DB_POSTGRES_WAL_BUFFERS`                     | Write-ahead log buffer memory                                         | 512MB                                  | added in release 1.2.6  |
| `DB_POSTGRES_CHECKPOINT_COMPLETION_TARGET`    | Target checkpoint completion                                          | 0.7                                    | added in release 1.2.6  |
| `DB_POSTGRES_RANDOM_PAGE_COST`                | Cost estimate for random disk page access                             | 1.3                                    | added in release 1.2.6  |
| `DB_POSTGRES_EFFECTIVE_IO_CONCURRENCY`        | Concurrent I/O for table scans                                        | 2                                      | added in release 1.2.6  |
| `DB_POSTGRES_PARALLEL_TUPLE_COST`             | Cost per tuple in parallel queries                                    | 0.05                                   | added in release 1.2.6  |
| `DB_POSTGRES_PARALLEL_SETUP_COST`             | Cost for initiating parallel query workers                            | 500                                    | added in release 1.2.6  |
| `DB_POSTGRES_MAX_PARALLEL_WORKERS_PER_GATHER` | Parallel workers per query                                            | 4                                      | added in release 1.2.6  |
| `DB_POSTGRES_MAX_PARALLEL_WORKERS`            | Total parallel workers across all queries                             | 8                                      | added in release 1.2.6  |
| `DB_POSTGRES_SEQ_PAGE_COST`                   | Cost estimate for sequential disk page access                         | 1.0                                    | added in release 1.2.6  |
| `DB_POSTGRES_JIT`                             | Just-In-Time compilation setting                                      | off                                    | added in release 1.2.6  |
| `DB_POSTGRES_BGWRITER_LRU_MAXPAGES`           | Max pages for background writer per cycle                             | 200                                    | added in release 1.2.5  |
| `DB_POSTGRES_BGWRITER_DELAY`                  | Delay between background writer cycles                                | 200ms                                  | added in release 1.2.5  |
| `DB_POSTGRES_AUTOVACUUM_MAX_WORKERS`          | Maximum number of autovacuum workers                                  | 5                                      | added in release 1.4.0  |

</div>

## Deprecated Environment Variables (Previous Versions)

The following environment variables were available in previous versions but are no longer supported:

<div class="env-vars-table">

| Variable              | Description                                                     | Default            | Notes                                                                                         |
|-----------------------|-----------------------------------------------------------------|--------------------|-----------------------------------------------------------------------------------------------|
| `DB_IMAGE_NAME`       | Postgres docker image name                                      | postgres           | available in releases 1.0.0 - 1.2.8, replaced by `PG_VERSION_TAG` in 1.2.9                    |
| `DB_IMAGE_TAG`        | Postgres docker image tag                                       | 14.11-bullseye     | available in releases 1.0.0 - 1.2.8, replaced by `PG_VERSION_TAG` in 1.2.9                    |
| `PRUNING_ENABLED`     | Enable spent UTXO pruning to reduce storage requirements        | false              | available in releases 1.0.0 - 1.2.8, replaced by `REMOVE_SPENT_UTXOS`                         |
| `PRUNING_SAFE_BLOCKS` | Number of recent blocks to keep spent UTXOs for (safety margin) | 2160               | available in releases 1.2.4 - 1.2.8, replaced by `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT` |
| `PRUNING_INTERVAL`    | Interval in seconds between pruning cleanup jobs                | 600                | available in releases 1.2.4 - 1.2.8, no longer configurable                                   |
| `LIMIT`               | Search limit                                                    | 100                | available in releases 1.0.0 - 1.3.1, replaced by `SEARCH_LIMIT` in 1.3.2                      |
| `API_DOCKER_IMAGE_TAG`| Docker tag for API image                                        | main               | available in releases 1.0.0 - 1.4.x, replaced by `RELEASE_VERSION` in 2.0.0                   |
| `INDEXER_DOCKER_IMAGE_TAG` | Docker tag for Indexer image                               | main               | available in releases 1.0.0 - 1.4.x, replaced by `RELEASE_VERSION` in 2.0.0                   |
| `ROSETTA_VERSION`     | Rosetta spec version (was incorrectly used for image tags)      | 1.4.13             | available in releases 1.0.0 - 1.4.x, removed in 2.0.0                                         |

</div>
