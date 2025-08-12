# Database Snapshot Infrastructure

This directory contains infrastructure-specific tools for creating database snapshots of the Cardano Rosetta Java application. These tools are intended for use by infrastructure teams and are separate from client-facing services.

## Overview

The snapshot system provides:
- **Automated snapshot creation** with compression and validation
- **Metadata generation** with comprehensive snapshot information
- **Retention management** with configurable cleanup policies
- **Checksum verification** for data integrity
- **Parallel processing** for improved performance

## Quick Start

### 1. Create Snapshot

Run the snapshot creation service using the preprod environment:

```bash
# From the snapshots directory, create snapshot from preprod database
docker-compose --env-file ../../.env.docker-compose-preprod -f docker-compose-snapshot-create.yaml up db-snapshot-create

# Or from project root
docker-compose --env-file .env.docker-compose-preprod -f infra/snapshots/docker-compose-snapshot-create.yaml up db-snapshot-create
```

**Snapshots are stored locally in `./snapshots/` directory** with filenames like:
- `snapshot_preprod_YYYYMMDD_HHMMSS.sql.gz`
- `snapshot_preprod_YYYYMMDD_HHMMSS.metadata.json`  
- `snapshot_preprod_YYYYMMDD_HHMMSS.checksum`

**Note**: This uses the same PostgreSQL image as the main database service but with a different entrypoint (`/sbin/create-snapshot.sh`) for snapshot creation functionality.

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | rosetta-java | Database name |
| `DB_USER` | rosetta_db_admin | Database user |
| `DB_SECRET` | - | Database password |
| `DB_SCHEMA` | mainnet | Schema to snapshot |
| `NETWORK` | mainnet | Network identifier |
| `SNAPSHOT_RETENTION_DAYS` | 30 | Days to keep old snapshots |
| `LOG_LEVEL` | INFO | Logging level (DEBUG, INFO, WARN, ERROR) |

### Fixed Configuration (Built-in defaults)

| Setting | Value | Description |
|---------|-------|-------------|
| Compression | gzip (pigz) | Always enabled for efficiency |
| Parallel jobs | 4 | Fixed for optimal performance |
| Include indexes | true | Always included in snapshots |
| Include constraints | true | Always included in snapshots |
| Validation | enabled | Always validates after creation |

## Generated Files

Each snapshot operation creates three files:

1. **Snapshot File**: `snapshot_<network>_<timestamp>.sql[.gz]`
   - The actual database dump
   - Optionally compressed with gzip

2. **Metadata File**: `snapshot_<network>_<timestamp>.metadata.json`
   - Comprehensive snapshot information
   - Database statistics and configuration
   - Creation timestamp and validation info

3. **Checksum File**: `snapshot_<network>_<timestamp>.checksum`
   - SHA256 checksum for integrity verification
   - Used for validation during restore

## Example Metadata

```json
{
  "snapshot": {
    "name": "snapshot_mainnet_20240728_143022.sql.gz",
    "file": "/snapshots/snapshot_mainnet_20240728_143022.sql.gz",
    "created_at": "2024-07-28T14:30:22Z",
    "size_bytes": 1024000000,
    "checksum_sha256": "abc123...",
    "compressed": true
  },
  "source": {
    "network": "mainnet",
    "database": {
      "host": "localhost",
      "port": 5432,
      "name": "rosetta-java",
      "schema": "mainnet",
      "size": "2.5 GB",
      "schema_size": "2.3 GB",
      "table_count": 15
    }
  }
}
```

## Usage Scenarios

### 1. Regular Backup Creation

```bash
# Daily backup with retention (from snapshots directory)
SNAPSHOT_RETENTION_DAYS=7 \
docker-compose --env-file ../../.env.docker-compose-preprod -f docker-compose-snapshot-create.yaml up db-snapshot-create
```

### 2. Debug Mode

```bash
# Create snapshot with debug logging
LOG_LEVEL=DEBUG \
docker-compose --env-file ../../.env.docker-compose-preprod -f docker-compose-snapshot-create.yaml up db-snapshot-create
```

## Best Practices

### 1. Network Isolation
- Run snapshot creation in maintenance windows
- Use dedicated network for large snapshots
- Monitor database performance impact

### 2. Storage Management
- Use fast storage for snapshot destination
- Monitor disk space usage
- Implement monitoring for snapshot sizes

### 3. Security
- Store database credentials securely
- Restrict access to snapshot files
- Use encrypted storage for sensitive data

### 4. Monitoring
- Set up alerts for failed snapshots
- Monitor snapshot creation duration
- Track storage usage trends

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database connectivity
   docker run --rm postgres:14 pg_isready -h <DB_HOST> -p <DB_PORT>
   ```

2. **Insufficient Disk Space**
   ```bash
   # Check available space
   docker run --rm -v snapshots-storage:/data alpine df -h /data
   ```

3. **Permission Issues**
   ```bash
   # Check volume permissions
   docker run --rm -v snapshots-storage:/data alpine ls -la /data
   ```

### Debugging

Enable debug logging:
```bash
LOG_LEVEL=DEBUG docker-compose --env-file .env -f docker-compose-snapshot-create.yaml --profile infra up db-snapshot-create
```

## Integration with Client Services

Once snapshots are created, they can be used by client services through the restore functionality:

1. Copy snapshots to client-accessible location
2. Configure client environment variables
3. Enable restore service with `SNAPSHOT_RESTORE_ENABLED=true`

See main documentation for restore service usage.