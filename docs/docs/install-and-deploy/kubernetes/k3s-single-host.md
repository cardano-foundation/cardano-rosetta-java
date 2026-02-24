---
sidebar_position: 2
title: K3s Single-Host Deployment
description: Deploy Cardano Rosetta Java on a single host using K3s and Helm.
---

# K3s Single-Host Deployment

This guide deploys the full Cardano Rosetta Java stack on a single host using
[K3s](https://k3s.io/) (lightweight Kubernetes). It is the recommended path for local
testing and small production deployments (preprod or mainnet on `entry`/`mid` profile).

## Prerequisites

| Requirement | Preprod | Mainnet |
|---|---|---|
| vCPU | 4 | 8+ |
| RAM | 16 GB | 32 GB+ |
| Fast SSD | 150 GB | 700 GB+ |
| OS | Ubuntu 22.04 LTS / Debian 12 | same |
| Software | git, make | same |
| Outbound internet | Required (Mithril snapshot) | Required |

## Quick Start (Preprod)

```bash
# 1. Clone the repository
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
cd cardano-rosetta-java

# 2. Set the database password (required)
export DB_PASSWORD="<your-secure-password>"

# 3. Build chart dependencies
helm dependency build helm/cardano-rosetta-java

# 4. Deploy
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  -f helm/cardano-rosetta-java/values-preprod.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano --create-namespace \
  --wait --timeout 60m
```

:::info Why not `--wait-for-jobs`?
The `index-applier` post-install Job waits for the full blockchain to be indexed before
it completes — this can take **6–18 hours** on mainnet. Using `--wait-for-jobs` causes
Helm to block until that Job finishes, and `--atomic` would then roll back the entire
release on timeout. Omit `--wait-for-jobs`; the Job runs in the background and can be
monitored independently.
:::

## Monitor Deployment Progress

The deployment goes through three phases. Use these commands to follow along:

### Phase 1 — Mithril snapshot download (~15–60 min depending on network)
```bash
kubectl logs -f job/rosetta-mithril -n cardano
```

### Phase 2 — Cardano node sync (preprod: ~30 min, mainnet: several hours)
```bash
make k8s-logs-node              # cardano-node logs
kubectl get pods -n cardano -w  # watch all pods
```

### Phase 3 — All pods ready
```bash
kubectl get pods -n cardano
# NAME                                    READY   STATUS      RESTARTS
# rosetta-cardano-node-0                  2/2     Running     0
# rosetta-postgresql-0                    1/1     Running     0
# rosetta-yaci-indexer-<hash>             1/1     Running     0
# rosetta-rosetta-api-<hash>              1/1     Running     0
# rosetta-mithril-<hash>                  0/1     Completed   0
# rosetta-index-applier-<hash>            0/1     Completed   0   ← may still be Running
```

## Verify the Deployment

```bash
# Port-forward the API (accessible from local machine only)
kubectl port-forward svc/rosetta-rosetta-api 8082:8082 -n cardano

# Port-forward accessible from remote machines (e.g., your laptop connecting to a server)
kubectl port-forward --address 0.0.0.0 svc/rosetta-rosetta-api 8082:8082 -n cardano

# In a separate terminal — test a Rosetta endpoint
curl -s -X POST http://localhost:8082/network/list \
  -H 'Content-Type: application/json' \
  -d '{"metadata":{}}' | jq .

# Check sync stage
curl -s -X POST http://localhost:8082/network/status \
  -H 'Content-Type: application/json' \
  -d '{"network_identifier":{"blockchain":"cardano","network":"preprod"},"metadata":{}}' \
  | jq '{stage: .sync_status.stage, block: .current_block_identifier.index}'
```

Expected `/network/list` response:
```json
{
  "network_identifiers": [
    { "blockchain": "cardano", "network": "preprod" }
  ]
}
```

The `stage` field in `/network/status` cycles through:
- `SYNCING` — yaci-indexer still catching up to chain tip
- `APPLYING_INDEXES` — indexing done; building DB indexes (index-applier Job running)
- `LIVE` — fully operational

## Mainnet Deployment

```bash
export DB_PASSWORD="<your-secure-password>"

helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.network=mainnet \
  --set global.protocolMagic=764824073 \
  --set global.profile=mid \
  -n cardano --create-namespace \
  --wait --timeout 60m
```

## Common Operations

### View logs
```bash
make k8s-logs-api       # rosetta-api
make k8s-logs-indexer   # yaci-indexer
make k8s-logs-node      # cardano-node
```

### Upgrade to a new release
```bash
export DB_PASSWORD="<your-password>"
helm upgrade rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano --wait --timeout 60m
```

### Restart a component
```bash
kubectl rollout restart deployment/rosetta-rosetta-api -n cardano
kubectl rollout restart deployment/rosetta-yaci-indexer -n cardano
kubectl rollout restart statefulset/rosetta-postgresql -n cardano
```

### Monitor the index-applier Job
```bash
kubectl get job rosetta-index-applier -n cardano
kubectl logs -f job/rosetta-index-applier -n cardano
```

## Enable Monitoring (Prometheus + Grafana)

Monitoring is enabled by default (`monitoring.enabled: true` in `values.yaml`). To
disable it, pass `--set monitoring.enabled=false`. To access Grafana and Prometheus
after deployment:

```bash
# Local access only
kubectl port-forward svc/rosetta-grafana 3000:3000 -n cardano &
kubectl port-forward svc/rosetta-prometheus 9090:9090 -n cardano &

# Remote access — bind to all interfaces so your browser can reach the server by IP
kubectl port-forward --address 0.0.0.0 svc/rosetta-grafana 3000:3000 -n cardano &
kubectl port-forward --address 0.0.0.0 svc/rosetta-prometheus 9090:9090 -n cardano &
```

Then open:
- Grafana: `http://<server-ip>:3000` (default credentials: `admin` / `admin`)
- Prometheus: `http://<server-ip>:9090`

The **Rosetta Critical Operation Metrics** dashboard is pre-provisioned. It uses a
K8s-specific dashboard file (`config/monitoring/dashboards/rosetta-dashboards-k8s.json`)
that is separate from the Docker Compose dashboard to account for differences in service
naming and Prometheus label values.

:::note Grafana datasource UID
The K8s Grafana datasource is provisioned with `uid: DS_PROMETHEUS`. The K8s dashboard
JSON uses this literal UID (not the `${DS_PROMETHEUS}` template variable, which is only
resolved during interactive UI import and does not work for file-provisioned dashboards).
:::

### Run stress test (k6 smoke)
```bash
make k8s-stress-test
```

## Teardown

```bash
# Remove Helm release (preserves PVCs and blockchain data)
make k8s-local-down

# Remove Helm release AND all blockchain data (IRREVERSIBLE)
make k8s-local-reset

# Remove everything including k3s itself
./scripts/k3s-teardown.sh --uninstall-k3s --delete-pvcs
# Prompts for confirmation: type "yes-delete-all-data"
```

:::warning
PVCs are kept by default when running `helm uninstall` (`helm.sh/resource-policy: keep`).
Blockchain data (500 GB+ for mainnet) takes days to re-sync. Only use `--delete-pvcs`
if you intentionally want to wipe all data.
:::

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| `cardano-node` stuck in `Init:0/1` | Mithril download still running | `kubectl logs -f job/rosetta-mithril -n cardano` |
| `postgresql` stuck in `Init:0/1` | Node not fully synced | Check cardano-node logs; sync progress printed every ~30 s |
| `postgresql` stuck at "0% sync progress" | Grep pattern mismatch (fixed in chart ≥ 2.0.0) | Upgrade the chart; `cardano-cli query tip` returns `"syncProgress": "100.00"` (with space) |
| `rosetta-api` pod `0/1 Running` (not Ready) | yaci-indexer still syncing | `make k8s-logs-indexer`; wait for stage `LIVE` |
| `helm upgrade` times out and rolls back | Stale Helm hooks or insufficient timeout | Remove `--wait-for-jobs`; use `--wait --timeout 60m` only |
| Helm release stuck in `failed` state | Previous rollback left orphaned resources | Run `helm uninstall rosetta -n cardano --no-hooks` then redeploy |
| PVC `Pending` | No suitable StorageClass | `kubectl get sc`; ensure `local-path` provisioner is running |
| `ImagePullBackOff` | Rate limit or wrong image tag | Check `global.releaseVersion` in values.yaml |
| Prometheus target DOWN for `node-exporter` | Missing Service for node-exporter DaemonSet | Chart includes `node-exporter-service.yaml` from v2.0.0+; upgrade if missing |
| Grafana dashboard shows "No data" | `hideSeriesFrom` overrides referencing Docker Compose labels | Use `rosetta-dashboards-k8s.json` (included in chart); avoid importing Docker Compose dashboard in K8s |
| Port-forward not accessible from remote machine | Bound to `127.0.0.1` by default | Add `--address 0.0.0.0` to `kubectl port-forward` |
