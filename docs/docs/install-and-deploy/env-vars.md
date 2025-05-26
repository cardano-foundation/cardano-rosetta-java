---
sidebar_position: 3
title: Environment Variables
description: Configuration environment variables for Cardano Rosetta Java
---

# Environment variables

With environment variables the behaviour of the compiled application can be configured. The following table lists the available environment variables and their default values.

Within root folder of the project there are example `.env` files, which can be copied and adjusted to fit the needs of the deployment.

- `.env.IntegrationTest` - Is used for integration tests with yaci devkit
- `.env.docker-compose` - Is used for standard docker-compose setup (Copy this file and adjusted it to your needs)

| Variable                     | Description                  | Default                               | Notes |
| ---------------------------- | ---------------------------- | ------------------------------------- | ----- |
| `LOG`                        | Log level                    | INFO                                  | added in release 1.0.0      |
| `NETWORK`                    | Network                      | mainnet                               | added in release 1.0.0      |
| `MITHRIL_SYNC`               | Sync from Mithril snapshot   | true                                  | added in release 1.0.0      |
| `PROTOCOL_MAGIC`             | Cardano protocol magic       | 764824073                             | added in release 1.0.0      |
| `DB_IMAGE_NAME`              | Postgres docker image name   | postgres                              | added in release 1.0.0      |
| `DB_IMAGE_TAG`               | Postgres docker image tag    | 14.11-bullseye                        | added in release 1.0.0      |
| `DB_NAME`                    | Postgres database            | rosetta-java                          | added in release 1.0.0      |
| `DB_USER`                    | Postgres admin user          | rosetta_db_admin                      | added in release 1.0.0      |
| `DB_SECRET`                  | Postgres admin secret        | weakpwd#123_d                         | added in release 1.0.0      |
| `DB_HOST`                    | Postgres host                | db                                    | added in release 1.0.0      |
| `DB_PORT`                    | Postgres port                | 5432                                  | added in release 1.0.0      |
| `DB_SCHEMA`                  | Database schema              | mainnet                               | added in release 1.0.0      |
| `DB_PATH`                    | Database path                | /data                                 | added in release 1.0.0      |
| `CARDANO_NODE_HOST`          | Cardano node host            | cardano-node                          | added in release 1.0.0      |
| `CARDANO_NODE_PORT`          | Cardano node port            | 3001                                  | added in release 1.0.0      |
| `CARDANO_NODE_VERSION`       | Cardano node version         | 8.9.2                                 | added in release 1.0.0      |
| `CARDANO_NODE_SUBMIT_HOST`   | Cardano node submit api host | cardano-submit-api                    | added in release 1.0.0      |
| `NODE_SUBMIT_API_PORT`       | Cardano node submit api port | 8090                                  | added in release 1.0.0      |
| `CARDANO_NODE_SOCKET_PATH`   | Cardano node socket path     | /node                                 | added in release 1.0.0      |
| `CARDANO_NODE_SOCKET`        | Cardano node socket file     | /node/node.socket                     | added in release 1.0.0      |
| `CARDANO_NODE_DB`            | Cardano node db path         | /node/db                              | added in release 1.0.0      |
| `CARDANO_CONFIG`             | Cardano node config path     | /config/mainnet                       | added in release 1.0.0      |
| `API_DOCKER_IMAGE_TAG`       | Docker Tag for API Image     | main                                  | added in release 1.0.0      |
| `API_SPRING_PROFILES_ACTIVE` | Api spring profile           | staging                               | added in release 1.0.0      |
| `API_PORT`                   | Rosetta api exposed port     | 8082                                  | added in release 1.0.0      |
| `ROSETTA_VERSION`            | Rosetta version              | 1.4.13                                | added in release 1.0.0      |
| `TOPOLOGY_FILEPATH`          | Topology file path           | ./config/mainnet/topology.json        | added in release 1.0.0      |
| `GENESIS_SHELLEY_PATH`       | Genesis file path            | ./config/mainnet/shelley-genesis.json | added in release 1.0.0      |
| `GENESIS_BYRON_PATH`         | Genesis file path            | ./config/mainnet/byron-genesis.json   | added in release 1.0.0      |
| `GENESIS_ALONZO_PATH `       | Genesis file path            | ./config/mainnet/alonzo-genesis.json  | added in release 1.0.0      |
| `GENESIS_CONWAY_PATH`        | Genesis file path            | ./config/mainnet/conway-genesis.json  | added in release 1.0.0      |
| `INDEXER_DOCKER_IMAGE_TAG`   | Yaci indexer Docker version  | main                                  | added in release 1.0.0      |
| `PRUNING_ENABLED`            | If pruning should be enabled | false                                 | added in release 1.0.0      |
| `PRUNING_INTERVAL`           | The prunning interval in seconds               | 600                 | added in release 1.2.4      |
| `PRUNING_SAFE_BLOCKS`        | The number of safe blocks to keep in the store | 2160                | added in release 1.2.4      |
| `YACI_SPRING_PROFILES`       | Yaci indexer spring profile  | postgres                              | added in release 1.0.0      |
| `DEVKIT_ENABLED`             | Devkit enabled               | false                                 | added in release 1.0.0      |
| `YACI_HTTP_BASE_URL`         | Yaci Indexer's URL           | http://yaci-indexer:9095/api/v1       | added in release 1.2.1      |
| `YACI_INDEXER_PORT`          | Yaci Indexer's port          | 9095                                  | added in release 1.2.1      |
| `HTTP_CONNECT_TIMEOUT_SECONDS`   | Yaci connection timeout in second       | 5                      | added in release 1.2.1      |
| `HTTP_REQUEST_TIMEOUT_SECONDS`   | Yaci request timeout in second          | 5                      | added in release 1.2.1      |
| `API_DB_POOL_MIN_COUNT`          | Minimum number of connections API<->DB  | 12                     | added in release 1.2.5      |
| `API_DB_POOL_MAX_COUNT`          | Maximum number of connections API<->DB  | 12                     | added in release 1.2.5      |
| `API_DB_POOL_MAX_LIFETIME_MS`    | Description                             | 2000000                | added in release 1.2.5      |
| `API_DB_POOL_CONNECTION_TIMEOUT_MS`   | Connection time out in second      | 100000                 | added in release 1.2.5      |
| `API_DB_KEEP_ALIVE_MS`                | Keep Alive in second               | 60000                  | added in release 1.2.5      |
| `API_DB_LEAK_CONNECTIONS_WARNING_MS`  | Leak connection time in second     | 60000                  | added in release 1.2.5      |
| `API_DB_MONITOR_PERFORMANCE`          | Monitor performance enable?        | false                  | added in release 1.2.5      |
| `DB_POSTGRES_MAX_CONNECTIONS `        | Maximum concurrent database connections    | 300 (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_SHARED_BUFFERS `         | Memory for caching                         | 4GB (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_EFFECTIVE_CACHE_SIZE`    | Disk cache size                            | 8GB (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_WORK_MEM`                | Allocate memory per operation for query processing             | 64MB (mid-level profile)                        | added in release 1.2.6      |
| `DB_POSTGRES_MAINTENANCE_WORK_MEM`    | Allocate memory for maintenance tasks like index creation      | 512GB (mid-level profile)                       | added in release 1.2.6      |
| `DB_POSTGRES_WAL_BUFFERS`             | Allocate memory for writing-ahead log, buffering transactions  | 512MB (mid-level profile)                       | added in release 1.2.6      |
| `DB_POSTGRES_CHECKPOINT_COMPLETION_TARGET`   | Targets checkpoint completion                           | 0.7 (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_RANDOM_PAGE_COST `       | Cost estimate for random disk page access                      | 1.3 (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_EFFECTIVE_IO_CONCURRENCY `   | Concurrent I/O operation for table scans                   | 2 (mid-level profile)                           | added in release 1.2.6      |
| `DB_POSTGRES_PARALLEL_TUPLE_COST`     | Cost of processing each tuple in parallel queries              | 0.05 (mid-level profile)                        | added in release 1.2.6      |
| `DB_POSTGRES_PARALLEL_SETUP_COST`     | Cost for initiating parallel query workers                     | 500 (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_MAX_PARALLEL_WORKERS_PER_GATHER`   | Limits parallel workers per query                    | 4 (mid-level profile)                           | added in release 1.2.6      |
| `DB_POSTGRES_MAX_PARALLEL_WORKERS`    | Total parallel workers across all queries                      | 8 (mid-level profile)                           | added in release 1.2.6      |
| `DB_POSTGRES_SEQ_PAGE_COST `          | Cost estimate for sequential disk page access                  | 1.0                          | added in release 1.2.6      |
| `DB_POSTGRES_JIT`                     | Disables Just-In-Time compilation to reduce overhead           | off                          | added in release 1.2.6      |
| `DB_POSTGRES_BGWRITER_LRU_MAXPAGES`   | Limits background writer to clean 50 pages per cycle           | 100 (mid-level profile)                         | added in release 1.2.6      |
| `DB_POSTGRES_BGWRITER_DELAY`          | Delay between background writer cycles                         | 200ms (mid-level profile)                       | added in release 1.2.6      |



