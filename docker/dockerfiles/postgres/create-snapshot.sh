#!/bin/bash
set -e

# Simplified snapshot creation script - only handles pg_dump
# GitHub Actions workflow handles: sync checking, indexer pause/resume, metadata generation

# Parse command line arguments
BLOCK_NUMBER=""
SNAPSHOT_NAME=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --block-number)
            BLOCK_NUMBER="$2"
            shift 2
            ;;
        --snapshot-name)
            SNAPSHOT_NAME="$2"
            shift 2
            ;;
        *)
            echo "Unknown parameter: $1"
            exit 1
            ;;
    esac
done

# Validate required parameters
if [ -z "$BLOCK_NUMBER" ] || [ -z "$SNAPSHOT_NAME" ]; then
    echo "Error: Missing required parameters"
    echo "Usage: $0 --block-number <number> --snapshot-name <name>"
    exit 1
fi

# Configuration from environment
DB_SCHEMA=${DB_SCHEMA:-${NETWORK}}
SNAPSHOT_DIR=${SNAPSHOT_DIR:-/snapshots}
DATE_FOLDER=$(date +%Y-%m-%d)
NETWORK=${NETWORK:-mainnet}

# Create output directory
SNAPSHOT_OUTPUT_DIR="${SNAPSHOT_DIR}/${DATE_FOLDER}"
mkdir -p "${SNAPSHOT_OUTPUT_DIR}"

# Set PostgreSQL binary path
PG_BIN=${PG_BIN:-/usr/local/pgsql/bin}
export PATH="${PG_BIN}:${PATH}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }

# Wait for database
log "🔄 Waiting for database to be ready..."
until "${PG_BIN}/pg_isready" -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}; do
    sleep 2
done
log "✅ Database is ready"

# Create snapshot
log "🚀 Creating snapshot: ${SNAPSHOT_NAME}"
log "   Block Number: ${BLOCK_NUMBER}"
log "   Network: ${NETWORK}"
log "   Schema: ${DB_SCHEMA}"
log "   Output: ${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}"
log "   This may take several hours for large databases..."

DUMP_START_TIME=$(date +%s)

"${PG_BIN}/pg_dump" \
    -h ${DB_HOST} \
    -p ${DB_PORT} \
    -U ${DB_USER} \
    -d ${DB_NAME} \
    --schema=${DB_SCHEMA} \
    --exclude-table=${DB_SCHEMA}.flyway_schema_history \
    --format=custom \
    --compress=9 \
    --no-owner \
    --no-privileges \
    --verbose \
    --file="${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}"

DUMP_END_TIME=$(date +%s)
DUMP_DURATION=$((DUMP_END_TIME - DUMP_START_TIME))
DUMP_DURATION_MIN=$((DUMP_DURATION / 60))

# Verify dump file was created
if [ ! -f "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}" ]; then
    log "❌ ERROR: Dump file not created"
    exit 1
fi

FILE_SIZE=$(stat -f%z "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}" 2>/dev/null || stat -c%s "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}" 2>/dev/null)
FILE_SIZE_MB=$((FILE_SIZE / 1024 / 1024))

log "✅ Snapshot created successfully"
log "📊 Statistics:"
log "   - File: ${SNAPSHOT_NAME}"
log "   - Size: ${FILE_SIZE_MB} MB"
log "   - Duration: ${DUMP_DURATION_MIN} minutes"
log "   - Location: ${SNAPSHOT_OUTPUT_DIR}/"
log "   - Block: ${BLOCK_NUMBER}"

# Output file path for GitHub Actions
echo "SNAPSHOT_FILE=${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}" >> ${GITHUB_OUTPUT:-/dev/null}
echo "SNAPSHOT_SIZE_MB=${FILE_SIZE_MB}" >> ${GITHUB_OUTPUT:-/dev/null}
echo "DUMP_DURATION_MIN=${DUMP_DURATION_MIN}" >> ${GITHUB_OUTPUT:-/dev/null}

log "🎉 Snapshot creation completed"
