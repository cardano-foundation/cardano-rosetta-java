---
sidebar_position: 1
title: Overview
description: Kubernetes deployment overview — when to use it, architecture, and prerequisites.
---

# Kubernetes Deployment Overview

Cardano Rosetta Java ships official [Helm charts](https://helm.sh/) for Kubernetes
deployment. The charts support a **K3s single-host baseline** (for local testing and
small production deployments) and any **managed Kubernetes cluster** (EKS, GKE, AKS).

## When to Use Kubernetes Instead of Docker Compose

| Requirement | Docker Compose | Kubernetes (Helm) |
|---|---|---|
| Local development / quick start | ✅ Recommended | Possible via K3s |
| Single-host production | Possible | ✅ K3s |
| Multi-node / HA production | Not supported | ✅ Managed K8s |
| Resource isolation & limits | No | ✅ Pod resource profiles |
| Rolling upgrades with zero downtime | No | ✅ Deployment strategy |
| Observability (Prometheus + Grafana) | Manual | ✅ Included subchart |

## Architecture

The deployment consists of **5 Helm subcharts** plus orchestration resources in the
parent chart:

```
Parent chart: cardano-rosetta-java
├── cardano-node          StatefulSet — Cardano blockchain node + socat sidecar
├── postgresql            StatefulSet — PostgreSQL with blockchain-tuned configuration
├── yaci-indexer          Deployment  — Blockchain data indexer (Yaci Store)
├── rosetta-api           Deployment  — Rosetta HTTP API
└── monitoring            Deployment  — Prometheus + Grafana (disabled by default)
```

### Key Architectural Differences from Docker Compose

**UNIX socket bridging via socat**

Docker Compose shares the Cardano node's UNIX socket (`/node/node.socket`) via volume
mounts. In Kubernetes, pods cannot share UNIX sockets. A `socat` sidecar inside the
`cardano-node` pod forwards TCP connections on port `3002` to the UNIX socket. The
`yaci-indexer` connects using the `n2c-socat` Spring profile.

**Startup ordering via init containers**

Docker Compose `depends_on` chains are replaced by Kubernetes init containers:

```
Mithril Job ──────────────────────────────► (one-shot snapshot download)
  cardano-node (wait-for-mithril initContainer polls PVC)
    postgresql (wait-for-node-sync initContainer: cardano-cli query tip ≥ 99%)
      yaci-indexer (wait-for-postgres: pg_isready)
        rosetta-api (wait-for-postgres + wait-for-indexer)

index-applier Job ────────────────────────► (Helm post-install hook, runs in background)
```

The `index-applier` is a Helm **post-install hook** that runs as a separate Kubernetes
Job after all main pods are ready. It waits for the API to reach `APPLYING_INDEXES` stage
and then builds optimised database indexes. This Job can take **6–18 hours** on mainnet.

:::important
Do **not** use `--wait-for-jobs` in your `helm upgrade --install` command. That flag makes
Helm block until the `index-applier` Job completes, which triggers a timeout and (with
`--atomic`) rolls back the entire release. Use `--wait` only, which returns once all
Deployments and StatefulSets are ready.
:::

**Three sync stages**

| Stage | What's happening | Pods ready |
|---|---|---|
| `SYNCING` | Mithril download + node syncing | cardano-node only |
| `APPLYING_INDEXES` | DB indexes being built | All pods up, API responding |
| `LIVE` | Full operation | All pods ready, API fully functional |

## Hardware Profiles

Three built-in profiles scale resources across all components:

| Profile | Total RAM | Total vCPU | Use case |
|---|---|---|---|
| `entry` | 32 GB | 4 cores | Preprod, single host, K3s |
| `mid` | 48 GB | 8 cores | Mainnet production |
| `advanced` | 94 GB | 16 cores | High-throughput production |

See [Helm Values Reference](./helm-values) for per-component breakdown.

## Prerequisites

### Software
- **Helm** v3.14+ (`helm version`)
- **kubectl** configured for your cluster
- **k3s** v1.28+ (for single-host deployments, installed automatically by the setup script)

### Access
- A `DB_PASSWORD` for PostgreSQL (min 16 characters recommended for production)
- Outbound internet access (Mithril snapshot download, ~20 GB for mainnet)

### Hardware (minimum for preprod / `entry` profile)
- 8 vCPU
- 32 GB RAM
- 150 GB fast SSD (for node data + PostgreSQL)

### Hardware (recommended for mainnet / `mid` profile)
- 16 vCPU
- 64 GB RAM
- 700 GB fast SSD (500 GB node + 200 GB PostgreSQL)

## Next Steps

- [K3s Single-Host Deployment](./k3s-single-host) — get running locally in minutes
- [Helm Values Reference](./helm-values) — full configuration reference
- [Deployment Runbook](../../../../runbooks/deployment) — operator checklist for preprod and mainnet
