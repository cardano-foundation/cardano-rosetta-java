---
sidebar_position: 1
title: Advanced Configuration and Performance
description: Advanced configuration options and performance tuning
---

# Advanced Configuration and Performance

This guide provides details on how to tune **cardano-rosetta-java** for various workloads and resource constraints. It covers:

1. [Pruning (Disk Usage Optimization)](#1-pruning-disk-usage-optimization)
2. [Database Pool Settings (HikariCP)](#2-database-pool-settings-hikaricp)
3. [Tomcat Thread Configuration](#3-tomcat-thread-configuration)
4. [Example `.env` Settings](#4-example-env-settings)

---

## 1. Pruning (Disk Usage Optimization)

Pruning removes spent (consumed) UTXOs from local storage, keeping only unspent UTXOs. This can reduce on-disk storage from ~1TB down to ~400GB, but discards historical transaction data.

- Only unspent outputs are preserved.
- You can still validate the chain’s current state (and spend tokens), since active UTXOs remain.
- **Enable Pruning**: Set `PRUNING_ENABLED=true` in your environment (e.g., in `.env.dockerfile` or `.env.docker-compose`).
- **Disable Pruning** (default): Set `PRUNING_ENABLED=false`.

### 1.1. When to Enable Pruning

- **Low Disk Environments**: If you need to minimize disk usage and only require UTXO data for current balances.
- **Exploratory / Dev Environments**: If historical queries are not critical.

### 1.2. When to Avoid Pruning

- **Full Historical Data Requirements**: If you need the complete transaction history—whether for exchange operations, audit trails, or compliance mandates—do not enable pruning. Pruning discards spent UTXOs, which removes older transaction data and prevents certain types of historical lookups or reporting.

---

## 2. Database Pool Settings (HikariCP)

cardano-rosetta-java uses [HikariCP](https://github.com/brettwooldridge/HikariCP) as the JDBC connection pool. Tuning these values can help manage concurrency and performance.

| Variable                                          | Purpose                                                  | Common Defaults | Possible Tuning |
| ------------------------------------------------- | -------------------------------------------------------- | --------------- | --------------- |
| `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE`        | Max number of DB connections in the pool                 | 10 (example)    | 20–100          |
| `SPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD` | Time (ms) before a connection leak warning is logged     | 30,000          | 300,000         |
| `SPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT`      | Max time (ms) to wait for a free connection before error | 30,000          | 300,000         |

### 2.1. Example

If you’re dealing with high API request volume, consider:

```bash
SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=50
SPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD=300000
SPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT=300000
```

- This allows up to 50 connections in the pool.
- The large leak detection threshold (5 minutes) can help in debugging slow queries.

### 2.2 When to Increase Pool Size

- If your logs show “connection timeout” or “pool is exhausted,” your current pool size may be insufficient.
- Only increase `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE` if your database has the resources (CPU, RAM, I/O) to handle additional connections.

---

## 3. Tomcat Thread Configuration

By default, Spring Boot (Tomcat) handles incoming HTTP requests with a thread pool. If you anticipate **very high** concurrency, you might adjust:

- **`SERVER_TOMCAT_THREADS_MAX`**: Maximum number of threads Tomcat uses to handle requests.
  - Start with the default (`200`).
  - Increasing this limit can help in high-concurrency scenarios, but if your system’s bottleneck is elsewhere (e.g., database, network, or CPU), you may see limited performance gains.
  - **Only** increase if profiling shows that Tomcat threads are maxed out and your DB can keep up.
  - Check CPU/memory usage carefully; going too high can lead to contention and slowdowns.

---

## 4. Example `.env` Settings

Below is a snippet of how you might configure `.env.dockerfile` or `.env.docker-compose` for higher throughput:

```bash
# --- Pruning Toggle ---
PRUNING_ENABLED=false
# Keep full history, requires ~1TB of disk space

# --- HikariCP Database Pool ---
SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=50
SPRING_DATASOURCE_HIKARI_LEAKDETECTIONTHRESHOLD=300000
SPRING_DATASOURCE_HIKARI_CONNECTIONTIMEOUT=300000

# --- Tomcat Thread Pool ---
# SERVER_TOMCAT_THREADS_MAX=200
# Uncomment and set a higher value if needed:
# SERVER_TOMCAT_THREADS_MAX=400
```

---

## Further Reading

- [Rosetta API Reference](https://docs.cdp.coinbase.com/mesh/docs/api-reference/)
- [Yaci-Store Repository](https://github.com/bloxbean/yaci-store)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/index.html) (for more advanced server and DB config)
