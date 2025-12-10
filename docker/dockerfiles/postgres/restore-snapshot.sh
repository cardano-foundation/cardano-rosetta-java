#!/bin/bash
set -e

# Configuration
SNAPSHOT_RESTORE_ENABLED=${SNAPSHOT_RESTORE_ENABLED:-false}
SNAPSHOT_URL=${SNAPSHOT_URL:-""}
DB_SCHEMA=${DB_SCHEMA:-${NETWORK}}
SNAPSHOT_DIR=${SNAPSHOT_DIR:-"/snapshots"}

# Internal variables (derived from SNAPSHOT_URL)
SNAPSHOT_FILE=""
SNAPSHOT_CHECKSUM_FILE=""

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }
log_error() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ ERROR: $1" >&2; }
log_warn() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  WARNING: $1"; }

# Download snapshot and checksum from URL
download_snapshot() {
    log "📥 Downloading snapshot from URL: $SNAPSHOT_URL"

    # Install curl if not available
    if ! command -v curl &> /dev/null; then
        log "Installing curl..."
        apt-get update -qq && apt-get install -y -qq curl > /dev/null 2>&1
    fi

    # Extract filename from URL (e.g., snapshot_preprod_20251201_104851.dump)
    local filename=$(basename "$SNAPSHOT_URL")
    local download_path="${SNAPSHOT_DIR}/${filename}"

    # Derive checksum filename (replace .dump with .checksum)
    local checksum_filename
    if [[ "$filename" == *.dump ]]; then
        checksum_filename="${filename%.dump}.checksum"
    else
        checksum_filename="${filename}.checksum"
    fi
    local checksum_url=$(dirname "$SNAPSHOT_URL")/${checksum_filename}
    local checksum_path="${SNAPSHOT_DIR}/${checksum_filename}"

    # Create snapshots directory if it doesn't exist
    mkdir -p "${SNAPSHOT_DIR}"

    log "Downloading snapshot to: $download_path"

    # Download snapshot with progress and retry
    if curl -L --fail --retry 3 --retry-delay 5 \
        --progress-bar \
        -o "$download_path" \
        "$SNAPSHOT_URL"; then
        log "✅ Snapshot download completed: $download_path"

        # Set SNAPSHOT_FILE to point to downloaded file
        SNAPSHOT_FILE="$download_path"

        # Download checksum file
        log "Downloading checksum from: $checksum_url"
        if curl -L --fail --silent --retry 3 --retry-delay 2 \
            -o "$checksum_path" \
            "$checksum_url" 2>/dev/null; then
            log "✅ Checksum file downloaded: $checksum_path"
            SNAPSHOT_CHECKSUM_FILE="$checksum_path"
        else
            log_warn "No checksum file found at $checksum_url"
            log_warn "Checksum verification will be skipped"
        fi

        return 0
    else
        log_error "Failed to download snapshot from $SNAPSHOT_URL"
        return 1
    fi
}

# Wait for PostgreSQL
wait_for_db() {
    log "🔄 Waiting for PostgreSQL to be ready..."
    until pg_isready -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}; do
        sleep 2
    done
    log "✅ PostgreSQL is ready"
}


# Validate snapshot file
validate_snapshot() {
    log "🔍 Validating snapshot file..."

    if [ ! -f "$SNAPSHOT_FILE" ]; then
        log_error "Snapshot file not found: $SNAPSHOT_FILE"
        return 1
    fi

    log "✅ Snapshot file exists: $SNAPSHOT_FILE"

    # Check if it's a valid PostgreSQL custom format dump
    if ! file "$SNAPSHOT_FILE" | grep -q "PostgreSQL custom database dump"; then
        log_warn "File may not be a PostgreSQL custom format dump"
        log_warn "$(file $SNAPSHOT_FILE)"
    fi

    # Verify checksum if provided
    if [ -n "$SNAPSHOT_CHECKSUM_FILE" ] && [ -f "$SNAPSHOT_CHECKSUM_FILE" ]; then
        log "🔐 Verifying checksum..."

        # Get directory of snapshot file for checksum verification
        local snapshot_dir=$(dirname "$SNAPSHOT_FILE")
        cd "$snapshot_dir"

        if sha256sum -c "$SNAPSHOT_CHECKSUM_FILE" 2>&1 | grep -q "OK"; then
            log "✅ Checksum verification passed"
        else
            log_error "Checksum verification failed"
            return 1
        fi
    else
        log_warn "No checksum file provided - skipping verification"
    fi

    log "✅ Snapshot validation passed"
    return 0
}

# Restore snapshot
restore_snapshot() {
    log "🚀 Starting snapshot restoration..."
    log "Target schema: ${DB_SCHEMA}"
    log "Restoring from: $SNAPSHOT_FILE"

    # Restore using pg_restore
    # --clean: clean (drop) database objects before recreating
    # --if-exists: use IF EXISTS when dropping objects
    # --exit-on-error: exit if an error occurs
    # --jobs: number of parallel jobs
    PGPASSWORD=${DB_SECRET} pg_restore \
        -h ${DB_HOST} \
        -p ${DB_PORT} \
        -U ${DB_USER} \
        -d ${DB_NAME} \
        --clean \
        --if-exists \
        --jobs=${RESTORE_THREADS:-4} \
        --verbose \
        "$SNAPSHOT_FILE"

    log "✅ Restoration completed"

    # Drop flyway_schema_history to ensure clean Flyway state
    # This ensures version-independent snapshots work with any application version
    log "🔧 Ensuring clean Flyway state..."
    PGPASSWORD=${DB_SECRET} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -c \
        "DROP TABLE IF EXISTS ${DB_SCHEMA}.flyway_schema_history CASCADE;" || true
    log "✅ Flyway history cleared - will be recreated on first application startup"
}

# Validate restoration
validate_restore() {
    log "🔍 Validating restoration..."

    local table_count=$(PGPASSWORD=${DB_SECRET} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c \
        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_SCHEMA}';" | xargs)

    log "Found ${table_count} tables in schema '${DB_SCHEMA}'"

    if [ "$table_count" -gt 0 ]; then
        # Get highest block number if block table exists
        local highest_block=$(PGPASSWORD=${DB_SECRET} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -t -c \
            "SELECT COALESCE(MAX(number), 0) FROM ${DB_SCHEMA}.block;" 2>/dev/null | xargs || echo "N/A")

        if [ "$highest_block" != "N/A" ]; then
            log "📊 Highest block in restored database: ${highest_block}"
            log "📝 Yaci-Store will automatically resume from block $((highest_block + 1))"
        fi

        log "✅ Validation successful"
        return 0
    else
        log_error "No tables found after restoration"
        return 1
    fi
}

# Main execution
main() {
    log "========================================="
    log "Snapshot Restore Init Container Started"
    log "========================================="
    log "Configuration:"
    log "  SNAPSHOT_RESTORE_ENABLED: ${SNAPSHOT_RESTORE_ENABLED}"
    log "  DB_SCHEMA: ${DB_SCHEMA}"
    log "  SNAPSHOT_URL: ${SNAPSHOT_URL}"
    log "========================================="

    wait_for_db

    if [ "$SNAPSHOT_RESTORE_ENABLED" != "true" ]; then
        log "Snapshot restore is disabled. Skipping restoration."
        log "To enable, set SNAPSHOT_RESTORE_ENABLED=true"
        log "✅ Exiting gracefully."
        exit 0
    fi

    # Download snapshot from URL
    if [ -z "$SNAPSHOT_URL" ]; then
        log_error "SNAPSHOT_URL not specified"
        log_error "Please set SNAPSHOT_URL environment variable"
        exit 1
    fi

    # Clean up old snapshot files before downloading
    log "🧹 Cleaning up old snapshot files in ${SNAPSHOT_DIR}..."
    rm -rf ${SNAPSHOT_DIR}/*.dump ${SNAPSHOT_DIR}/*.checksum ${SNAPSHOT_DIR}/*.metadata.json 2>/dev/null || true
    log "✅ Old snapshots cleaned"

    download_snapshot || exit 1
    validate_snapshot || exit 1
    restore_snapshot || exit 1
    validate_restore || exit 1
    # Clean up downloaded snapshot files after successful restore
    log "🧹 Cleaning up downloaded snapshot files..."
    rm -f "$SNAPSHOT_FILE" "$SNAPSHOT_CHECKSUM_FILE" 2>/dev/null || true
    log "✅ Downloaded snapshots cleaned"


    log "========================================="
    log "✅ Snapshot restoration completed successfully"
    log "========================================="
    log "Next steps:"
    log "  1. Yaci-indexer will start automatically"
    log "  2. It will resume syncing from the last block in the snapshot"
    log "  3. Monitor logs: docker logs yaci-indexer -f"
    log "========================================="
}

main "$@"
