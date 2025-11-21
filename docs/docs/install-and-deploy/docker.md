---
sidebar_position: 1
title: Getting Started with Docker
description: Guide to deploying Cardano Rosetta Java using Docker
---

# Deploying with Docker

This guide provides instructions for deploying Cardano Rosetta Java using Docker.

## Prerequisites

Before you begin, ensure you have the following installed:

- Docker
- Docker Compose
- Java 24
- For integration tests: Node 14+

## Deployment Options

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="compose" label="Docker Compose" default>

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

  </TabItem>
  <TabItem value="prebuilt" label="Pre-built Docker Image (Deprecated)">

### Using Pre-built Docker Image (Deprecated)

:::danger Deprecated Feature
⚠️ **The single Docker image deployment is deprecated and not recommended for production use.**

This method bundles all components (cardano-node, indexer, API, and PostgreSQL) into a single container, which:
- Makes resource management difficult
- Complicates debugging and maintenance
- Prevents independent component scaling
- Is less reliable for production workloads

**We strongly recommend using the Docker Compose deployment method instead**, which offers better modularity, resource management, and maintainability.
:::

For every release, pre-built docker images are available on [DockerHub](https://hub.docker.com/orgs/cardanofoundation/repositories):

```bash
docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node \
  --env-file ./docker/.env.dockerfile \
  --env-file ./docker/.env.docker-profile-mid-level \
  -p 8082:8082 --shm-size=4g -d \
  cardanofoundation/cardano-rosetta-java:1.2.8
```

#### Using the Submit API

If you want to use the `cardano-submit-api`, you can additionally expose port `8090`:

```bash
docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node \
  --env-file ./docker/.env.dockerfile \
  --env-file ./docker/.env.docker-profile-mid-level \
  -p 8090:8090 -p 8082:8082 --shm-size=4g -d \
  cardanofoundation/cardano-rosetta-java:1.2.8
```

:::tip
The `cardano-submit-api` can be used to submit raw CBOR transactions.
API documentation is available [here](https://input-output-hk.github.io/cardano-rest/submit-api/).
:::

  </TabItem>
  <TabItem value="source" label="Build from Source (Single Image)">

### Building Docker Image from Source (Single Container)

:::warning Deprecated Approach
This builds the deprecated single Docker image that bundles all components together. For production deployments, use the Docker Compose method which builds and manages components separately.
:::

```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java
cd cardano-rosetta-java
docker build -t rosetta-java -f ./docker/Dockerfile .
docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node \
  --env-file ./docker/.env.dockerfile \
  --env-file ./docker/.env.docker-profile-mid-level \
  -p 8082:8082 --shm-size=4g -d rosetta-java
```

:::warning
The build can take up to 1.5 hours. This Dockerfile builds the node, indexer, and API from source.
:::

#### Customizing Build Arguments

You can specify Cabal, GHC, Cardano node, and PostgreSQL versions when building an image:

```bash
docker build -t {image_name} --build-arg PG_VERSION=14 -f ./docker/Dockerfile .
```

Default values:

- `CABAL_VERSION=3.12.1.0`
- `GHC_VERSION=9.6.7`
- `CARDANO_NODE_VERSION=10.5.2`
- `PG_VERSION=14`

#### Configuration

Configure the Docker image using the environment file at `./docker/.env.dockerfile`, which defines all relevant variables like which network to use.

#### Running the Container

```bash
docker run --env-file ./docker/.env.dockerfile \
  --env-file .env.docker-profile-mid-level \
  -p 8082:8082 -it {image_name}:latest
```

The standard port for the API is `8082`.

#### Mounting Node Data

To mount Node data into the container:

```bash
docker run --env-file ./docker/.env.dockerfile \
  --env-file .env.docker-profile-mid-level \
  -p 8082:8082 --shm-size=4g \
  -v {node_snapshot}:/node/db -it {image_name}:latest
```

  </TabItem>
</Tabs>

## Running Modes

Cardano Rosetta Java supports several operating modes that can be configured based on your requirements:

### Initial Sync Mode

For users who don't have a fully synced node and need to start from scratch without using a snapshot:

```bash
docker run -e SYNC=true --env-file ./docker/.env.dockerfile \
  -p 8082:8082 --shm-size=4g -d {image_name}:latest
```

In this mode, the container will verify chunks and synchronize the node. When it reaches the tip, the API starts automatically.

### Online and Offline Functionality

For information about running in online mode (default) or offline mode (for air-gapped environments), see the [Operation Modes](../core-concepts/operation-modes) documentation.

## Monitoring and Maintenance

### Useful Commands

<Tabs>
  <TabItem value="logs" label="Viewing Logs" default>

```bash
# For Docker Compose deployments:
docker compose logs -f api
docker compose logs -f yaci-indexer
docker compose logs -f cardano-node

# For single container deployments (deprecated):
docker logs rosetta -f
docker exec rosetta tail -f /logs/node.log
docker exec rosetta tail -f /logs/indexer.log
```

  </TabItem>
  <TabItem value="interactive" label="Interactive Shell">

```bash
# For Docker Compose deployments:
docker exec -it cardano-rosetta-java-cardano-node-1 bash
docker exec -it cardano-rosetta-java-api-1 bash
docker exec -it cardano-rosetta-java-yaci-indexer-1 bash

# For single container deployments (deprecated):
docker exec -it rosetta bash

# Useful commands within the container
cardano-cli query tip --mainnet # check node sync status
tail -f /logs/node.log          # follow node logs
tail -f /logs/indexer.log       # follow indexer logs
```

  </TabItem>
</Tabs>

### Troubleshooting Common Issues

:::caution Connection Issues
If you experience connection losses between yaci-store and the cardano node, you can fix it by running:

```bash
docker restart yaci-indexer
```

:::

### Important Container Paths

| Path                       | Description                |
| -------------------------- | -------------------------- |
| `/node/db`                 | Cardano Node Data          |
| `/var/lib/postgresql/data` | PostgreSQL Data            |
| `/networks`                | Network config location    |
| `/logs`                    | Log files for each service |

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
