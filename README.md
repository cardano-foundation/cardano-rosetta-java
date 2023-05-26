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
- Copy `.env.example`  to `.env`
- Fill the `.env` file with your values (explain below)
- Create if not exists external network `cardano-rosetta-java` with `docker network create cardano-rosetta-java`
- From `/cardano-rosetta-java` run `docker-compose -f docker-compose.yml up --build` to build and start all service


### Environment variables

- `CONSUMER_PROFILES_ACTIVE` : Consumer spring profile [local, dev, test, prod]. Default is local.
- `DB_HOST` : Postgres host. Default is db
- `DB_PORT` : Postgres port. Default is 5432
- `DB_SCHEMA` : Database schema [testnet, preprod, preview, mainnet]
- `DB_ADMIN_USER_NAME` : Postgres admin user. Default is rosetta_db_admin
- `DB_ADMIN_USER_SECRET` : Postgres admin secret. Default is weakpwd#123_d
- `SERVICE_USER_NAME` : Postgres user service. Default is rosetta_db_service_user
- `SERVICE_USER_SECRET` : Postgres password service. Default is weakerpwd#123_d
- `DB_NAME` : Postgres database. Default is rosetta
- `SCHEMA` : Postgres schema [testnet, preprod, preview, mainnet]. Default is testnet
- `LIQUIBASE_ENABLE` : "true" only for first time run application and "false" from second run 

- `API_EXPOSED_PORT` : Rosetta api exposed port. Default is 8080
- `API_BIND_PORT` : Rosetta api bind port. Default is 8080
- `CONSUMER_EXPOSED_PORT` : Rosetta consumer exposed port. Default is 8081
- `CONSUMER_BIND_PORT` : Rosetta consumer bind port. Default is 8081
- `BOOSTRAP_SERVER_PORT` : Kafka bootstrap server port. Default is 9092

- `BLOCKS_TOPIC` : Kafka topic for blocks. Default is local.crawler.blocks.
- `LOG`: Log level [INFO, DEBUG, TRACE]. Default is INFO.
- `BLOCKS_BATCH_SIZE`: Batch size for bulk insert
- `COMMIT_THRESHOLD`: Amount of time the consumer has to insert data to database (It depend on time of a new block mining time or kafka and crawler latencies. For now we set COMMIT_THRESHOLD = a new block mining time / 2)
- `MAXIMUM_POOL_SIZE`: Consumer select parallel when preparing data before inserting into the database. If you want to consume as fast as possible set the `MAXIMUM_POOL_SIZE` as much as possible (cpu core * 4). This will reduce explorer api performance. When consuming 100% network you can reduce `MAXIMUM_POOL_SIZE` to 16 or 8


## Contributing

File an issue or a PR or reach out directly to us if you want to contribute.

When contributing to this project and interacting with others, please follow our [Contributing Guidelines](./CONTRIBUTING.md) and [Code of Conduct](./CODE-OF-CONDUCT.md).

---

Thanks for visiting and enjoy :heart:!
