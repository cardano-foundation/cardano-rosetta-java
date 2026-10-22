---
sidebar_position: 3
title: Querying a Smart Wallet Address
sidebar_label: Querying a Smart Wallet Address
description: Read CIP-113 programmable tokens with the standard Rosetta Data API endpoints
---

# Querying a Smart Wallet Address

Once you have the customer’s smart wallet address, query it like any other Cardano address.

| Need | Endpoint | Send |
| --- | --- | --- |
| Aggregate ADA and token balances | `/account/balance` | Smart wallet address |
| List the UTxOs at the address | `/account/coins` | Smart wallet address |
| Find transactions involving the address | `/search/transactions` | Smart wallet address |
| Inspect one transaction | `/block/transaction` | Transaction hash |

:::important Query both addresses
A smart wallet balance includes the assets locked at that script address. It does not include assets held at the customer’s regular address.
:::

## Balance

Send the smart wallet address as `account_identifier.address`.

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "preprod"
  },
  "account_identifier": {
    "address": "addr_test1zzl9nam4pfweg7akf8ns646dqenermp55807u2sg0jz3rc6as3x55d8yw38nxlpxn3ysgqzlyj334dcj3h9lv5nhakcsudhvdm"
  }
}
```

The response contains the block the balance was read at and one balance entry per currency:

```json
{
  "block_identifier": {
    "index": 5317690,
    "hash": "f3d1a03283bd1089ae95b9d1d0a4f1db71020e40ac537f4a2a5f9d513f1b2fe8"
  },
  "balances": [
    {
      "value": "19100000",
      "currency": { "symbol": "ADA", "decimals": 6 }
    },
    {
      "value": "7",
      "currency": {
        "symbol": "524550",
        "decimals": 0,
        "metadata": {
          "policyId": "ba74882344a7946a04139263c4896703a7107f522bbf061c311aac6c"
        }
      }
    },
    {
      "value": "78",
      "currency": {
        "symbol": "524550",
        "decimals": 0,
        "metadata": {
          "policyId": "5568d71be0b1614f2942f0efbfd98c4d01a2eabe1d97ddf65b92ef86"
        }
      }
    },
    {
      "value": "7",
      "currency": {
        "symbol": "524550",
        "decimals": 0,
        "metadata": {
          "policyId": "c4e75d70e0df490e4f208681889f62615bc19a89490f81caa39e86db"
        }
      }
    }
  ]
}
```

The response includes ADA and three assets with the same asset name, `524550` (hex for `REP`). Their policy IDs distinguish them.

## Coins and transactions

`/account/coins` returns the UTxOs at the smart wallet address. Token quantities for each UTxO are included in its metadata, using the same native-token representation described in the [Native Tokens guide](../multi-assets.md).

`/search/transactions` finds transactions involving the address. Use `/block/transaction` to inspect a specific transaction. The usual Rosetta operation rules apply:

- Inputs have negative amounts and `coin_spent`.
- Outputs have positive amounts and `coin_created`.
- Native assets appear in operation `metadata.tokenBundle`.
- A token’s `currency.symbol` is its hex-encoded asset name.
- `currency.metadata.policyId` identifies the policy.
- Registry-provided decimals are returned in `currency.decimals`.

CIP-113 uses zero-ADA withdrawals to invoke some validators. Rosetta omits all zero-ADA withdrawals from `withdrawal` operations in `/block`, `/block/transaction` and `/search/transactions` because they do not change balances. The remaining operations describe the ADA and token movement.

## Is a token programmable?

Rosetta reports programmable tokens like other native assets and does not mark them with a separate flag. To determine whether a policy is registered as programmable, inspect the on-chain CIP-113 registry with CIP-113 tooling or another indexer. Rosetta does not expose that registry.

## Common mistakes

- Querying the regular address instead of the smart wallet address returns no programmable-token balance.
- Deriving with a payment key when the sender used a stake key—or the reverse—produces a different address and no error.
- Identifying an asset by symbol alone is unsafe; use policy ID and asset name together.
- Waiting for `withdrawal` operations of 0 ADA: Rosetta does not return them.

## References

- [Native Tokens guide](../multi-assets.md)
- [Token Metadata guide](../../advanced-configuration/token-metadata.md)
- [CIP-113 specification](https://cips.cardano.org/cip/CIP-0113)
- [Data API reference](/cardano-rosetta-java/api)
