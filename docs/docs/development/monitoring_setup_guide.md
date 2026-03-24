# Monitoring Setup Guide

This guide explains how to set up monitoring for **Docker Compose** deployments.

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

