---
sidebar_position: 1
title: System Architecture
description: Overview of Cardano Rosetta Java architecture
---

# System Architecture

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

:::info
This solution is an implementation of the [Mesh API](https://docs.cloud.coinbase.com/rosetta/docs/welcome) (formerly known as Rosetta API) specification for Cardano Blockchain.
:::

Here and below we use [C4](https://en.wikipedia.org/wiki/C4_model) notation to describe the solution architecture.

![Context Diagram](media/ContextDiagram.drawio.svg)

_Figure 1: Context Diagram showing system boundaries and external dependencies_

The specific changes in this implementation can be found in [Cardano Specific API Additions](./cardano-addons.md)

:::tip Getting Started
To use this Rosetta API for Cardano you can build the project from source or use the pre-built docker image.

[Docker Images](https://hub.docker.com/r/cardanofoundation/cardano-rosetta-java)
:::

The solution provides Construction API (mutation of data) and Data API (read data) according to the Rosetta spec accessible via an REST API that allows you to interact with the Cardano blockchain.

## Implementation Details

The architecture consists of four essential components:

- **[Cardano Node](#cardano-node)**: The foundational layer that maintains blockchain state and connects to the Cardano network
- **[Yaci Indexer App](#yaci-indexer-app)**: Processes and transforms blockchain data into queryable database records
- **[Rosetta API App](#rosetta-api-app)**: Implements the Rosetta specification endpoints for blockchain interaction
- **[Database](#database)**: Stores optimized blockchain data for efficient API access

The Cardano Node serves as the primary source of blockchain data. The Yaci Indexer App fetches data block-by-block from the node, processes it, and stores only the necessary information in the Database, optimized for query performance.

For Data API requests, the Rosetta API App reads this indexed data directly from the Database. For Construction API requests, it uses the Cardano Node to validate and submit transactions to the Cardano network.

![Component Diagram](media/ComponentDiagram.drawio.svg)

_Figure 2: Component Diagram showing internal architecture_

## Key Components

### Cardano Node

The Cardano Node is a full implementation of the Cardano blockchain protocol that connects to the Cardano network, validates transactions and blocks, and maintains the blockchain state.

:::info
**Version**: 10.5.2 (configurable via build args)  
**Built with**: GHC 9.6.7 and Cabal 3.12.1.0  
**Runtime socket path**: `/node/node.socket`  
**Data directory**: `/node/db`  
**Network options**: mainnet, preprod, preview, devkit  
**Configuration files**: stored in `/config` directory
:::

The node is the foundation of the system, providing blockchain data to the indexer and validating/submitting transactions to the network.

### Cardano Submit API

The Cardano Submit API provides a REST interface for submitting transactions to the Cardano network.

:::info
**Port**: 8090 (configurable)  
**Configuration file**: `/cardano-submit-api-config/cardano-submit-api.yaml`  
**Connects to**: Cardano Node via its socket  
**Functionality**: Exposes HTTP endpoints for transaction submission  
**Startup**: Started by the entrypoint script after node initialization
:::

This component provides a standard interface for transaction submission, allowing the Rosetta API to submit user transactions to the Cardano network.

### Yaci Indexer App

The YACI (Yet Another Cardano Indexer) Indexer processes blockchain data from the Cardano Node and stores it in a structured format in the PostgreSQL database.

:::info
**Base library**: yaci version 0.3.5  
**API port**: 9095  
:::

The indexer features modular components that support different aspects of the Cardano blockchain, including blocks, transactions, UTXOs, staking, and epoch data. It handles efficient data processing with configurable pruning options to manage database size.

### Database

PostgreSQL database stores indexed blockchain data in a structured format optimized for API queries. While YACI store supports multiple database options, PostgreSQL is the default and recommended choice for this implementation.

:::info
**Version**: 14 (configurable)  
**Schema name**: Based on network (e.g., mainnet, preprod)  
**Default credentials**: Username: rosetta_db_admin, Password: configurable  
**Data directory**: `/var/lib/postgresql/data` (mapped to `${DB_PATH}` on the host)
:::

Configurable performance parameters through [hardware profiles](../install-and-deploy/hardware-profiles).

### Rosetta API App

The Rosetta API implements the Mesh (formerly Rosetta) API specification, providing a standardized interface for blockchain interaction. This application handles [Data API](https://docs.cloud.coinbase.com/rosetta/docs/data-api-overview) requests by reading aggregated data from the database, and manages [Construction API](https://docs.cloud.coinbase.com/rosetta/docs/construction-api-overview) requests by constructing and submitting transactions to the Cardano Node using the [cardano-client-lib](https://github.com/bloxbean/cardano-client-lib).

:::info
**Implements**: Rosetta API version 1.4.13  
**Built with**: Spring Boot 3.2.3  
**Port**: 8082  
**Java version**: 21  
**Operation modes**: Online (with node connection), Offline (transaction construction only)
:::
