---
sidebar_position: 1
title: Spent UTXO Pruning
description: Optimizing disk usage with spent UTXO pruning
---

# Spent UTXO Pruning

This guide explains how to optimize disk usage in **cardano-rosetta-java** through spent UTXO pruning, including its impact on Rosetta API endpoints and configuration options.

## Understanding Spent UTXO Pruning

Spent UTXO pruning is a disk optimization mechanism in `cardano-rosetta-java`, powered by its underlying indexer, Yaci-Store. This feature selectively removes data related to spent UTXOs from the local database.

**Core Principles:**

- **Targeted Deletion**: Only _spent_ UTXOs are removed. All _current, unspent_ UTXOs are preserved, ensuring the accuracy of the present blockchain state and balances.
- **Distinction from Other Pruning**: This mechanism differs from what is commonly understood as 'pruning' in some other blockchain contexts, including certain descriptions in the Coinbase Mesh API (formerly Rosetta). Unlike methods such as Bitcoin's pruning (which removes entire historical blocks), our approach retains full block history but selectively trims the UTXO set by removing only spent outputs.

**How it Works:**
When enabled, the pruning process operates as follows:

1.  New UTXOs are indexed as transactions occur.
2.  UTXOs are marked as spent when consumed in subsequent transactions.
3.  A background job periodically permanently deletes spent UTXOs that are older than a configurable safety margin (default: 2,160 blocks, ~12 hours on mainnet). This buffer safeguards data integrity against chain rollbacks within Cardano's finality window.

**Impact Summary:**
| Aspect | Effect |
| :------------------------- | :----------------------------------------------------------- |
| **Disk Storage** | ✅ Significantly reduced (e.g., mainnet from ~1TB to ~500GB) |
| **Current UTXO Set** | ✅ Fully preserved; current balances remain accurate |
| **Historical Spent UTXOs** | ⚠️ Permanently deleted beyond the safety margin |
| **Query Performance** | ✅ Improved for queries against the current UTXO set |

## Impact on Rosetta API Endpoints

Spent UTXO pruning affects Rosetta API endpoints differently based on their reliance on historical transaction data. The table below summarizes the impact. Note that "Recent" refers to data within the safety margin (default ~12 hours).

:::info Oldest Block Identifier
When pruning is enabled, the `/network/status` endpoint includes an additional `oldest_block_identifier` object in its response. This identifier corresponds to the latest fully queryable block with complete data. Below this block index, blocks might have missing data due to pruning, making historical queries unreliable.
:::

| **Endpoint**           | **Current State** | **Historical Queries** | **Impact & Notes**                                                           |
| ---------------------- | ----------------- | ---------------------- | ---------------------------------------------------------------------------- |
| `/account/balance`     | ✅ Works          | ⚠️ Limited             | **Low** - Current balances unaffected                                        |
| `/account/coins`       | ✅ Works          | ⚠️ Limited             | **Low** - Current UTXO lists complete                                        |
| `/block`               | ✅ Recent only    | ❌ Incomplete          | **High** - Missing old transaction inputs                                    |
| `/block/transaction`   | ✅ Recent only    | ❌ Incomplete          | **High** - Missing spent UTXOs operation details                             |
| `/search/transactions` | ⚠️ Recent only    | ❌ Limited             | **Medium** - Hash search works, address limited                              |
| `/network/status`      | ✅ Works          | ✅ Works               | **None** - Returns additional `oldest_block_identifier` when pruning enabled |
| `/network/*`           | ✅ Works          | ✅ Works               | **None** - Independent of UTXO data                                          |
| `/construction/*`      | ✅ Works          | ✅ Works               | **None** - Uses current UTXOs only                                           |

After enabling pruning, searching for transactions by their hash will always work, because transaction records themselves are never pruned. However, searching by address is limited: address-based searches rely on the UTXO set, and once spent UTXOs older than the pruning window are deleted, only transactions involving current or recently spent UTXOs can be found by address. Older history is not returned once pruned.

## When Spent UTxO Removal should be enabled?

:::tip Recommended Use Cases
Pruning improves performance by reducing the amount of data processed during API responses, leading to faster query times and lower resource consumption. It also optimizes disk space by focusing on current data rather than maintaining a complete historical record. Consider enabling pruning if your use case aligns with the following:

- **Exchange Integrations & Wallet Services**: Primarily for tracking current balances, processing recent deposits/withdrawals, and validating recent transactions.
- **Resource-Constrained Environments**: Ideal when disk space is a significant limitation (e.g., under 1TB available for mainnet data).
- **Tip-of-Chain Operations**: For applications focused on the latest blockchain state rather than deep historical analysis.
- **Development and Testing**: Useful when a full historical dataset is not essential for development or testing purposes.
  :::

:::note Hybrid Deployment Strategy
We recommend running **pruned nodes for live, day-to-day operations** to benefit from performance improvements and reduced storage needs, while maintaining **non-pruned (full-history) backup nodes** to handle historical transaction reconciliation or audit-related queries as needed.
:::

## When to avoid setting UtxO Removal feature?

:::warning Not Suitable For
Avoid pruning if your operational or regulatory requirements necessitate access to complete and auditable historical blockchain data. Pruning is generally not suitable if you need:

- **Complete Historical Data & Deep Queries**: For comprehensive auditing, compliance, data analytics, or block explorer-like functionality that requires querying full transaction history from any point in time.
- **Strict Compliance and Audit Trails**: If regulatory mandates demand immutable, complete historical records. Pruned data cannot be recovered without a full resync, and historical queries for `/block` and `/block/transaction` become unreliable beyond the safety window.
  :::

:::danger Data Loss Warning
Once data is pruned, it cannot be recovered without a full blockchain resynchronization. Assess your historical data needs carefully before enabling pruning.
:::

## Configuration

Spent UTXO pruning is configured via environment variables, typically set in your `.env.dockerfile` or `.env.docker-compose` file:

```bash
# --- Spent UTXO Pruning Configuration ---

# Enable or disable spent UTXO pruning.
# Default: false (Pruning is disabled by default)
# To enable, set to: true
REMOVE_SPENT_UTXOS=true

# Safety margin: Number of recent blocks for which spent UTXOs are retained.
# Default: 2160 (approximately 12 hours of blocks on mainnet)
# This value balances safety for rollbacks against storage savings.
# Example: To keep ~24 hours of spent UTXOs, set to 4320.
# Note: Larger REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT values provide longer historical query support
# but use more disk space and delay the realization of storage benefits.
REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=2160
```

:::note Configuration Guidelines

- Start with the default settings (`REMOVE_SPENT_UTXOS=false` keeps pruning off).
- The provided defaults (`REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=2160`) offer ~12 h of rollback safety on mainnet.
- Increase `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT` if you need a longer historical query window; decrease it for more aggressive space savings.
  :::

## Migration and Operational Notes

This section outlines key considerations when changing pruning settings or managing a system with pruning enabled.

### Changing Pruning Settings on an Existing Deployment

To change the pruning configuration, update the `REMOVE_SPENT_UTXOS` variable in your environment (to either `true` or `false`) and restart your `cardano-rosetta-java` services.

:::info Resynchronization Is Required to Apply Changes
It is critical to understand that disabling pruning **only affects how new blocks are handled**; it does not retroactively alter your existing database. To have entire historical data again for a pruned enabled instance, an [indexer resynchronization](#how-to-resynchronize-the-indexer) is required.

- **When disabling pruning (`false`)**, a resync is required to rebuild the complete transaction history that was previously pruned away.
- **When enabling pruning (`true`)**, a resync is not required to clear out historically spent UTXOs and reclaim disk space.

Without a resynchronization, your database will exist in a mixed state, and you will not see the expected results of your configuration change immediately.
:::

### How to Resynchronize the Indexer

The resynchronization process rebuilds the indexer database from your existing Cardano node data, which is much faster than resyncing the entire blockchain from scratch.

This is necessary in two main scenarios:

- **To reclaim disk space**: When you enable pruning on an existing instance, a resync will clear out historically spent UTXOs.
- **To restore full history**: When you disable pruning, a resync will rebuild the complete transaction history that was previously pruned away.

:::tip Quick Resynchronization Steps

1.  **Stop the stack**: Gracefully shut down your services using `docker compose down`.
2.  **Remove the indexer volume**: Delete the persistent storage used by the indexer's Postgres database (do **not** touch the Cardano node data).

    ```bash
    # If your compose file uses a **bind mount** (default):
    sudo rm -rf ${DB_PATH}        # replace ${DB_PATH} with the value from your .env file
    ```

3.  **Restart the stack**: Start the services again with `docker compose up -d`. The indexer will begin resyncing from the node, applying your new configuration.
    :::

:::danger Do Not Delete Cardano Node Data
Removing the node's data volume is unnecessary for this process and will trigger a full, time-consuming blockchain resynchronization, leading to significant downtime.
:::

## Further Reading

- [Environment Variables Reference](../install-and-deploy/env-vars.md)
- [Yaci-Store Repository](https://github.com/bloxbean/yaci-store)
- [Coinbase Mesh (formerly Rosetta) API Specification](https://docs.cdp.coinbase.com/mesh/docs/api-reference/)
- [Cardano UTXO Model Documentation](https://docs.cardano.org/learn/eutxo-explainer/)
