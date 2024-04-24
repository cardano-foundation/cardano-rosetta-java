LOG=INFO
NETWORK=preprod
# mainnet, preprod, testnet, devkit
PROTOCOL_MAGIC=1
# mainnet 764824073, preprod 1, testnet 2, devkit 42
NETWORK_MAGIC=${PROTOCOL_MAGIC}

# Common env
DB_USER=rosetta_db_admin
DB_SECRET=weakpwd#123_d

# Postgres variables
DB_NAME=rosetta-java-preprod
DB_HOST=localhost
DB_PORT=5432
DB_SCHEMA=${NETWORK}

# Cardano node variables
CARDANO_NODE_HOST=localhost
CARDANO_NODE_PORT=3001
CARDANO_NODE_VERSION=8.9.0
CARDANO_NODE_SUBMIT_HOST=cardano-submit-api
NODE_SUBMIT_API_PORT=8090
CARDANO_NODE_SOCKET=/ipc/node.socket

# Application env
API_SPRING_PROFILES_ACTIVE=dev
API_PORT=8081
TRANSACTION_TTL=3000

DB_CONNECTION_PARAMS_PROVIDER_TYPE=ENVIRONMENT
DB_DRIVER_CLASS_NAME=org.postgresql.Driver

ROSETTA_VERSION=1.4.13
TOPOLOGY_FILEPATH=/current/topology.json
GENESIS_SHELLEY_PATH=/current/shelley-genesis.json
GENESIS_BYRON_PATH=/current/byron-genesis.json
GENESIS_ALONZO_PATH=/current/alonzo-genesis.json
GENESIS_CONWAY_PATH=/current/conway-genesis.json
API_NODE_SOCKET_PATH=./node/node.socket

PRINT_EXCEPTION=true

YACI_SPRING_PROFILES=postgres
INDEXER_NODE_PORT=3001
MEMPOOL_ENABLED=true

# Devkit
HOST_N2C_SOCAT_PORT=3333
DEVKIT_ENABLED=true
