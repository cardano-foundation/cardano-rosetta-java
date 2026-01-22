#!/bin/bash
set -e

# Simplified snapshot creation script
# Assumes indexer is already paused by workflow
# Queries database for block number and creates snapshot with metadata

# Configuration from environment
DB_SCHEMA=${DB_SCHEMA:-${NETWORK}}
SNAPSHOT_DIR=${SNAPSHOT_DIR:-/snapshots}
DATE_FOLDER=$(date +%Y-%m-%d)
NETWORK=${NETWORK:-mainnet}

# Create day-wise folder structure
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

# Collect metadata from database
log "📊 Collecting metadata from database..."
HIGHEST_BLOCK=$("${PG_BIN}/psql" -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c \
    "SELECT COALESCE(MAX(number), 0) FROM ${DB_SCHEMA}.block;")
HIGHEST_SLOT=$("${PG_BIN}/psql" -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c \
    "SELECT COALESCE(MAX(slot), 0) FROM ${DB_SCHEMA}.block;")
TABLE_COUNT=$("${PG_BIN}/psql" -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_SCHEMA}';")

# Trim whitespace
HIGHEST_BLOCK=$(echo $HIGHEST_BLOCK | xargs)
HIGHEST_SLOT=$(echo $HIGHEST_SLOT | xargs)
TABLE_COUNT=$(echo $TABLE_COUNT | xargs)

log "   - Highest Block: ${HIGHEST_BLOCK}"
log "   - Highest Slot: ${HIGHEST_SLOT}"
log "   - Tables: ${TABLE_COUNT}"

# Generate snapshot name with block number and timestamp
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TIMESTAMP_ISO=$(date -u +%Y-%m-%dT%H:%M:%SZ)
SNAPSHOT_NAME="snapshot_${NETWORK}_${TIMESTAMP}-block${HIGHEST_BLOCK}"

log "📦 Snapshot name: ${SNAPSHOT_NAME}.dump"

# Create snapshot
log "🚀 Creating PostgreSQL dump..."
log "   Network: ${NETWORK}"
log "   Schema: ${DB_SCHEMA}"
log "   Block: ${HIGHEST_BLOCK}"
log "   Output: ${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}.dump"
log "   This may take several hours for large databases..."

DUMP_START_TIME=$(date +%s)

"${PG_BIN}/pg_dump" \
    -h ${DB_HOST} \
    -p ${DB_PORT} \
    -U ${DB_USER} \
    -d ${DB_NAME} \
    --schema=${DB_SCHEMA} \
    --format=custom \
    --compress=9 \
    --no-owner \
    --no-privileges \
    --verbose \
    --file="${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}.dump"

DUMP_END_TIME=$(date +%s)
DUMP_DURATION=$((DUMP_END_TIME - DUMP_START_TIME))
DUMP_DURATION_MIN=$((DUMP_DURATION / 60))

log "✅ Dump completed in ${DUMP_DURATION_MIN} minutes"

# Verify dump file was created
if [ ! -f "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}.dump" ]; then
    log "❌ ERROR: Dump file not created"
    exit 1
fi

# Get file size (Linux stat)
FILE_SIZE=$(stat -c%s "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}.dump" 2>/dev/null)
FILE_SIZE_MB=$((FILE_SIZE / 1024 / 1024))

# Generate checksum
log "🔐 Generating SHA256 checksum..."
cd "${SNAPSHOT_OUTPUT_DIR}"
sha256sum "${SNAPSHOT_NAME}.dump" > "${SNAPSHOT_NAME}.checksum"
CHECKSUM=$(cat "${SNAPSHOT_NAME}.checksum" | awk '{print $1}')
log "✅ Checksum: ${CHECKSUM:0:16}..."

# Generate metadata JSON
log "📝 Creating metadata file..."
cat > "${SNAPSHOT_OUTPUT_DIR}/${SNAPSHOT_NAME}.metadata.json" <<EOF
{
  "snapshot": {
    "name": "${SNAPSHOT_NAME}.dump",
    "created_at": "${TIMESTAMP_ISO}",
    "format": "pg_dump custom format",
    "size_bytes": ${FILE_SIZE},
    "size_mb": ${FILE_SIZE_MB},
    "checksum_sha256": "${CHECKSUM}",
    "compressed": true,
    "compression_level": 9
  },
  "source": {
    "network": "${NETWORK}",
    "database": {
      "host": "${DB_HOST}",
      "port": ${DB_PORT},
      "name": "${DB_NAME}",
      "schema": "${DB_SCHEMA}",
      "table_count": ${TABLE_COUNT},
      "highest_block": ${HIGHEST_BLOCK},
      "highest_slot": ${HIGHEST_SLOT}
    }
  },
  "restore_info": {
    "command": "pg_restore -h \${DB_HOST} -U \${DB_USER} -d \${DB_NAME} --clean --if-exists --schema=${DB_SCHEMA} ${SNAPSHOT_NAME}.dump",
    "yaci_store_auto_resume": true,
    "note": "Yaci-Store will automatically resume from block ${HIGHEST_BLOCK}"
  }
}
EOF

log "✅ Snapshot creation complete!"
log ""
log "📦 Files created in ${DATE_FOLDER}/:"
log "   - ${SNAPSHOT_NAME}.dump"
log "   - ${SNAPSHOT_NAME}.checksum"
log "   - ${SNAPSHOT_NAME}.metadata.json"
log ""
log "📊 Statistics:"
log "   - Network: ${NETWORK}"
log "   - Schema: ${DB_SCHEMA}"
log "   - Highest Block: ${HIGHEST_BLOCK}"
log "   - Highest Slot: ${HIGHEST_SLOT}"
log "   - Tables: ${TABLE_COUNT}"
log "   - File Size: ${FILE_SIZE_MB} MB"
log "   - Dump Duration: ${DUMP_DURATION_MIN} minutes"
log ""
log "📁 Full path: ${SNAPSHOT_OUTPUT_DIR}/"
log "🎉 Done!"
