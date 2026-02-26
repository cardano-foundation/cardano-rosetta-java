# Monitoring Setup Guide

This guide explains how to run the monitoring stack (Prometheus + Grafana) for both
**Docker Compose** and **Kubernetes (Helm)** deployments.

---

## Docker Compose

### Prerequisites

The monitoring containers are declared in `docker-compose-monitor.yaml`. By default,
monitoring is enabled. To disable it, comment out the relevant line:

```yaml
include:
  - docker-compose-indexer.yaml
  - docker-compose-node.yaml
  - docker-compose-api.yaml
  - docker-compose-monitor.yaml  # comment this line to disable monitoring
```

Set monitoring ports in `.env.docker-compose`:

```dotenv
## Monitoring port variables
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
POSTGRESQL_EXPORTER_PORT=9187
```

### Start Monitoring

Since `docker-compose-monitor.yaml` is included in the main `docker-compose.yaml`, the
monitoring containers start automatically. These include:

- `prometheus` — metrics collector
- `grafana` — dashboard visualizer
- `postgresql-exporter` — PostgreSQL metrics exporter

```bash
docker compose ps
```

### Access Grafana (Docker Compose)

```
http://localhost:<GRAFANA_PORT>
```

Default credentials: **admin** / **admin** (you will be prompted to change on first login).

Navigate to: **Dashboards → Rosetta-java-dashboards → Rosetta Critical Operation Metrics**

The dashboard is pre-provisioned from `config/monitoring/dashboards/rosetta-dashboards.json`
and loads automatically when Grafana starts.

---

## Kubernetes (Helm)

### Enable Monitoring

Monitoring is enabled by default (`monitoring.enabled: true` in `values.yaml`). To
disable it for a specific environment, pass `--set monitoring.enabled=false` or override
in your values file:

```yaml
monitoring:
  enabled: false
```

The monitoring subchart deploys:
- **Prometheus** — scrapes metrics from all stack components
- **Grafana** — pre-provisioned with the Rosetta dashboard
- **postgresql-exporter** — PostgreSQL metrics
- **node-exporter** — host-level metrics (CPU, memory, disk, network)

### Access Services

The monitoring services use `ClusterIP` (not accessible from outside the cluster by
default). Use `kubectl port-forward` to reach them:

**Local machine only** (port-forward binds to `127.0.0.1`):
```bash
kubectl port-forward svc/rosetta-grafana    3000:3000 -n cardano &
kubectl port-forward svc/rosetta-prometheus 9090:9090 -n cardano &
```

### Grafana Dashboard

The **Rosetta Critical Operation Metrics** dashboard is automatically provisioned from
`config/monitoring/dashboards/rosetta-dashboards-k8s.json`.

This is a **separate file** from the Docker Compose dashboard (`rosetta-dashboards.json`)
because the two environments differ in:

| Aspect | Docker Compose | Kubernetes |
|--------|---------------|-----------|
| Service names | `api`, `cardano-node` | `rosetta-rosetta-api`, `rosetta-cardano-node` |
| Prometheus labels | `instance="api:8082"` | `instance="rosetta-rosetta-api:8082"` |
| Datasource UID | `${DS_PROMETHEUS}` (resolved on UI import) | `DS_PROMETHEUS` (literal, for provisioned dashboards) |

**Do not use the Docker Compose dashboard JSON in Kubernetes.** The label-based series
overrides baked into the Docker Compose dashboard reference Docker-specific service names
and will hide all K8s metrics.

### Verify Prometheus Targets

All targets should be `UP`:

```bash
# Via Prometheus API
curl -s http://localhost:9090/api/v1/targets | \
  python3 -c "import json,sys; [print(t['health'], t['labels'].get('instance')) for t in json.load(sys.stdin)['data']['activeTargets']]"

# Expected — 6 targets all UP:
# up  rosetta-rosetta-api:8082
# up  rosetta-yaci-indexer:9095
# up  rosetta-cardano-node:12798
# up  rosetta-pg-exporter:9187
# up  rosetta-node-exporter:9100
# up  localhost:9090
```

### Reload Grafana Dashboards After ConfigMap Update

If you update the Grafana dashboard ConfigMap, the pod's mounted volume takes ~1–2 minutes
to sync. After the volume syncs, trigger a provisioning reload:

```bash
# Check if volume has synced (count should match expected occurrences)
GRAFANA_POD=$(kubectl get pod -n cardano -l app=rosetta-grafana -o jsonpath='{.items[0].metadata.name}')
kubectl exec $GRAFANA_POD -n cardano -- \
  grep -c 'DS_PROMETHEUS' /etc/grafana/provisioning/dashboards/json/rosetta-dashboards.json

# Reload provisioned dashboards (no pod restart needed)
curl -s -u admin:admin -X POST http://localhost:3000/api/admin/provisioning/dashboards/reload
```
