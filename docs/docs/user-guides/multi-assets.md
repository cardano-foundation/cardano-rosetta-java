---
sidebar_position: 1
title: Multi-Assets
description: Native token support in Cardano Rosetta implementation
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Multi-Assets

## Overview

Native tokens (Multi-Assets) on Cardano allow users to create and transact with custom tokens alongside ADA. In the Rosetta API implementation, these tokens are represented in both transaction operations and account balances.

:::info
More information about Native Tokens can be found in the [Cardano Documentation](https://docs.cardano.org/developer-resources/native-tokens).
:::

## Key Concepts

- `symbol` - The Asset Name stored in the Ledger (passed as hex string)
- `policyId` - The Policy ID that controls the asset (passed as hex string)
- Both values are required for token operations

:::note
Token Name is not required by Cardano protocol rules. Since Rosetta [symbol](https://www.rosetta-api.org/docs/1.4.4/models/Currency.html) is a required field, it will be represented as `\\x` when no name is provided.
:::

## Operations with Multi-Assets

Multi-Assets can be included in both input and output operations. The token bundles are associated with each operation as metadata.

<Tabs>
  <TabItem value="input" label="Input Operation" default>

```json
{
  "operation_identifier": { "index": 0, "network_index": 0 },
  "type": "input",
  "status": "success",
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
            "value": "10000",
            "currency": { "symbol": "6e7574636f696e", "decimals": 0 }
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
  "related_operations": [{ "index": 0 }],
  "type": "output",
  "status": "success",
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
            "currency": { "symbol": "6e7574636f696e", "decimals": 0 }
          }
        ]
      }
    ]
  }
}
```

  </TabItem>
</Tabs>

## Account Balance Queries

When querying account balances using `/account/balance`, the response will include both ADA and any native tokens owned by the address.

```json
{
  "balances": [
    {
      "value": "71103107",
      "currency": {
        "symbol": "ADA",
        "decimals": 6
      }
    },
    {
      "value": "9648589196",
      "currency": {
        "symbol": "4141504c",
        "decimals": 0,
        "metadata": {
          "policyId": "12e65fa3585d80cba39dcf4f59363bb68b77f9d3c0784734427b1517"
        }
      }
    },
    {
      "value": "9648589196",
      "currency": {
        "symbol": "4150504c45",
        "decimals": 0,
        "metadata": {
          "policyId": "12e65fa3585d80cba39dcf4f59363bb68b77f9d3c0784734427b1517"
        }
      }
    }
  ],
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
            "policyId": "12e65fa3585d80cba39dcf4f59363bb68b77f9d3c0784734427b1517",
            "tokens": [
              {
                "value": "9648589196",
                "currency": {
                  "symbol": "4141504c",
                  "decimals": 0,
                  "metadata": {
                    "policyId": "12e65fa3585d80cba39dcf4f59363bb68b77f9d3c0784734427b1517"
                  }
                }
              },
              {
                "value": "9648589196",
                "currency": {
                  "symbol": "4150504c45",
                  "decimals": 0,
                  "metadata": {
                    "policyId": "12e65fa3585d80cba39dcf4f59363bb68b77f9d3c0784734427b1517"
                  }
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
