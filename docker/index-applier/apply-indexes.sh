#!/bin/bash
set -euo pipefail

# Environment variables (with defaults)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-rosetta-java}"
DB_USER="${DB_USER:-rosetta_db_admin}"
DB_SECRET="${DB_SECRET:-}"
DB_SCHEMA="${DB_SCHEMA:-public}"
NETWORK="${NETWORK:-mainnet}"
API_URL="${API_URL:-http://localhost:8082}"
POLL_INTERVAL="${POLL_INTERVAL:-60}"
CONFIG_PATH="${CONFIG_PATH:-/config/db-indexes.yaml}"

export PGPASSWORD="$DB_SECRET"
PSQL_CONN="psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -v ON_ERROR_STOP=1"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 1. Wait for APPLYING_INDEXES state (or exit if already synced)
log "Waiting for APPLYING_INDEXES state..."
while true; do
  response=$(curl -sf "$API_URL/network/status" \
    -H "Content-Type: application/json" \
    -d "{\"network_identifier\":{\"blockchain\":\"cardano\",\"network\":\"$NETWORK\"}}" \
    2>/dev/null || echo '{}')

  stage=$(echo "$response" | jq -r '.sync_status.stage // empty')
  synced=$(echo "$response" | jq -r '.sync_status.synced // false')

  # If already synced (LIVE), indexes are already applied - exit immediately
  if [ "$synced" == "true" ]; then
    log "System is already synced (stage: $stage). Indexes already applied. Exiting."
    exit 0
  fi

  if [ "$stage" == "APPLYING_INDEXES" ]; then
    log "Stage is APPLYING_INDEXES, starting index application..."
    break
  fi

  log "Current stage: ${stage:-unknown}, synced: $synced, waiting ${POLL_INTERVAL}s..."
  sleep "$POLL_INTERVAL"
done

# 2. Get index definitions from YAML
log "Reading index definitions from $CONFIG_PATH..."
index_count=$(yq -r '.cardano.rosetta.db_indexes | length' "$CONFIG_PATH")
log "Found $index_count indexes to process"

# 3. Cleanup invalid indexes
log "Checking for invalid indexes..."
for i in $(seq 0 $((index_count - 1))); do
  name=$(yq -r ".cardano.rosetta.db_indexes[$i].name" "$CONFIG_PATH")

  is_valid=$($PSQL_CONN -t -A -c "
    SELECT indisvalid FROM pg_index i
    JOIN pg_class c ON i.indexrelid = c.oid
    WHERE c.relname = '$name'
  " 2>/dev/null || echo "t")

  if [ "$is_valid" == "f" ]; then
    log "Dropping invalid index: $name"
    $PSQL_CONN -c "DROP INDEX IF EXISTS ${DB_SCHEMA}.${name}" 2>/dev/null || true
  fi
done

# 4. Apply indexes
log "Applying indexes..."
for i in $(seq 0 $((index_count - 1))); do
  name=$(yq -r ".cardano.rosetta.db_indexes[$i].name" "$CONFIG_PATH")
  create_statement=$(yq -r ".cardano.rosetta.db_indexes[$i].command" "$CONFIG_PATH")

  log "Creating index: $name"
  if $PSQL_CONN -c "$create_statement" 2>&1; then
    log "  OK: $name"
  else
    log "  WARN: $name (may already exist or failed)"
  fi
done

log "All indexes applied. Exiting."
