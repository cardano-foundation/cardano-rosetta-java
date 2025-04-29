---
sidebar_position: 1
title: Pruning UTXOs
description: Optimizing disk usage with pruning
---

# Pruning UTXOs

This guide explains how to optimize disk usage in **cardano-rosetta-java** through pruning.

## What is Pruning?

Pruning removes spent (consumed) UTXOs from local storage, keeping only unspent UTXOs. This can reduce on-disk storage from ~1TB down to ~400GB, but discards historical transaction data.

- Only unspent outputs are preserved.
- You can still validate the chain's current state (and spend tokens), since active UTXOs remain.
- **Enable Pruning**: Set `PRUNING_ENABLED=true` in your environment (e.g., in `.env.dockerfile` or `.env.docker-compose`).
- **Disable Pruning** (default): Set `PRUNING_ENABLED=false`.

## When to Enable Pruning

- **Low Disk Environments**: If you need to minimize disk usage and only require UTXO data for current balances.
- **Exploratory / Dev Environments**: If historical queries are not critical.

## When to Avoid Pruning

- **Full Historical Data Requirements**: If you need the complete transaction history—whether for exchange operations, audit trails, or compliance mandates—do not enable pruning. Pruning discards spent UTXOs, which removes older transaction data and prevents certain types of historical lookups or reporting.

## Example Configuration

Below is a snippet of how you might configure `.env.dockerfile` or `.env.docker-compose` for pruning:

```bash
# --- Pruning Toggle ---
PRUNING_ENABLED=true
# Enables pruning to reduce disk space requirements
```

## Further Reading

- [Rosetta API Reference](https://docs.cdp.coinbase.com/mesh/docs/api-reference/)
- [Yaci-Store Repository](https://github.com/bloxbean/yaci-store)
