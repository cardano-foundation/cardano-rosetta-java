# Environment variables
With environment variables the behaviour of the compiled application can be configured. The following table lists the available environment variables and their default values.

Within root folder of the project there are example `.env` files, which can be copied and adjusted to fit the needs of the deployment.
- `.env.IntegrationTest` - Is used for integration tests with yaci devkit
- `.env.docker-compose` - Is used for standard docker-compose setup (Copy this file and adjusted it to your needs)

| Variable                             | Description                                        | Default                                                   | 
|--------------------------------------|----------------------------------------------------|-----------------------------------------------------------|
| `LOG`                                | Log level                                          | INFO                                                      |
| `NETWORK`                            | Network                                            | preprod - Options are `mainnet, testnet, preprod, devkit` |
| `PROTOCOL_MAGIC`                     | Cardano protocol magic                             | 1                                                         |
| `DB_ADMIN_USER_NAME`                 | Postgres admin user                                | rosetta_db_admin                                          | 
| `DB_ADMIN_USER_SECRET`               | Postgres admin secret                              | weakpwd#123_d                                             |
| `DB_IMAGE_NAME`                      | Postgres docker image name                         | rosetta                                                   |
| `DB_IMAGE_TAG`                       | Postgres docker image tag                          | latest                                                    |
| `DB_NAME`                            | Postgres database                                  | rosetta                                                   |
| `DB_HOST`                            | Postgres host                                      | db                                                        |
| `DB_PORT`                            | Postgres port                                      | 5432                                                      |
| `DB_SCHEMA`                          | Database schema                                    | testnet                                                   |
| `DB_PATH`                            | Database path                                      | ./data                                                    |
| `CARDANO_NODE_HOST`                  | Cardano node host                                  | cardano-node                                              |
| `CARDANO_NODE_PORT`                  | Cardano node port                                  | 3001                                                      |
| `CARDANO_NODE_VERSION`               | Cardano node version                               | 8.9.0                                                     |
| `NODE_SUBMIT_API_PORT`               | Cardano node submit api port                       | 8090                                                      |
| `CARDANO_NODE_SOCKET`                | Cardano node socket Path                           | /ipc/node.socket                                          |
| `CARDANO_NODE_DB`                    | Cardano node db path                               | /data/cardano-node/db                                     |
| `CARDANO_CONFIG`                     | Cardano node config path                           | ./config/${NETWORK}/config                                |
| `API_SPRING_PROFILES_ACTIVE_API`     | Api spring profile                                 | dev                                                       |
| `API_PORT`                           | Rosetta api exposed port                           | 8080                                                      |
| `TRANSACTION_TTL`                    | Transaction ttl                                    | 3000                                                      |
| `DB_CONNECTION_PARAMS_PROVIDER_TYPE` | Database connection params provider type           | ENVIRONMENT                                               |
| `DB_DRIVER_CLASS_NAME`               | Database driver class name                         | "org.postgresql.Driver"                                   |
| `ROSETTA_VERSION`                    | Rosetta version                                    | 1.4.13                                                    |
| `TOPOLOGY_FILEPATH`                  | Topology file path                                 | ./config/${NETWORK}/topology.json                         |
| `GENESIS_SHELLEY_PATH`               | Genesis file path                                  | ./config/${NETWORK}/shelley-genesis.json                  |
| `GENESIS_BYRON_PATH`                 | Genesis file path                                  | ./config/${NETWORK}/byron-genesis.json                    |
| `PRINT_EXCEPTION`                    | Print exception                                    | true                                                      |
| `YACI_SPRING_PROFILES`               | Yaci indexer spring profile                        | dev,postgres                                              |
| `INDEXER_NODE_PORT`                  | Cardano node port that the indexer will connect to | ${CARDANO_NODE_PORT}                                      |
| `DEVKIT_ENABLED`                     | Devkit enabled                                     | false                                                     |