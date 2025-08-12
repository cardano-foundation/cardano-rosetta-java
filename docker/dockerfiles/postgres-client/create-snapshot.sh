#!/bin/bash

set -e

# Default configuration
SNAPSHOT_DESTINATION_PATH=${SNAPSHOT_DESTINATION_PATH:-/snapshots}
SNAPSHOT_RETENTION_DAYS=${SNAPSHOT_RETENTION_DAYS:-30}
LOG=${LOG:-WARN}

# IAGON Upload Configuration
IAGON_UPLOAD_ENABLED=${IAGON_UPLOAD_ENABLED:-false}
IAGON_API_KEY=${IAGON_API_KEY:-""}
IAGON_GATEWAY=${IAGON_GATEWAY:-"https://gw.iagon.com"}
IAGON_CHUNK_SIZE_MB=${IAGON_CHUNK_SIZE_MB:-190}
IAGON_CHUNK_PREFIX="snapshot_part_"
CHUNK_DIR="/tmp/chunks"

# PostgreSQL client tools are in standard location for alpine postgres image
PG_BIN="/usr/local/bin"

# Validate required environment variables
if [[ -z "$NETWORK" ]]; then
    echo "[ERROR] NETWORK environment variable is required" >&2
    exit 1
fi

if [[ -z "$DB_HOST" || -z "$DB_PORT" || -z "$DB_NAME" || -z "$DB_USER" || -z "$DB_SECRET" || -z "$DB_SCHEMA" ]]; then
    echo "[ERROR] Database connection variables are required: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_SECRET, DB_SCHEMA" >&2
    exit 1
fi

# Generate snapshot name with timestamp
SNAPSHOT_NAME="snapshot_${NETWORK}_$(date +%Y%m%d_%H%M%S).sql.gz"
SNAPSHOT_FILE="$SNAPSHOT_DESTINATION_PATH/$SNAPSHOT_NAME"
METADATA_FILE="$SNAPSHOT_DESTINATION_PATH/${SNAPSHOT_NAME%.sql.gz}.metadata.json"
CHECKSUM_FILE="$SNAPSHOT_DESTINATION_PATH/${SNAPSHOT_NAME%.sql.gz}.checksum"

log_info() {
    if [ "$LOG" = "DEBUG" ] || [ "$LOG" = "INFO" ]; then
        echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
    fi
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

get_database_info() {
    log_info "Collecting database information..." >&2
    
    # Get database size
    local db_size=$(PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -t -c "SELECT pg_size_pretty(pg_database_size('$DB_NAME'));" | tr -d ' ')
    
    # Get schema size
    local schema_size=$(PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -t -c "SELECT pg_size_pretty(SUM(pg_total_relation_size(schemaname||'.'||tablename))) FROM pg_tables WHERE schemaname = '$DB_SCHEMA';" | tr -d ' ')
    
    # Get table count
    local table_count=$(PGPASSWORD="$DB_SECRET" "$PG_BIN/psql" \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_SCHEMA';" | tr -d ' ')
    
    echo "$db_size|$schema_size|$table_count"
}

cleanup_old_snapshots() {
    if [ "$SNAPSHOT_RETENTION_DAYS" -gt 0 ]; then
        log_info "Cleaning up snapshots older than $SNAPSHOT_RETENTION_DAYS days..."
        
        find "$SNAPSHOT_DESTINATION_PATH" -name "snapshot_${NETWORK}_*.sql*" -type f -mtime +$SNAPSHOT_RETENTION_DAYS -delete 2>/dev/null || true
        find "$SNAPSHOT_DESTINATION_PATH" -name "snapshot_${NETWORK}_*.metadata.json" -type f -mtime +$SNAPSHOT_RETENTION_DAYS -delete 2>/dev/null || true
        find "$SNAPSHOT_DESTINATION_PATH" -name "snapshot_${NETWORK}_*.checksum" -type f -mtime +$SNAPSHOT_RETENTION_DAYS -delete 2>/dev/null || true
        
        log_info "Cleanup completed"
    fi
}

create_snapshot() {
    log_info "Starting database snapshot creation"
    log_info "Target file: $SNAPSHOT_FILE"
    
    # Ensure destination directory exists
    mkdir -p "$SNAPSHOT_DESTINATION_PATH"
    
    # Build pg_dump command with defaults
    local dump_cmd=(
        "$PG_BIN/pg_dump"
        -h "$DB_HOST"
        -p "$DB_PORT"
        -U "$DB_USER"
        -d "$DB_NAME"
        --schema="$DB_SCHEMA"
        --no-owner
        --no-privileges
        --format=plain
        --verbose
    )
    
    local start_time=$(date +%s)
    
    # Always create compressed snapshot
    log_info "Creating compressed snapshot..."
    if PGPASSWORD="$DB_SECRET" "${dump_cmd[@]}" | pigz > "$SNAPSHOT_FILE"; then
        log_info "Compressed snapshot created successfully"
    else
        log_error "Failed to create compressed snapshot"
        return 1
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log_info "Snapshot creation completed in ${duration} seconds"
    return 0
}

create_checksum() {
    log_info "Creating checksum for snapshot..." >&2
    
    if sha256sum "$SNAPSHOT_FILE" > "$CHECKSUM_FILE"; then
        log_info "Checksum created: $CHECKSUM_FILE" >&2
        local checksum=$(cat "$CHECKSUM_FILE" | cut -d' ' -f1)
        echo "$checksum"
    else
        log_error "Failed to create checksum"
        return 1
    fi
}

create_metadata() {
    local db_info="$1"
    local checksum="$2"
    local snapshot_size
    
    if [ -f "$SNAPSHOT_FILE" ]; then
        snapshot_size=$(stat -c%s "$SNAPSHOT_FILE" 2>/dev/null || echo "unknown")
    else
        snapshot_size="unknown"
    fi
    
    local db_size=$(echo "$db_info" | cut -d'|' -f1)
    local schema_size=$(echo "$db_info" | cut -d'|' -f2)
    local table_count=$(echo "$db_info" | cut -d'|' -f3)
    
    log_info "Creating metadata file..."
    
    # Base metadata structure
    local metadata_content='{
  "snapshot": {
    "name": "'$SNAPSHOT_NAME'",
    "file": "'$SNAPSHOT_FILE'",
    "created_at": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
    "size_bytes": '$snapshot_size',
    "checksum_sha256": "'$checksum'",
    "compressed": true
  },
  "source": {
    "network": "'$NETWORK'",
    "database": {
      "host": "'$DB_HOST'",
      "port": '$DB_PORT',
      "name": "'$DB_NAME'",
      "schema": "'$DB_SCHEMA'",
      "size": "'$db_size'",
      "schema_size": "'$schema_size'",
      "table_count": '$table_count'
    }
  },
  "configuration": {
    "parallel_jobs": 1,
    "include_indexes": true,
    "include_constraints": true
  }'
    
    # Add IAGON upload information if enabled and successful
    if [[ "$IAGON_UPLOAD_ENABLED" == "true" && ${#IAGON_UPLOAD_RESULTS[@]} -gt 0 ]]; then
        metadata_content="$metadata_content"',
  "iagon_upload": {
    "enabled": true,
    "directory_name": "'$IAGON_DIRECTORY_NAME'",
    "directory_id": "'$IAGON_DIRECTORY_ID'",
    "chunk_size_mb": '$IAGON_CHUNK_SIZE_MB',
    "total_chunks": '${#IAGON_UPLOAD_RESULTS[@]}',
    "chunks": ['
        
        # Add chunk information
        local chunk_json=""
        for i in "${!IAGON_UPLOAD_RESULTS[@]}"; do
            if [[ $i -gt 0 ]]; then
                chunk_json="$chunk_json,"
            fi
            chunk_json="$chunk_json${IAGON_UPLOAD_RESULTS[$i]}"
        done
        
        metadata_content="$metadata_content$chunk_json"'],
    "reconstruction": {
      "command": "cat snapshot_part_* > reconstructed_'$SNAPSHOT_NAME'",
      "note": "Download all chunks to the same directory and run the command above"
    }
  }'
    else
        metadata_content="$metadata_content"',
  "iagon_upload": {
    "enabled": false
  }'
    fi
    
    metadata_content="$metadata_content"'
}'
    
    # Write metadata to file
    echo "$metadata_content" | jq '.' > "$METADATA_FILE" 2>/dev/null || echo "$metadata_content" > "$METADATA_FILE"
    
    log_info "Metadata created: $METADATA_FILE"
}

validate_snapshot() {
    log_info "Validating created snapshot..."
    
    # Check file exists and is readable
    if [ ! -f "$SNAPSHOT_FILE" ] || [ ! -r "$SNAPSHOT_FILE" ]; then
        log_error "Snapshot file is not readable or doesn't exist"
        return 1
    fi
    
    # Check file size
    local file_size=$(stat -c%s "$SNAPSHOT_FILE" 2>/dev/null || echo "0")
    if [ "$file_size" -eq 0 ]; then
        log_error "Snapshot file is empty"
        return 1
    fi
    
    # Verify checksum
    if [ -f "$CHECKSUM_FILE" ]; then
        if sha256sum -c "$CHECKSUM_FILE" >/dev/null 2>&1; then
            log_info "Checksum validation passed"
        else
            log_error "Checksum validation failed"
            return 1
        fi
    fi
    
    log_info "Snapshot validation completed successfully"
    return 0
}

# IAGON Upload Functions
setup_iagon_directory() {
    IAGON_DIRECTORY_NAME=$(date +"%Y-%m-%d")
    
    log_info "Setting up IAGON directory: $IAGON_DIRECTORY_NAME"
    
    # Check if directory already exists
    local list_dirs_response
    list_dirs_response=$(curl -s -w "HTTP_CODE:%{http_code}" \
        -X GET "${IAGON_GATEWAY}/api/v2/storage/directory" \
        -H "x-api-key: $IAGON_API_KEY" \
        -H "User-Agent: CardanoRosettaSnapshot/1.0" \
        -H "Accept: application/json" \
        --connect-timeout 30 \
        --max-time 60 \
        2>/dev/null)
    
    # Extract HTTP code and response
    local http_code=$(echo "$list_dirs_response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)
    local response_body=$(echo "$list_dirs_response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    # Validate response
    if [[ "$http_code" != "200" ]]; then
        log_error "IAGON API request failed (HTTP $http_code)"
        return 1
    fi
    
    if ! echo "$response_body" | jq empty 2>/dev/null; then
        log_error "Invalid JSON response from IAGON API"
        return 1
    fi
    
    # Check if our directory exists
    IAGON_DIRECTORY_ID=$(echo "$response_body" | jq -r --arg dir_name "$IAGON_DIRECTORY_NAME" '
        (.data.directories[] | select(.directory_name == $dir_name) | ._id) //
        (.data.nestedDirectories[] | select(.directory_name == $dir_name) | ._id) //
        empty' 2>/dev/null)
    
    if [[ -n "$IAGON_DIRECTORY_ID" && "$IAGON_DIRECTORY_ID" != "null" ]]; then
        log_info "Directory '$IAGON_DIRECTORY_NAME' already exists with ID: $IAGON_DIRECTORY_ID"
    else
        log_info "Creating new directory: $IAGON_DIRECTORY_NAME"
        
        local create_dir_response
        create_dir_response=$(curl -s -w "HTTP_CODE:%{http_code}" \
            -X POST "${IAGON_GATEWAY}/api/v2/storage/directory" \
            -H "x-api-key: $IAGON_API_KEY" \
            -H "Content-Type: application/json" \
            -H "User-Agent: CardanoRosettaSnapshot/1.0" \
            -H "Accept: application/json" \
            --connect-timeout 30 \
            --max-time 60 \
            --data "{\"directory_name\": \"$IAGON_DIRECTORY_NAME\"}" \
            2>/dev/null)
        
        # Extract HTTP code and response
        local create_http_code=$(echo "$create_dir_response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)
        local create_response_body=$(echo "$create_dir_response" | sed 's/HTTP_CODE:[0-9]*$//')
        
        # Validate HTTP response for create directory
        if [[ "$create_http_code" != "200" && "$create_http_code" != "201" ]]; then
            log_error "Failed to create directory (HTTP $create_http_code)"
            return 1
        fi
        
        # Validate response is valid JSON
        if ! echo "$create_response_body" | jq empty 2>/dev/null; then
            log_error "Invalid JSON response from create directory API"
            return 1
        fi
        
        IAGON_DIRECTORY_ID=$(echo "$create_response_body" | jq -r '.data._id // empty')
        
        if [[ -z "$IAGON_DIRECTORY_ID" || "$IAGON_DIRECTORY_ID" == "null" ]]; then
            log_warn "Failed to create directory. Proceeding with root directory upload."
            IAGON_DIRECTORY_ID=""
        else
            log_info "Directory created successfully with ID: $IAGON_DIRECTORY_ID"
        fi
    fi
}

split_snapshot_for_upload() {
    log_info "Splitting snapshot into ${IAGON_CHUNK_SIZE_MB}MB chunks for upload..."
    
    # Setup chunks directory
    rm -rf "$CHUNK_DIR" 2>/dev/null || true
    mkdir -p "$CHUNK_DIR"
    
    # Split the file
    if ! split -b "${IAGON_CHUNK_SIZE_MB}M" "$SNAPSHOT_FILE" "${CHUNK_DIR}/${IAGON_CHUNK_PREFIX}"; then
        log_error "Failed to split file into chunks"
        return 1
    fi
    
    local chunk_count=$(ls -1 "${CHUNK_DIR}/${IAGON_CHUNK_PREFIX}"* 2>/dev/null | wc -l)
    log_info "File split into $chunk_count chunks"
    
    return 0
}

upload_chunks_to_iagon() {
    log_info "Starting chunk upload to IAGON..."
    
    declare -a upload_results=()
    local chunk_count=0
    local total_chunks=$(ls -1 "${CHUNK_DIR}/${IAGON_CHUNK_PREFIX}"* 2>/dev/null | wc -l)
    
    for chunk_file in "${CHUNK_DIR}/${IAGON_CHUNK_PREFIX}"*; do
        chunk_count=$((chunk_count + 1))
        local chunk_name=$(basename "$chunk_file")
        
        log_info "Uploading chunk $chunk_count/$total_chunks"
        
        # Check if file already exists
        local existing_file_id=""
        if [[ -n "$IAGON_DIRECTORY_ID" ]]; then
            local list_files_response
            list_files_response=$(curl -s -X GET "${IAGON_GATEWAY}/api/v2/storage/directory?parent_directory_id=$IAGON_DIRECTORY_ID" \
                -H "x-api-key: $IAGON_API_KEY" 2>/dev/null || echo '{"data":{"files":[]}}')
            
            existing_file_id=$(echo "$list_files_response" | jq -r --arg name "$chunk_name" '.data.files[] | select(.name == $name) | ._id // empty' 2>/dev/null)
        fi
        
        local file_id
        if [[ -n "$existing_file_id" && "$existing_file_id" != "null" ]]; then
            log_info "File '$chunk_name' already exists, skipping upload"
            file_id="$existing_file_id"
        else
            # Upload new file
            local upload_response
            if [[ -n "$IAGON_DIRECTORY_ID" ]]; then
                upload_response=$(curl -s -X POST "${IAGON_GATEWAY}/api/v2/storage/upload" \
                    -H "x-api-key: $IAGON_API_KEY" \
                    -F "file=@${chunk_file}" \
                    -F "directoryId=$IAGON_DIRECTORY_ID" 2>/dev/null || echo '{"data":{}}')
            else
                upload_response=$(curl -s -X POST "${IAGON_GATEWAY}/api/v2/storage/upload" \
                    -H "x-api-key: $IAGON_API_KEY" \
                    -F "file=@${chunk_file}" 2>/dev/null || echo '{"data":{}}')
            fi
            
            
            file_id=$(echo "$upload_response" | jq -r '.data._id // empty')
            
            if [[ "$file_id" == "null" || -z "$file_id" ]]; then
                log_error "Upload failed for $chunk_file"
                continue
            fi
            
            log_info "Upload successful! File ID: $file_id"
        fi
        
        # Make file publicly shareable
        local share_response
        share_response=$(curl -s -w "%{http_code}" -X PATCH "${IAGON_GATEWAY}/api/v2/storage/${file_id}/share" \
            -H "x-api-key: $IAGON_API_KEY" \
            -F "password=" 2>/dev/null || echo "000")
        
        local http_code="${share_response: -3}"
        local share_body="${share_response%???}"
        
        # Extract download URL
        local download_url
        if [[ "$http_code" -ge 200 && "$http_code" -lt 300 && -n "$share_body" ]]; then
            local base_url=$(echo "$share_body" | jq -r '.data.baseShareableLink // empty' 2>/dev/null)
            local concatenator=$(echo "$share_body" | jq -r '.data.concatenator // empty' 2>/dev/null)
            local key=$(echo "$share_body" | jq -r '.data.key // empty' 2>/dev/null)
            
            if [[ -n "$base_url" && -n "$concatenator" && -n "$key" ]]; then
                download_url="${base_url}${concatenator}${key}"
            else
                download_url="${IAGON_GATEWAY}/api/v2/storage/download/${file_id}"
            fi
        else
            download_url="${IAGON_GATEWAY}/api/v2/storage/download/${file_id}"
        fi
        
        # Store upload result
        local chunk_size=$(stat -c%s "$chunk_file" 2>/dev/null || echo "0")
        upload_results+=("{\"chunk\":\"$chunk_name\",\"file_id\":\"$file_id\",\"download_url\":\"$download_url\",\"chunk_number\":$chunk_count,\"size_bytes\":$chunk_size}")
        
    done
    
    # Store results for metadata
    IAGON_UPLOAD_RESULTS=("${upload_results[@]}")
    
    log_info "All $total_chunks chunks processed for IAGON upload!"
    return 0
}

upload_to_iagon() {
    if [[ "$IAGON_UPLOAD_ENABLED" != "true" ]]; then
        log_info "IAGON upload is disabled"
        return 0
    fi
    
    if [[ -z "$IAGON_API_KEY" ]]; then
        log_error "IAGON_API_KEY is required for upload but not provided"
        return 1
    fi
    
    log_info "Starting IAGON upload process..."
    
    # Setup IAGON directory
    if ! setup_iagon_directory; then
        log_error "Failed to setup IAGON directory"
        return 1
    fi
    
    # Check if snapshot file exists before attempting upload
    if [[ ! -f "$SNAPSHOT_FILE" ]]; then
        log_info "Snapshot file does not exist ($SNAPSHOT_FILE), skipping upload"
        log_info "IAGON API connectivity test completed successfully"
        return 0
    fi
    
    # Split snapshot into chunks
    if ! split_snapshot_for_upload; then
        log_error "Failed to split snapshot for upload"
        return 1
    fi
    
    # Upload chunks
    if ! upload_chunks_to_iagon; then
        log_error "Failed to upload chunks to IAGON"
        return 1
    fi
    
    # Cleanup chunks
    rm -rf "$CHUNK_DIR" 2>/dev/null || true
    
    log_info "IAGON upload completed successfully"
    return 0
}

output_metrics() {
    log_info "=== SNAPSHOT METRICS ==="
    log_info "Snapshot file: $SNAPSHOT_FILE"
    
    if [ -f "$SNAPSHOT_FILE" ]; then
        local file_size=$(stat -c%s "$SNAPSHOT_FILE" | numfmt --to=iec)
        log_info "Snapshot size: $file_size"
    fi
    
    if [ -f "$METADATA_FILE" ]; then
        log_info "Metadata: $METADATA_FILE"
    fi
    
    if [ -f "$CHECKSUM_FILE" ]; then
        log_info "Checksum: $(cat "$CHECKSUM_FILE" | cut -d' ' -f1)"
    fi
    
    # IAGON upload metrics
    if [[ "$IAGON_UPLOAD_ENABLED" == "true" && ${#IAGON_UPLOAD_RESULTS[@]} -gt 0 ]]; then
        log_info "IAGON upload: ${#IAGON_UPLOAD_RESULTS[@]} chunks uploaded"
        log_info "IAGON directory: $IAGON_DIRECTORY_NAME"
    fi
    
    log_info "========================="
}

main() {
    log_info "Starting database snapshot creation service"
    log_info "Configuration:"
    log_info "  Network: $NETWORK"
    log_info "  Database: $DB_HOST:$DB_PORT/$DB_NAME"
    log_info "  Schema: $DB_SCHEMA"
    log_info "  Destination: $SNAPSHOT_DESTINATION_PATH"
    log_info "  Snapshot name: $SNAPSHOT_NAME"
    log_info "  Compression: enabled (gzip)"
    log_info "  Parallel jobs: disabled (single-threaded)"
    log_info "  Retention days: $SNAPSHOT_RETENTION_DAYS"
    
    # Wait for database
    if ! wait_for_db; then
        log_error "Cannot connect to database"
        exit 1
    fi
    
    # Get database information
    local db_info
    if ! db_info=$(get_database_info); then
        log_error "Failed to collect database information"
        exit 1
    fi
    
    # Clean up old snapshots
    cleanup_old_snapshots
    
    # Create snapshot
    if ! create_snapshot; then
        log_error "Snapshot creation failed"
        exit 1
    fi
    
    # Create checksum
    local checksum
    if ! checksum=$(create_checksum); then
        log_error "Checksum creation failed"
        exit 1
    fi
    
    # Validate snapshot
    if ! validate_snapshot; then
        log_error "Snapshot validation failed"
        exit 1
    fi
    
    # Upload to IAGON if enabled
    if ! upload_to_iagon; then
        log_error "IAGON upload failed"
        # Don't exit - snapshot creation was successful
    fi
    
    # Create metadata (includes IAGON upload info if available)
    create_metadata "$db_info" "$checksum"
    
    # Output metrics
    output_metrics
    
    log_info "Snapshot creation completed successfully"
    log_info "Snapshot available at: $SNAPSHOT_FILE"
}

# Ensure the script exits cleanly
trap 'log_error "Script interrupted"; exit 1' INT TERM

# Run main function
main "$@"