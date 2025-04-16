---
sidebar_position: 1
title: Getting Started with Docker
description: Guide to deploying Cardano Rosetta Java using Docker
---

# Run with Docker

## Prerequisites

- Docker
- Docker Compose
- Java 21
- For integration tests: Node 14+

### Node Prerequisites

It is recommend to reach the Tip with the node before starting to sync to avoid instabilities between the connection of the node and the indexer.
To speed the process up you can rely on snapshots which can be obtained here: [Cardano Blockchain Snapshots](https://csnapshots.io/)

Known instabilities are:

- connection losses between yaci-store and the cardano node - Can be fixed by running `docker restart yaci-indexer`

## How to run with Docker Compose

- Clone the repository
- For local environment:
  - Copy `.env.docker-compose` to `.env`
  - Fill the `.env` file with your values (explain below) or use the provided for docker-compose setup
  - Run `docker compose -f docker-compose.yaml up --build` to start rosetta api service including yaci-store and a cardano node
  - Using the provided env file `docker-compose --env-file .env.docker-compose -f docker-compose.yaml up --build`

* Note: the first time you run the command, it will take a little bit of your time to build the cardano-node, and next time it will be cached when run. So please be patient.

If you want to sync it by yourself you can start the node alone by running (.env file used from next chapter):

`docker-compose --env-file .env.docker-compose -f docker-compose.yaml up cardano-node`

As soon as the node reached the tip you can start the rest by running:

`docker-compose --env-file .env.docker-compose -f docker-compose.yaml up`

## How to run integration tests

- Run `docker compose --env-file .env.IntegrationTest -f docker-integration-test-environment.yaml up --build -d --wait`
- Using CLI
  - Install newman `npm install -g newman` (Node version 14+ needed)
  - Run `newman run ./postmanTests/rosetta-java.postman_collection.json -e ./postmanTests/Rosetta-java-env.postman_environment.json -r cli`
- Using Postman
  - Install [Postman](https://www.postman.com)
  - Import the collection `./postmanTests/rosetta-java.postman_collection.json`
  - Import the environment `./postmanTests/Rosetta-java-env.postman_environment.json`
  - Run the collection

# Build standalone Docker Container

## Introduction

Dockerfile contains cardano-node, Postgres and Cardano Rosetta API Java implementation

The Cardano-node, api and yaci indexer are compiled from source while the build process.
Postgres is installed from the official repository.

### 0. Configuration of the Docker Image

We are using an environment file for alle configuration possibilities. These can be found at `./docker/.env.dockerfile`.
Within this file all relevant variables are defined e.g. which network to use.

### 1. How to build the Docker Image from Source

```
docker build -t {image_name} -f ./docker/Dockerfile .
```

The build can take up to 1.5 hours. This dockerfile takes care takes care of building the node, indexer and api from source.
You can specify Cabal, GHC, Cardano node, and Postgres versions when building an image.

```
docker build -t {image_name} --build-arg PG_VERSION=14 -f ./docker/Dockerfile .
```

The default values:  
`CABAL_VERSION=3.8.1.0`  
`GHC_VERSION=8.10.7  `  
`CARDANO_NODE_VERSION=8.9.2  `  
`PG_VERSION=14  `

### 2. How to run the container

```
docker run --env-file ./docker/.env.dockerfile --env-file .env.docker-profile-mid-level -p 8082:8082 -it {image_name}:latest
```

You need to specify the path to the environment variables file and open the port. The standard Port for the API is `8082`.

It is recommended to use a fully synced node, otherwise it can lead to instabilities during the sync process. To speed up the process it is also recommended to use Snapshots, how to use these is explained in Chapter **Node Prerequisites**

To mount Node data into the container use the following command:

```
docker run --env-file ./docker/.env.dockerfile --env-file .env.docker-profile-mid-level -p 8082:8082 --shm-size=4g -v {node_snapshot}:/node/db -it {image_name}:latest
```

Relevant datapathes within the container:

- `/node/db` - Cardano Node Data
- `/node/postgres` - Postgres Data
- `/networks` - Network config location
- `/logs` - Log files for each service

### 3. Synchronization mode

The synchronization mode is to for users who don't have a fully synced node and need to start from scratch without using a snapshot.

```
docker run -e SYNC=true --env-file .\docker\.env.dockerfile -p 8082:8082 --shm-size=4g -d {image_name}:latest
```

The container can be started in synchronization mode. In this case, the container will verify chunks and synchronize the node, when it reaches the tip, the API is started automatically.
To start it you need to change the SYNC variable in .env.dockerfile or by adding the -e SYNC=true key when starting the container.
Progress can be tracked in the container log.
