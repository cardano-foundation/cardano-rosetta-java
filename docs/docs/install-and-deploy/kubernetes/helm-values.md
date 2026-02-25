---
sidebar_position: 3
title: Helm Values Reference
description: Complete reference for all Helm chart values, with Docker Compose equivalents.
---

# Helm Values Reference

This page documents all configurable values in the Cardano Rosetta Java Helm charts.
For each value, the equivalent Docker Compose environment variable is shown where applicable.

## Global Values

These values are shared across all subcharts via `global.*`.

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `global.namespace` | `cardano` | — | Kubernetes namespace |
| `global.network` | `mainnet` | `NETWORK` | Blockchain network: `mainnet`, `preprod`, `preview` |
| `global.protocolMagic` | `764824073` | `PROTOCOL_MAGIC` | Cardano protocol magic number |
| `global.releaseVersion` | `2.0.0` | `RELEASE_VERSION` | Docker image tag for API and indexer |
| `global.cardanoNodeVersion` | `10.5.3` | `CARDANO_NODE_VERSION` | Cardano node image tag |
| `global.pgVersionTag` | `REL_18_0` | `PG_VERSION_TAG` | PostgreSQL image tag |
| `global.mithrilVersion` | `2543.1-hotfix` | `MITHRIL_VERSION` | Mithril client image tag |
| `global.profile` | `mid` | — | Hardware profile: `entry`, `mid`, `advanced` |
| `global.sync` | `true` | `SYNC` | Set `false` for offline (API-only) mode |
| `global.configHostPath` | `""` **(required)** | — | Host directory containing network-specific config files. Template appends `/<network>` to form the full path (e.g. `/opt/cardano/config/node` → `/opt/cardano/config/node/mainnet`). Mounted read-only at `/config` in pods — equivalent to Docker's bind-mount. Must be set in your environment values file (e.g. `values-k3s.yaml`). |

### Database (`global.db`)

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `global.db.name` | `rosetta-java` | `DB_NAME` | PostgreSQL database name |
| `global.db.user` | `rosetta_db_admin` | `DB_USER` | PostgreSQL user |
| `global.db.password` | `""` **(required)** | `DB_SECRET` | PostgreSQL password — pass via `--set` or Sealed Secret |
| `global.db.schema` | `public` | `DB_SCHEMA` | PostgreSQL schema |
| `global.db.port` | `5432` | `DB_PORT` | PostgreSQL port |
| `global.db.host` | `""` | `DB_HOST` | Override to use external PostgreSQL. Empty = use in-cluster service |

### Mithril (`global.mithril`)

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `global.mithril.enabled` | `true` | `MITHRIL_SYNC` | Download Mithril snapshot on first install |
| `global.mithril.snapshotDigest` | `latest` | `SNAPSHOT_DIGEST` | Specific snapshot digest or `latest` |
| `global.mithril.aggregatorEndpoint` | `""` | `AGGREGATOR_ENDPOINT` | Custom aggregator URL. Empty = use network default |
| `global.mithril.genesisVerificationKey` | `""` | `GENESIS_VERIFICATION_KEY` | Custom genesis verification key |
| `global.mithril.ancillaryVerificationKey` | `""` | `ANCILLARY_VERIFICATION_KEY` | Custom ancillary verification key |

### Storage (`global.storage`)

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `global.storage.cardanoNode.size` | `500Gi` | `CARDANO_NODE_DIR` volume | PVC size for blockchain data |
| `global.storage.cardanoNode.storageClass` | `""` | — | StorageClass name. Empty = cluster default |
| `global.storage.postgresql.size` | `200Gi` | `DB_PATH` volume | PVC size for PostgreSQL data |
| `global.storage.postgresql.storageClass` | `""` | — | StorageClass name. Empty = cluster default |
| `global.storage.prometheus.size` | `50Gi` | — | PVC size for Prometheus data |
| `global.storage.grafana.size` | `10Gi` | — | PVC size for Grafana data |

---

## Hardware Profiles

Set `global.profile` to one of: `entry`, `mid`, `advanced`.

### Resource Requests / Limits

| Profile | cardano-node | postgresql | yaci-indexer | rosetta-api |
|---------|-------------|-----------|-------------|------------|
| `entry` req | 1 CPU / 6 Gi | 1 CPU / 6 Gi | 500m / 2 Gi | 250m / 1 Gi |
| `entry` lim | 4 CPU / 12 Gi | 4 CPU / 12 Gi | 2 CPU / 4 Gi | 1 CPU / 2 Gi |
| `mid` req | 2 CPU / 12 Gi | 2 CPU / 12 Gi | 1 CPU / 4 Gi | 500m / 2 Gi |
| `mid` lim | 8 CPU / 24 Gi | 8 CPU / 24 Gi | 4 CPU / 8 Gi | 2 CPU / 4 Gi |
| `advanced` req | 4 CPU / 24 Gi | 4 CPU / 24 Gi | 2 CPU / 8 Gi | 1 CPU / 4 Gi |
| `advanced` lim | 16 CPU / 48 Gi | 16 CPU / 48 Gi | 8 CPU / 16 Gi | 4 CPU / 8 Gi |

### PostgreSQL Tuning (per profile)

| Parameter | `entry` | `mid` | `advanced` | Docker Compose env |
|-----------|---------|-------|------------|-------------------|
| `maxConnections` | 120 | 300 | 600 | `DB_POSTGRES_MAX_CONNECTIONS` |
| `sharedBuffers` | 1GB | 4GB | 32GB | `DB_POSTGRES_SHARED_BUFFERS` |
| `effectiveCacheSize` | 2GB | 8GB | 32GB | `DB_POSTGRES_EFFECTIVE_CACHE_SIZE` |
| `workMem` | 16MB | 64MB | 96MB | `DB_POSTGRES_WORK_MEM` |
| API `poolMin` | 12 | 150 | 100 | `API_DB_POOL_MIN_COUNT` |
| API `poolMax` | 12 | 150 | 550 | `API_DB_POOL_MAX_COUNT` |

---

## yaci-indexer Values

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `yaci-indexer.env.removeSpentUtxos` | `true` | `REMOVE_SPENT_UTXOS` | Prune spent UTXOs to save disk |
| `yaci-indexer.env.removeSpentUtxosLastBlocksGraceCount` | `129600` | `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT` | Blocks to retain (~30 days) |
| `yaci-indexer.env.removeSpentUtxosBatchSize` | `3000` | `REMOVE_SPENT_UTXOS_BATCH_SIZE` | Pruning batch size |
| `yaci-indexer.env.blockTransactionApiTimeoutSecs` | `120` | `BLOCK_TRANSACTION_API_TIMEOUT_SECS` | Timeout for block/tx API calls |
| `yaci-indexer.env.searchLimit` | `5000` | `SEARCH_LIMIT` | Maximum search results |
| `yaci-indexer.env.continueParsingOnError` | `true` | `CONTINUE_PARSING_ON_ERROR` | Continue syncing on parse errors |
| `yaci-indexer.env.peerDiscovery` | `false` | `PEER_DISCOVERY` | Enable peer discovery |
| `yaci-indexer.env.logLevel` | `info` | `LOG` | Log level: `info`, `debug`, `error` |

---

## rosetta-api Values

| Value | Default | Docker Compose equivalent | Description |
|-------|---------|--------------------------|-------------|
| `rosetta-api.replicaCount` | `1` | — | Number of API replicas |
| `rosetta-api.env.httpConnectTimeoutSeconds` | `5` | `HTTP_CONNECT_TIMEOUT_SECONDS` | HTTP client connect timeout |
| `rosetta-api.env.httpRequestTimeoutSeconds` | `5` | `HTTP_REQUEST_TIMEOUT_SECONDS` | HTTP client request timeout |
| `rosetta-api.env.syncGraceSlotsCount` | `100` | `SYNC_GRACE_SLOTS_COUNT` | Slots behind tip before reporting out-of-sync |
| `rosetta-api.env.removeSpentUtxos` | `true` | `REMOVE_SPENT_UTXOS` | Must match yaci-indexer setting |
| `rosetta-api.env.removeSpentUtxosLastBlocksGraceCount` | `129600` | `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT` | Must match yaci-indexer setting |
| `rosetta-api.env.blockTransactionApiTimeoutSecs` | `120` | `BLOCK_TRANSACTION_API_TIMEOUT_SECS` | Timeout for block queries |
| `rosetta-api.env.devkitEnabled` | `false` | `DEVKIT_ENABLED` | Enable DevKit integration |
| `rosetta-api.env.tokenRegistryEnabled` | `false` | `TOKEN_REGISTRY_ENABLED` | Enable Cardano token registry |
| `rosetta-api.env.tokenRegistryBaseUrl` | `""` | `TOKEN_REGISTRY_BASE_URL` | Token registry URL (leave empty = disabled) |
| `rosetta-api.env.tokenRegistryCacheTtlHours` | `12` | `TOKEN_REGISTRY_CACHE_TTL_HOURS` | Token metadata cache TTL |
| `rosetta-api.env.tokenRegistryLogoFetch` | `false` | `TOKEN_REGISTRY_LOGO_FETCH` | Fetch token logos (bandwidth-intensive) |
| `rosetta-api.env.tokenRegistryRequestTimeoutSeconds` | `2` | `TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS` | Token registry request timeout |

### Ingress

```yaml
rosetta-api:
  ingress:
    enabled: true
    className: nginx
    host: rosetta.example.com
    tls:
      - secretName: rosetta-tls
        hosts:
          - rosetta.example.com
```

---

## Subchart Toggles

| Value | Default | Description |
|-------|---------|-------------|
| `cardano-node.enabled` | `true` | Deploy the Cardano node |
| `postgresql.enabled` | `true` | Deploy in-cluster PostgreSQL |
| `yaci-indexer.enabled` | `true` | Deploy the Yaci indexer |
| `rosetta-api.enabled` | `true` | Deploy the Rosetta API |
| `monitoring.enabled` | `true` | Deploy Prometheus + Grafana |

---

## Using an External PostgreSQL

To use an external managed database (RDS, Cloud SQL, etc.):

```yaml
# values-external-db.yaml
postgresql:
  enabled: false

global:
  db:
    host: "my-rds-instance.cluster-xyz.us-east-1.rds.amazonaws.com"
    name: "rosetta-java"
    user: "rosetta_db_admin"
    password: ""   # pass via --set global.db.password=...
```

```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f values-external-db.yaml \
  --set global.db.password="${DB_PASSWORD}"
```

---

## Example: Preprod on K3s (entry profile)

```yaml
# values-preprod.yaml (already included in the chart)
global:
  network: preprod
  protocolMagic: 1
  profile: entry
  storage:
    cardanoNode:
      size: 100Gi
      storageClass: local-path
    postgresql:
      size: 50Gi
      storageClass: local-path

yaci-indexer:
  env:
    removeSpentUtxos: false
    peerDiscovery: true

rosetta-api:
  env:
    removeSpentUtxos: false
```

```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  -f helm/cardano-rosetta-java/values-preprod.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano --create-namespace
```
