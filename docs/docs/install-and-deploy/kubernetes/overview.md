---
sidebar_position: 1
title: Overview
description: Kubernetes deployment overview — when to use it, architecture, and prerequisites.
---

# Kubernetes Deployment Overview

Cardano Rosetta Java ships official [Helm charts](https://helm.sh/) for Kubernetes
deployment. The charts support both **single-host K3s** deployments and any **managed
Kubernetes cluster** (EKS, GKE, AKS).

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
  cardano-node (wait-for-mithril initContainer polls K8s API)
    postgresql (wait-for-node-sync initContainer: /sbin/wait-for-node-sync.sh ≥ 100%)
      yaci-indexer (wait-for-postgres: pg_isready)
        rosetta-api (wait-for-postgres + wait-for-indexer: /actuator/health)
          index-applier Job ──────────────► (plain Job, runs automatically with the release)
```

The `cardano-node` pod runs **three containers**: the node itself, a `socat` sidecar
(bridges the UNIX socket to TCP port 3002), and `cardano-submit-api` — so `READY` shows
`3/3` when fully up.

The `index-applier` is a plain Kubernetes Job that runs automatically with the release
(default `indexApplier.mode: automatic`). It waits for the Rosetta API to become
responsive, then builds optimised database indexes. This Job can take **6–18 hours** on
mainnet. It is cleaned up automatically after 24 hours via `ttlSecondsAfterFinished`.

:::note
The `index-applier` runs as a plain Job by default — no `--no-hooks` flag is needed.
Operators who prefer explicit, operator-triggered index building can switch to legacy hook
mode with `--set indexApplier.mode=hook`. In hook mode, monitor the Job independently and
never use `--wait-for-jobs`.
:::

**Three sync stages**

| Stage | What's happening | Pods ready |
|---|---|---|
| `SYNCING` | yaci-indexer catching up to chain tip | All pods up, API responding |
| `APPLYING_INDEXES` | Indexer reached tip, DB indexes being built | All pods up, API responding |
| `LIVE` | Fully synced, all indexes valid | All pods ready, API fully functional |

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
- **K3s** v1.28+ or any managed Kubernetes cluster (EKS, GKE, AKS)

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

- [Deployment Runbook](./deployment) — step-by-step deploy for preprod and mainnet
- [Helm Values Reference](./helm-values) — full configuration reference
