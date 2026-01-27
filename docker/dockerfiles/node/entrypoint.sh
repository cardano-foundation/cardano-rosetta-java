#!/bin/bash

set -euo pipefail

if [ "$NETWORK" == "mainnet" ]; then
  NETWORK_STR="--mainnet"
else
  NETWORK_STR="--testnet-magic $PROTOCOL_MAGIC"
fi

# Function to convert Mithril snapshot from Legacy to LMDB format for UTxO-HD
convert_snapshot_if_needed() {
  echo "Checking if snapshot conversion is needed..."

  # Only convert if MITHRIL_SYNC is enabled
  if [ "${MITHRIL_SYNC:-false}" != "true" ]; then
    echo "MITHRIL_SYNC is not enabled, skipping snapshot conversion"
    return 0
  fi

  # Check if Legacy snapshot exists (Mithril downloads to /node/immutable and /node/ledger)
  if [ ! -d "${CARDANO_NODE_DIR}/immutable" ] || [ ! -d "${CARDANO_NODE_DIR}/ledger" ]; then
    echo "No Mithril snapshot found, skipping conversion"
    return 0
  fi

  # Check if conversion already done (marker file)
  CONVERSION_MARKER="${CARDANO_NODE_DB}/.lmdb_converted"
  if [ -f "$CONVERSION_MARKER" ]; then
    echo "Snapshot already converted to LMDB format (marker file exists)"
    return 0
  fi

  echo "================================"
  echo "Converting Mithril snapshot from Legacy to LMDB format..."
  echo "================================"

  # Find the latest snapshot directory
  LATEST_SNAPSHOT=$(find "${CARDANO_NODE_DIR}/ledger" -maxdepth 1 -type d -name '[0-9]*' | sort -n | tail -1)

  if [ -z "$LATEST_SNAPSHOT" ]; then
    echo "ERROR: No snapshot directory found in ${CARDANO_NODE_DIR}/ledger"
    return 1
  fi

  SNAPSHOT_SLOT=$(basename "$LATEST_SNAPSHOT")
  echo "Found snapshot at slot: $SNAPSHOT_SLOT"
  echo "Snapshot path: $LATEST_SNAPSHOT"

  # Prepare output directory
  CONVERTED_SNAPSHOT="${CARDANO_NODE_DIR}/ledger/${SNAPSHOT_SLOT}_lmdb"

  echo "Converting snapshot..."
  echo "  Input:  $LATEST_SNAPSHOT"
  echo "  Output: $CONVERTED_SNAPSHOT"

  # Run snapshot-converter
  # Format: snapshot-converter FORMAT-IN PATH-IN FORMAT-OUT PATH-OUT COMMAND
  if ! snapshot-converter Legacy "$LATEST_SNAPSHOT" LMDB "$CONVERTED_SNAPSHOT" cardano --config /config/config.json; then
    echo "ERROR: Snapshot conversion failed!"
    return 1
  fi

  echo "Snapshot conversion completed successfully!"

  # Clean up old ledger directory and replace with converted one
  echo "Cleaning up ${CARDANO_NODE_DB}/ledger/* ..."
  rm -rf "${CARDANO_NODE_DB}/ledger"/*

  echo "Copying converted snapshot to ${CARDANO_NODE_DB}/ledger/${SNAPSHOT_SLOT} ..."
  mkdir -p "${CARDANO_NODE_DB}/ledger"
  cp -r "$CONVERTED_SNAPSHOT" "${CARDANO_NODE_DB}/ledger/${SNAPSHOT_SLOT}"

  # Create marker file to avoid re-conversion
  touch "$CONVERSION_MARKER"
  echo "Created conversion marker file: $CONVERSION_MARKER"

  echo "================================"
  echo "Snapshot conversion complete!"
  echo "================================"
}

cmd="$1"; shift
case "$cmd" in
  cardano-node)
    echo "Preparing Cardano node..."

    # Convert snapshot if needed (only when MITHRIL_SYNC=true)
    convert_snapshot_if_needed

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
