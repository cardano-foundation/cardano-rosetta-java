---
sidebar_position: 1
title: Native Tokens
description: Native token support in Cardano Rosetta implementation
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Native Tokens

## Overview

Native tokens on Cardano allow users to create and transact with custom tokens alongside ADA. In the Rosetta API implementation, these tokens are represented in both transaction operations and account balances.

:::info
More information about Native Tokens can be found in the [Cardano Documentation](https://docs.cardano.org/developer-resources/native-tokens).
:::

## Key Concepts

- `symbol` - The Asset Name stored in the Ledger (passed as ASCII string, changed in [v1.3.2 released on 2025-09-05](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.3.2))
- `policyId` - The Policy ID that controls the asset (passed as hex string)
- Both values are required for token operations

:::note
Token Name is not required by Cardano protocol rules. Since Mesh [symbol](https://docs.cdp.coinbase.com/mesh/mesh-api-spec/models/models#currency) is a required field, it should be represented as an empty string `""` when no name is provided.
:::

## Operations with Native Tokens

Native tokens can be included in both input and output operations. The token bundles are associated with each operation as metadata.

:::important Sign Convention
When spending native tokens in **input operations**, the token values in the `tokenBundle` MUST be **negative**. This follows the same convention as ADA amounts in inputs. Output operations use positive values.
:::

:::info Minting and Burning Observations
**Rosetta API only supports transferring existing native tokens through the Construction API.** Minting and burning operations cannot be initiated through Rosetta.

However, when querying blockchain data through endpoints like `/block/transactions`, you may observe minting and burning transactions with these patterns:
- **Minting**: Token appears only in outputs with positive value (no corresponding input)
- **Burning**: Token appears only in inputs with negative value (no corresponding output)  
- **Partial burning**: Token in inputs (negative) and outputs (positive remainder). The burned amount is the difference
- **Transfer**: Token in both inputs (negative) and outputs (positive) with net zero change

The Rosetta representation doesn't explicitly label these as mint/burn operations - this must be inferred from the token flow patterns.
:::

<Tabs>
  <TabItem value="input" label="Input Operation" default>

```json
{
  "operation_identifier": { "index": 0 },
  "type": "input",
  "account": {
    "address": "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx"
  },
  "amount": {
    "value": "-90000",
    "currency": { "symbol": "ADA", "decimals": 6 }
  },
  "coin_change": {
    "coin_identifier": {
      "identifier": "2f23fd8cca835af21f3ac375bac601f97ead75f2e79143bdf71fe2c4be043e8f:1"
    },
    "coin_action": "coin_spent"
  },
  "metadata": {
    "tokenBundle": [
      {
        "policyId": "b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7",
        "tokens": [
          {
            "value": "-10000",
            "currency": { "symbol": "nutcoin", "decimals": 0 }
          }
        ]
      }
    ]
  }
}
```

  </TabItem>
  <TabItem value="output" label="Output Operation">

```json
{
  "operation_identifier": { "index": 1 },
  "type": "output",
  "account": {
    "address": "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx"
  },
  "amount": {
    "value": "10000",
    "currency": { "symbol": "ADA", "decimals": 6 }
  },
  "metadata": {
    "tokenBundle": [
      {
        "policyId": "b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7",
        "tokens": [
          {
            "value": "10000",
            "currency": { "symbol": "nutcoin", "decimals": 0 }
          }
        ]
      }
    ]
  }
}
```

  </TabItem>
</Tabs>

## Account Balance and UTXO Queries

Cardano Rosetta provides two separate endpoints for querying account information:

<Tabs>
  <TabItem value="balance" label="/account/balance" default>

### Account Balance

The `/account/balance` endpoint returns the **aggregated balance** of all ADA and native tokens owned by an address.

:::info Note on Stake Addresses
When queried with a stake address (reward address), the response includes available rewards that can be withdrawn.

**Known Issue:** Historical queries for stake address rewards currently return the current reward value instead of the historical value at the specified block. See [Issue #590](https://github.com/cardano-foundation/cardano-rosetta-java/issues/590).
:::

**Request:**
```json
{
  "network_identifier": {"blockchain": "cardano", "network": "mainnet"},
  "account_identifier": {"address": "addr1..."}
}
```

**Response:**
```json
{
  "block_identifier": {
    "index": 10453789,
    "hash": "6e9e89632bc5c72030d3a486647e889c48d63e4da0643191b13566ad816d2d57"
  },
  "balances": [
    {
      "value": "71103107",
      "currency": {
        "symbol": "ADA",
        "decimals": 6
      }
    },
    {
      "value": "10000",
      "currency": {
        "symbol": "nutcoin",
        "decimals": 0,
        "metadata": {
          "policyId": "b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7"
        }
      }
    }
  ]
}
```

  </TabItem>
  <TabItem value="coins" label="/account/coins">

### Account UTXOs

The `/account/coins` endpoint returns the **individual UTXOs** (unspent transaction outputs) owned by an address, including any native tokens attached to each UTXO.

**Request:**
```json
{
  "network_identifier": {"blockchain": "cardano", "network": "mainnet"},
  "account_identifier": {"address": "addr1..."}
}
```

**Response:**
```json
{
  "block_identifier": {
    "index": 10453789,
    "hash": "6e9e89632bc5c72030d3a486647e889c48d63e4da0643191b13566ad816d2d57"
  },
  "coins": [
    {
      "coin_identifier": {
        "identifier": "414afe46bc6b7e52739dd5dd76eef30812168912a34bb31676b9872881aeacd2:0"
      },
      "amount": {
        "value": "71103107",
        "currency": {
          "symbol": "ADA",
          "decimals": 6
        }
      },
      "metadata": {
        "414afe46bc6b7e52739dd5dd76eef30812168912a34bb31676b9872881aeacd2:0": [
          {
            "policyId": "b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7",
            "tokens": [
              {
                "value": "10000",
                "currency": {
                  "symbol": "nutcoin",
                  "decimals": 0
                }
              }
            ]
          }
        ]
      }
    }
  ]
}
```

:::tip
Native tokens in Cardano are always attached to UTXOs containing ADA. The `metadata` field shows which tokens are contained in each specific UTXO.
:::

  </TabItem>
</Tabs>
