---
sidebar_position: 2
title: Cardano API Additions
description: Documentation for Cardano-specific API extensions
---

# Cardano API Additions

Although `Cardano Rosetta Java` is compliant with [Rosetta Spec](https://docs.cloud.coinbase.com/rosetta/docs/welcome),
some changes were added, mostly as metadata, as they contain Cardano specific information that needs to be either processed or returned.

To keep it easy, clear and upgradable for changes in the future, all specific metadata are added at the end of the API documentation.

To get a detailed view of the API and the changes, refer to the Rosetta API reference documentation.

## Cardano Operation Types

Operations are used to represent components of transactions across the Rosetta API. They are returned from Data API endpoints and used by Construction API endpoints to build transactions.

Cardano Rosetta supports all operations available within the Cardano blockchain:

- <a href="/cardano-rosetta-java/api#model/Operation" target="_blank">`Input`</a> - Represents ADA or tokens being spent from a UTXO
- <a href="/cardano-rosetta-java/api#model/Operation" target="_blank">`Output`</a> - Represents ADA or tokens being sent to an address
- <a href="/cardano-rosetta-java/api#model/OperationMetadata" target="_blank">`Stake_Key_Registration`</a> - Registers a stake key for staking
- <a href="/cardano-rosetta-java/api#model/OperationMetadata" target="_blank">`Stake_Key_Deregistration`</a> - Deregisters a stake key
- <a href="/cardano-rosetta-java/api#model/OperationMetadata" target="_blank">`Stake_Delegation`</a> - Delegates a stake to a pool
- <a href="/cardano-rosetta-java/api#model/OperationMetadata" target="_blank">`Withdrawal`</a> - Withdraws rewards from a reward account
- <a href="/cardano-rosetta-java/api#model/PoolRegistrationParams" target="_blank">`Pool_Registration`</a> - Registers a stake pool
- <a href="/cardano-rosetta-java/api#model/OperationMetadata" target="_blank">`Pool_Retirement`</a> - Retires a stake pool
- <a href="/cardano-rosetta-java/api#model/VoteRegistrationMetadata" target="_blank">`Vote_Registration`</a> - Registers for voting (Catalyst)
- <a href="/cardano-rosetta-java/api#model/DRepParams" target="_blank">`dRepVoteDelegation`</a> - Delegates voting power to a Delegated Representative (DRep) for Cardano governance

To support these operations, extra metadata is added to the standard Rosetta operation structure:

```json
{
  "withdrawalAmount": { "type": "Amount" },
  "depositAmount": { "type": "Amount" },
  "refundAmount": { "type": "Amount" },
  "staking_credential": { "type": "PublicKey" },
  "pool_key_hash": { "type": "string" },
  "epoch": { "type": "number" },
  "tokenBundle": { "type": "TokenBundleItem" },
  "poolRegistrationCert": { "type": "string" },
  "poolRegistrationParams": { "type": "PoolRegistrationParams" },
  "voteRegistrationMetadata": { "type": "VoteRegistrationMetadata" },
  "drep": { "type": "DRepObject" }
}
```

## Endpoint specific changes

### `/search/transactions`

Added `block_identifier` this enables the user to search for transactions within a particular block. Either index or hash must be set.
If both are set from a different block, the returned list will be empty.

```json
{
  "block_identifier": {
    "hash": "XXXXXX",
    "index": "000"
  }
}
```

This API can be disabled by setting `DISABLE_SEARCH_API` env variable to `t`, `true` or 1.

Max amount of transactions allowed to be requested is defined by `PAGE_SIZE` env variable, which is the same used at `/block/transaction` endpoint. Also, this value will be used if no limit parameter is received.

:::info
`status` and `success` filters are equivalent. If they are both set and they don't match, an error will be thrown. In the same way works `address` and `account_identifier.address`.
`status` and `maxBlock` filters work as excluding filters, if they are set, besides operator value.
:::

### `/block`

The following metadata is also returned, when querying for block information:

```json
{
  "transactionsCount": { "type": "number" },
  "createdBy": { "type": "string" },
  "size": { "type": "number" },
  "epochNo": { "type": "number" },
  "slotNo": { "type": "integer", "format": "int64" }
}
```

For transaction fields for the `size` and `scriptSize` are added to Transaction metadata.

```json
{
  "size": { "type": "integer", "format": "int64" },
  "scriptSize": { "type": "integer", "format": "int64" }
}
```

### `/block/transactions`

When the block requested contains transactions with multi assets operations, the token bundles associated to each operation will be returned as metadata as follows:

```json
{
  "metadata": {
    "tokenBundle": [
      {
        "policyId": "3e6fc736d30770b830db70994f25111c18987f1407585c0f55ca470f",
        "tokens": [
          {
            "value": "-5",
            "currency": {
              "symbol": "6a78546f6b656e31",
              "decimals": 0
            }
          }
        ]
      }
    ]
  }
}
```

:::info
Assets will be returned sorted by name.
:::

### `/account/balance`

For accounts that have a multi asset balance, these will be returned with the corresponding policy passed as metadata at `currency` as follows:

```json
{
  "balances": [
    {
      "value": "4800000",
      "currency": {
        "symbol": "ADA",
        "decimals": 6
      }
    },
    {
      "value": "20",
      "currency": {
        "symbol": "",
        "decimals": 0,
        "metadata": {
          "policyId": "181aace621eea2b6cb367adb5000d516fa785087bad20308c072517e"
        }
      }
    },
    {
      "value": "10",
      "currency": {
        "symbol": "7376c3a57274",
        "decimals": 0,
        "metadata": {
          "policyId": "fc5a8a0aac159f035a147e5e2e3eb04fa3b5e67257c1b971647a717d"
        }
      }
    }
  ]
}
```

Also, `coins` will be returned with the token bundle list corresponding to each coin as metadata.

:::info Note on Stake Addresses
When the `/account/balance` endpoint is queried with a stake address (also known as a reward address), the response will include the available rewards that can be withdrawn from the stake address.

This means the API provides a consolidated view of both spendable funds (from payment addresses) and claimable rewards when a stake address is used in the query.
:::

### `/account/coins`

:::warning
`include_mempool` is an optional parameter that can be used to include mempool transactions in the response.
If not set, it will be ignored. Default is false, since mempool tracking is not activated by default as well.
**Note: Mempool functionality is not implemented yet.**
:::

### `/construction/derive`

Following the Rosetta specification, this endpoint returns an Enterprise address by default.
In addition to that, Cardano Rosetta Java allows the creation of Reward and Base addresses, which aren't supported in the Rosetta specification.
Therefore, following optional parameters were added as metadata:

- `address_type`: Either `Reward`, `Base` or `Enterprise`. It will default to `Enterprise` and will throw an error if any other value is provided.
- `staking_credential`: The public key that will be used for creating a Base address. This field is only mandatory if the provided `address_type` is `Base`. It's ignored in other cases since the Reward and the Enterprise addresses are created with the public key already included in the request.

#### Examples

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="base" label="Base address">

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "public_key": {
    "hex_bytes": "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Base",
    "staking_credential": {
      "hex_bytes": "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760",
      "curve_type": "edwards25519"
    }
  }
}
```

  </TabItem>
  <TabItem value="reward" label="Reward address">

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "public_key": {
    "hex_bytes": "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Reward"
  }
}
```

  </TabItem>
  <TabItem value="enterprise" label="Enterprise address">

In this case the metadata is optional. If it's provided, then the `address_type` should be `Enterprise` and any `staking_credential` will be ignored.

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "public_key": {
    "hex_bytes": "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Enterprise"
  }
}
```

  </TabItem>
</Tabs>

### `/construction/preprocess`

Cardano transactions require a `ttl` (Time-to-live) to be defined. As it's explained in the Cardano docs, TTL represents a slot, or deadline by which a transaction must be submitted. The TTL is an absolute slot number, rather than a relative one, which means that the `ttl` value should be greater than the current slot number. A transaction becomes invalid once its ttl expires.

There are several restrictions that require a more complex workflow when defining `ttl` for a transaction:

- `ttl` depends on the latest block slot number and Rosetta spec only allows online data to be fetched in `/construction/metadata`.
- `/construction/metadata` only accepts parameters produced, without any modifications, by `/construction/preprocess`.

To be able to stay compliant with Rosetta spec but also let the user define a specific `ttl`, a new optional parameter `relative_ttl` within the metadata was introduced. If not set, a `DEFAULT_RELATIVE_TTL` will be used.

Additionally, deposit parameters can be added to the metadata, which are used to take staking and pool operations into account while calculating the transaction size.

#### Example metadata

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "operations": [...],
  "metadata": {
    "relative_ttl": "100",
    "deposit_parameters": {
      "poolDeposit": "500000000",
      "keyDeposit": "2000000"
    }
  }
}
```

### `/construction/metadata`

Metadata endpoint needs to receive the `relative_ttl` returned in process so it can calculate the actual `ttl` based on latest block slot number.

```json
{
  "metadata": {
    "ttl": "65294",
    "suggested_fee": [
      {
        "currency": {
          "decimals": 6,
          "symbol": "ADA"
        },
        "value": "900000"
      }
    ]
  }
}
```

### `/construction/payloads`

Not only input and output operations are allowed but also special staking operations. Furthermore, transaction `ttl` needs to be sent as string in the metadata.

```json
{
  "metadata": {
    "ttl": "65294"
  }
}
```

### `/construction/parse`

The request of this endpoint has no specific change but the response will have the operations parsed in the same way as the ones that are used to send as payload in the `/construction/payloads` and `/construction/preprocess` endpoints. This means that the order used in those two endpoints needs to be maintained exactly, otherwise the parse endpoint will not be able to reproduce the operations in the same order and the workflow will fail.

### `/construction/combine`

In order to support Byron addresses an extra field called `chain_code` in the `account_identifier`'s `metadata` of the corresponding `signing_payload` must be added when requesting to sign payloads. This value can be obtained by any of the Bip 32 Keys.

```json
{
  "signatures": [
    {
      "signing_payload": {
        "account_identifier": {
          "address": "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx",
          "metadata": {
            "chain_code": "dd75e154da417becec55cdd249327454138f082110297d5e87ab25e15fad150f"
          }
        },
        "hex_bytes": "31fc9813a71d8db12a4f2e3382ab0671005665b70d0cd1a9fb6c4a4e9ceabc90",
        "signature_type": "ed25519"
      },
      "public_key": {
        "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
        "curve_type": "edwards25519"
      },
      "signature_type": "ed25519",
      "hex_bytes": "00000000000000000000000000"
    }
  ]
}
```

## Other considerations

### Encoded transactions

Both `signed_unsigned` and `unsigned_transaction` don't correspond to a valid Cardano Transaction that can be forwarded to the network as they contain extra data required in the Rosetta workflow. This means that such transactions cannot be decoded nor sent directly to a `cardano-node`.

Transaction's metadata, needed for example for vote registration operations, is also encoded as extra data.

There is no expectation that the transactions which are constructed in Rosetta can be parsed by network-specific tools or broadcast on a non-Rosetta node. All parsing and broadcast of these transactions will occur exclusively over the Rosetta API.
