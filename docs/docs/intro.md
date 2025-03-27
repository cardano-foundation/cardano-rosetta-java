---
sidebar_position: 1
title: Home
description: Introduction to Cardano Rosetta Java implementation
---

# Introduction

## Overview

Cardano Rosetta Java is a lightweight Java implementation of the [Rosetta API](https://github.com/coinbase/mesh-specifications) for the Cardano blockchain. This implementation follows the [Rosetta API specification](https://docs.cdp.coinbase.com/mesh/docs/api-reference/) and is compatible with the [Rosetta CLI](https://docs.cdp.coinbase.com/mesh/docs/mesh-cli/), while including specific extensions to accommodate Cardano's unique features.

## Architecture

The Cardano Rosetta Java implementation consists of four key components:

- **Cardano Node**: A full Cardano node processing and validating blockchain data
- **Cardano Submit API**: An API for transaction submission to the Cardano network
- **Indexer**: A [Yaci-Store](https://github.com/bloxbean/yaci-store) based indexer that stores blockchain data in PostgreSQL
- **Rosetta API**: The implementation of the Mesh (formerly Rosetta) API specification

## System Requirements

As Yaci-Store is relatively lightweight compared to other chain indexers, the system requirements are:

- 4 CPU Cores
- 32GB RAM
- Storage:
  - 1TB (with pruning disabled - default)
  - 400GB (with pruning enabled)

Better hardware resources will improve indexer and node performance, resulting in faster chain synchronization.

## Getting Started

The simplest way to run Cardano Rosetta Java is using Docker. You can either:

- Build from source
- Use pre-built images from the Cardano Foundation [DockerHub repository](https://hub.docker.com/orgs/cardanofoundation/repositories)
- Deploy using Docker Compose

Detailed installation and configuration instructions are available in the [Getting Started with Docker](getting-started/docker) section of this documentation.
