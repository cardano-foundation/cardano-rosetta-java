---
sidebar_position: 4
title: Enabling Token Metadata
description: Configure native asset metadata enrichment (CIP-26 and CIP-68)
---

# Enabling Token Metadata

## Overview

Cardano native tokens (multi-assets) can have associated metadata that provides human-readable information such as names, tickers, descriptions, decimal precision, and logos. Exposing this metadata in API responses helps exchanges and wallets display accurate token information instead of raw hex-encoded policy IDs.

Cardano Rosetta Java merges metadata from two complementary sources:

- **CIP-26** — the off-chain token metadata registry maintained on the [Cardano Foundation GitHub repository](https://github.com/cardano-foundation/cardano-token-registry). Used for static information about fungible tokens.
- **CIP-68** — an on-chain metadata standard where a reference NFT (prefix `000643b0`) holds the metadata for its sibling fungible token (prefix `0014df10`).

### Decimals vs enrichment — two different things

Rosetta's `Currency` object has two places where token information lands, and they're governed by different rules:

| Wire field | Scope | Always populated? |
|------------|-------|-------------------|
| `currency.decimals` | Mandatory cross-chain Rosetta field — clients expect every token to have a numeric decimal count. | **Yes, always.** Resolved from CIP-26 / CIP-68 when available; falls back to `0` otherwise. |
| `currency.metadata.{subject, name, description, ticker, url, logo, version}` | Cardano-specific enrichment. Convenient but not part of the core Rosetta cross-chain contract. | Only when `TOKEN_REGISTRY_ENABLED=true`. |
| `currency.metadata.policyId` | Needed to disambiguate native assets on Cardano. | **Yes, always** (regardless of the flag). |

On most Rosetta-supported chains, `decimals` is a native property of the asset and there's no registry at all. On Cardano, native tokens don't advertise decimals on-chain, so we always consult the yaci-store assets-ext tables to find the right value — even for deployments that don't want to expose the rest of the enrichment to API clients.

### Why Exchanges Need This

Without metadata enrichment, native tokens in API responses appear only as hex-encoded policy IDs and asset names. With enrichment enabled:

- **User experience** — readable token names instead of hex strings
- **Accurate information** — correct tickers and descriptions
- **Trust** — verified metadata from the official Cardano registry
- **Compliance** — proper token identification for regulatory requirements

Decimals are always correct regardless of the flag, so exchanges that prefer to render native-asset columns minimally can keep enrichment off and still rely on Rosetta reporting the right decimal precision for each token.

### What the Enrichment Flag Controls

When `TOKEN_REGISTRY_ENABLED=true`, the following endpoints include enriched metadata in their `currency.metadata` objects:

- `/block` — block retrieval with transaction details
- `/block/transaction` — individual transaction details
- `/account/balance` — account balance information
- `/account/coins` — UTXO details for accounts
- `/search/transactions` — transaction search results

#### Metadata Fields Added

| Field | Gated by `TOKEN_REGISTRY_ENABLED` | Description | Example |
|-------|:-:|-------------|---------|
| `policyId` | — (always present) | 56-hex-char minting policy ID | `"4d07e0ce…"` |
| `subject` | yes | Base16-encoded `policyId + assetName` | `"5dac8536…4e4d4b52"` |
| `name` | yes | Human-readable token name | `"MKT coin"` |
| `description` | yes | Token description | `"Utility token for …"` |
| `ticker` | yes | Token ticker/symbol | `"MKT"` |
| `url` | yes | Project website URL | `"https://example.com"` |
| `logo` | yes — and only when `TOKEN_REGISTRY_LOGO_FETCH=true` | Logo payload — base64 (CIP-26) or URL (CIP-68) | `{ "format": "url", "value": "ipfs://…" }` |
| `version` | yes | CIP-68 metadata version | `1` |

Separately on `currency.decimals` (not inside `metadata`):

| Wire field | Description | Example |
|------------|-------------|---------|
| `currency.decimals` | Always present, always correct — reflects CIP-26/CIP-68 value or `0` fallback | `6` |

When both standards describe the same token, **CIP-68 takes priority** field-by-field — any field left unset in CIP-68 falls back to the CIP-26 value.

#### Before and After Examples

**Flag off, token has CIP-26 decimals:**
```json
{
  "currency": {
    "symbol":   "567946695f43726564656e7469616c",
    "decimals": 6,
    "metadata": {
      "policyId": "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5"
    }
  }
}
```
Note `decimals: 6` — the right value is still returned, just the human-readable enrichment is suppressed.

**Flag on, same token:**
```json
{
  "currency": {
    "symbol":   "567946695f43726564656e7469616c",
    "decimals": 6,
    "metadata": {
      "policyId":    "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5",
      "subject":     "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5567946695f43726564656e7469616c",
      "name":        "MKT coin",
      "description": "MKT description of token coin",
      "ticker":      "MKT",
      "url":         "https://example.com"
    }
  }
}
```

**Flag on, unregistered token:**
```json
{
  "currency": {
    "symbol":   "deadbeef",
    "decimals": 0,
    "metadata": {
      "policyId": "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5"
    }
  }
}
```
Decimals falls back to `0` because neither CIP-26 nor CIP-68 has a row for this token. The other fields are absent for the same reason.

:::note Optional Metadata
All enrichment fields are optional even when the flag is on — registered tokens populate what they've published, unregistered tokens simply leave them null. Missing metadata is not an error.
:::

## Architecture

Metadata enrichment is fully database-backed — there is no runtime HTTP call to any external registry from the API hot path. The database lookup always runs (so `decimals` can always be resolved correctly), and the `TOKEN_REGISTRY_ENABLED` flag is applied at the serialization boundary.

```
┌─────────────────────┐     ┌──────────────────────┐     ┌──────────────────┐
│ Cardano token       │     │ On-chain             │     │ Cardano          │
│ registry (GitHub)   │     │ reference NFTs       │     │ blockchain       │
│  CIP-26 metadata    │     │  (CIP-68 datums)     │     │                  │
└──────────┬──────────┘     └──────────┬───────────┘     └────────┬─────────┘
           │                           │                          │
           │  periodic sync            │  indexed as blocks        │
           ▼                           ▼  are processed            ▼
     ┌──────────────────────────────────────────────────────────────────┐
     │  yaci-indexer (yaci-store assets-ext module, always on)          │
     │   - ft_offchain_metadata   (CIP-26 fields, incl. decimals)       │
     │   - ft_offchain_logo       (CIP-26 logos)                        │
     │   - metadata_reference_nft (CIP-68 reference NFT datums)         │
     └────────────────────────────────┬─────────────────────────────────┘
                                      │
                                      ▼
                    ┌─────────────────────────────────────┐
                    │ Rosetta API                         │
                    │   TokenQueryService  (always runs)  │ → decimals always resolved
                    │   DataMapper         (flag applied) │ → enrichment suppressed
                    │                                       when TOKEN_REGISTRY_ENABLED=false
                    └─────────────────────────────────────┘
```

**Key properties:**
- The `yaci-store-assets-ext` module runs inside the `yaci-indexer` and populates the three tables above. It is **always enabled** in the bundled `yaci-indexer` — you do not need to install or configure a separate service.
- Initial CIP-26 sync from GitHub typically takes a few minutes after the indexer starts. CIP-68 data is indexed continuously as blocks are processed.
- The Rosetta API reads these tables in a constant number of batched queries per request (one CIP-26 metadata query, optionally one CIP-26 logo query, and one CIP-68 query only if the request contains CIP-68 fungible tokens).
- The flag only controls serialization; it does not skip DB reads. Decimals must be resolved correctly even when the flag is off.

## Enabling Token Metadata

Enrichment is controlled entirely from the API side with two environment variables.

### Step 1: Update the Rosetta API environment

Edit your environment file (e.g. `.env.docker-compose`, `.env.docker-compose-preprod`, `.env.docker-compose-preview`, or your Kubernetes values file):

```bash
# Expose CIP-26/CIP-68 enrichment fields (subject, name, description, ticker,
# url, logo, version) in currency.metadata. Default: false.
# Note: this does NOT affect currency.decimals — decimals is always resolved
# correctly regardless of this flag.
TOKEN_REGISTRY_ENABLED=true

# Optionally include logos in the enrichment output. Logos can be large
# (base64-encoded PNGs for CIP-26) and significantly increase payload size,
# so this is off by default. Only has an effect when TOKEN_REGISTRY_ENABLED=true.
TOKEN_REGISTRY_LOGO_FETCH=false
```

#### Recommended defaults per network

| Network | `TOKEN_REGISTRY_ENABLED` | `TOKEN_REGISTRY_LOGO_FETCH` | Template file |
|---------|:-:|:-:|----|
| mainnet | `false` | `false` | `.env.docker-compose` |
| preprod | `true`  | `true`  | `.env.docker-compose-preprod` |
| preview | `true`  | `true`  | `.env.docker-compose-preview` |

Preprod and preview default to `true` because they're the networks where exchanges exercise integration scenarios including CIP-68 tokens, and having the full metadata on the wire accelerates verification. Mainnet defaults to `false` so the flag is an explicit opt-in for production deployments.

:::note Legacy flags
The variables `TOKEN_REGISTRY_BASE_URL`, `TOKEN_REGISTRY_CACHE_TTL_HOURS`, and `TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS` are **no longer used** — the API no longer makes HTTP calls to a remote registry. If still set in your environment, they are simply ignored and can be removed.
:::

### Step 2: Restart the Rosetta API

```bash
docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-mid-level \
  -f docker-compose.yaml \
  down api

docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-mid-level \
  -f docker-compose.yaml \
  up -d api
```

:::tip Hardware Profiles
Adjust the profile file (`entry-level`, `mid-level`, or `advanced-level`) to match your deployment. See [Hardware Profiles](../install-and-deploy/hardware-profiles) for details.
:::

The `yaci-indexer` does not need to be restarted — it indexes the assets-ext tables regardless of the API's enrichment setting.

### Step 3: Verify

Query a block that is known to contain native tokens and inspect a `currency` object:

```bash
curl -X POST http://localhost:8082/block \
  -H "Content-Type: application/json" \
  -d '{
    "network_identifier": { "blockchain": "cardano", "network": "mainnet" },
    "block_identifier":   { "index": 10000259 }
  }' | jq '.block.transactions[].operations[]
           | select(.metadata.tokenBundle)
           | .metadata.tokenBundle[].tokens[0].currency'
```

For a registered token with enrichment on you should see:

```json
{
  "symbol":   "4e4d4b52",
  "decimals": 0,
  "metadata": {
    "policyId":    "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc92843489",
    "subject":     "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc928434894e4d4b52",
    "name":        "NMKR",
    "description": "Utility Token for Tokenization & NFT Infrastructure by NMKR",
    "ticker":      "NMKR",
    "url":         "https://nmkr.io"
  }
}
```

With enrichment off, the same token returns the correct `decimals` but only `policyId` under `metadata`.

:::tip Testing Other Endpoints
All endpoints that return `currency` objects (`/account/balance`, `/account/coins`, `/search/transactions`, `/block/transaction`) follow the same rules: decimals always correct, enrichment gated.
:::

## Troubleshooting

### Decimals are wrong (e.g. `0` when it should be `6`)

**Symptom:** `currency.decimals` is `0` for a token that should have a non-zero value.

- Confirm the token has a CIP-26 entry: `SELECT decimals FROM ft_offchain_metadata WHERE subject = '<policyId||assetName>'`.
- If the row is missing, the assets-ext module hasn't synced that subject yet. Initial GitHub sync takes a few minutes after indexer startup.
- If the row exists but `decimals` is `null`, the token issuer did not register a decimal count — `0` is the correct fallback in that case.
- For CIP-68 fungible tokens, check `metadata_reference_nft` with the `000643b0` prefix in `asset_name` — the reference NFT holds the decimals.

### Enrichment fields are missing (metadata only has `policyId`)

**Symptom:** `currency.metadata` contains only `policyId`, even for well-known tokens.

**Check 1 — the flag is on:**
```bash
docker exec cardano-rosetta-java-api-1 \
  env | grep TOKEN_REGISTRY_ENABLED
```
Expected: `TOKEN_REGISTRY_ENABLED=true`. If `false` or unset, update the env file and restart the API.

**Check 2 — the token is actually registered.** Unregistered tokens correctly return only `policyId`. Test with a known registered token (e.g. NMKR, HOSKY on mainnet) to confirm the end-to-end path works.

**Check 3 — the indexer has finished its initial CIP-26 sync:**
```bash
docker exec cardano-rosetta-java-db-1 \
  psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM ft_offchain_metadata;"
```
On a freshly-started indexer this may be `0` for a few minutes.

### Logos Are Missing

**Symptom:** `currency.metadata.logo` is never populated.

- Confirm both flags are set: `TOKEN_REGISTRY_ENABLED=true` **and** `TOKEN_REGISTRY_LOGO_FETCH=true`, then restart the API.
- For CIP-26 tokens, verify the logo table has rows:
  ```bash
  docker exec cardano-rosetta-java-db-1 \
    psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM ft_offchain_logo WHERE logo IS NOT NULL;"
  ```
- For CIP-68 tokens, the logo value is taken directly from the on-chain reference NFT datum and will be `null` whenever the issuer didn't include one.

### Slower Responses After Enabling Enrichment

The DB lookups run either way — turning the flag on only affects serialization, not query volume. So the latency impact of the flag itself should be negligible. If responses slow down after turning enrichment on:

- Disable logo fetching (`TOKEN_REGISTRY_LOGO_FETCH=false`) — it saves one DB query and avoids shipping base64 blobs over the wire. This is the biggest contributor to response size.
- Check the indexer's PostgreSQL is healthy (`pg_stat_activity`, `pg_stat_statements`) — a struggling indexer DB also slows the API's metadata lookups.

## Further Reading

- [CIP-26 — Cardano Off-Chain Metadata](https://cips.cardano.org/cip/CIP-0026)
- [CIP-68 — On-Chain Datum Metadata Standard](https://cips.cardano.org/cip/CIP-0068)
- [Cardano Token Registry (GitHub)](https://github.com/cardano-foundation/cardano-token-registry)
- [yaci-store assets-ext module](https://github.com/bloxbean/yaci-store)
