[![License](https://img.shields.io/github/license/cardano-foundation/cf-metadata-server)](https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/LICENSE)
![GitHub top language](https://img.shields.io/github/languages/top/cardano-foundation/cardano-rosetta-java)
[![Build](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/main.yaml/badge.svg)](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/main.yaml)
[![CodeQL](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/codeql.yaml/badge.svg)](https://github.com/cardano-foundation/cardano-rosetta-java/actions/workflows/codeql.yaml)
![coverage](https://github.com/cardano-foundation/cardano-rosetta-java/blob/badges/jacoco.svg)
![branches](https://github.com/cardano-foundation/cardano-rosetta-java/blob/badges/branches.svg)
[![Issues](https://img.shields.io/github/issues/cardano-foundation/cardano-rosetta-java)](https://github.com/cardano-foundation/cardano-rosetta-java/issues)

---

# Cardano Rosetta API Java implementation
This is a RosettaAPI implementation using LedgeSync as a backend and Yaci/Bloxbean library for ledger data serialization and transaction submission.

## Getting Started

### Prerequisites

- Docker && Docker Compose

### Installing and Run

- Clone the repository
- Copy `./.m2/settings.default.xml` to `./.m2/settings.xml`
- Fill `{username_github}` and `{token_github}` in `./.m2/settings.xml` with your github username and token. Guide to generate a token with `read:packages` scope [here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-personal-access-token-classic)
- Copy `.env.example`  to `.env`
- Fill the `.env` file with your values (explain below)
- From `/consumer` and run `mvn clean install`
- From `/api` and run `mvn clean install`
- Create if not exists external network `cardano-rosetta-java` with `docker network create cardano-rosetta-java`
- From `/cardano-rosetta-java` run `docker-compose -f docker-compose.yml up -d` to start the containers


### Environment variables

- `CONSUMER_PROFILES_ACTIVE` : Consumer spring profile [local, dev, test, prod]. Default is local.
- `POSTGRES_HOST` : Postgres host. Default is postgres
- `POSTGRES_PORT` : Postgres port. Default is 5432
- `POSTGRES_USER` : Postgres user. Default is rosetta_db_admin
- `POSTGRES_PASSWORD` : Postgres password. Default is postgres
- `POSTGRES_DB` : Postgres database. Default is explorer
- `SCHEMA` : Postgres schema [testnet, preprod, preview, mainnet]. Default is testnet

- `BOOSTRAP_SERVER_HOST` : Kafka bootstrap server. Default is kafka
- `BOOSTRAP_SERVER_PORT` : Kafka bootstrap server port. Default is 9092

- `BLOCKS_TOPIC` : Kafka topic for blocks. Default is local.crawler.blocks.
- `LOG`: Log level [INFO, DEBUG, TRACE]. Default is INFO.
- `BLOCKS_BATCH_SIZE`: Batch size for bulk insert
- `COMMIT_THRESHOLD`: Amount of time the consumer has to insert data to database (It depend on time of a new block mining time or kafka and crawler latencies. For now we set COMMIT_THRESHOLD = a new block mining time / 2)
- `FLYWAY_ENABLE`: Migrate schema, set `true` if this is the first time run app
- `MAXIMUM_POOL_SIZE`: Consumer select parallel when preparing data before inserting into the database. If you want to consume as fast as possible set the `MAXIMUM_POOL_SIZE` as much as possible (cpu core * 4). This will reduce explorer api performance. When consuming 100% network you can reduce `MAXIMUM_POOL_SIZE` to 16 or 8


## Contributing

File an issue or a PR or reach out directly to us if you want to contribute.

When contributing to this project and interacting with others, please follow our [Contributing Guidelines](./CONTRIBUTING.md) and [Code of Conduct](./CODE-OF-CONDUCT.md).

---

Thanks for visiting and enjoy :heart:!
