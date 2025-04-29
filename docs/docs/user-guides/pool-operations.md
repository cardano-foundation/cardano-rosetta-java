---
sidebar_position: 4
title: Pool Operations
description: How to manage stake pools with Cardano Rosetta API
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Stake Pool Operations

## Overview

Stake pools are a central element of Cardano's Proof of Stake system. The Rosetta API implementation supports operations for registering, updating, and retiring stake pools.

## Operation Types

In addition to standard transaction operations, the following operations are available for stake pool management:

| Operation Type             | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `poolRegistration`         | Register a new stake pool                       |
| `poolRegistrationWithCert` | Register a pool using a pre-created certificate |
| `poolRetirement`           | Retire an existing stake pool                   |

:::note
For all pool operations, the cold key is needed to sign the payloads, so it will be passed as an address.
:::

## Pool Registration

<Tabs>
  <TabItem value="standard" label="Standard Registration" default>

A pool registration operation requires `poolRegistrationParams` as metadata. The pool registration deposit amount is taken from the protocol parameters.

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "operations": [
    {
      "operation_identifier": {
        "index": 3
      },
      "type": "poolRegistration",
      "status": "success",
      "account": {
        "address": "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5"
      },
      "metadata": {
        "poolRegistrationParams": {
          "vrfKeyHash": "8dd154228946bd12967c12bedb1cb6038b78f8b84a1760b1a788fa72a4af3db0",
          "rewardAddress": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5",
          "pledge": "5000000",
          "cost": "3000000",
          "poolOwners": [
            "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er"
          ],
          "relays": [
            {
              "type": "single_host_addr",
              "ipv4": "127.0.0.1",
              "ipv6": "2345:0425:2ca1:0000:0000:0567:5673:23b5",
              "port": "32"
            }
          ],
          "margin": {
            "numerator": "1",
            "denominator": "1"
          },
          "poolMetadata": {
            "url": "poolMetadataUrl",
            "hash": "9ac2217288d1ae0b4e15c41b58d3e05a13206fd9ab81cb15943e4174bf30c90b"
          }
        }
      }
    }
  ],
  "metadata": {
    "ttl": "1000"
  }
}
```

  </TabItem>
  <TabItem value="with-cert" label="Registration With Certificate">

For pool registration using a pre-created certificate, you need to include the certificate as a hex string in the `poolRegistrationCert` field.

```json
{
  "operation_identifier": {
    "index": 3
  },
  "type": "poolRegistrationWithCert",
  "status": "success",
  "account": {
    "address": "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5"
  },
  "metadata": {
    "poolRegistrationCert": "8a03581c1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b558208dd154228946bd12967c12bedb1cb6038b78f8b84a1760b1a788fa72a4af3db01a004c4b401a002dc6c0d81e820101581de1bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb81581c7a9a4d5a6ac7a9d8702818fa3ea533e56c4f1de16da611a730ee3f008184001820445820f5d9505820f5d9ea167fd2e0b19647f18dd1e0826f706f6f6c4d6574616461746155726c58209ac2217288d1ae0b4e15c41b58d3e05a13206fd9ab81cb15943e4174bf30c90b"
  }
}
```

  </TabItem>
  <TabItem value="retirement" label="Pool Retirement">

To retire a stake pool, you need to specify the epoch in which the pool should be retired.

```json
{
  "operation_identifier": {
    "index": 1
  },
  "type": "poolRetirement",
  "status": "success",
  "account": {
    "address": "153806dbcd134ddee69a8c5204e38ac80448f62342f8c23cfe4b7edf"
  },
  "metadata": {
    "epoch": 200
  }
}
```

  </TabItem>
</Tabs>

## Required Signatures

For pool registration, the following signatures are required:

- **Cold key signature** - Used to sign the transaction
- **Reward address signature** - For the creator's reward address
- **Pool owner signatures** - For each pool owner's reward address

These signatures will be specifically requested in the `/construction/payloads` response and must be provided at the `/construction/combine` endpoint.

## Relay Configuration

When registering a stake pool, relay information must be provided in a specific format:

### IPv4 Format

Each segment must be a number between 0 and 255:

```
###.###.###.###
Example: 127.0.0.1
```

### IPv6 Format

Each segment must be a hexadecimal number between 0000 and FFFF:

```
####:####:####:####:####:####:####:####
Example: 2345:0425:2ca1:0000:0000:0567:5673:23b5
```

## Data API Examples

The Rosetta Data API returns information about pool operations in `/block` and `/block/transaction` endpoints.

<Tabs>
  <TabItem value="retirement-response" label="Pool Retirement Response" default>

```json
{
  "transaction_identifier": {
    "hash": "dcbff41c50c5b4012d49be5be75b11a0c5289515258ef4cf108eb6ec4ed5f37a"
  },
  "operations": [
    {
      "operation_identifier": { "index": 0 },
      "type": "poolRetirement",
      "status": "success",
      "account": {
        "address": "d6aafa5358b98373449434542e3da3564bc71635ae3247dc1a2b7b0e"
      },
      "metadata": {
        "epoch": 676,
        "refundAmount": {
          "value": "-500000000",
          "currency": { "symbol": "ADA", "decimals": 6 }
        }
      }
    }
  ]
}
```

  </TabItem>
  <TabItem value="registration-response" label="Pool Registration Response">

```json
{
  "transaction_identifier": {
    "hash": "dcbff41c50c5b4012d49be5be75b11a0c5289515258ef4cf108eb6ec4ed5f37a"
  },
  "operations": [
    {
      "operation_identifier": { "index": 2 },
      "type": "poolRegistration",
      "status": "success",
      "account": {
        "address": "503c82138b10d84b0ba36ff2e7342ea7fc40c57498dbc6fafe0cd322"
      },
      "metadata": {
        "depositAmount": {
          "currency": {
            "decimals": 6,
            "symbol": "ADA"
          },
          "value": "500000000"
        },
        "poolRegistrationParams": {
          "rewardAddress": "e08a1766394908dedeb89d2d47673cc1851140acceaa0746a5e870eae2",
          "cost": "340000000",
          "margin_percentage": "0.08",
          "pledge": "799450000000",
          "poolOwners": [
            "8a1766394908dedeb89d2d47673cc1851140acceaa0746a5e870eae2"
          ],
          "relays": [
            {
              "dnsName": "relays.cardano-launchpad.chaincrucial.io",
              "ipv4": "",
              "ipv6": "",
              "port": "23001"
            }
          ],
          "vrfKeyHash": "74511e297e8d8670729af5a4eb08ff8b49f0247f1100f28ce5599b44f07b57b4"
        }
      }
    }
  ]
}
```

  </TabItem>
</Tabs>
