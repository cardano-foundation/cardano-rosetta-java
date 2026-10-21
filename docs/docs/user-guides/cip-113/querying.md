---
sidebar_position: 3
title: Querying a Programmable Address
sidebar_label: Querying a Programmable Address
description: Read the programmable tokens held at a CIP-113 programmable address with the standard Rosetta Data API endpoints
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Querying a Programmable Address

## Overview

Programmable tokens need **no new Data API endpoints**. Once you have the customer's [programmable address](./addresses.md), you query it like any other address, because the indexer stores every address as it appears on chain. Balances, UTxOs, transactions, operations and token names work as described in [Native Tokens](../multi-assets.md) and [Enabling Token Metadata](../../advanced-configuration/token-metadata.md). This page covers only what is specific to programmable addresses.

| Question                                       | Endpoint                                     | What to send         |
| ---------------------------------------------- | -------------------------------------------- | -------------------- |
| How many tokens does the customer hold?        | `/account/balance`                           | Programmable address |
| Which UTxOs hold them?                         | `/account/coins`                             | Programmable address |
| Which transactions moved them?                 | `/search/transactions`                       | Programmable address |
| What does one transaction contain?             | `/block/transaction`, `/search/transactions` | Transaction hash     |

:::important Two independent addresses per customer
The enterprise address holds ADA and regular native tokens. The programmable address holds programmable tokens (and the minimum ADA that travels with them). Query both. They are separate accounts and there is no linking between them.
:::

## Example: balance of a programmable address

Send the programmable address as `account_identifier.address`. The example is a real preprod programmable address, queried at block `5317690`:

<Tabs>
  <TabItem value="request" label="Request" default>

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

  </TabItem>
  <TabItem value="response" label="Response">

```json
{
  "block_identifier": {
    "index": 5317690,
    "hash": "f3d1a03283bd1089ae95b9d1d0a4f1db71020e40ac537f4a2a5f9d513f1b2fe8"
  },
  "balances": [
    {
      "value": "19100000",
      "currency": {
        "symbol": "ADA",
        "decimals": 6
      }
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

  </TabItem>
</Tabs>

The address holds 19.1 ADA (the minimum ADA that travels with its 13 outputs) and tokens of three programmable token policies. All three use the same asset name, `524550` (`REP`), so each entry is told apart by its `policyId`. The token registry has no metadata for them, so `decimals` is `0` and there is no name or ticker.

`/account/coins` and `/search/transactions` take the same address and return the usual responses. The programmable tokens appear in the same `tokenBundle` metadata as any other native asset.

:::note Balances are snapshots
Programmable tokens are ordinary on-chain assets, and their holders move them. Re-running the request later returns different numbers. The `block_identifier` in the response tells you which block the answer is for.
:::

## Is a token programmable?

Rosetta reports a programmable token like any other native asset, and does not mark it as programmable. To find out, look up the policy ID in the on-chain **registry**: if a registry entry with that policy ID as key exists, the token is programmable, and the entry names the module that governs it. Registry entries can be updated in place by the token's issuer, so resolve the current entry at the time you need it instead of caching it.

Rosetta does not expose the registry, so this lookup is done with the CIP-113 tooling or an indexer that reads the registry. Most exchanges only need it when they list a new policy.

## Pitfalls

| Pitfall                                                 | Explanation and fix |
| ------------------------------------------------------- | ------------------- |
| Querying the enterprise address for programmable tokens | They are not there. Query the programmable address. |
| Wrong credential convention gives a zero balance        | If the sender used a stake key hash as owner and you derived the address from the payment key (or the reverse), the address differs and no error is returned. See [the warning](./addresses.md#the-owner-credential-rosetta-uses). |
| Treating a script owner as a person                     | A programmable address can be owned by a script (a dApp or a DAO). Its stake credential slot is a script hash. Do not assume that every owner is a wallet. |
| Identifying a token by symbol or ticker                 | Different policies can use the same asset name. Key on `policyId` plus `symbol`. The `/account/balance` `currencies` filter matches on the symbol only, so check `currency.metadata.policyId` in the result. `/account/coins` matches on policy ID and symbol. |
| Expecting zero-value withdrawals in the operations      | They are filtered. The remaining operations describe only the movement of ADA and tokens. |

## Next steps

- [Deriving and Resolving Addresses](./addresses.md): get the programmable address of a customer.
- [Native Tokens](../multi-assets.md): token operations and the sign convention.
- [Enabling Token Metadata](../../advanced-configuration/token-metadata.md): show token names, tickers and decimals.
- [API Reference](/cardano-rosetta-java/api): schemas of all Data API endpoints.
