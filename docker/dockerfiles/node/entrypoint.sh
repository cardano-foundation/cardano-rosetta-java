#!/bin/bash

if [ "$NETWORK" == "mainnet" ]; then
    NETWORK_STR="--mainnet"
else
    NETWORK_STR="--testnet-magic $PROTOCOL_MAGIC"
fi

function clean_up() {
  # Killing all processes before exiting
  kill -2 "$CARDANO_NODE_PID" "$CARDANO_SUBMIT_PID" 
  wait $CARDANO_NODE_PID
  exit
}

trap clean_up SIGHUP SIGINT SIGTERM

echo "Starting Cardano node..."


mkdir -p "$(dirname "$CARDANO_NODE_SOCKET_PATH")"
sleep 1
cardano-node run --socket-path "$CARDANO_NODE_SOCKET_PATH" --port $CARDANO_NODE_PORT --database-path /node/db --config /config/config.json --topology /config/topology.json &
CARDANO_NODE_PID=$!
sleep 2

echo "Starting Cardano submit api..."
cardano-submit-api --listen-address 0.0.0.0 --socket-path "$CARDANO_NODE_SOCKET_PATH" --port $NODE_SUBMIT_API_PORT $NETWORK_STR  --config /cardano-submit-api-config/cardano-submit-api.yaml &
CARDANO_SUBMIT_PID=$!

wait -n $CARDANO_NODE_PID $CARDANO_SUBMIT_PID
