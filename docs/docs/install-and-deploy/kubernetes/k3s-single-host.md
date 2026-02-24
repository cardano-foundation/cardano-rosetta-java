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

# 3. Deploy (installs k3s automatically if missing, ~90 min for preprod sync)
make k8s-local-up
```

The setup script (`scripts/k3s-setup.sh preprod`):
1. Installs k3s if not already present
2. Creates the `cardano` namespace
3. Runs `helm dependency build`
4. Deploys with `helm upgrade --install rosetta ... --atomic --wait --wait-for-jobs`

## Monitor Deployment Progress

The deployment goes through three phases. Use these commands to follow along:

### Phase 1 — Mithril snapshot download (~15–60 min depending on network)
```bash
kubectl logs -f -l job-name=rosetta-mithril-job -n cardano
```

### Phase 2 — Cardano node sync (preprod: ~30 min, mainnet: several hours)
```bash
make k8s-logs-node          # cardano-node logs
kubectl get pods -n cardano -w  # watch all pods
```

### Phase 3 — All pods ready
```bash
make k8s-status
# NAME                                    READY   STATUS    RESTARTS
# rosetta-cardano-node-0                  2/2     Running   0
# rosetta-postgresql-0                    1/1     Running   0
# rosetta-yaci-indexer-<hash>             1/1     Running   0
# rosetta-rosetta-api-<hash>              1/1     Running   0
```

## Verify the Deployment

```bash
# Port-forward the API
make k8s-port-forward   # forwards localhost:8082 → rosetta-api:8082

# In a separate terminal — test a Rosetta endpoint
curl -s http://localhost:8082/network/list \
  -H 'Content-Type: application/json' \
  -d '{}' | jq .

# Run the built-in Helm test suite
make k8s-test
```

Expected `/network/list` response:
```json
{
  "network_identifiers": [
    { "blockchain": "cardano", "network": "preprod" }
  ]
}
```

## Mainnet Deployment

```bash
export DB_PASSWORD="<your-secure-password>"
./scripts/k3s-setup.sh mainnet
```

This uses the mainnet defaults in `helm/cardano-rosetta-java/values.yaml` (`mid`
profile, 500 Gi node storage, 200 Gi PostgreSQL).

To override the profile:
```bash
./scripts/k3s-setup.sh mainnet cardano advanced
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
make k8s-local-up       # re-runs helm upgrade --install --atomic
```

### Run stress test (k6 smoke)
```bash
make k8s-stress-test
```

### Enable monitoring (Prometheus + Grafana)
```bash
export DB_PASSWORD="<your-password>"
helm upgrade rosetta helm/cardano-rosetta-java \
  -n cardano \
  --set global.db.password="${DB_PASSWORD}" \
  --set monitoring.enabled=true \
  --reuse-values
kubectl port-forward svc/rosetta-grafana 3000:3000 -n cardano
# Open http://localhost:3000 (admin / admin)
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

| Symptom | Likely cause | Fix |
|---|---|---|
| `cardano-node` pod stuck in `Init:0/1` | Mithril download still running | `kubectl logs -f -l job-name=rosetta-mithril-job -n cardano` |
| `postgresql` pod stuck in `Init:0/1` | Node not fully synced | Check `cardano-node` logs; can take hours for mainnet |
| `rosetta-api` pod `0/1 Running` (not Ready) | Indexer still catching up | `make k8s-logs-indexer`; wait for "LIVE" log message |
| `Error: INSTALLATION FAILED: timed out` | Network slow or insufficient resources | Increase timeout: `--timeout 120m` |
| PVC `Pending` | No suitable StorageClass | Check `kubectl get sc`; ensure `local-path` provisioner is running |
| `ImagePullBackOff` | Rate limit or wrong image tag | Check `global.releaseVersion` in values.yaml |
