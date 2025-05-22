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

| Variable                     | Description                                                    | Default                               |
|------------------------------|----------------------------------------------------------------|---------------------------------------|
| `LOG`                        | Log level                                                      | INFO                                  |
| `NETWORK`                    | Network                                                        | mainnet                               |
| `MITHRIL_SYNC`               | Sync from Mithril snapshot                                     | true                                  |
| `PROTOCOL_MAGIC`             | Cardano protocol magic                                         | 764824073                             |
| `DB_IMAGE_NAME`              | Postgres docker image name                                     | postgres                              |
| `DB_IMAGE_TAG`               | Postgres docker image tag                                      | 14.11-bullseye                        |
| `DB_NAME`                    | Postgres database                                              | rosetta-java                          |
| `DB_USER`                    | Postgres admin user                                            | rosetta_db_admin                      |
| `DB_SECRET`                  | Postgres admin secret                                          | weakpwd#123_d                         |
| `DB_HOST`                    | Postgres host                                                  | db                                    |
| `DB_PORT`                    | Postgres port                                                  | 5432                                  |
| `DB_SCHEMA`                  | Database schema                                                | mainnet                               |
| `DB_PATH`                    | Database path                                                  | /data                                 |
| `CARDANO_NODE_HOST`          | Cardano node host                                              | cardano-node                          |
| `CARDANO_NODE_PORT`          | Cardano node port                                              | 3001                                  |
| `CARDANO_NODE_VERSION`       | Cardano node version                                           | 8.9.2                                 |
| `CARDANO_NODE_SUBMIT_HOST`   | Cardano node submit api host                                   | cardano-submit-api                    |
| `NODE_SUBMIT_API_PORT`       | Cardano node submit api port                                   | 8090                                  |
| `CARDANO_NODE_SOCKET_PATH`   | Cardano node socket path                                       | /node                                 |
| `CARDANO_NODE_SOCKET`        | Cardano node socket file                                       | /node/node.socket                     |
| `CARDANO_NODE_DB`            | Cardano node db path                                           | /node/db                              |
| `CARDANO_CONFIG`             | Cardano node config path                                       | /config/mainnet                       |
| `API_DOCKER_IMAGE_TAG`       | Docker Tag for API Image                                       | main                                  |
| `API_SPRING_PROFILES_ACTIVE` | API spring profile                                             | staging                               |
| `API_PORT`                   | Rosetta api exposed port                                       | 8082                                  |
| `ROSETTA_VERSION`            | Rosetta version                                                | 1.4.13                                |
| `TOPOLOGY_FILEPATH`          | Topology file path                                             | ./config/mainnet/topology.json        |
| `GENESIS_SHELLEY_PATH`       | Genesis file path                                              | ./config/mainnet/shelley-genesis.json |
| `GENESIS_BYRON_PATH`         | Genesis file path                                              | ./config/mainnet/byron-genesis.json   |
| `GENESIS_ALONZO_PATH `       | Genesis file path                                              | ./config/mainnet/alonzo-genesis.json  |
| `GENESIS_CONWAY_PATH`        | Genesis file path                                              | ./config/mainnet/conway-genesis.json  |
| `INDEXER_DOCKER_IMAGE_TAG`   | Yaci indexer Docker version                                    | main                                  |
| `REMOVE_SPENT_UTXOS`         | If spent UTxO pruning is enabled                               | true                                  |
| `YACI_SPRING_PROFILES`       | Yaci Indexer spring profile                                    | postgres                              |
| `DEVKIT_ENABLED`             | Devkit enabled (development / integration tests)               | false                                 |
| `SYNC_GRACE_SLOTS_COUNT`     | Number of absolute slots to consider rosetta node to be synced | 100                                   |