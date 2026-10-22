---
sidebar_position: 2
title: Deriving and Resolving Addresses
sidebar_label: Deriving and Resolving Addresses
description: Derive or resolve a CIP-113 smart wallet address with Cardano Rosetta Java
---

# Deriving and Resolving Addresses

Programmable tokens are held in script-locked UTxOs at a smart wallet address. Cardano Rosetta Java provides two ways to obtain this address:

| Starting with | Use | Result |
| --- | --- | --- |
| A public key | `/construction/derive` with `address_type: "CIP-113"` | A smart wallet address using the public-key hash as owner |
| An enterprise or base address | `/call` with `resolve_smart_wallet_addr` | A smart wallet address using the appropriate key credential |

Configure `CIP113_BASE_SCRIPT_HASH` for the network in `network_identifier` before using either endpoint. See [Environment Variables](../../install-and-deploy/env-vars.md) for the deployed network values.

## The owner credential Rosetta uses

Rosetta builds the address as follows:

```text
smart wallet address = PLB script payment credential + owner key stake credential
```

The owner credential is selected from the input:

| Input | Owner credential |
| --- | --- |
| Public key (`/construction/derive`) | Hash of the public key |
| Enterprise address (`/call`) | Payment key hash |
| Base address (`/call`) | Stake key hash |
| Smart wallet address (`/call`) | Existing stake key hash; returned unchanged |

Use the returned smart wallet address for deposits and withdrawals as follows:

- **Deposits**: store and credit the exact address returned by `/construction/derive`.
- **Withdrawals**: when a customer gives you a base address, `/call` returns the smart wallet address of its stake key. Confirm with the customer that this is their intended smart wallet address before sending programmable tokens to it.

## Deriving an address (`/construction/derive`)

Set `metadata.address_type` to `"CIP-113"` and send the public key used for the customer’s enterprise address.

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "preprod"
  },
  "public_key": {
    "hex_bytes": "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "CIP-113"
  }
}
```

The response contains the smart wallet address in `account_identifier.address`:

```json
{
  "account_identifier": {
    "address": "addr_test1zzl9nam4pfweg7akf8ns646dqenermp55807u2sg0jz3rcamgrc6v3au3rqm66mn3kuwke340kfxga82tl7kh2nke8as2mmcxg"
  }
}
```

Rules:

- `address_type` is case-sensitive. Valid values include `Enterprise`, `Base`, `Reward` and `CIP-113`.
- `staking_credential` is not allowed with `CIP-113`; sending it returns `5062`.
- `hex_bytes` must contain a public key encoded as 64 hexadecimal characters, or 128 for an extended public key. Supply the key itself, not its hash, without a `0x` prefix.
- `curve_type` may be omitted, but if supplied it must be `edwards25519`.
- The result is a base address with the PLB script as payment credential and a key stake credential.

## Resolving an address (`/call`)

Call `resolve_smart_wallet_addr` when you have an address but not its public key.

```json
{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "preprod"
  },
  "method": "resolve_smart_wallet_addr",
  "parameters": {
    "address": "addr_test1vpwcgn22xnj8gnen0snfcjgyqp0jfgc6kufgmjlk2fm7mvg5ys3p3"
  }
}
```

The result contains `account_identifier.address` and `idempotent: true`:

```json
{
  "result": {
    "account_identifier": {
      "address": "addr_test1zzl9nam4pfweg7akf8ns646dqenermp55807u2sg0jz3rc6as3x55d8yw38nxlpxn3ysgqzlyj334dcj3h9lv5nhakcsudhvdm"
    }
  },
  "idempotent": true
}
```

The method validates the address network before returning a result. Rosetta supports smart wallet addresses with a key hash in the stake component. Addresses with a script hash in that component are unsupported.

## Recommended integration flow

1. For a new deposit account, call `/construction/derive` with the customer’s deposit public key and store the returned smart wallet address alongside the regular address.
2. For a customer-provided address, call `/call` and use the returned smart wallet address as the destination for programmable-token tooling. For a base address, confirm that the result is the customer’s intended smart wallet address first (see [above](#the-owner-credential-rosetta-uses)).
3. Query the smart wallet address separately from the regular address.
4. Treat the smart wallet address as a normal Cardano address when calling the Data API.

Rosetta does not build the programmable-token transfer itself. It provides the address and reports confirmed on-chain activity.

## Common errors

| Code | Meaning | Fix |
| --- | --- | --- |
| `5062` | Staking credential supplied with CIP-113 derive | Remove `metadata.staking_credential`. |
| `5063` | PLB script hash is not configured | Set `CIP113_BASE_SCRIPT_HASH`. |
| `5064` | PLB script hash is invalid | Supply exactly 56 hexadecimal characters. |
| `5065` | Script payment credential cannot be mapped to a key owner. This includes enterprise script-payment addresses and base addresses with a non-PLB script payment. | Use an enterprise key address or a base address with the configured PLB and key stake credential. |
| `5066` | Address type is unsupported. This includes reward, pointer, Byron, and base addresses with a script hash in the stake component, including smart wallet addresses with that credential type. | Use an enterprise key address or a base address with a key hash in the stake component and a payment credential that is either a key hash or the configured PLB script hash. |
| `5067` | Address is malformed or on the wrong network | Check the address and `network_identifier`. |
| `5051` | Call address parameter is missing or invalid | Send a non-empty string in `parameters.address`. |
| `4007` / `4016` | Invalid public key or address type | Check `hex_bytes`, `curve_type` and the case-sensitive address type. |

See `/network/options` and the [API Reference](/cardano-rosetta-java/api) for the complete error definitions.

## References

- [CIP-113 specification](https://cips.cardano.org/cip/CIP-0113)
- [CIP-113 platform](https://github.com/cardano-foundation/cip113-programmable-tokens-platform)
- [Construction API reference](/cardano-rosetta-java/api)
