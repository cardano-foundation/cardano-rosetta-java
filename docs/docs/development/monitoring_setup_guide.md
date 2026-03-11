# Monitoring Setup Guide

This guide explains how to set up monitoring for both **Docker Compose** (bundled stack)
and **Kubernetes** (integration with existing Prometheus/Grafana) deployments.

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

### Architecture

The Helm chart does **not** deploy Prometheus or Grafana. Production clusters are expected
to provide their own monitoring stack — typically
[kube-prometheus-stack](https://prometheus-community.github.io/helm-charts/).

The chart provides integration artifacts:

- **ServiceMonitor** resources (one per service) for automatic Prometheus Operator discovery
- **Grafana dashboard ConfigMaps** with the `grafana_dashboard: "1"` label for sidecar import
- **postgres-exporter** as an optional deployment alongside PostgreSQL

### Enable Integration

```yaml
serviceMonitor:
  enabled: true
  releaseLabel: prometheus   # must match your kube-prometheus-stack release name

pgExporter:
  enabled: true              # deploys postgres-exporter alongside PostgreSQL
```

### Grafana Dashboard

The **Rosetta Critical Operation Metrics** dashboard is shipped as a ConfigMap
(`rosetta-grafana-dashboards`). When `serviceMonitor.enabled: true`, the ConfigMap is
created with the `grafana_dashboard: "1"` label, which the Grafana sidecar from
kube-prometheus-stack automatically discovers and imports.

To manually import:
```bash
kubectl get configmap rosetta-grafana-dashboards -n cardano -o jsonpath='{.data.rosetta-dashboards\.json}' > dashboard.json
# Import dashboard.json via Grafana UI → Dashboards → Import
```

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

### Verify ServiceMonitor Targets

Once ServiceMonitors are created, verify Prometheus is scraping all targets:

```bash
# List ServiceMonitors
kubectl get servicemonitors -n cardano

# Check via Prometheus API (port-forward your existing Prometheus first)
curl -s http://localhost:9090/api/v1/targets | \
  python3 -c "import json,sys; [print(t['health'], t['labels'].get('instance')) for t in json.load(sys.stdin)['data']['activeTargets']]"

# Expected — 4 targets all UP:
# up  rosetta-rosetta-api:8082
# up  rosetta-yaci-indexer:9095
# up  rosetta-cardano-node:12798
# up  rosetta-pg-exporter:9187
```
