#!/bin/bash

set -e

# Validate required environment variables
if [[ -z "$DB_HOST" || -z "$DB_PORT" || -z "$DB_NAME" || -z "$DB_USER" || -z "$DB_SECRET" || -z "$DB_SCHEMA" ]]; then
    echo "[ERROR] Database connection variables are required: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_SECRET, DB_SCHEMA" >&2
    exit 1
fi

SNAPSHOT_SOURCE_PATH=${SNAPSHOT_SOURCE_PATH:-/snapshots}
RESTORE_VALIDATION_ENABLED=${RESTORE_VALIDATION_ENABLED:-true}
SNAPSHOT_RESTORE_ENABLED=${SNAPSHOT_RESTORE_ENABLED:-false}
CLEANUP_TEMP_FILES=${CLEANUP_TEMP_FILES:-true}

# IAGON Configuration
IAGON_METADATA_FILE=${IAGON_METADATA_FILE:-""}
DOWNLOAD_DIR="/tmp/iagon_download"
CHUNKS_DIR="/tmp/chunks"

# SNAPSHOT_FILE will be set dynamically after downloading and reconstructing from IAGON
SNAPSHOT_FILE=""

# Trap to ensure cleanup on exit/interruption
cleanup_on_exit() {
    if [ -d "$CHUNKS_DIR" ] || [ -d "$DOWNLOAD_DIR" ] || [ -f "/tmp/restore.log" ]; then
        log_info "Script interrupted or exiting - performing cleanup..."
        cleanup_temp_files 2>/dev/null || true
    fi
}
trap cleanup_on_exit EXIT INT TERM

# PostgreSQL client tools are in standard location for alpine postgres image
PG_BIN="/usr/local/bin"

log_info() {
    echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - $1" >&2
}

log_warn() {
    echo "[WARN] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

wait_for_db() {
    log_info "Waiting for database to be ready..."
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if PGPASSWORD="$DB_SECRET" "$PG_BIN/pg_isready" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
            log_info "Database is ready"
            return 0
        fi
        
        log_info "Attempt $attempt/$max_attempts: Database not ready, waiting..."
        sleep 5
        ((attempt++))
    done
    
    log_error "Database failed to become ready after $max_attempts attempts"
    return 1
}

validate_snapshot() {
    local snapshot_file="$1"
    
    if [ ! -f "$snapshot_file" ]; then
        log_error "Snapshot file not found: $snapshot_file"
        return 1
    fi
    
    # Check if file is readable and has content
    if [ ! -r "$snapshot_file" ] || [ ! -s "$snapshot_file" ]; then
        log_error "Snapshot file is not readable or empty: $snapshot_file"
        return 1
    fi
    
    # Basic validation - skip file command as it's not available in alpine postgres image
    log_info "Snapshot file basic validation passed"
    
    # Check for PostgreSQL dump markers
    if head -20 "$snapshot_file" | grep -q "PostgreSQL database dump\|-- Dumped from database version"; then
        log_info "Snapshot file appears to be a valid PostgreSQL dump"
    else
        log_warn "Snapshot file does not contain recognizable PostgreSQL dump headers"
    fi
    
    return 0
}

# IAGON Download Functions
download_metadata_from_local() {
    local metadata_path="$1"
    
    if [ ! -f "$metadata_path" ]; then
        log_error "Metadata file not found: $metadata_path"
        return 1
    fi
    
    log_info "Using local metadata file: $metadata_path"
    cp "$metadata_path" "$DOWNLOAD_DIR/metadata.json"
    return 0
}

download_chunk_from_iagon() {
    local download_url="$1"
    local chunk_name="$2"
    local output_file="$3"
    
    log_info "Downloading chunk: $chunk_name"
    
    # Use wget for simple public download (no API key needed)
    if wget --quiet --timeout=300 --tries=3 --output-document="$output_file" "$download_url"; then
        local file_size
        file_size=$(du -h "$output_file" | cut -f1)
        return 0
    else
        log_error "Failed to download chunk $chunk_name from $download_url"
        return 1
    fi
}

download_chunks_from_iagon() {
    local metadata_file="$DOWNLOAD_DIR/metadata.json"
    
    if [ ! -f "$metadata_file" ]; then
        log_error "Metadata file not found for chunk download"
        return 1
    fi
    
    log_info "Reading chunk information from metadata..."
    
    # Create chunks directory
    mkdir -p "$CHUNKS_DIR"
    
    # Parse chunks from metadata and download each one
    local chunk_count
    chunk_count=$(jq -r '.iagon_upload.chunks | length' "$metadata_file")
    
    if [[ "$chunk_count" == "null" || "$chunk_count" -eq 0 ]]; then
        log_error "No chunks found in metadata file"
        return 1
    fi
    
    log_info "Found $chunk_count chunks to download"
    
    for i in $(seq 0 $((chunk_count - 1))); do
        local chunk_info
        chunk_info=$(jq -r ".iagon_upload.chunks[$i]" "$metadata_file")
        
        local download_url=$(echo "$chunk_info" | jq -r '.download_url')
        local chunk_name=$(echo "$chunk_info" | jq -r '.chunk')
        
        if [[ "$download_url" == "null" || "$chunk_name" == "null" ]]; then
            log_error "Invalid chunk data at index $i: download_url=$download_url, chunk_name=$chunk_name"
            return 1
        fi
        
        # Use chunk_name (snapshot_part_aa, snapshot_part_ab, etc.) for proper reconstruction
        local output_file="$CHUNKS_DIR/$chunk_name"
        
        if ! download_chunk_from_iagon "$download_url" "$chunk_name" "$output_file"; then
            log_error "Failed to download chunk $chunk_name"
            return 1
        fi
        
        # Verify chunk was downloaded and has content
        if [ ! -f "$output_file" ] || [ ! -s "$output_file" ]; then
            log_error "Downloaded chunk is empty or missing: $output_file"
            return 1
        fi
        
    done
    
    log_info "All chunks downloaded successfully"
    return 0
}

reconstruct_snapshot_from_chunks() {
    log_info "Reconstructing snapshot from downloaded chunks..."
    
    # Get snapshot name from metadata
    local metadata_file="$DOWNLOAD_DIR/metadata.json"
    local snapshot_name
    snapshot_name=$(jq -r '.snapshot.name' "$metadata_file")
    
    if [[ "$snapshot_name" == "null" ]]; then
        log_error "Cannot determine snapshot name from metadata"
        return 1
    fi
    
    local reconstructed_file="$DOWNLOAD_DIR/$snapshot_name"
    
    # Sort chunks by original filename (snapshot_part_aa, snapshot_part_ab, etc.)
    local sorted_chunks
    sorted_chunks=$(ls -1 "$CHUNKS_DIR"/snapshot_part_* | sort)
    
    if [ -z "$sorted_chunks" ]; then
        log_error "No chunk files found in $CHUNKS_DIR"
        return 1
    fi
    
    log_info "Concatenating chunks to reconstruct snapshot..."
    
    # Concatenate all chunks in order
    cat $sorted_chunks > "$reconstructed_file"
    
    # Verify the reconstructed file
    if [ ! -f "$reconstructed_file" ] || [ ! -s "$reconstructed_file" ]; then
        log_error "Failed to reconstruct snapshot file"
        return 1
    fi
    
    # Verify checksum if available
    local expected_checksum
    expected_checksum=$(jq -r '.snapshot.checksum_sha256' "$metadata_file")
    
    # If checksum is empty in metadata, try to get it from alternative field
    if [[ "$expected_checksum" == "null" || "$expected_checksum" == "" ]]; then
        expected_checksum=$(jq -r '.snapshot.checksum' "$metadata_file")
    fi
    
    if [[ "$expected_checksum" != "null" && "$expected_checksum" != "" ]]; then
        log_info "Verifying reconstructed snapshot checksum..."
        local actual_checksum
        actual_checksum=$(sha256sum "$reconstructed_file" | cut -d' ' -f1)
        
        if [[ "$actual_checksum" == "$expected_checksum" ]]; then
            log_info "Checksum verification passed"
        else
            log_error "Checksum verification failed - reconstructed file may be corrupted"
            return 1
        fi
    else
        log_warn "No checksum available in metadata - skipping integrity verification"
        log_warn "Consider adding checksum to metadata for data validation"
    fi
    
    local file_size
    file_size=$(du -h "$reconstructed_file" | cut -f1)
    log_info "Snapshot reconstruction completed - size: $file_size"
    
    # ✅ IMPORTANT: Update SNAPSHOT_FILE to point to reconstructed file
    # This overrides the default local file path when using IAGON restore
    SNAPSHOT_FILE="$reconstructed_file"
    log_info "SNAPSHOT_FILE updated to: $SNAPSHOT_FILE"
    
    return 0
}

download_snapshot_from_iagon() {
    log_info "Starting IAGON snapshot download process..."
    
    # Prepare download directory
    mkdir -p "$DOWNLOAD_DIR"
    mkdir -p "$CHUNKS_DIR"
    
    # Step 1: Get metadata file
    if [[ -n "$IAGON_METADATA_FILE" ]]; then
        # Use provided metadata file path
        if ! download_metadata_from_local "$IAGON_METADATA_FILE"; then
            log_error "Failed to load metadata file"
            return 1
        fi
    else
        log_error "IAGON_METADATA_FILE not specified - cannot download without metadata"
        return 1
    fi
    
    # Check if IAGON upload was enabled in the metadata
    local metadata_file="$DOWNLOAD_DIR/metadata.json"
    local iagon_enabled=$(jq -r '.iagon_upload.enabled' "$metadata_file")
    
    if [[ "$iagon_enabled" != "true" ]]; then
        log_info "IAGON upload was disabled for this snapshot - using local file instead"
        
        # Get local snapshot file path from metadata
        local snapshot_file=$(jq -r '.snapshot.file' "$metadata_file")
        if [[ -f "$snapshot_file" ]]; then
            log_info "Using local snapshot file: $snapshot_file"
            SNAPSHOT_FILE="$snapshot_file"
            return 0
        else
            log_error "Local snapshot file not found: $snapshot_file"
            return 1
        fi
    fi
    
    # Step 2: Download all chunks
    if ! download_chunks_from_iagon; then
        log_error "Failed to download chunks from IAGON"
        return 1
    fi
    
    # Step 3: Reconstruct snapshot
    if ! reconstruct_snapshot_from_chunks; then
        log_error "Failed to reconstruct snapshot from chunks"
        return 1
    fi
    
    log_info "IAGON snapshot download completed successfully"
    return 0
}

ensure_database_setup() {
    log_info "Ensuring database and schema are properly set up..."
    
    # Check if database exists, if not this will fail
    if ! PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" >/dev/null 2>&1; then
        log_error "Database '$DB_NAME' does not exist or user '$DB_USER' lacks access"
        log_error "Ensure the database server has created the database and user first"
        return 1
    fi
    
    # Check if schema exists
    local schema_exists=$(PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -t -c "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '$DB_SCHEMA';" | tr -d ' ')
    
    if [ "$schema_exists" = "0" ]; then
        log_info "Creating schema '$DB_SCHEMA'..."
        if PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
            -h "$DB_HOST" \
            -p "$DB_PORT" \
            -U "$DB_USER" \
            -d "$DB_NAME" \
            -c "CREATE SCHEMA IF NOT EXISTS \"$DB_SCHEMA\";"; then
            log_info "Schema '$DB_SCHEMA' created successfully"
        else
            log_error "Failed to create schema '$DB_SCHEMA'"
            return 1
        fi
    else
        log_info "Schema '$DB_SCHEMA' already exists"
    fi
    
    return 0
}

clear_database() {
    log_info "Clearing existing data in schema '$DB_SCHEMA'..."
    if PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -c "DROP SCHEMA IF EXISTS \"$DB_SCHEMA\" CASCADE;"; then
        log_info "Schema '$DB_SCHEMA' cleared (will be recreated by restore)"
    else
        log_error "Failed to clear schema '$DB_SCHEMA'"
        return 1
    fi
    return 0
}

restore_snapshot() {
    local snapshot_file="$1"
    
    log_info "Starting database restore from: $snapshot_file"
    
    # Determine if we need to decompress
    local restore_cmd
    if [[ "$snapshot_file" == *.gz ]]; then
        log_info "Decompressing and restoring gzipped snapshot..."
        restore_cmd="zcat \"$snapshot_file\""
    else
        log_info "Restoring uncompressed snapshot..."
        restore_cmd="cat \"$snapshot_file\""
    fi
    
    # Restore from snapshot - allow schema exists errors but catch real errors
    if eval "$restore_cmd" | PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        --set="ON_ERROR_STOP=0" \
        2>&1 | tee /tmp/restore.log; then
        
        # Check for critical errors (excluding schema exists)
        if grep -E "ERROR:" /tmp/restore.log | grep -v "already exists"; then
            log_error "Database restore failed with critical errors"
            return 1
        else
            log_info "Database restore completed successfully"
            return 0
        fi
    else
        log_error "Database restore command failed"
        return 1
    fi
}

validate_restore() {
    log_info "Validating restored database..."
    
    # Check if we can connect and basic schema exists
    if PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_SCHEMA';" >/dev/null 2>&1; then
        
        local table_count=$(PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
            -h "$DB_HOST" \
            -p "$DB_PORT" \
            -U "$DB_USER" \
            -d "$DB_NAME" \
            -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_SCHEMA';")
        
        table_count=$(echo "$table_count" | tr -d ' ')
        
        if [ "$table_count" -gt 0 ]; then
            log_info "Validation successful: Found $table_count tables in schema '$DB_SCHEMA'"
            return 0
        else
            log_error "Validation failed: No tables found in schema '$DB_SCHEMA'"
            return 1
        fi
    else
        log_error "Validation failed: Cannot connect to database or query schema"
        return 1
    fi
}

main() {
    log_info "Starting IAGON snapshot restore service"
    log_info "Configuration:"
    log_info "  SNAPSHOT_RESTORE_ENABLED: $SNAPSHOT_RESTORE_ENABLED"
    log_info "  IAGON_METADATA_FILE: $IAGON_METADATA_FILE"
    log_info "  Download directory: $DOWNLOAD_DIR"
    log_info "  Chunks directory: $CHUNKS_DIR"
    log_info "  DB_HOST: $DB_HOST"
    log_info "  DB_PORT: $DB_PORT"
    log_info "  DB_NAME: $DB_NAME"
    log_info "  DB_SCHEMA: $DB_SCHEMA"
    log_info "  RESTORE_VALIDATION_ENABLED: $RESTORE_VALIDATION_ENABLED"
    log_info "  CLEANUP_TEMP_FILES: $CLEANUP_TEMP_FILES"
    
    # Check if restore is enabled
    if [ "$SNAPSHOT_RESTORE_ENABLED" != "true" ]; then
        log_info "Snapshot restore is disabled. Set SNAPSHOT_RESTORE_ENABLED=true to enable."
        log_info "Database setup will still be ensured, then container will exit."
        
        # Wait for database to be ready
        if ! wait_for_db; then
            log_error "Exiting due to database connectivity issues"
            exit 1
        fi
        
        # Ensure database setup even if not restoring
        if ! ensure_database_setup; then
            log_error "Database setup failed"
            exit 1
        fi
        
        log_info "Database setup completed. No restore requested. Container will now exit."
        exit 0
    fi
    
    # Wait for database to be ready
    if ! wait_for_db; then
        log_error "Exiting due to database connectivity issues"
        exit 1
    fi
    
    # Ensure database and schema exist
    if ! ensure_database_setup; then
        log_error "Database setup failed"
        exit 1
    fi
    
    # Clear database if requested
    if ! clear_database; then
        log_error "Database clear failed"
        exit 1
    fi
    
    # Download snapshot from IAGON
    log_info "Downloading snapshot from IAGON cloud..."
    
    if [[ -z "$IAGON_METADATA_FILE" ]]; then
        log_error "IAGON_METADATA_FILE is required"
        log_error "Example: IAGON_METADATA_FILE=/snapshots/snapshot_\${NETWORK}_20250731_164531.metadata.json"
        exit 1
    fi
    
    if ! download_snapshot_from_iagon; then
        log_error "Failed to download snapshot from IAGON"
        exit 1
    fi
    
    log_info "IAGON snapshot download completed - proceeding with restore"
    
    # Validate snapshot file
    if [ "$RESTORE_VALIDATION_ENABLED" = "true" ]; then
        if ! validate_snapshot "$SNAPSHOT_FILE"; then
            log_error "Snapshot validation failed"
            exit 1
        fi
    fi
    
    # Perform restore
    if ! restore_snapshot "$SNAPSHOT_FILE"; then
        log_error "Snapshot restore failed"
        exit 1
    fi
    
    # Validate restore if enabled
    if [ "$RESTORE_VALIDATION_ENABLED" = "true" ]; then
        if ! validate_restore; then
            log_error "Restore validation failed"
            exit 1
        fi
    fi
    
    log_info "Snapshot restore completed successfully"
    
    # Clean up temporary files after successful restoration
    cleanup_temp_files
    
    log_info "Indexer can now start syncing from the restored data"
    log_info "Restore service completed. Container will now exit."
}

cleanup_temp_files() {
    if [[ "$CLEANUP_TEMP_FILES" != "true" ]]; then
        log_info "Temporary file cleanup is disabled (CLEANUP_TEMP_FILES=$CLEANUP_TEMP_FILES)"
        log_info "Temporary files preserved at: $CHUNKS_DIR and $DOWNLOAD_DIR"
        return 0
    fi
    
    log_info "Cleaning up temporary files and directories..."
    
    # Clean up chunks directory
    if [ -d "$CHUNKS_DIR" ]; then
        local chunk_count=$(ls -1 "$CHUNKS_DIR"/* 2>/dev/null | wc -l || echo 0)
        if [ "$chunk_count" -gt 0 ]; then
            rm -rf "$CHUNKS_DIR" 2>/dev/null || log_warn "Failed to remove chunks directory"
        fi
    fi
    
    # Clean up download directory (includes reconstructed snapshot)
    if [ -d "$DOWNLOAD_DIR" ]; then
        rm -rf "$DOWNLOAD_DIR" 2>/dev/null || log_warn "Failed to remove download directory"
    fi
    
    # Clean up restore log
    if [ -f "/tmp/restore.log" ]; then
        rm -f "/tmp/restore.log" 2>/dev/null || log_warn "Failed to remove restore log"
    fi
    
    log_info "Temporary file cleanup completed"
}

# Run main function
main "$@"