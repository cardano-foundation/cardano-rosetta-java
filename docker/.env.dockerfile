# In offline mode there will be no synchronization with the network
# and the following components will be disabled: Node, Submit-api, Mithril, Yaci Indexer
# Set to offline for offline mode
# Set to online for online mode
API_SPRING_PROFILES_ACTIVE=online

## Main variables
LOG=INFO
NETWORK=mainnet
# mainnet, preprod, preview, sanchonet, devkit
PROTOCOL_MAGIC=764824073
# mainnet 764824073, preprod 1, preview 2, sanchonet 4, devkit 42

# Node synchronization
SYNC=true
VERIFICATION=true

# Mithril snapshots
MITHRIL_SYNC=true
SNAPSHOT_DIGEST=latest
# if not set standard values will be used
AGGREGATOR_ENDPOINT=
# if not set standard values will be used
GENESIS_VERIFICATION_KEY=

## Postgres variables
DB_NAME=rosetta-java
DB_USER=rosetta_db_admin
DB_SECRET=weakpwd#123_d
DB_HOST=localhost
DB_PORT=5432

## Cardano node variables
CARDANO_NODE_HOST=localhost
CARDANO_NODE_PORT=3001
CARDANO_NODE_VERSION=10.3.1
CARDANO_NODE_SUBMIT_HOST=localhost
NODE_SUBMIT_API_PORT=8090
CARDANO_NODE_SOCKET_PATH=/node/node.socket
## Api env
API_PORT=8082

ROSETTA_VERSION=1.4.13
TOPOLOGY_FILEPATH=/config/topology.json
GENESIS_SHELLEY_PATH=/config/shelley-genesis.json
GENESIS_BYRON_PATH=/config/byron-genesis.json
GENESIS_ALONZO_PATH=/config/alonzo-genesis.json
GENESIS_CONWAY_PATH=/config/conway-genesis.json
PRINT_EXCEPTION=true

## Yaci Indexer env
YACI_SPRING_PROFILES=postgres,n2c-socket
PRUNING_ENABLED=false
PRUNING_INTERVAL=600
PRUNING_SAFE_BLOCKS=2160
# database profiles: h2, h2-testdata, postgres
MEMPOOL_ENABLED=false
# Haven't implemented yet
INITIAL_BALANCE_CALCULATION_BLOCK=0

YACI_HTTP_BASE_URL=http://localhost:9095/api/v1
YACI_INDEXER_PORT=9095
HTTP_CONNECT_TIMEOUT_SECONDS=5
HTTP_REQUEST_TIMEOUT_SECONDS=15

## Rosetta API to turn off or on DB show SQL for query debugging
API_DB_SHOW_SQL=false

# Common DB settings for Rosetta API Connection Pool
API_DB_POOL_MAX_LIFETIME_MS=2000000
API_DB_POOL_CONNECTION_TIMEOUT_MS=100000
API_DB_KEEP_ALIVE_MS=60000
API_DB_LEAK_CONNECTIONS_WARNING_MS=60000
API_DB_MONITOR_PERFORMANCE=false
