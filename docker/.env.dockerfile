LOG=INFO
NETWORK=preprod
# mainnet, preprod, preview, sanchonet, devkit
PROTOCOL_MAGIC=1
# mainnet 764824073, preprod 1, preview 2, sanchonet 4, devkit 42
NETWORK_MAGIC=${PROTOCOL_MAGIC}

# Postgres variables
DB_NAME=rosetta-java
DB_USER=rosetta_db_admin
DB_SECRET=weakpwd#123_d
DB_HOST=localhost
DB_PORT=5432
DB_SCHEMA=${NETWORK}

# Cardano node variables
CARDANO_NODE_HOST=localhost
CARDANO_NODE_PORT=3001
CARDANO_NODE_VERSION=8.9.0
CARDANO_NODE_SUBMIT_HOST=cardano-submit-api
NODE_SUBMIT_API_PORT=8090
CARDANO_NODE_SOCKET_PATH=./node-ipc
CARDANO_NODE_SOCKET=${CARDANO_NODE_SOCKET_PATH}/node.socket
# Api env
API_SPRING_PROFILES_ACTIVE=dev
# staging, h2, test. Additional profiles: mempool (if mempool should be activated)
API_PORT=8081

ROSETTA_VERSION=1.4.13
TOPOLOGY_FILEPATH=/current/topology.json
GENESIS_SHELLEY_PATH=/current/shelley-genesis.json
GENESIS_BYRON_PATH=/current/byron-genesis.json
GENESIS_ALONZO_PATH=/current/alonzo-genesis.json
GENESIS_CONWAY_PATH=/current/conway-genesis.json
API_NODE_SOCKET_PATH=./node/node.socket

PRINT_EXCEPTION=true

## Yaci Indexer env
YACI_SPRING_PROFILES=postgres
# database profiles: h2, h2-testData, postgres
INDEXER_NODE_PORT=3001
MEMPOOL_ENABLED=false
# Haven't implemented yet
