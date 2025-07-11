[![Build](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/feature-mvn-build.yaml/badge.svg)](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/feature-mvn-build.yaml)
[![License](https://img.shields.io:/github/license/cardano-foundation/cardano-rosetta-java?label=license)](https://github.com/cardano-foundation/cardano-rosetta-java/blob/master/LICENSE)
![Discord](https://img.shields.io/discord/1022471509173882950)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=coverage)](https://sonarcloud.io/summary/overall?id=cardano-foundation_cardano-rosetta-java)

## What the project is about?

This repository provides a lightweight java implementation of the [Rosetta API](https://github.com/coinbase/mesh-specifications). It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from a Cardano node.

This component consists of:

- a full Cardano node
- a Cardano Submit API
- an indexer which stores data in Postgres
- the Mesh (formerly Rosetta) API

This implementation follows the [Rosetta API](https://docs.cdp.coinbase.com/mesh/docs/api-reference/) specification and is compatible with the [Rosetta CLI](https://docs.cdp.coinbase.com/mesh/docs/mesh-cli/).
It contains some extensions to fit the needs of the Cardano blockchain. These changes are documented in the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/core-concepts/cardano-addons).

## Documentation

Detailed explanation to all components can be found in the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/intro) of this repository.
It includes explanations about the Architecture, how to build and run the components and explanations to environment variables.

## System requirements

Since [Yaci-Store](https://github.com/bloxbean/yaci-store) is a comparatively lightweight indexer, the system requirements are lower than for other chain indexers. The following are the recommended system requirements for running this component:

- 4CPU Cores
- 32GB RAM
- ~1.3 TB total storage (node ~250 GB + Rosetta DB ~1 TB) — pruning disabled [default]
- ~750 GB total storage (node ~250 GB + Rosetta DB ~500 GB) — pruning enabled

Better hardware will improve the performance of the indexer and the node, which will result in faster syncing times.

## Installation

By default this Cardano-node will sync the entire chain from Genesis.
This will take up to 48-72 hours (dependening on the system resources).

### Docker (build from source)

If your user is not in the `docker` group you might have to execute these commands with `sudo`.
The default config is focused on mainnet. If you want to test this on other Cardano netwoks (like `preview` or `preprod`) please adjust the `docker/.env.dockerfile` or read the documentation page on [Environment variables](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/env-vars) on other options and their default values.

```bash
    git clone https://github.com/cardano-foundation/cardano-rosetta-java
    cd cardano-rosetta-java
    docker build -t rosetta-java -f ./docker/Dockerfile .
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile --env-file ./docker/.env.docker-profile-mid-level -p 8082:8082 --shm-size=4g -d rosetta-java
```

Detailed explanation can be found in the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/docker).

Depending on using a snapshot feature or not, this will take X amount of time. You can follow along with the commands below. Your instance is ready when you see: `DONE`.

### Offline mode

If you want to run rosetta-java in offline mode you need to set the `API_SPRING_PROFILES_ACTIVE` environment variable to `offline` in `./docker/.env.dockerfile`.
This will disable the syncing of the node and won't start the db and the indexer.
Default is `online`.

**Useful commands:**

- Following Docker container logs:

```bash
    docker logs rosetta -f
```

- Access node logs:

```bash
    docker exec rosetta tail -f /logs/node.log
```

- Access indexer logs:

```bash
    docker exec rosetta tail -f /logs/indexer.log
```

- Interactive access to container:

```bash
    docker exec -it rosetta bash # direct bash access within the container


    # Useful commands within the container
    cardano-cli query tip --mainnet # check node sync status
    tail -f /logs/node.log # follow node logs
    tail -f /logs/indexer.log # follow indexer logs
```

### Docker (using pre-built image)

For every Release we provide pre-built docker images stored in the DockerHub Repositories of the Cardano Foundation ([DockerHub](https://hub.docker.com/orgs/cardanofoundation/repositories))
To start it use the following command:

```bash
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile --env-file ./docker/.env.docker-profile-mid-level -p 8082:8082 --shm-size=4g -d cardanofoundation/cardano-rosetta-java:1.2.10
```

Changes to the configuration can be made by adjusting the `docker/.env.dockerfile` file. For more information on the environment variables, please refer to the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/env-vars).

If you want to use the `cardano-submit-api` you can additionally expose port `8090`. It can then be used to submit raw cbor transaction (API documentation here: [Link](https://input-output-hk.github.io/cardano-rest/submit-api/))

```bash
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile --env-file ./docker/.env.docker-profile-mid-level -p 8090:8090 -p 8082:8082 --shm-size=4g -d cardanofoundation/cardano-rosetta-java:1.2.10
```

### Docker compose

If needed we also provide all components needed to run Rosetta in a docker-compose file.
This will start:

- Cardano-node
- Cardano-Submit-API
- Yaci-Store
- Rosetta-API
- Postgres

### Entry level hardware profile

```bash
   docker compose --env-file .env.docker-compose --env-file .env.docker-compose-profile-mid-level -f docker-compose.yaml up -d
```

### A complete list of hardware profiles:

```
.env.docker-compose-profile-entry-level
.env.docker-compose-profile-mid-level
.env.docker-compose-profile-advanced-level
```

See the [hardware profiles documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/hardware-profiles) for a full list of hardware profiles and their configurations.

Further adjustments can be made by changing `.env.docker-compose` file. For more information on the environment variables, please refer to the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/env-vars).

---

Thanks for visiting us and enjoy :heart:!

