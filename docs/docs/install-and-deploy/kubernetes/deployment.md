---
sidebar_position: 2
title: Deployment Runbook
description: This runbook covers deploying and operating **Cardano Rosetta Java** on Kubernetes (K3s single-host or managed cluster) for both **preprod** and **mainnet**.
---
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
- K3s or a managed K8s cluster (EKS, GKE, AKS)

### Network
- Outbound TCP 3001 — cardano-node peer-to-peer (N2N)
- Outbound HTTPS — Mithril snapshot download + genesis key fetch from GitHub
- Inbound TCP 8082 — Rosetta API (if exposing externally)

---

## Deployment

### Deployment steps

#### Step 1 — Clone and configure

```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
cd cardano-rosetta-java

# Save password (needed for all future upgrades)
export DB_PASSWORD="$(openssl rand -base64 32)"
echo "DB_PASSWORD=${DB_PASSWORD}" > .env.k8s
chmod 600 .env.k8s
```

#### Step 2 — Pre-create the namespace

> **Do NOT use `--create-namespace`** — the chart has `templates/namespace.yaml` which
> creates the namespace with Helm ownership labels. Using `--create-namespace` causes a
> resource conflict.

```bash
kubectl create namespace cardano
kubectl label namespace cardano \
  app.kubernetes.io/managed-by=Helm \
  app.kubernetes.io/name=cardano-rosetta-java
kubectl annotate namespace cardano \
  meta.helm.sh/release-name=rosetta \
  meta.helm.sh/release-namespace=cardano
```

> On re-deploy (namespace already exists), skip this step.

#### Step 3 — Deploy

> **Never use `--wait` or `--wait-for-jobs`** — the deployment takes hours (Mithril
> download + node sync + indexer sync). Helm would time out and roll back.

The `index-applier` Job runs automatically as part of the release (default
`indexApplier.mode: automatic`). It waits for the Rosetta API to become ready, then
builds DB indexes in the background. No second `helm upgrade` is needed.

**Preprod (K3s, entry profile):**
```bash
export DB_PASSWORD=$(grep DB_PASSWORD .env.k8s | cut -d= -f2-)
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-preprod.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=preprod \
  "--set=global.protocolMagic=1" \
  --set global.profile=entry \
  --set global.configHostPath=$(pwd)/config/node \
  "--set=global.db.password=${DB_PASSWORD}" \
  -n cardano 2>&1 | grep -v "walk.go"
```

**Mainnet (K3s, mid profile):**
```bash
export DB_PASSWORD=$(grep DB_PASSWORD .env.k8s | cut -d= -f2-)
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=mainnet \
  "--set=global.protocolMagic=764824073" \
  --set global.profile=mid \
  --set global.configHostPath=$(pwd)/config/node \
  "--set=global.db.password=${DB_PASSWORD}" \
  -n cardano 2>&1 | grep -v "walk.go"
```

**Managed Kubernetes (EKS / GKE / AKS):**
```bash
helm upgrade --install rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  --set global.network=mainnet \
  "--set=global.protocolMagic=764824073" \
  --set global.profile=mid \
  "--set=global.db.password=${DB_PASSWORD}" \
  --set global.configHostPath="$(pwd)/config/node" \
  --set global.storage.cardanoNode.storageClass="gp3" \
  --set global.storage.postgresql.storageClass="gp3" \
  -n cardano 2>&1 | grep -v "walk.go"
```

> For managed K8s, ensure the config directory exists on every node that may run
> `cardano-node` and `yaci-indexer` pods, or pin pods to a specific node via `nodeSelector`.

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

Mithril downloads a verified snapshot of the blockchain. The Mithril Job fetches the
aggregator endpoint and verification keys automatically from GitHub based on `NETWORK`
(same behaviour as Docker Compose — no manual key configuration required).

The `cardano-node` pod stays in `Init:0/1` (`wait-for-mithril`) until the Job completes.

```bash
# Watch job progress
kubectl logs -f job/rosetta-mithril -n cardano

# Check job status
kubectl get job rosetta-mithril -n cardano -o jsonpath='{.status}'
```

> **Reinstall note:** The Mithril Job has `helm.sh/resource-policy: keep` — it survives
> `helm uninstall`. On reinstall, delete it first if it already exists:
> ```bash
> kubectl delete job rosetta-mithril -n cardano --ignore-not-found
> ```

### Phase 2 — Cardano Node Sync

After Mithril completes, `cardano-node` starts and syncs remaining blocks to tip.
After a machine reboot, the node re-validates all ImmutableDB chunks (~90 min for mainnet)
before the startup probe passes — this is normal.

```bash
kubectl logs -f statefulset/rosetta-cardano-node -c cardano-node -n cardano
kubectl get pods -n cardano -w
```

### Phase 3 — PostgreSQL Startup

PostgreSQL starts as soon as its PVC is available — it has no dependency on cardano-node.
While the node is syncing, PostgreSQL initialises its data directory and schema migrations
run immediately. This means yaci-indexer can begin connecting and indexing as soon as the
node's socat bridge (TCP port 3002) is reachable, without waiting hours for full sync.

```bash
kubectl logs -f statefulset/rosetta-postgresql -n cardano
```

### Phase 4 — yaci-Indexer Syncing

```bash
kubectl logs -f deployment/rosetta-yaci-indexer -n cardano
# Look for: Block No: XXXXXXX , Era: Babbage/Conway
```

Occasional `Connection reset` + immediate reconnect in logs is normal — it's the socat
TCP bridge timing out on idle and Yaci reconnecting in milliseconds.

### Phase 5 — Rosetta API

```bash
# Port-forward (local access)
kubectl port-forward svc/rosetta-rosetta-api 8082:8082 -n cardano

# Port-forward (remote machine access)
kubectl port-forward --address 0.0.0.0 svc/rosetta-rosetta-api 8082:8082 -n cardano &

# Check sync stage
curl -s -X POST http://localhost:8082/network/status \
  -H "Content-Type: application/json" \
  -d '{"network_identifier":{"blockchain":"cardano","network":"preprod"},"metadata":{}}' \
  | jq '{stage: .sync_status.stage, block: .current_block_identifier.index}'
```

Stages:
- `SYNCING` — yaci-indexer still catching up
- `APPLYING_INDEXES` — indexer reached tip; index-applier Job should now be triggered
- `LIVE` — fully operational

### Phase 6 — Index Applier

The index-applier Job is deployed automatically as part of the release
(`indexApplier.mode: automatic` default). It waits for the Rosetta API to respond, then
builds optimised database indexes. This Job takes **1–2 hours on preprod** and **~6 hours
on mainnet**. The Job is auto-cleaned up 24 hours after completion.

```bash
# Monitor progress
kubectl logs -f job/rosetta-index-applier -n cardano
```

:::note
Operators who prefer explicit, operator-triggered indexing can use `indexApplier.mode: hook`
and trigger with a standard `helm upgrade` (without additional flags). See
[Helm Values Reference](./helm-values#index-applier) for details.
:::

---

## Verification

```bash
# All pods Running or Completed
kubectl get pods -n cardano

# Resource usage
kubectl top pods -n cardano

# API live check
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
kubectl logs -f deployment/rosetta-rosetta-api -n cardano             # Rosetta API
kubectl logs -f deployment/rosetta-yaci-indexer -n cardano            # yaci-indexer
kubectl logs -f statefulset/rosetta-cardano-node -c cardano-node -n cardano  # Cardano node
kubectl logs -f statefulset/rosetta-postgresql -n cardano             # PostgreSQL
```

### Port-forward services
```bash
# Local access only
kubectl port-forward svc/rosetta-rosetta-api 8082:8082 -n cardano &

# Remote access — bind to all interfaces
kubectl port-forward --address 0.0.0.0 svc/rosetta-rosetta-api 8082:8082 -n cardano &
```

### Upgrade to a new release
```bash
export DB_PASSWORD=$(grep DB_PASSWORD .env.k8s | cut -d= -f2-)

helm upgrade rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=mainnet \
  "--set=global.protocolMagic=764824073" \
  --set global.configHostPath=$(pwd)/config/node \
  "--set=global.db.password=${DB_PASSWORD}" \
  --set global.releaseVersion="2.1.0" \
  -n cardano 2>&1 | grep -v "walk.go"
```

### Restart a component
```bash
kubectl rollout restart deployment/rosetta-rosetta-api -n cardano
kubectl rollout restart deployment/rosetta-yaci-indexer -n cardano
kubectl rollout restart statefulset/rosetta-cardano-node -n cardano
```

### Scale API replicas
```bash
kubectl scale deployment rosetta-rosetta-api --replicas=2 -n cardano
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
kubectl delete job rosetta-mithril -n cardano --ignore-not-found
```

---

## Security (Mainnet Checklist)

- [ ] **Rotate DB password** — never use the default. Use `openssl rand -base64 32`.
- [ ] **Restrict API access** — use a LoadBalancer security group or Ingress with allowlisted IPs.
- [ ] **Disable debug endpoints** — ensure `PRINT_EXCEPTION=false` in production.
- [ ] **Secrets management** — use Sealed Secrets or Vault instead of `--set global.db.password`.
- [ ] **Network Policy** — restrict pod-to-pod traffic.
- [ ] **Token Registry** — review `rosetta-api.env.tokenRegistryEnabled`; requires outbound internet.

---

## Troubleshooting

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| `Error: cannot patch ... Job is invalid: spec.template: field is immutable` | Stale Mithril Job from previous install | `kubectl delete job rosetta-mithril -n cardano` then redeploy |
| `Error: configHostPath is required` | `global.configHostPath` not set | Add `--set global.configHostPath=$(pwd)/config/node` |
| `cardano-node` stuck in `Init:0/1` | Mithril Job not succeeded yet | `kubectl logs -f job/rosetta-mithril -n cardano` |
| `postgresql` stuck in `Init:0/1` | PVC not bound | `kubectl get pvc -n cardano`; check StorageClass |
| `yaci-indexer` stuck in `Init:1/2` | cardano-node socat bridge not up yet | `kubectl logs <yaci-pod> -c wait-for-node-tcp -n cardano` |
| `rosetta-api` stuck with `FailedMount` | `configHostPath` directory missing on host | Verify `$(pwd)/config/node/<network>/` exists on the server |
| Mithril: `signature error: Verification equation was not satisfied` | Empty verification key passed as env var | Upgrade chart ≥ 2.0.0 — keys are now auto-fetched by entrypoint |
| `yaci-indexer` CrashLoopBackOff (HikariCP timeout) | DB connection pool exhausted | Increase `hikariMaxPoolSize` in values (default 40) |
| `yaci-indexer` two pods simultaneously | Normal rolling update behaviour | Wait — old pod terminates once new one is ready |
| `helm upgrade` fails on Job immutability | Mithril Job already exists | `kubectl delete job rosetta-mithril -n cardano` |
| OOMKilled | Insufficient memory | Use a higher profile (`mid` or `advanced`) |
| PVC `Pending` | No suitable StorageClass | `kubectl get sc`; ensure `local-path` provisioner is running |
| `ImagePullBackOff` | Wrong image tag | Check `global.releaseVersion` in values |
| Port-forward not reachable from remote | Bound to 127.0.0.1 | Add `--address 0.0.0.0` to `kubectl port-forward` |
| cardano-node restart loop after reboot | Startup probe too short for ImmutableDB validation | `startupProbe.failureThreshold: 720` (3 hours); force-delete pod if StatefulSet update is blocked |

---

## Resource Usage

### Preprod — entry profile

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 1 / 2 CPU | 4 / 8 Gi | 100 Gi |
| postgresql | 1 / 2 CPU | 2 / 6 Gi | 50 Gi |
| yaci-indexer | 500m / 1 CPU | 1 / 2 Gi | — |
| rosetta-api | 250m / 1 CPU | 512Mi / 1 Gi | — |

### Mainnet — mid profile

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 2 / 8 CPU | 12 / 24 Gi | 500 Gi |
| postgresql | 2 / 8 CPU | 16 / 32 Gi | 200 Gi |
| yaci-indexer | 1 / 4 CPU | 4 / 8 Gi | — |
| rosetta-api | 500m / 2 CPU | 2 / 4 Gi | — |
| **Total** | **~5.5 / 22 CPU** | **~34 / 68 Gi** | **700 Gi** |

---

## Production Configuration (Mainnet)

```yaml
# values-mainnet-prod.yaml
global:
  profile: mid
  network: mainnet
  protocolMagic: "764824073"

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
export DB_PASSWORD=$(grep DB_PASSWORD .env.k8s | cut -d= -f2-)
helm upgrade rosetta helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  -f helm/cardano-rosetta-java/values-k3s.yaml \
  --set global.network=mainnet \
  "--set=global.protocolMagic=764824073" \
  --set global.configHostPath=$(pwd)/config/node \
  "--set=global.db.password=${DB_PASSWORD}" \
  -n cardano 2>&1 | grep -v "walk.go"
```
