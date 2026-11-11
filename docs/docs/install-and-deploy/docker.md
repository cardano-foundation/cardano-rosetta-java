---
sidebar_position: 2
title: Getting Started with Docker Compose
description: Guide to deploying Cardano Rosetta Java using Docker Compose
---

# Deploying with Docker Compose

Starting from version 2.0.0, Docker Compose is the only supported deployment method. This guide provides instructions for deploying Cardano Rosetta Java using Docker Compose.

## Prerequisites

Before you begin, ensure you have the following installed:

- Docker
- Docker Compose
- Java 24
- For integration tests: Node 14+

## Deployment

### Using Docker Compose

1. Clone the repository
```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
```
2. Use the provided environment files or modify them if necessary:
   - The default configuration is in `.env.docker-compose`
   - Choose a hardware profile from the available options (see [Hardware Profiles](./hardware-profiles) for details):

```bash
# Entry level hardware profile
docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-entry-level \
  -f docker-compose.yaml up -d

# Mid-level hardware profile
docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-mid-level \
  -f docker-compose.yaml up -d
```

:::tip Managing Environment Files
To avoid environment variable warnings when running `docker compose` commands later (like `docker compose logs`), you can merge the environment files:
```bash
# For mainnet with mid-level profile:
cat .env.docker-compose .env.docker-compose-profile-mid-level > .env

# For preprod with entry-level profile:
cat .env.docker-compose-preprod .env.docker-compose-profile-entry-level > .env

# Now you can run commands without warnings:
docker compose ps
docker compose logs -f
```
:::

:::note
The first time you run the command, it will take significant time to build the cardano-node.
On subsequent runs, it will use the cached version.
:::

#### Syncing the Node First

If you want to sync the node first before starting other services:

```bash
# Start only the cardano-node
docker compose --env-file .env.docker-compose -f docker-compose.yaml up cardano-node

# Once the node reaches the tip, start the rest of the services
docker compose --env-file .env.docker-compose -f docker-compose.yaml up
```

:::tip Node Synchronization
To speed up the initial synchronization process, you can use [Mithril](https://mithril.network/doc/) snapshots by setting `MITHRIL_SYNC=true` in your environment file.

Mithril provides cryptographically certified blockchain snapshots for multiple Cardano networks (mainnet, preprod, preview) and is integrated directly into the Docker setup. This can reduce synchronization time from days to hours.
:::

#### Index Application

After blockchain synchronization reaches tip, `yaci-indexer` automatically creates the required database indexes using `CREATE INDEX CONCURRENTLY`. This takes approximately 6 hours on mainnet.

Monitor progress:
```bash
curl -s http://localhost:9095/actuator/rosetta-indexes | jq
```

For details on customizing indexes, see [Index Management](../advanced-configuration/index-management).

## Operation Modes

For information about running in online mode (default) or offline mode (for air-gapped environments), see the [Operation Modes](../core-concepts/operation-modes) documentation.

## Monitoring and Maintenance

### Useful Commands

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="logs" label="Viewing Logs" default>

```bash
# Follow service logs
docker compose logs -f api
docker compose logs -f yaci-indexer
docker compose logs -f cardano-node

# View all logs
docker compose logs -f
```

  </TabItem>
  <TabItem value="management" label="Service Management">

```bash
# Check service status
docker compose ps

# Stop all services
docker compose down

# Restart a specific service
docker compose restart api

# Start specific services
docker compose up -d cardano-node db
```

  </TabItem>
  <TabItem value="interactive" label="Interactive Shell">

```bash
# Get interactive bash shell in cardano-node
docker compose exec cardano-node bash

# Check node sync status
docker compose exec cardano-node cardano-cli query tip --mainnet
```

  </TabItem>
</Tabs>

### Troubleshooting Common Issues

:::caution Connection Issues
If you experience connection losses between yaci-indexer and the cardano node, you can fix it by running:

```bash
docker compose restart yaci-indexer
```

:::

### Important Container Paths

| Path                       | Description                |
| -------------------------- | -------------------------- |
| `/node/db`                 | Cardano Node Data          |
| `/var/lib/postgresql` | PostgreSQL volume. The 18.6 cluster is at `/var/lib/postgresql/18/docker` |
| `/networks`                | Network config location    |
| `/logs`                    | Log files for each service |

## Upgrading to PostgreSQL 18

The database image is PostgreSQL 18.6, built from the `REL_18_6` source tag. Official Docker Hub tag `postgres:18` is the same 18.6 release.

A data directory created by PostgreSQL 16 or 17 cannot be started by PostgreSQL 18. Resync the indexer and keep the Cardano node data, or migrate the cluster with `pg_upgrade` or dump/restore.

`pg_upgrade` requires the same data-checksum setting on both clusters. `initdb` in PostgreSQL 18 enables data checksums by default, and this image keeps that default. A source cluster created without checksums must have checksums enabled first (`pg_checksums --enable` while that server is stopped), or the new cluster must be initialized with `POSTGRES_INITDB_ARGS=--no-data-checksums`.

Mount the database volume at `/var/lib/postgresql`, not `/var/lib/postgresql/data`. The cluster files live at `/var/lib/postgresql/18/docker`. On startup, a PostgreSQL 18 cluster found at the previous location is moved there. A cluster from any other major version is left in place and the container exits with an error.

## Running Integration Tests

<Tabs>
  <TabItem value="setup" label="Setup Environment" default>

Start the test environment:

```bash
docker compose --env-file .env.IntegrationTest \
  -f docker-integration-test-environment.yaml up --build -d --wait
```

  </TabItem>
  <TabItem value="cli" label="Using CLI">

```bash
# Install newman (Node version 14+ needed)
npm install -g newman

# Run tests
newman run ./postmanTests/rosetta-java.postman_collection.json \
  -e ./postmanTests/Rosetta-java-env.postman_environment.json -r cli
```

  </TabItem>
  <TabItem value="postman" label="Using Postman">

1. Install [Postman](https://www.postman.com)
2. Import the collection:
   - `./postmanTests/rosetta-java.postman_collection.json`
3. Import the environment:
   - `./postmanTests/Rosetta-java-env.postman_environment.json`
4. Run the collection

  </TabItem>
</Tabs>

## Hardware Profiles

Cardano Rosetta Java offers different hardware profiles to optimize performance based on your available resources. These profiles configure PostgreSQL and connection pooling parameters.

For detailed information about available profiles and their configurations, see [Hardware Profiles](./hardware-profiles).
