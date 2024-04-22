
---

# :rotating_light: This Repository is under active development. :rotating_light:

# Cardano Rosetta API Java implementation
This repository provides a lightweight java implementation of the Rosetta API. It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from the node. 

## :construction: Current Development status :construction:
- [x] Architecture clean up for yaci-store
- [x] Docker-compose setup 
- [x] Integration test setup
- API calls
  - Data API
    - [x] /network/*
    - [x] /block/*
    - [x] /account/*
    - /mempool
      - [x] /mempool
      - [ ] /mempool/transaction
  - Construction API
    - [x] /construction/derive
    - [x] /construction/preprocess
    - [x] /construction/metadata
    - [x] /construction/payloads
    - [x] /construction/combine
    - [x] /construction/parse
    - [x] /construction/hash
    - [x] /construction/submit

## Getting Started

### Prerequisites

- Docker 
- Docker Compose
- Java 21
- For integration tests: Node 14+

### How to build

- Clone the repository
- For local environment: 
  - Copy `.env.docker-compose`  to `.env`
  - Fill the `.env` file with your values (explain below) or use the provided for docker-compose setup
  - Start SpringBoot application with `mvn spring-boot:run` within submodule `api` or `yaci-indexer`
- Run `docker compose -f docker-compose.yaml up --build` to start rosetta api service including yaci-store and a cardano node
  - Using the provided env file `docker-compose --env-file .env.docker-compose -f docker-compose.yaml up --build`
* Note: the first time you run the command, it will take a little bit of your time to build the cardano-node, and next time it will be cached when run. So please be patient.

### How to run integration tests

- Run `docker compose --env-file .env.IntegrationTest -f docker-integration-test-environment.yaml up --build -d --wait`
- Using CLI
  - Install newman `npm install -g newman` (Node version 14+ needed)
  - Run `newman run ./postmanTests/rosetta-java.postman_collection.json -e ./postmanTests/Rosetta-java-env.postman_environment.json -r cli`
- Using Postman
  - Install [Postman](https://www.postman.com)
  - Import the collection `./postmanTests/rosetta-java.postman_collection.json` 
  - Import the environment `./postmanTests/Rosetta-java-env.postman_environment.json`
  - Run the collection


### Restore a snapshot
**TBD for yaci-store**

A node snapshot can be downloaded from [here](https://csnapshots.io/). Download the snapshot and place the files within the `CARDANO_NODE_DB` Path.

For mainnet the following command can be used to restore the snapshot:
```bash
curl -o - https://downloads.csnapshots.io/mainnet/$(curl -s https://downloads.csnapshots.io/mainnet/mainnet-db-snapshot.json| jq -r .[].file_name ) | lz4 -c -d - | tar -x -C ${CARDANO_NODE_DB}
```

### Mempool Monitoring
Mempool monitoring can be activated when adding the spring profile `mempool` to the rosetta api service.
It will be turned off by default. Since it is only working for nodes, which participate in the network and available within P2P from other nodes. 
So the Node itself needs extra configuration to be able to query mempool transactions.

### Documentation
- [Architecture Overview](https://github.com/cardano-foundation/cardano-rosetta-java/wiki)
- [Cardano specific API Additions](./docs/cardano-specific-api-additions.md)
- [Dev H2 Quickstart Guide](./docs/dev-h2-quick-start-guide.md)
- [Environment Variables](./docs/environment-variables.md)

## Contributing

File an issue or a PR or reach out directly to us if you want to contribute.

When contributing to this project and interacting with others, please follow our [Contributing Guidelines](./CONTRIBUTING.md) and [Code of Conduct](./CODE-OF-CONDUCT.md).

---

Thanks for visiting and enjoy :heart:!
