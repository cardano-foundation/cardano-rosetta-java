---
sidebar_position: 1
title: Remove Spent UTXOs
description: Optimizing disk usage with removal of spent UTxOs
---

# Remove Spent UTXOs

This guide explains how to optimize disk usage in **cardano-rosetta-java** through removal of spent utxo (a special rosetta-java specific form of pruning).

## What is UTxO Spent Pruning (removal of spent UTxOs)?

UTxO Spent Pruning removes spent (consumed) UTXOs from local storage, keeping only unspent UTXOs. This can reduce on-disk storage from ~1TB down to ~400GB, but discards historical transaction data.

- Only unspent outputs are preserved.
- You can still validate the chain's current state (and spend tokens), since active UTXOs remain.
- You should be able to build transaction because only spent UTxOs are removed (unspent stay)

**Enable Spent UTxO removal **: Set `REMOVE_SPENT_UTXOS=true` in your environment (e.g., in `.env.dockerfile` or `.env.docker-compose`).
**Disable Spent UTxO removal ** (default): Set `REMOVE_SPENT_UTXOS=false`.

## When Spent UTxO Removal should be enabled?   

- **Low Disk Environments**: If you need to minimize disk usage and only require UTXO data for current balances.
- **Exploratory / Dev Environments**: If historical queries are not critical.
- **Performance**: if you are running into performance / scalability issues, i.e. especially on /account/balance when working with large addresses

## When to avoid setting UtxO Removal feature?

- **Full Historical Data Requirements**: If you need the complete transaction history—whether for exchange operations, audit trails, or compliance mandates—do not enable pruning. Pruning discards spent UTXOs, which removes older transaction data and prevents certain types of historical lookups or reporting.

## Example Configuration

Below is a snippet of how you might configure `.env.dockerfile` or `.env.docker-compose` for pruning:

```bash
# --- Remove Spent UTxOs Toggle ---
REMOVE_SPENT_UTXOS=true
```

## Further Reading

- [Rosetta API Reference](https://docs.cdp.coinbase.com/mesh/docs/api-reference/)
- [Yaci-Store Repository](https://github.com/bloxbean/yaci-store)
