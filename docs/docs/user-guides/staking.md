---
sidebar_position: 2
title: Staking Operations
description: How to perform staking operations through Cardano Rosetta API
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Staking Operations

## Overview

Cardano's Proof of Stake system allows ADA holders to delegate their stake to stake pools and participate in the network consensus. The Rosetta API implementation supports all staking operations required to participate in this system.

## Staking Operation Types

Each staking operation is sent in requests to the `/construction/preprocess` and `/construction/payloads` endpoints with specific operation types:

| Operation Type           | Description              |
| ------------------------ | ------------------------ |
| `input`                  | Transaction input        |
| `output`                 | Transaction output       |
| `stakeKeyRegistration`   | Register a stake key     |
| `stakeKeyDeregistration` | Deregister a stake key   |
| `stakeDelegation`        | Delegate stake to a pool |
| `withdrawal`             | Withdraw rewards         |

:::important
The order of operations is important since they are processed in a specific sequence:

1. Inputs and outputs
2. Operations requiring certificates (registrations, deregistrations, delegations)
3. Withdrawals
   :::

## Operation Examples

<Tabs>
  <TabItem value="input" label="Input Operation" default>

```json
{
  "operation_identifier": {
    "index": 0,
    "network_index": 0
  },
  "type": "input",
  "status": "success",
  "account": {
    "address": "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx"
  },
  "amount": {
    "value": "-9000000",
    "currency": {
      "symbol": "ADA",
      "decimals": 6
    }
  },
  "coin_change": {
    "coin_identifier": {
      "identifier": "2f23fd8cca835af21f3ac375bac601f97ead75f2e79143bdf71fe2c4be043e8f:1"
    },
    "coin_action": "coin_spent"
  }
}
```

  </TabItem>
  <TabItem value="output" label="Output Operation">

```json
{
  "operation_identifier": {
    "index": 1
  },
  "related_operations": [
    {
      "index": 0
    }
  ],
  "type": "output",
  "status": "success",
  "account": {
    "address": "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx"
  },
  "amount": {
    "value": "10000",
    "currency": {
      "symbol": "ADA",
      "decimals": 6
    }
  }
}
```

  </TabItem>
  <TabItem value="stake-key-registration" label="Stake Key Registration">

```json
{
  "operation_identifier": {
    "index": 3
  },
  "type": "stakeKeyRegistration",
  "status": "success",
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    }
  }
}
```

:::note
No amount is needed for this operation. The deposit will be calculated using the fixed minimum key deposit value times the number of stake key registrations.
:::

  </TabItem>
  <TabItem value="stake-delegation" label="Stake Delegation">

```json
{
  "operation_identifier": {
    "index": 3
  },
  "type": "stakeDelegation",
  "status": "success",
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    },
    "pool_key_hash": "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5"
  }
}
```

  </TabItem>
  <TabItem value="stake-key-deregistration" label="Stake Key Deregistration">

```json
{
  "operation_identifier": {
    "index": 3
  },
  "type": "stakeKeyDeregistration",
  "status": "success",
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    }
  }
}
```

:::note
No amount is needed for this operation. The refund will be calculated using the fixed minimum key deposit value times the number of stake key deregistrations.
:::

  </TabItem>
  <TabItem value="withdrawal" label="Withdrawal">

```json
{
  "operation_identifier": {
    "index": 4
  },
  "type": "withdrawal",
  "status": "success",
  "amount": {
    "value": "10000",
    "currency": {
      "symbol": "ADA",
      "decimals": 6
    }
  },
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    }
  }
}
```

  </TabItem>
</Tabs>

## Required Metadata

Each staking operation requires specific metadata:

| Operation                | Required Metadata                               |
| ------------------------ | ----------------------------------------------- |
| Stake Key Registration   | `staking_credential` (hex bytes and curve type) |
| Stake Delegation         | `staking_credential` and `pool_key_hash`        |
| Stake Key Deregistration | `staking_credential` (hex bytes and curve type) |
| Withdrawal               | `staking_credential` and `amount`               |
