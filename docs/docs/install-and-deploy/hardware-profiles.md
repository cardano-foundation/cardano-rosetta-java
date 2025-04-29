---
sidebar_position: 1
title: Hardware Profiles
description: Hardware profiles for Cardano Rosetta Java
keywords: [hardware, configuration, performance, profiles, PostgreSQL]
---

# Hardware Profiles

Hardware profiles are used to configure the hardware resources for the Cardano Rosetta Java application. They provide preconfigured sets of environment variables to optimize PostgreSQL and connection pooling based on different hardware capabilities.

## Overview

Hardware profiles are implemented as environment files that are passed to Docker or Docker Compose when launching Cardano Rosetta Java services. The entrypoint script verifies these settings and configures PostgreSQL and application connection pools accordingly.

## Available Hardware Profiles

Cardano Rosetta Java provides preconfigured hardware profiles to match different deployment scenarios:

<Tabs>
<TabItem value="entry" label="Entry-Level">

### Entry-Level Hardware Profile

Designed for development environments or smaller deployments with limited resources:

- 4 CPU cores
- 32 GB RAM
- Suitable for development, testing, or light production use

</TabItem>
<TabItem value="mid" label="Mid-Level">

### Mid-Level Hardware Profile

Optimized for production environments with moderate resources:

- 8 CPU cores
- 48 GB RAM
- Suitable for standard production deployments with moderate traffic

</TabItem>
</Tabs>

:::tip
Choose the mid-level profile for production environments with moderate to high traffic.
:::

## Hardware Profile Configuration Parameters

Hardware profiles define two main sets of configuration parameters:

### Profile Parameters Comparison

| Parameter                        | Entry-Level | Mid-Level | Purpose                                       |
| -------------------------------- | ----------- | --------- | --------------------------------------------- |
| API_DB_POOL_MIN_COUNT            | 12          | 150       | Minimum database connections                  |
| API_DB_POOL_MAX_COUNT            | 12          | 150       | Maximum database connections                  |
| DB_POSTGRES_MAX_CONNECTIONS      | 120         | 300       | Maximum PostgreSQL connections                |
| DB_POSTGRES_SHARED_BUFFERS       | 1GB         | 4GB       | Memory for data caching                       |
| DB_POSTGRES_EFFECTIVE_CACHE_SIZE | 2GB         | 8GB       | Estimate of memory available for disk caching |
| DB_POSTGRES_WORK_MEM             | 16MB        | 64MB      | Memory for query operations                   |
| DB_POSTGRES_MAINTENANCE_WORK_MEM | 128MB       | 512MB     | Memory for maintenance operations             |
| DB_POSTGRES_WAL_BUFFERS          | 16MB        | 512MB     | Memory for write-ahead logging                |
| DB_POSTGRES_MAX_PARALLEL_WORKERS | 4           | 8         | Maximum parallel query workers                |

## How to Use Hardware Profiles

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="docker" label="Docker" default>
    ```bash
    docker run --name rosetta -v {MOUNT_PATH}:/node \
      --env-file ./docker/.env.dockerfile \
      --env-file ./docker/.env.docker-profile-mid-level \
      -p 8082:8082 --shm-size=4g -d cardanofoundation/cardano-rosetta-java:latest
    ```
  </TabItem>
  <TabItem value="docker-compose" label="Docker Compose">
    ```bash
    docker compose --env-file .env.docker-compose \
      --env-file .env.docker-compose-profile-mid-level \
      -f docker-compose.yaml up -d
    ```
  </TabItem>
</Tabs>

## Custom Hardware Profiles

You can create custom hardware profiles by:

1. Copying an existing profile file, e.g., `docker/.env.docker-profile-mid-level`
2. Modifying the parameters to match your hardware
3. Using the custom profile file when launching with Docker or Docker Compose

When creating custom profiles, consider:

- Maintain a healthy ratio between `API_DB_POOL_*_COUNT` and `DB_POSTGRES_MAX_CONNECTIONS`.
- Adjust memory parameters (`DB_POSTGRES_SHARED_BUFFERS`, `DB_POSTGRES_EFFECTIVE_CACHE_SIZE`, `DB_POSTGRES_WORK_MEM`, etc.) based on your system's available RAM.
- Set `DB_POSTGRES_MAX_PARALLEL_WORKERS` according to your CPU core count.

:::info
Hardware profiles help optimize PostgreSQL and connection pooling based on your available resources.
:::
