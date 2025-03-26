---
sidebar_position: 1
title: Architecture Overview
description: Overview of Cardano Rosetta Java architecture
---

# Architecture Overview

## Context

This solution is an implementation of the [Rosetta API](https://docs.cloud.coinbase.com/rosetta/docs/welcome) specification for Cardano Blockchain.

Here and below we use [C4](https://en.wikipedia.org/wiki/C4_model) notation to describe the solution architecture.

**Figure 1. Context Diagram**

![Context Diagram](media/ContextDiagram.drawio.svg)

The specific changes in this implementation can be found in [cardano-specific-api-additions](https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/docs/cardano-specific-api-additions.md)

To use this Rosetta API for Cardano you can build the project from source or use the pre-built docker image.

[Docker Images](https://hub.docker.com/orgs/cardanofoundation/repositories?search=rosetta-java)

The solution provides Construction API (mutation of data) and Data API (read data) according to the Rosetta spec accessible via an REST API that allows you to interact with the Cardano blockchain.

## Implementation

The solution consists of the following four components:

- Cardano Node
- Yaci Indexer App (extended from [yaci-store](https://github.com/bloxbean/yaci-store))
- Rosetta API App
- Database

This solution relies on the Cardano Node to provide the blockchain data. The Cardano Node is a full node that stores the entire history of the Cardano blockchain. The Cardano Node is used to query the blockchain data and to submit transactions to the blockchain.

Yaci Indexer App retrieves data on per block basis from the Cardano Node and stores it in a database. The data stored in efficient way that is only required by the Rosetta API.

Rosetta API App in case of [Data API](https://docs.cloud.coinbase.com/rosetta/docs/data-api-overview) read data from the database and in case of [Construction API](https://docs.cloud.coinbase.com/rosetta/docs/construction-api-overview) it uses Cardano Node to submit transactions to the blockchain.

**Figure 2. Component Diagram**

![Component Diagram](media/ComponentDiagram.drawio.svg)

### Cardano Node

The [Cardano node](https://github.com/IntersectMBO/cardano-node#overview-of-the-cardano-node-repository) is the top-level component within the network. Network nodes connect to each other within the networking layer, which is the driving force for delivering information exchange requirements. This includes new block diffusion and transaction information for establishing a better data flow. Cardano nodes maintain connections with peers that have been chosen via a custom peer-selection process.
https://docs.cardano.org/learn/cardano-node

### Yaci Indexer App

For indexing data from Cardano Blockchain we are using [yaci-store](https://github.com/bloxbean/yaci-store) project. This project provides a set of Spring Boot starters with customization possibilities.

To limit data footprint we use a set of mappers to map Cardano Blockchain block data to the data only required by the Rosetta API.

### Rosetta API App

The Rosetta API App is a Spring Boot application that provides a REST API for interacting with the Cardano blockchain.

For [Data API](https://docs.cloud.coinbase.com/rosetta/docs/data-api-overview) it reads aggregated data from the database.

For [Construction API](https://docs.cloud.coinbase.com/rosetta/docs/construction-api-overview) it sends transactions into Cardano Blockchain using [cardano-client-lib](https://github.com/bloxbean/cardano-client-lib)

### Database

You can use any relational database, such as MySql, Postgres SQL or H2 database.
The scheme is created automatically by the application
(JPA).

## Deployment

We provide two modes of deployment:

- All-in-one container mode (packing everything into one container) [More details](./4.-Getting-Started-with-Docker)
- Docker compose with multiple containers [More details](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/4.-Getting-Started-with-Docker#how-to-run-with-docker-compose)

**Figure 3. Container Diagram**
![containerDiagram](https://github.com/cardano-foundation/cardano-rosetta-java/assets/15213725/9b77e714-01cc-4401-96ea-4f4bbc3a4b1d)
