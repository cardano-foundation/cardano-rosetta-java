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

### How to build

- Clone the repository
- Copy `./.m2/settings.default.xml` to `./.m2/settings.xml`
- Copy `.env.example`  to `.env`
- Fill the `.env` file with your values (explain below)
- Create if not exists external network `cardano-rosetta-java` with `docker network create cardano-rosetta-java`
- From `/cardano-rosetta-java` run `docker build -t cardano-rosetta-java-base .` to build jar file for all service
- From `/cardano-rosetta-java` run `docker compose -f docker-common.yaml up` to start environment (Kafka, Postges, Redis, Cardano node)
- Check all service is up by `docker ps` and run `chmod a+rwx /node-ipc/`

### How to run
- From `/cardano-rosetta-java` run `docker compose -f docker-api.yaml up` to start rosetta api service
- From `/cardano-rosetta-java` run `docker compose -f docker-consumer.yaml up` to start rosetta consumer service
* Note: Consumer need to run with an instance of ledger sync crawler on same topic
### Environment variables

- `DB_ADMIN_USER_NAME` : Postgres admin user. Default is rosetta_db_admin
- `DB_ADMIN_USER_SECRET` : Postgres admin secret. Default is weakpwd#123_d
- `SERVICE_USER_NAME` : Postgres user service. Default is rosetta_db_service_user
- `SERVICE_USER_SECRET` : Postgres password service. Default is weakerpwd#123_d

- `DB_IMAGE_NAME` : Postgres docker image name. Default is rosetta
- `DB_IMAGE_TAG` : Postgres docker image tag. Default is latest
- `DB_NAME` : Postgres database. Default is rosetta
- `DB_HOST` : Postgres host. Default is db
- `DB_PORT` : Postgres port. Default is 5432
- `DB_SCHEMA` : Database schema [testnet, preprod, preview, mainnet]
- `MAXIMUM_POOL_SIZE` : Database max pool size. Default is 80
- `JDBC_BATCH_SIZE` : JDBC batch size. Default is 1000
- `SCHEMA` : Postgres schema [testnet, preprod, preview, mainnet]. Default is testnet

- `BOOSTRAP_SERVER_HOST` : Kafka boostrap server host. Default is kafka
- `BOOSTRAP_SERVER_PORT` : Kafka boostrap server port. Default is 9092

- `BLOCKS_TOPIC` : Kafka topic for blocks. Default is preprod.crawler.blocks.
- `BLOCKS_BATCH_SIZE`: Batch size for bulk insert. Default is 1000
- `COMMIT_THRESHOLD`: Amount of time the consumer has to insert data to database (It depend on time of a new block mining time or kafka and crawler latencies. For now we set COMMIT_THRESHOLD = a new block mining time / 2)

- `LOG`: Log level [INFO, DEBUG, TRACE]. Default is INFO.

- `REDIS_MASTER_PASSWORD`: Redis master password. Default is redis_master_pw.
- `REDIS_SLAVE_PASSWORD`: Redis slave password]. Default is redis_slave_pw.
- `REDIS_MASTER_SET`: Redis master set. Default is mymaster.
- `REDIS_SENTINEL_PASSWORD`: Redis sentinel password. Default is redis_sentinel_pw.

- `EXTERNAL_REDIS_MASTER_HOST`: Redis master host. Default is localhost.
- `EXTERNAL_REDIS_MASTER_PORT`: Redis master port]. Default is 6371.
- `EXTERNAL_REDIS_SLAVE_PORT`: Redis slave port. Default is 6372.
- `REDIS_SENTINEL_HOST`: Redis sentinel port. Default is redis-sentinel.
- `REDIS_SENTINEL_PORT`: Redis sentinel port. Default is 26371.

- `API_SPRING_PROFILES_ACTIVE` : Api spring profile [local, dev, test, prod]. Default is dev, sentinel.
- `API_EXPOSED_PORT` : Rosetta api exposed port. Default is 8080
- `API_BIND_PORT` : Rosetta api bind port. Default is 8080
- `SUBMIT_SOCKET_CONNECTION` : Submit socket connection. Default is /ipc/node.socket
- `NETWORK_MAGIC` : Network magic. Default is 1
- `TRANSACTION_TTL` : Transaction ttl. Default is 3000
- `NETWORK` : Default is preprod

- `DB_CONNECTION_PARAMS_PROVIDER_TYPE` : Database connection params provider type. Default is ENVIRONMENT.
- `DB_DRIVER_CLASS_NAME` : Database driver class name. Default is "org.postgresql.Driver".

- `ROSETTA_VERSION` : Rosetta version. Current is 1.4.13.
- `TOPOLOGY_FILEPATH` : Topology file path. Default is empty.
- `CARDANO_NODE_PATH` : Cardano node path. Default is empty.
- `EXEMPTION_TYPES_PATH` : Exemption types path. Default is empty.

- `PRINT_EXCEPTION` : Print exception. Default is true.

- `CONSUMER_PROFILES_ACTIVE` : Consumer spring profile [local, dev, test, prod]. Default is dev.
- `CONSUMER_EXPOSED_PORT` : Rosetta consumer exposed port. Default is 8081
- `CONSUMER_BIND_PORT` : Rosetta consumer bind port. Default is 8081
- `LIQUIBASE_ENABLE` : "true" only for first time run application and "false" from second run 

- `SUBMIT_API_CONFIG_URL` : Default is /cf-rosetta-consumer/networks/preprod/submit-api-config.json
- `BYRON_GENESIS_URL` : Default is /cf-rosetta-consumer/networks/preprod/byron-genesis.json
- `SHELLEY_GENESIS_URL` : Default is /cf-rosetta-consumer/networks/preprod/shelley-genesis.json
- `ALONZO_GENESIS_URL` : Default is /cf-rosetta-consumer/networks/preprod/alonzo-genesis.json
- `CONWAY_GENESIS_URL` : Default is /cf-rosetta-consumer/networks/preprod/conway-genesis.json

## Contributing

File an issue or a PR or reach out directly to us if you want to contribute.

When contributing to this project and interacting with others, please follow our [Contributing Guidelines](./CONTRIBUTING.md) and [Code of Conduct](./CODE-OF-CONDUCT.md).

---

Thanks for visiting and enjoy :heart:!
