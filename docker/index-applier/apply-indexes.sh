#!/bin/bash
set -euo pipefail

# Shared Docker/Kubernetes index-applier entrypoint.
# Waits for yaci-indexer readiness, then applies indexes directly in PostgreSQL.

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-rosetta-java}"
DB_USER="${DB_USER:-rosetta_db_admin}"
DB_SECRET="${DB_SECRET:-}"
DB_SCHEMA="${DB_SCHEMA:-public}"
YACI_URL="${YACI_URL:-http://localhost:9095}"
YACI_WAIT_INTERVAL_SECONDS="${YACI_WAIT_INTERVAL_SECONDS:-30}"
CONFIG_PATH="${CONFIG_PATH:-/config/db-indexes.yaml}"

export PGPASSWORD="$DB_SECRET"
# Use PGOPTIONS to set search_path at connection time (works with CONCURRENTLY)
export PGOPTIONS="-c search_path=$DB_SCHEMA"
PSQL_CONN="psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -v ON_ERROR_STOP=1"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

FAILED_COUNT=0

log "Configuration: DB=$DB_USER@$DB_HOST:$DB_PORT/$DB_NAME schema=$DB_SCHEMA yaci=$YACI_URL"

# 1. Wait for yaci-indexer readiness (synced to tip)
log "Waiting for yaci-indexer readiness at ${YACI_URL}/actuator/health/readiness ..."
until curl -sf "${YACI_URL}/actuator/health/readiness" >/dev/null 2>&1; do
  log "yaci-indexer not ready yet, retrying in ${YACI_WAIT_INTERVAL_SECONDS}s..."
  sleep "$YACI_WAIT_INTERVAL_SECONDS"
done
log "yaci-indexer readiness is UP. Proceeding with index checks."

# 2. Get index definitions from YAML
log "Reading index definitions from ${CONFIG_PATH}..."
index_count=$(yq -r '.cardano.rosetta.db_indexes | length' "$CONFIG_PATH")
log "Found $index_count indexes to process"

# 3. Exit early if all indexes already exist and are valid
all_valid=true
for i in $(seq 0 $((index_count - 1))); do
  name=$(yq -r ".cardano.rosetta.db_indexes[$i].name" "$CONFIG_PATH")

  is_valid=$($PSQL_CONN -t -A -c "
    SELECT indisvalid FROM pg_index i
    JOIN pg_class c ON i.indexrelid = c.oid
    JOIN pg_namespace n ON c.relnamespace = n.oid
    WHERE c.relname = '$name' AND n.nspname = '$DB_SCHEMA'
  " 2>/dev/null || echo "")

  if [ "$is_valid" != "t" ]; then
    all_valid=false
    break
  fi
done

if [ "$all_valid" = "true" ]; then
  log "All indexes already exist and are valid. Exiting."
  exit 0
fi

# 4. Cleanup invalid indexes
log "Checking for invalid indexes..."
for i in $(seq 0 $((index_count - 1))); do
  name=$(yq -r ".cardano.rosetta.db_indexes[$i].name" "$CONFIG_PATH")

  is_valid=$($PSQL_CONN -t -A -c "
    SELECT indisvalid FROM pg_index i
    JOIN pg_class c ON i.indexrelid = c.oid
    JOIN pg_namespace n ON c.relnamespace = n.oid
    WHERE c.relname = '$name' AND n.nspname = '$DB_SCHEMA'
  " 2>/dev/null || echo "t")

  if [ "$is_valid" == "f" ]; then
    log "Dropping invalid index: $name"
    $PSQL_CONN -c "DROP INDEX IF EXISTS ${DB_SCHEMA}.${name}" 2>/dev/null || true
  fi
done

# 5. Apply indexes
log "Applying indexes..."
for i in $(seq 0 $((index_count - 1))); do
  name=$(yq -r ".cardano.rosetta.db_indexes[$i].name" "$CONFIG_PATH")
  create_statement=$(yq -r ".cardano.rosetta.db_indexes[$i].command" "$CONFIG_PATH")

  log "Creating index: $name"
  if $PSQL_CONN -c "$create_statement" 2>&1; then
    log "  OK: $name"
  else
    log "  FAILED: $name"
    FAILED_COUNT=$((FAILED_COUNT + 1))
  fi
done

if [ "$FAILED_COUNT" -gt 0 ]; then
  log "ERROR: $FAILED_COUNT index(es) failed to create. Exiting with error."
  exit 1
fi

log "All indexes applied successfully. Exiting."
