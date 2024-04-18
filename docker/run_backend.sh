#!/bin/bash
CARDANO_NODE_PORT="3001"

echo "run_backend.sh > run node"
cardano-node run --port $CARDANO_NODE_PORT --config /node/config/config.json --topology /node/config/topology.json --database-path /node/data/db --socket-path /ipc/node.socket
#cardano-node.exe run --topology ./configuration/cardano/mainnet-topology.json --database-path ./state --port 3001 --config ./configuration/cardano/mainnet-config.yaml  --socket-path \\.\pipe\cardano-node

echo "run_backend.sh > run indexer"
#exec java -jar -DdbUrl="${DB_URL}" -DdbUser="${DB_USER}" -DdbSecret="${DB_SECRET}" -DdbDriverName="${DB_DRIVER_CLASS_NAME}" ./app.jar



