---
sidebar_position: 3
title: DRep Delegation
description: Documentation for Delegated Representative (DRep) voting in Cardano Rosetta Java
---

# DRep Delegation

:::info Version Compatibility
DRep delegation is only available in Cardano Rosetta Java version 1.2.4 and later.
:::

Delegated Representatives (DReps) are a key feature of Cardano's governance system introduced in [CIP-1694](https://cips.cardano.org/cip/CIP-1694). DReps allow ADA holders to delegate their voting power to trusted representatives who can vote on governance actions on their behalf, enabling broader participation in the governance process.

## Construction API

In the request for `/construction/preprocess` and `/construction/payloads` endpoints, the operation type `dRepVoteDelegation` is used to delegate voting power to a Delegated Representative (DRep) for Cardano governance. This operation allows users to participate in Cardano's governance system without having to vote directly on every proposal.

## DRep Delegation Types

The Cardano Rosetta Java implementation supports four different types of DRep delegations:

1. **Abstain** - Explicitly abstain from voting
2. **No Confidence** - Express no confidence in the current governance system
3. **Key Hash** - Delegate to a specific DRep identified by a key hash
4. **Script Hash** - Delegate to a DRep managed by a script (identified by script hash)

## DRep Vote Delegation Operations

A DRep vote delegation operation requires a staking credential and the DRep information. The staking key must be registered before delegation.

:::info CIP-129 Hash Support
[CIP-129](https://cips.cardano.org/cip/CIP-0129) defines a tagged format for hashes where the first byte indicates the credential type (e.g., DRep key hash or script hash).

**cardano-rosetta-java now accepts both formats:**
- **29-byte hash** (with CIP-129 prefix): The prefix will be automatically stripped internally
- **28-byte hash** (raw, without prefix): Standard format

**Important:** The `type` field is always required regardless of hash format.

_Examples:_
- With prefix: `"id": "0374984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1"` (prefix `03` will be stripped)
- Without prefix: `"id": "74984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1"`

Both examples require `"type": "key_hash"` to be specified.

The API automatically detects the format and handles the prefix internally. When returning data, the API maintains the 28-byte format with separate type field for consistency.
:::

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

When delegating to a specific DRep with a key hash, you must provide the **DRep's ID** (as a 28-byte raw hash, see notice above).

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

Similar to key hash delegation, but delegating to a DRep managed by a script. This requires the **script hash as the ID** (as a 28-byte raw hash, see notice above).

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

## Usage Notes

1. The stake key must be registered before performing a DRep delegation.
2. The operation includes a transaction fee but no deposit is required.
3. DRep delegation can be combined with other operations in a single transaction.

## Data API

:::note
The Data API support for DRep vote delegation is only available in version 2.X and above (currently a work in progress).
:::

DRep vote delegation operations are also returned in `/block` and `/block/transaction` endpoints when applicable. Since a DRep delegation is included in the transaction as a certificate, every transaction that contains a DRep delegation certificate will include a corresponding DRep vote delegation operation in the API response.

### Response Example

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

## Staking Workflow

A typical workflow for DRep delegation might include:

1. Register a stake key (`stakeKeyRegistration` operation)
2. Delegate to a stake pool (`stakeDelegation` operation)
3. Delegate to a DRep (`dRepVoteDelegation` operation)
4. Update DRep delegation as needed with new `dRepVoteDelegation` operations
5. Optionally deregister the stake key when done (`stakeKeyDeregistration` operation)

## Related Cardano Improvement Proposals

The DRep delegation functionality is based on the following Cardano Improvement Proposals:

- [CIP-1694](https://cips.cardano.org/cip/CIP-1694) - Introduced the concept of Delegated Representatives (DReps).
- [CIP-0129](https://cips.cardano.org/cip/CIP-0129) - Defines Conway era serialization formats, including the DRep ID prefix handling discussed earlier.
