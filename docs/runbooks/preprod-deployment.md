# Preprod Deployment Runbook

This runbook describes how to deploy and operate **Cardano Rosetta Java** on the Cardano **preprod** testnet using Kubernetes (K3s single-host or managed cluster).

---

## Prerequisites

### Hardware
| Resource | Minimum | Recommended |
|----------|---------|-------------|
| CPU      | 4 cores | 8 cores     |
| RAM      | 32 GB   | 48 GB       |
| Disk     | 150 GB SSD | 200 GB NVMe |
| OS       | Ubuntu 22.04+ / Debian 12+ | — |

### Software
- `kubectl` >= 1.28
- `helm` >= 3.12
- `jq` (for status checks)
- K3s (auto-installed) or a managed K8s cluster (EKS, GKE, AKS)

### Network
- Outbound TCP 3001 for cardano-node peer-to-peer (N2N protocol)
- Outbound HTTPS for Mithril snapshot download
- Inbound TCP 8082 for Rosetta API access (if exposing externally)

---

## Quick Start (K3s)

```bash
# 1. Clone the repository
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
cd cardano-rosetta-java

# 2. Set the database password (required)
export DB_PASSWORD="$(openssl rand -base64 24)"
echo "DB_PASSWORD=${DB_PASSWORD}" > .env.k8s  # save for reference
chmod 600 .env.k8s

# 3. Deploy (installs k3s if not present, requires root or sudo)
make k8s-local-up
# OR equivalently:
./scripts/k3s-setup.sh preprod
```

---

## Deployment Phases

After running `make k8s-local-up`, monitor the following phases:

### Phase 1 — Mithril Snapshot Download (~30 min on preprod)

Mithril downloads a verified snapshot of the Cardano blockchain, avoiding a full chain replay.

```bash
# Watch the Mithril Job
kubectl logs -f job/rosetta-mithril -n cardano

# Check job status
kubectl get job rosetta-mithril -n cardano
```

The `cardano-node` pod waits in its `wait-for-mithril` initContainer until the download completes. If the node DB already exists (e.g., on re-deploy), the Job exits immediately.

### Phase 2 — Cardano Node Startup

```bash
make k8s-logs-node
# or:
kubectl logs -f -l component=cardano-node -c cardano-node -n cardano
```

The node starts syncing from the Mithril snapshot. Watch for the socket to appear:
```
Chain extended, slot 42000000 ...
```

### Phase 3 — PostgreSQL Startup

PostgreSQL starts after the node is fully synced (its initContainer runs `cardano-cli query tip` via socat bridge until `syncProgress` reaches 99%+).

```bash
kubectl logs -f -l component=postgresql -n cardano
kubectl get pods -n cardano -w
```

### Phase 4 — Yaci-Indexer Syncing (SYNCING stage)

Once PostgreSQL is ready, yaci-indexer starts parsing blockchain data into the database.

```bash
make k8s-logs-indexer
```

### Phase 5 — Rosetta API Ready

When yaci-indexer has processed enough blocks, the API becomes available.

```bash
# Port-forward the API
make k8s-port-forward
# In another terminal:
curl -s http://localhost:8082/network/status \
  -H "Content-Type: application/json" \
  -d '{"network_identifier":{"blockchain":"cardano","network":"preprod"},"metadata":{}}' \
  -X POST | jq '{stage: .sync_status.stage, block: .current_block_identifier.index}'
```

Stages:
- `SYNCING` — Still indexing blocks
- `APPLYING_INDEXES` — Indexing complete; creating DB indexes (~1-2 hours on preprod)
- `LIVE` — Production-ready

### Phase 6 — Index Applier (automatic)

After the API reports `APPLYING_INDEXES`, the `index-applier` Helm post-install hook automatically creates optimised database indexes. Monitor it:

```bash
kubectl get jobs -n cardano
kubectl logs -f job/rosetta-index-applier -n cardano
```

---

## Verification

```bash
# Run Helm tests (network/list, network/options, yaci actuator/health)
make k8s-test

# Check all pods are Running/Completed
make k8s-status

# Run smoke stress test
make k8s-stress-test
```

---

## Common Operations

### View logs
```bash
make k8s-logs-api       # Rosetta API
make k8s-logs-indexer   # Yaci indexer
make k8s-logs-node      # Cardano node
```

### Upgrade the release
```bash
helm upgrade rosetta ./helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-preprod.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano --wait
```

### Scale API replicas
```bash
kubectl scale deployment rosetta-rosetta-api --replicas=2 -n cardano
```

### Restart a component
```bash
kubectl rollout restart deployment/rosetta-yaci-indexer -n cardano
kubectl rollout restart statefulset/rosetta-cardano-node -n cardano
```

### Teardown (preserve data)
```bash
make k8s-local-down
```

### Full reset (delete all data)
```bash
make k8s-local-reset
```

---

## Troubleshooting

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| Mithril Job fails | Network issue or invalid aggregator URL | Check `kubectl logs job/rosetta-mithril -n cardano`; retry with `kubectl delete job rosetta-mithril -n cardano` then redeploy |
| `cardano-node` pod stuck in `Init:0/1` | Mithril still running | Wait; check Phase 1 |
| `postgresql` pod stuck in `Init:0/1` | Node not fully synced | Check node sync progress in Phase 2 |
| `yaci-indexer` CrashLoopBackOff | DB connection issue | Verify Secret: `kubectl get secret rosetta-db-secret -n cardano -o jsonpath='{.data.db-secret}' \| base64 -d` |
| API returns 503 | yaci-indexer not ready | Check indexer readiness: `kubectl get pods -n cardano` |
| OOMKilled | Insufficient memory | Increase resource limits or use `mid` profile |
| PVC bound but empty | StorageClass issue | Check: `kubectl describe pvc -n cardano` |
| `helm lint` fails | Template error | Run `make k8s-template` and inspect YAML |

---

## Resource Usage (entry profile, preprod)

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 1 / 4 CPU | 6 / 12 Gi | 100 Gi |
| postgresql | 1 / 4 CPU | 6 / 12 Gi | 50 Gi |
| yaci-indexer | 500m / 2 CPU | 2 / 4 Gi | — |
| rosetta-api | 250m / 1 CPU | 1 / 2 Gi | — |
| monitoring (optional) | 350m / 1.7 CPU | 832Mi / 2.5 Gi | 12 Gi |
