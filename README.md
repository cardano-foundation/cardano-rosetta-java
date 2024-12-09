[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=cardano-foundation_cardano-rosetta-java)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=coverage)](https://sonarcloud.io/summary/new_code?id=cardano-foundation_cardano-rosetta-java)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B45571%2Fgithub.com%2Fcardano-foundation%2Fcardano-rosetta-java.svg?type=shield&issueType=license)](https://app.fossa.com/projects/custom%2B45571%2Fgithub.com%2Fcardano-foundation%2Fcardano-rosetta-java?ref=badge_shield&issueType=license)

## What the project is about?

This repository provides a lightweight java implementation of the [Rosetta API](https://github.com/coinbase/mesh-specifications). It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from a Cardano node. 

This component consists of:
- a full Cardano node
- a Cardano Submit API
- an indexer which stores data in Postgres
- the Mesh (formerly Rosetta) API

This implementation follows the [Rosetta API](https://docs.cdp.coinbase.com/mesh/docs/api-reference/) specification and is compatible with the [Rosetta CLI](https://docs.cdp.coinbase.com/mesh/docs/mesh-cli/).
It contains some extensions to fit the needs of the Cardano blockchain. These changes are documented in the [wiki](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/2.-Cardano-Specific-API-Additions).

## Documentation
Detailed explanation to all components can be found in the [wiki pages](https://github.com/cardano-foundation/cardano-rosetta-java/wiki) of this repository.
It includes explanations about the Architecture, how to build and run the components and explanations to environment variables.

## System requirements
Since [Yaci-Store](https://github.com/bloxbean/yaci-store) is a comparatively lightweight indexer, the system requirements are lower than for other chain indexers. The following are the recommended system requirements for running this component:
- 4CPU Cores
- 32GB RAM
- 1TB of storage (PRUNING_ENABLED=false) [default]
- 400GB of storage (PRUNING_ENABLED=true)

Better hardware will improve the performance of the indexer and the node, which will result in faster syncing times.

## Installation
By default this Cardano-node will sync the entire chain from Genesis. 
This will take up to 48-72 hours (dependening on the system resources).

### Docker (build from source)
If your user is not in the `docker` group you might have to execute these commands with `sudo`.
The default config is focused on mainnet. If you want to test this on other Cardano netwoks (like `preview` or `preprod`) please adjust the `docker/.env.dockerfile` or read the Wiki page on [Environment variables](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/5.-Environment-Variables) on other options and their default values.

```bash
    git clone https://github.com/cardano-foundation/cardano-rosetta-java
    cd cardano-rosetta-java
    docker build -t rosetta-java -f ./docker/Dockerfile .
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile -p 8082:8082 -d rosetta-java
```
Detailed explanation can be found in the [Wiki](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/3.-Getting-Started-with-Docker).

Depending on using a snapshot feature or not, this will take X amount of time. You can follow along with the commands below. Your instance is ready when you see: `DONE`.

### Offline mode
If you want to run rosetta-java in offline mode you need to set the `API_SPRING_PROFILES_ACTIVE` environment variable to `offline` in `./docker/.env.dockerfile`. 
Default is `online`.
This will disable the syncing of the node and the indexer.

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
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile -p 8082:8082 -d cardanofoundation/cardano-rosetta-java:1.0-rc4
```
Changes to the configuration can be made by adjusting the `docker/.env.dockerfile` file. For more information on the environment variables, please refer to the [Wiki](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/5.-Environment-Variables).

If you want to use the `cardano-submit-api` you can additionally expose port `8090`. It can then be used to submit raw cbor transaction (API documentation here: [Link](https://input-output-hk.github.io/cardano-rest/submit-api/))
```bash
    docker run --name rosetta -v {CUSTOM_MOUNT_PATH}:/node --env-file ./docker/.env.dockerfile -p 8090:8090 -p 8082:8082 -d cardanofoundation/cardano-rosetta-java:1.0.1
```

### Docker compose
If needed we also provide all components needed to run Rosetta in a docker-compose file.
This will start:
- Cardano-node
- Cardano-Submit-API
- Yaci-Store
- Rosetta-API
- Postgres

```bash
    docker-compose --env-file .env.docker-compose -f docker-compose.yaml up -d 
```
Adjustments can be made by changing `.env.docker-compose` file. For more information on the environment variables, please refer to the [Wiki](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/5.-Environment-Variables).

---
Thanks for visiting us and enjoy :heart:!
