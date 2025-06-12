#!/bin/bash

if [ "$NETWORK" == "mainnet" ]; then
  NETWORK_STR="--mainnet"
else
  NETWORK_STR="--testnet-magic $PROTOCOL_MAGIC"
fi

cmd="$1"; shift
case "$cmd" in
  cardano-node)
    echo "Starting Cardano node..."
    exec cardano-node run \
      --socket-path "$CARDANO_NODE_SOCKET_PATH" \
      --port "$CARDANO_NODE_PORT" \
      --database-path "$CARDANO_NODE_DB" \
      --config /config/config.json \
      --topology /config/topology.json
    ;;
  cardano-submit-api)
    echo "Starting Cardano submit api..."
    exec cardano-submit-api \
      --listen-address 0.0.0.0 \
      --socket-path "$CARDANO_NODE_SOCKET_PATH" \
      --port "$NODE_SUBMIT_API_PORT" \
      $NETWORK_STR \
      --config /cardano-submit-api-config/cardano-submit-api.yaml
    ;;
  *)
    exit 1
    ;;
esac
