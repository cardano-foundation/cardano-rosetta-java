---
sidebar_position: 5
title: Catalyst Voting
description: How to participate in Catalyst voting using Cardano Rosetta API
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Catalyst Voting

## Overview

Project Catalyst is Cardano's innovation and governance program, allowing ADA holders to vote on proposals. The Rosetta API implementation supports Catalyst voting registration through a dedicated operation type.

:::info
For more information about Catalyst voting and how the data structures work, see the [CIP-15 specification](https://cips.cardano.org/cips/cip15/).
:::

## Vote Registration Operation

To register for Catalyst voting, use the `voteRegistration` operation type with the `/construction/preprocess` and `/construction/payloads` endpoints.

<Tabs>
  <TabItem value="request" label="Construction Request" default>

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "mainnet"
  },
  "operations": [
    {
      "operation_identifier": {
        "index": 1
      },
      "status": "success",
      "type": "voteRegistration",
      "metadata": {
        "voteRegistrationMetadata": {
          "rewardAddress": "addr1q9nr7yuhzsmmdchsjacuqef5cnlajk2s4j2rjre5uzgmtw5vcjwuayent36vkw403c8hat9csyawfgg88qlwweyeshnqsh9r5q",
          "stakeKey": {
            "curve_type": "edwards25519",
            "hex_bytes": "56f29f391a3bb5ff90637b2d2d0a32590214871284b0577e4671b0c1a83f79ba"
          },
          "votingKey": {
            "curve_type": "edwards25519",
            "hex_bytes": "8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a"
          },
          "votingNonce": 26912766,
          "votingSignature": "f75f7a54a79352f9d0e2c4de4e8ded8ae9304fa0f3b021754f8d149c90c7b01e1c6bbfdd623c294d82f5e5cbbfc0bd6fd1c674780db4025446e2eafc87f61b0a"
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
  <TabItem value="metadata-structure" label="Metadata Structure">

The registration data is included in the transaction metadata with the following structure:

```json
{
  "61284": {
    // voting_key - CBOR byte array
    "1": "0xa6a3c0447aeb9cc54cf6422ba32b294e5e1c3ef6d782f2acff4a70694c4d1663",
    // stake_pub - CBOR byte array
    "2": "0xad4b948699193634a39dd56f779a2951a24779ad52aa7916f6912b8ec4702cee",
    // reward_address - CBOR byte array
    "3": "0x00588e8e1d18cba576a4d35758069fe94e53f638b6faf7c07b8abd2bc5c5cdee47b60edc7772855324c85033c638364214cbfc6627889f81c4",
    // nonce
    "4": 5479467
  },
  "61285": {
    // signature - ED25119 signature CBOR byte array
    "1": "0x8b508822ac89bacb1f9c3a3ef0dc62fd72a0bd3849e2381b17272b68a8f52ea8240dcc855f2264db29a8512bfcd522ab69b982cb011e5f43d0154e72f505f007"
  }
}
```

  </TabItem>
  <TabItem value="response" label="Data API Response">

Vote registration operations are returned in `/block` and `/block/transaction` endpoints:

```json
{
  "transaction_identifier": {
    "hash": "adeb7b6845f3f4b0e74275588412cf00912b615e4bbf76d111326ce899260c59"
  },
  "operations": [
    {
      "operation_identifier": {
        "index": 1
      },
      "status": "success",
      "type": "voteRegistration",
      "metadata": {
        "voteRegistrationMetadata": {
          "rewardAddress": "addr1q9nr7yuhzsmmdchsjacuqef5cnlajk2s4j2rjre5uzgmtw5vcjwuayent36vkw403c8hat9csyawfgg88qlwweyeshnqsh9r5q",
          "stakeKey": {
            "curve_type": "edwards25519",
            "hex_bytes": "56f29f391a3bb5ff90637b2d2d0a32590214871284b0577e4671b0c1a83f79ba"
          },
          "votingKey": {
            "curve_type": "edwards25519",
            "hex_bytes": "8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a"
          },
          "votingNonce": 26912766,
          "votingSignature": "f75f7a54a79352f9d0e2c4de4e8ded8ae9304fa0f3b021754f8d149c90c7b01e1c6bbfdd623c294d82f5e5cbbfc0bd6fd1c674780db4025446e2eafc87f61b0a"
        }
      }
    }
  ]
}
```

  </TabItem>
</Tabs>

## Required Parameters

The `voteRegistrationMetadata` field requires the following parameters:

| Parameter         | Description                                                  |
| ----------------- | ------------------------------------------------------------ |
| `rewardAddress`   | Staking address which will receive voting rewards            |
| `stakeKey`        | Public key of the corresponding staking account (hex string) |
| `votingKey`       | Catalyst voting public key (hex string)                      |
| `votingNonce`     | Current slot number                                          |
| `votingSignature` | Vote data signed with the staking private key (hex string)   |

## Generating the Voting Signature

The voting signature is created by:

1. Taking the voting data (corresponding to the "61284" field in the metadata)
2. Calculating its blake2b-256 hash
3. Signing this hash with the staking private key
