# Monitoring setup guide

This guide explains :
- How to run the monitoring stack using Docker Compose 
- Configure Prometheus as a data source in Grafa GUI. 
- Import a Grafana dashboard from a JSON file.

---

## 📦 1. Prerequisites

- The monitoring containers are used as services in rossetta-java so they are declared in the file  `docker-compose.yaml`. By default, monitoring is enabled. You can disable it by commenting out the following line in your:

  ```yaml
  include:
    - docker-compose-indexer.yaml
    - docker-compose-node.yaml
    - docker-compose-api.yaml
    - docker-compose-monitor.yaml  # uncomment this line if you want to disable monitoring
  ```

- The following variables in `.env.docker-compose` must be set to appropriate values ​​and avoid port conflicts with other running services:

  ```dotenv
  ## Monitoring port variables
  PROMETHEUS_PORT=9090
  GRAFANA_PORT=3000
  POSTGRESQL_EXPORTER_PORT=9187
  ```

---

## 🚀 2. Start the Monitoring containers
Since `docker-compose-monitor.yaml` is included in the main `docker-compose.yaml`, the monitoring containers will automatically start when you bring up the Rosetta-java docker compose. These containers include:

- `prometheus`: the backend metrics collector
- `grafana`: the dashboard visualizer
- `postgresql-exporter`: collects PostgreSQL metrics and exposes them to Prometheus

You can verify services are running with:

```bash
docker compose ps
```
---

## 🌐 3. Access Grafana

Once services are running, open your browser and go to:

```
http://localhost:<GRAFANA_PORT>
```

Replace `<GRAFANA_PORT>` with the actual port defined in your `.env` file (default: `3000`).

**Default login credentials:**

- **Username**: `admin`
- **Password**: `admin` (you'll be prompted to change this)

After logging in, go to the Grafana menu → Dashboards → Rossetta-java-dashboards folder → Rosetta Critical Operation Metrics.


> **Note:** The dashboard is preloaded when the Grafana container is started, so no manual import is necessary.
---