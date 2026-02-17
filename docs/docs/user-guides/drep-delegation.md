---
sidebar_position: 3
title: DRep Delegation
description: Documentation for Delegated Representative (DRep) voting in Cardano Rosetta Java
---

# DRep Delegation

:::info Version Compatibility
DRep delegation is only available in Cardano Rosetta Java version 1.2.4 and later.
:::

A **DRep** (Delegated Representative) is an entity defined in [CIP-1694](https://cips.cardano.org/cip/CIP-1694) that can vote on your behalf in Cardano governance.

This guide shows how to build and read `dRepVoteDelegation` operations in Cardano Rosetta Java, including how delegation state affects reward withdrawal flows.

## What this operation does

Use operation type `dRepVoteDelegation` in `/construction/preprocess` and `/construction/payloads` when you want to delegate governance voting power.

At a high level, you provide:
- a staking credential (who is delegating), and
- DRep information (who receives the delegation, or a special delegation mode like `abstain`).

## When to use each delegation mode

The Cardano Rosetta Java implementation supports four different types of DRep delegations:

1. **Abstain** - Explicitly abstain from voting
2. **No Confidence** - Express no confidence in the current governance system
3. **Key Hash** - Delegate to a specific DRep identified by a key hash
4. **Script Hash** - Delegate to a DRep managed by a script (identified by script hash)

<details>
<summary><strong>DRep ID Format for key_hash/script_hash (CIP-129)</strong></summary>

:::info
[CIP-129](https://cips.cardano.org/cip/CIP-0129) defines a tagged id format. In practice, this means a DRep id may include a prefix byte that already tells the id kind (`key_hash` or `script_hash`).

**Accepted formats**
- **29-byte id** with CIP-129 prefix (`0x22` key hash, `0x23` script hash)
- **28-byte id** without prefix (raw hash)

**`type` rules**
- Prefixed id: `type` is optional (inferred from prefix). If provided, it must match the prefix.
- Raw 28-byte id: `type` is required (`key_hash` or `script_hash`).
- API responses use 28-byte ids with a separate `type` field.

_Examples:_
- With prefix (key hash): `"id": "22abcdef0123456789abcdef0123456789abcdef0123456789abcdef01"`
- With prefix (script hash): `"id": "23abcdef0123456789abcdef0123456789abcdef0123456789abcdef01"`
- Without prefix: `"id": "abcdef0123456789abcdef0123456789abcdef0123456789abcdef01"`
:::

</details>

## Input rules

A `dRepVoteDelegation` operation requires staking credential data and DRep data.

- The stake key must be registered before delegation.
- The operation includes a transaction fee but no deposit.
- DRep delegation can be combined with other operations in a single transaction.

## Construction examples

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="drep-types">
  <TabItem value="abstain" label="Abstain" default>

This option explicitly abstains from voting. **No DRep ID is required.**

```json
{
  "operation_identifier": {
    "index": 0
  },
  "type": "dRepVoteDelegation",
  "account": {
    "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
  },
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    },
    "drep": {
      "type": "abstain"
    }
  }
}
```

  </TabItem>
  <TabItem value="no_confidence" label="No Confidence">

This option expresses no confidence in the current governance system. **No DRep ID is required.**

```json
{
  "operation_identifier": {
    "index": 0
  },
  "type": "dRepVoteDelegation",
  "account": {
    "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
  },
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    },
    "drep": {
      "type": "no_confidence"
    }
  }
}
```

  </TabItem>
  <TabItem value="key_hash" label="Key Hash">

When delegating to a specific DRep with a key hash, provide the DRep id in either supported format (CIP-129 prefixed or raw 28-byte).

```json
{
  "operation_identifier": {
    "index": 0
  },
  "type": "dRepVoteDelegation",
  "account": {
    "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
  },
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    },
    "drep": {
      "id": "74984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1",
      "type": "key_hash"
    }
  }
}
```

  </TabItem>
  <TabItem value="script_hash" label="Script Hash">

Similar to key hash delegation, but delegating to a DRep managed by a script. The id can be CIP-129 prefixed or raw 28-byte.

```json
{
  "operation_identifier": {
    "index": 0
  },
  "type": "dRepVoteDelegation",
  "account": {
    "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
  },
  "metadata": {
    "staking_credential": {
      "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
      "curve_type": "edwards25519"
    },
    "drep": {
      "id": "41868c2b4e5289022a3a1f6f47f86823bc605c609d2c47a2db58e04a",
      "type": "script_hash"
    }
  }
}
```

  </TabItem>
</Tabs>

## Data API representation

:::note
Data API support for DRep vote delegation is available in version 2.1.0 and later.
:::

DRep vote delegation operations are also returned in `/block` and `/block/transaction` endpoints when applicable. Since a DRep delegation is included in the transaction as a certificate, every transaction that contains a DRep delegation certificate will include a corresponding DRep vote delegation operation in the API response.

### Response examples

<Tabs groupId="drep-response-types">
  <TabItem value="abstain-response" label="Abstain" default>

```json
{
  "transaction_identifier": {
    "hash": "dcbff41c50c5b4012d49be5be75b11a0c5289515258ef4cf108eb6ec4ed5f37a"
  },
  "operations": [
    // other operations
    {
      "operation_identifier": {
        "index": 3
      },
      "type": "dRepVoteDelegation",
      "account": {
        "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
      },
      "metadata": {
        "staking_credential": {
          "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
          "curve_type": "edwards25519"
        },
        "drep": {
          "type": "abstain"
        }
      }
    }
    // other operations here
  ]
}
```

  </TabItem>
  <TabItem value="no-confidence-response" label="No Confidence">

```json
{
  "transaction_identifier": {
    "hash": "dcbff41c50c5b4012d49be5be75b11a0c5289515258ef4cf108eb6ec4ed5f37a"
  },
  "operations": [
    // other operations
    {
      "operation_identifier": {
        "index": 3
      },
      "type": "dRepVoteDelegation",
      "account": {
        "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
      },
      "metadata": {
        "staking_credential": {
          "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
          "curve_type": "edwards25519"
        },
        "drep": {
          "type": "no_confidence"
        }
      }
    }
    // other operations here
  ]
}
```

  </TabItem>
  <TabItem value="key-hash-response" label="Key Hash">

```json
{
  "transaction_identifier": {
    "hash": "dcbff41c50c5b4012d49be5be75b11a0c5289515258ef4cf108eb6ec4ed5f37a"
  },
  "operations": [
    // other operations
    {
      "operation_identifier": {
        "index": 3
      },
      "type": "dRepVoteDelegation",
      "account": {
        "address": "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5"
      },
      "metadata": {
        "staking_credential": {
          "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
          "curve_type": "edwards25519"
        },
        "drep": {
          "id": "74984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1",
          "type": "key_hash"
        }
      }
    }
    // other operations here
  ]
}
```

  </TabItem>
</Tabs>

## DRep Delegation Workflow

A typical DRep delegation workflow includes:

1. Ensure the stake key is registered (`stakeKeyRegistration` if needed).
2. Submit a `dRepVoteDelegation` operation with one supported delegation mode.
3. Verify the applied delegation in Data API responses (`/block` or `/block/transaction`).
4. Update delegation later by submitting a new `dRepVoteDelegation` operation (for example, changing DRep or setting `abstain`/`no_confidence`).

## Related CIPs

- [CIP-1694](https://cips.cardano.org/cip/CIP-1694): DRep governance model.
- [CIP-0129](https://cips.cardano.org/cip/CIP-0129): prefixed hash format used by DRep ids.
