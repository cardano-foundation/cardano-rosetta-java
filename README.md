
---

# :rotating_light: This Repository is under active development. :rotating_light:
The latest version from main was backupped in branch [backup-old-architecture](https://github.com/cardano-foundation/cardano-rosetta-java/tree/backup-old-architecture)

# Cardano Rosetta API Java implementation
This repository provides a lightweight java implementation of the Rosetta API. It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from the node. 

## :construction: Current Development status :construction:
- [x] Architecture clean up for yaci-store
- [x] Docker-compose setup 
- API calls
    - [x] /network/list
    - [x] /network/status
    - [x] /network/options
    - [ ] /block/*
    - [ ] /mempool/*
    - [ ] /account/*
    - [ ] /construction/*


## Getting Started

### Prerequisites

- Docker && Docker Compose

### How to build

- Clone the repository
- Copy `./.m2/settings.default.xml` to `./.m2/settings.xml`
- Copy `.env.docker-compose`  to `.env`
- Fill the `.env` file with your values (explain below)
- Run `docker compose -f docker-yaci.yaml up --build` to start rosetta api service including yaci-store and a cardano node
* Note: the first time you run the command, it will take a little bit of your time to build the cardano-node, and next time it will be cached when run. So please be patient.

### Environment variables

- `DB_ADMIN_USER_NAME` : Postgres admin user. Default is rosetta_db_admin
- `DB_ADMIN_USER_SECRET` : Postgres admin secret. Default is weakpwd#123_d

- `DB_IMAGE_NAME` : Postgres docker image name. Default is rosetta
- `DB_IMAGE_TAG` : Postgres docker image tag. Default is latest
- `DB_NAME` : Postgres database. Default is rosetta
- `DB_HOST` : Postgres host. Default is db
- `DB_PORT` : Postgres port. Default is 5432
- `DB_SCHEMA` : Database schema [testnet, preprod, preview, mainnet]
- `MAXIMUM_POOL_SIZE` : Database max pool size. Default is 80
- `JDBC_BATCH_SIZE` : JDBC batch size. Default is 1000
- `SCHEMA` : Postgres schema [testnet, preprod, preview, mainnet]. Default is testnet


- `LOG`: Log level [INFO, DEBUG, TRACE]. Default is INFO.


- `API_SPRING_PROFILES_ACTIVE_API` : Api spring profile [local, dev, test, prod]. Default is dev
- `API_EXPOSED_PORT` : Rosetta api exposed port. Default is 8080
- `API_BIND_PORT` : Rosetta api bind port. Default is 8080
- `TRANSACTION_TTL` : Transaction ttl. Default is 3000
- `NETWORK` : Default is preprod

- `DB_CONNECTION_PARAMS_PROVIDER_TYPE` : Database connection params provider type. Default is ENVIRONMENT.
- `DB_DRIVER_CLASS_NAME` : Database driver class name. Default is "org.postgresql.Driver".

- `ROSETTA_VERSION` : Rosetta version. Current is 1.4.13.
- `GENESIS_SHELLEY_PATH` : Genesis file path. Default is ./config/genesis/shelley.json.

- `PRINT_EXCEPTION` : Print exception. Default is true.
- `API_SPRING_PROFILES_ACTIVE_YACI_INDEXER` : Yaci indexer spring profile [dev, staging, h2, postgres] default is "dev,postgres"
## Contributing

File an issue or a PR or reach out directly to us if you want to contribute.

When contributing to this project and interacting with others, please follow our [Contributing Guidelines](./CONTRIBUTING.md) and [Code of Conduct](./CODE-OF-CONDUCT.md).

---

Thanks for visiting and enjoy :heart:!
