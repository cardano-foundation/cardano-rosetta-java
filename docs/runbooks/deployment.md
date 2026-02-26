# Deployment Runbook

This runbook covers deploying and operating **Cardano Rosetta Java** on Kubernetes
(K3s single-host or managed cluster) for both **preprod** and **mainnet**.

---

## Network Quick Reference

| Parameter | Preprod | Mainnet |
|-----------|---------|---------|
| `global.network` | `preprod` | `mainnet` |
| `global.protocolMagic` | `1` | `764824073` |
| `global.profile` | `entry` | `mid` |
| Node storage | 100 Gi | 500 Gi |
| DB storage | 50 Gi | 200 Gi |
| Mithril snapshot size | ~5 GB | ~50 GB |
| Time to `LIVE` | ~2–3 hours | ~12–18 hours |

---

## Prerequisites

### Hardware

| Resource | Preprod (entry) | Mainnet (mid) |
|----------|----------------|--------------|
| CPU | 4 cores | 8 cores |
| RAM | 32 GB | 48 GB |
| Disk | 150 GB SSD | 700 GB NVMe |
| OS | Ubuntu 22.04+ / Debian 12+ | same |

> Use NVMe SSDs for PostgreSQL on mainnet. HDDs or standard SSDs cause serious performance degradation.

### Software
- `kubectl` >= 1.28
- `helm` >= 3.12
- `jq` (for status checks)
- K3s (auto-installed) or a managed K8s cluster (EKS, GKE, AKS)

### Network
- Outbound TCP 3001 — cardano-node peer-to-peer (N2N)
- Outbound HTTPS — Mithril snapshot download
- Inbound TCP 8082 — Rosetta API (if exposing externally)

---

## Deployment

### Step 1 — Clone and configure

```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
cd cardano-rosetta-java

# Generate a strong password (save it — you'll need it for upgrades)
export DB_PASSWORD="$(openssl rand -base64 32)"
echo "DB_PASSWORD=${DB_PASSWORD}" > .env.k8s
chmod 600 .env.k8s

# Build chart dependencies
helm dependency build helm/cardano-rosetta-java
```

### Step 2 — Pre-create the namespace

The chart manages the namespace via its own `templates/namespace.yaml`. Using
`--create-namespace` alongside this template causes a resource conflict. Pre-create the
namespace with Helm ownership labels so Helm can adopt it:

```bash
kubectl create namespace cardano
kubectl label namespace cardano \
  app.kubernetes.io/managed-by=Helm \
  app.kubernetes.io/name=cardano-rosetta-java
kubectl annotate namespace cardano \
  meta.helm.sh/release-name=rosetta \
  meta.helm.sh/release-namespace=cardano
```

### Step 3 — Deploy

:::warning No `--wait-for-jobs`, use `--no-hooks`
The `index-applier` post-install hook is a long-running Kubernetes Job (1–2 h on preprod,
6–18 h on mainnet). Use `--no-hooks` to skip hook execution — Helm returns immediately
once all Deployments and StatefulSets are ready. Monitor the index-applier Job
separately. Never use `--wait-for-jobs` as it blocks Helm until the Job finishes.
:::

**Preprod (K3s, entry profile):**
```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=preprod \
  --set global.protocolMagic=1 \
  --set global.profile=entry \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano \
  --no-hooks --wait --timeout 60m
```

**Mainnet (K3s, mid profile):**
```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=mainnet \
  --set global.protocolMagic=764824073 \
  --set global.profile=mid \
  --set global.db.password="${DB_PASSWORD}" \
  -n cardano \
  --no-hooks --wait --timeout 90m
```

:::note Config files on K3s (hostPath mount)
`values-k3s.yaml` sets `global.configHostPath` pointing to `config/node/` in the
repository. Cardano genesis files, `topology.json`, and `checkpoints.json` are mounted
directly from the host filesystem — no ConfigMap, no size limits. To update a config
file, edit it on the host; pods see the change immediately.
:::

**Managed Kubernetes (EKS / GKE / AKS):**
```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  --set global.network=mainnet \
  --set global.protocolMagic=764824073 \
  --set global.profile=mid \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.configHostPath="/opt/cardano/config/node" \
  --set global.storage.cardanoNode.storageClass="gp3" \
  --set global.storage.postgresql.storageClass="gp3" \
  -n cardano \
  --no-hooks --wait --timeout 90m
```

> For managed K8s, ensure the config directory exists on every node that may run
> `cardano-node` and `yaci-indexer` pods, or use a DaemonSet / node provisioner to
> populate it. Alternatively, pin pods to a specific node via `nodeSelector`.

### Option — External Database (RDS / Cloud SQL)

```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  --set postgresql.enabled=false \
  --set global.db.host="my-rds-endpoint.us-east-1.rds.amazonaws.com" \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.profile=mid \
  -n cardano --create-namespace \
  --wait --timeout 60m
```

> Pre-create the database and user on the external instance matching `global.db.name`
> (`rosetta-java`) and `global.db.user` (`rosetta_db_admin`).

---

## Expected Timeline

| Phase | Preprod | Mainnet |
|-------|---------|---------|
| Mithril snapshot download | ~30 min | 2–4 hours |
| Cardano node to tip | ~30 min | 15–60 min |
| PostgreSQL startup | ~1 min | ~1 min |
| yaci-indexer SYNCING | ~30 min | 2–8 hours |
| APPLYING_INDEXES (index-applier Job) | ~1–2 hours | ~6 hours |
| **Total to LIVE** | **~2–3 hours** | **~12–18 hours** |

---

## Deployment Phases

### Phase 1 — Mithril Snapshot Download

Mithril downloads a verified snapshot of the blockchain, skipping a full chain replay.
The `cardano-node` pod waits in `wait-for-mithril` initContainer until complete.

```bash
kubectl logs -f job/rosetta-mithril -n cardano
kubectl get job rosetta-mithril -n cardano
```

### Phase 2 — Cardano Node Sync

```bash
kubectl logs -f statefulset/rosetta-cardano-node -c cardano-node -n cardano
kubectl get pods -n cardano -w
```

### Phase 3 — PostgreSQL Startup

PostgreSQL starts after the node reaches 99%+ sync. Its initContainer polls
`cardano-cli query tip` via the socat bridge.

```bash
kubectl logs -f statefulset/rosetta-postgresql -n cardano
```

:::note syncProgress format
`cardano-cli query tip` returns `"syncProgress": "100.00"` (space after colon).
The initContainer grep pattern handles this correctly from chart v2.0.0+.
:::

### Phase 4 — yaci-Indexer Syncing

```bash
kubectl logs -f deployment/rosetta-yaci-indexer -n cardano
```

### Phase 5 — Rosetta API

```bash
# Port-forward (local)
kubectl port-forward svc/rosetta-rosetta-api 8082:8082 -n cardano

# Port-forward (remote machine access)
kubectl port-forward --address 0.0.0.0 svc/rosetta-rosetta-api 8082:8082 -n cardano &

# Check sync stage (use preprod or mainnet as appropriate)
curl -s -X POST http://localhost:8082/network/status \
  -H "Content-Type: application/json" \
  -d '{"network_identifier":{"blockchain":"cardano","network":"preprod"},"metadata":{}}' \
  | jq '{stage: .sync_status.stage, block: .current_block_identifier.index}'
```

Stages:
- `SYNCING` — yaci-indexer still catching up
- `APPLYING_INDEXES` — indexing done; building DB indexes (index-applier Job running)
- `LIVE` — fully operational

### Phase 6 — Index Applier (background Job)

```bash
kubectl get jobs -n cardano
kubectl logs -f job/rosetta-index-applier -n cardano
```

---

## Verification

```bash
# All pods Running or Completed
kubectl get pods -n cardano

# API stage
curl -s -X POST http://localhost:8082/network/status \
  -H "Content-Type: application/json" \
  -d '{"network_identifier":{"blockchain":"cardano","network":"preprod"},"metadata":{}}' \
  | jq '.sync_status.stage'
# Expected: "LIVE"
```

---

## Common Operations

### View logs
```bash
kubectl logs -f deployment/rosetta-rosetta-api -n cardano    # Rosetta API
kubectl logs -f deployment/rosetta-yaci-indexer -n cardano   # yaci-indexer
kubectl logs -f statefulset/rosetta-cardano-node -n cardano  # Cardano node
kubectl logs -f statefulset/rosetta-postgresql -n cardano    # PostgreSQL
```

### Port-forward all services

```bash
# Local access only
kubectl port-forward svc/rosetta-rosetta-api 8082:8082 -n cardano &
kubectl port-forward svc/rosetta-grafana     3000:3000 -n cardano &
kubectl port-forward svc/rosetta-prometheus  9090:9090 -n cardano &

# Remote access — bind to all interfaces (access by server IP from another machine)
kubectl port-forward --address 0.0.0.0 svc/rosetta-rosetta-api 8082:8082 -n cardano &
kubectl port-forward --address 0.0.0.0 svc/rosetta-grafana     3000:3000 -n cardano &
kubectl port-forward --address 0.0.0.0 svc/rosetta-prometheus  9090:9090 -n cardano &
```

### Upgrade to a new release
```bash
helm upgrade rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.releaseVersion="2.1.0" \
  -n cardano --no-hooks --wait --timeout 60m
```

> `--no-hooks` skips the index-applier hook. If a schema migration is needed after
> upgrade, trigger the index-applier Job manually.

### Scale API replicas
```bash
kubectl scale deployment rosetta-rosetta-api --replicas=2 -n cardano
```

### Restart a component
```bash
kubectl rollout restart deployment/rosetta-rosetta-api -n cardano
kubectl rollout restart deployment/rosetta-yaci-indexer -n cardano
kubectl rollout restart statefulset/rosetta-cardano-node -n cardano
```

### Monitor index-applier Job
```bash
kubectl get job rosetta-index-applier -n cardano
kubectl logs -f job/rosetta-index-applier -n cardano
```

### Teardown (preserve blockchain data)
```bash
helm uninstall rosetta -n cardano --no-hooks
# PVCs survive by default (helm.sh/resource-policy: keep)
```

### Full reset (delete all data — IRREVERSIBLE)
```bash
helm uninstall rosetta -n cardano --no-hooks
kubectl delete pvc --all -n cardano
```

---

## Security (Mainnet Checklist)

- [ ] **Rotate DB password** — never use the default. Use `openssl rand -base64 32`.
- [ ] **Restrict API access** — use a LoadBalancer security group or Ingress with allowlisted IPs.
- [ ] **Disable debug endpoints** — ensure `PRINT_EXCEPTION=false` in production.
- [ ] **Mithril keys** — set `global.mithril.genesisVerificationKey` and `ancillaryVerificationKey` for mainnet verification.
- [ ] **Secrets management** — use Sealed Secrets or Vault instead of `--set global.db.password`.
- [ ] **Network Policy** — restrict pod-to-pod traffic.
- [ ] **Token Registry** — review `rosetta-api.env.tokenRegistryEnabled`; requires outbound internet.

### Example NetworkPolicy (restrict API ingress)

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-rosetta-api-ingress
  namespace: cardano
spec:
  podSelector:
    matchLabels:
      component: rosetta-api
  policyTypes:
    - Ingress
  ingress:
    - from:
        - ipBlock:
            cidr: 10.0.0.0/8   # internal only
      ports:
        - port: 8082
```

---

## Production Configuration (Mainnet)

```yaml
# values-mainnet-prod.yaml
global:
  profile: mid
  network: mainnet
  protocolMagic: 764824073

rosetta-api:
  env:
    tokenRegistryEnabled: true
    tokenRegistryBaseUrl: "https://tokens.cardano.org/api"
    tokenRegistryCacheTtlHours: 12
    removeSpentUtxos: true
    removeSpentUtxosLastBlocksGraceCount: 129600

  ingress:
    enabled: true
    className: nginx
    host: rosetta.example.com
    annotations:
      nginx.ingress.kubernetes.io/proxy-body-size: "0"
      cert-manager.io/cluster-issuer: letsencrypt-prod
    tls:
      - hosts: [rosetta.example.com]
        secretName: rosetta-tls

monitoring:
  enabled: true
  grafana:
    adminPassword: "<strong-password>"
```

---

## Disaster Recovery

### Backup PVCs

```bash
kubectl get pvc -n cardano

# Use Velero for cluster-level backup:
# velero backup create rosetta-backup --include-namespaces cardano
```

### Re-sync from Mithril (if node DB is corrupt)

```bash
# 1. Delete the existing Mithril Job and node PVC
kubectl delete job rosetta-mithril -n cardano --ignore-not-found
kubectl delete pvc rosetta-cardano-node-data -n cardano

# 2. Re-deploy (triggers fresh Mithril download)
helm upgrade rosetta helm/cardano-rosetta-java \
  --reuse-values --set global.db.password="${DB_PASSWORD}" \
  -n cardano --wait --timeout 60m
```

---

## Troubleshooting

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| `Error: rendered manifests contain a resource that already exists` | `--create-namespace` conflicts with chart's `templates/namespace.yaml` | Remove `--create-namespace`; pre-create namespace with Helm labels (Step 2) |
| `Error: configHostPath is required` | `global.configHostPath` not set | Add `global.configHostPath: /path/to/config/node` in your environment values file |
| Config file changes not visible in pod | `configHostPath` not set; using ConfigMap | Ensure `global.configHostPath` is set; hostPath mounts reflect host changes immediately |
| Mithril Job fails | Network issue or invalid aggregator URL | `kubectl logs job/rosetta-mithril -n cardano`; delete job and redeploy |
| `cardano-node` stuck in `Init:0/1` | Mithril still running | Wait; monitor Phase 1 |
| `postgresql` stuck in `Init:0/1` | Node not fully synced | Check cardano-node logs for sync progress |
| `postgresql` stuck at "0% sync progress" | grep space mismatch in chart < 2.0.0 | Upgrade chart; `cardano-cli query tip` returns `"syncProgress": "100.00"` (space after colon) |
| `yaci-indexer` CrashLoopBackOff | DB connection issue | `kubectl get secret rosetta-db-secret -n cardano -o jsonpath='{.data.db-secret}' \| base64 -d` |
| API returns 503 | yaci-indexer not ready | `kubectl get pods -n cardano`; wait for indexer |
| `helm upgrade` times out and rolls back | `--wait-for-jobs` blocking on index-applier | Use `--no-hooks` instead |
| Helm release stuck in `failed` state | Previous atomic rollback left orphaned state | `helm uninstall rosetta -n cardano --no-hooks` then redeploy |
| Template changes not applied after `helm upgrade` | Subchart `.tgz` used instead of source directory | `helm package charts/<subchart> -d charts/` to repackage edited subchart |
| OOMKilled | Insufficient memory | Use a higher profile (`mid` or `advanced`) |
| PVC `Pending` | No suitable StorageClass | `kubectl get sc`; ensure `local-path` provisioner is running |
| `ImagePullBackOff` | Wrong image tag | Check `global.releaseVersion` in values |
| Port-forward not reachable from remote | Bound to 127.0.0.1 | Add `--address 0.0.0.0` to `kubectl port-forward` |
| Prometheus node-exporter target DOWN | Missing Service for DaemonSet | Upgrade chart ≥ 2.0.0 |
| Grafana dashboard "No data" on node metrics | `hideSeriesFrom` overrides with Docker labels | Chart uses `rosetta-dashboards-k8s.json`; ensure it's the provisioned file |

---

## Resource Usage

### Preprod — entry profile

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 1 / 4 CPU | 6 / 12 Gi | 100 Gi |
| postgresql | 1 / 4 CPU | 6 / 12 Gi | 50 Gi |
| yaci-indexer | 500m / 2 CPU | 2 / 4 Gi | — |
| rosetta-api | 250m / 1 CPU | 1 / 2 Gi | — |
| monitoring | 350m / 1.7 CPU | 832Mi / 2.5 Gi | 12 Gi |

### Mainnet — mid profile

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 2 / 8 CPU | 12 / 24 Gi | 500 Gi |
| postgresql | 2 / 8 CPU | 12 / 24 Gi | 200 Gi |
| yaci-indexer | 1 / 4 CPU | 4 / 8 Gi | — |
| rosetta-api | 500m / 2 CPU | 2 / 4 Gi | — |
| monitoring | 350m / 1.7 CPU | 832Mi / 2.5 Gi | 60 Gi |
| **Total** | **~6 / 24 CPU** | **~31 / 63 Gi** | **760 Gi** |
