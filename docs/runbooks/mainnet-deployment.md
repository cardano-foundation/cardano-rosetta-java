# Mainnet Deployment Runbook

This runbook describes how to deploy and operate **Cardano Rosetta Java** on the Cardano **mainnet** using Kubernetes (K3s single-host or managed cluster).

> **Warning**: Mainnet operations involve real ADA. Ensure security practices are followed, especially credential management and network access control.

---

## Prerequisites

### Hardware Requirements

| Profile | CPU | RAM | Node Storage | DB Storage | Total Disk |
|---------|-----|-----|-------------|-----------|-----------|
| **mid** (recommended) | 8 cores | 48 GB | 500 GB NVMe | 200 GB NVMe | 700+ GB |
| **advanced** | 16 cores | 94 GB | 500 GB NVMe | 400 GB NVMe | 900+ GB |
| **entry** (minimal) | 4 cores | 32 GB | 500 GB SSD | 200 GB SSD | 700+ GB |

> Use NVMe SSDs for PostgreSQL. HDD or standard SSDs will cause serious performance degradation.

### Software
- `kubectl` >= 1.28, `helm` >= 3.12, `jq`
- K3s (single-host) or managed K8s (EKS/GKE/AKS)

### Network
- Outbound TCP 3001 for N2N protocol (cardano-node peers)
- Outbound HTTPS (Mithril aggregator, token registry)
- Consider restricting inbound access to port 8082 (API) with a LoadBalancer security group

---

## Deployment

### Option A: K3s (single-host)

```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
cd cardano-rosetta-java

# Generate a strong password
export DB_PASSWORD="$(openssl rand -base64 32)"
echo "DB_PASSWORD=${DB_PASSWORD}" > .env.k8s
chmod 600 .env.k8s

# Deploy mainnet with mid profile
./scripts/k3s-setup.sh mainnet
```

### Option B: Managed Kubernetes (EKS / GKE / AKS)

```bash
# 1. Configure kubectl to point to your managed cluster
export KUBECONFIG=~/.kube/config

# 2. Build chart dependencies
helm dependency build ./helm/cardano-rosetta-java

# 3. Install with mainnet values
helm upgrade --install rosetta ./helm/cardano-rosetta-java \
  -f ./helm/cardano-rosetta-java/values.yaml \
  --set global.profile=mid \
  --set global.db.password="${DB_PASSWORD}" \
  --set storage.cardanoNode.storageClass="gp3" \
  --set storage.postgresql.storageClass="gp3" \
  --namespace cardano --create-namespace \
  --timeout 90m --wait --wait-for-jobs
```

### Option C: External Database (RDS / Cloud SQL)

For production, use a managed database instead of the bundled PostgreSQL:

```bash
helm upgrade --install rosetta ./helm/cardano-rosetta-java \
  -f ./helm/cardano-rosetta-java/values.yaml \
  --set postgresql.enabled=false \
  --set global.db.host="my-rds-endpoint.us-east-1.rds.amazonaws.com" \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.profile=mid \
  --namespace cardano --create-namespace \
  --timeout 90m --wait
```

> Pre-create the database and user on RDS matching `global.db.name` and `global.db.user`.

---

## Expected Timeline (mainnet)

| Phase | Duration | Notes |
|-------|----------|-------|
| Mithril snapshot download | 2-4 hours | Depends on bandwidth; ~50 GB transfer |
| Cardano node to tip | 15-60 min | From Mithril snapshot |
| PostgreSQL sync wait | ~5 min | After node is at tip |
| yaci-indexer SYNCING | 2-8 hours | Indexed from genesis |
| APPLYING_INDEXES | ~6 hours | `CREATE INDEX CONCURRENTLY` on mainnet |
| **Total to LIVE** | **~12-18 hours** | |

---

## Deployment Phases

Same phases as preprod but with longer timings. Monitor with:

```bash
# Phase 1: Mithril
kubectl logs -f job/rosetta-mithril -n cardano

# Phase 2-3: Node sync progress
kubectl logs -f -l component=cardano-node -c cardano-node -n cardano

# Phase 4+: Overall pod status
kubectl get pods -n cardano -w

# API sync status (after port-forward)
make k8s-port-forward
curl -s http://localhost:8082/network/status \
  -H "Content-Type: application/json" \
  -d '{"network_identifier":{"blockchain":"cardano","network":"mainnet"},"metadata":{}}' \
  -X POST | jq '{stage: .sync_status.stage, block: .current_block_identifier.index}'
```

---

## Security Checklist

- [ ] **Rotate DB password**: Never use the default `weakpwd#123_d`. Use `openssl rand -base64 32`.
- [ ] **Restrict API access**: Use a LoadBalancer security group or Ingress with allowlisted IPs.
- [ ] **Disable debug endpoints**: Ensure `PRINT_EXCEPTION=false` in production.
- [ ] **Token Registry**: Review `tokenRegistryEnabled` setting — requires outbound internet access.
- [ ] **Mithril keys**: Set `mithril.genesisVerificationKey` and `mithril.ancillaryVerificationKey` for mainnet verification.
- [ ] **Network Policy**: Restrict pod-to-pod traffic with Kubernetes NetworkPolicy.
- [ ] **Secrets management**: Use Sealed Secrets or Vault instead of `--set global.db.password`.

### Example NetworkPolicy (restrict API access)

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

## Production Configuration

### Recommended mainnet values override

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

## Upgrading

```bash
# Upgrade to a new version
helm upgrade rosetta ./helm/cardano-rosetta-java \
  -f helm/cardano-rosetta-java/values.yaml \
  --set global.db.password="${DB_PASSWORD}" \
  --set global.releaseVersion="2.1.0" \
  -n cardano --wait --timeout 30m
```

> Note: The index-applier Job runs automatically on `post-upgrade`. If the schema has changed, allow time for re-indexing.

---

## Disaster Recovery

### Backup PVCs

```bash
# Identify PVC backing volumes
kubectl get pvc -n cardano

# Take snapshots via your cloud provider (e.g., AWS EBS Snapshot)
# Or use Velero for cluster-level backup:
# velero backup create rosetta-backup --include-namespaces cardano
```

### Re-sync from Mithril (if node DB is corrupt)

```bash
# 1. Delete the Mithril job (if it exists) and the node PVC
kubectl delete job rosetta-mithril -n cardano --ignore-not-found
kubectl delete pvc rosetta-cardano-node-data -n cardano

# 2. Re-install (will trigger fresh Mithril download)
helm upgrade rosetta ./helm/cardano-rosetta-java \
  --reuse-values --set global.db.password="${DB_PASSWORD}" \
  -n cardano --wait --timeout 90m
```

---

## Resource Usage (mid profile, mainnet)

| Component | CPU req/limit | RAM req/limit | Storage |
|-----------|-------------|--------------|---------|
| cardano-node + sidecars | 2 / 8 CPU | 12 / 24 Gi | 500 Gi |
| postgresql | 2 / 8 CPU | 12 / 24 Gi | 200 Gi |
| yaci-indexer | 1 / 4 CPU | 4 / 8 Gi | — |
| rosetta-api | 500m / 2 CPU | 2 / 4 Gi | — |
| monitoring (optional) | 350m / 1.7 CPU | 832Mi / 2.5 Gi | 60 Gi |
| **Total** | **~6 / 24 CPU** | **~31 / 63 Gi** | **760 Gi** |
