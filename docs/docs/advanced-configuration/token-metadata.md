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

### Why Exchanges Need This

Without metadata enrichment, native tokens in API responses appear only as hex-encoded policy IDs and asset names. With enrichment enabled:

- **User experience** — readable token names instead of hex strings
- **Accurate information** — correct decimals, tickers, and descriptions
- **Trust** — verified metadata from the official Cardano registry
- **Compliance** — proper token identification for regulatory requirements

### What This Integration Provides

When enabled, the following endpoints include enriched metadata in their `currency` objects:

- `/block` — block retrieval with transaction details
- `/block/transaction` — individual transaction details
- `/account/balance` — account balance information
- `/account/coins` — UTXO details for accounts
- `/search/transactions` — transaction search results

#### Metadata Fields Added

| Field | Description | Example |
|-------|-------------|---------|
| `subject` | Base16-encoded `policyId + assetName` | `"5dac8536…4e4d4b52"` |
| `name` | Human-readable token name | `"MKT coin"` |
| `description` | Token description | `"Utility token for …"` |
| `ticker` | Token ticker/symbol | `"MKT"` |
| `url` | Project website URL | `"https://example.com"` |
| `decimals` | Number of decimal places | `6` |
| `logo` | Logo payload — base64 (CIP-26) or URL (CIP-68); returned only when `TOKEN_REGISTRY_LOGO_FETCH=true` | `{ "format": "url", "value": "ipfs://…" }` |
| `version` | CIP-68 metadata version | `1` |

When both standards describe the same token, **CIP-68 takes priority** field-by-field — any field left unset in CIP-68 falls back to the CIP-26 value.

#### Before and After Examples

**Before** (enrichment disabled):
```json
{
  "currency": {
    "symbol": "567946695f43726564656e7469616c",
    "decimals": 0,
    "metadata": {
      "policyId": "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5"
    }
  }
}
```

**After** (enrichment enabled):
```json
{
  "currency": {
    "symbol": "567946695f43726564656e7469616c",
    "decimals": 0,
    "metadata": {
      "policyId": "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5",
      "subject": "4d07e0ceae00e6c53598cea00a53c54a94c6b6aa071482244cc0adb5567946695f43726564656e7469616c",
      "name": "MKT coin",
      "description": "MKT description of token coin",
      "ticker": "MKT",
      "url": "https://example.com"
    }
  }
}
```

:::note Optional Metadata
All metadata fields are optional. If a token has no CIP-26 registry entry and no CIP-68 reference NFT, only `policyId` is returned. Missing metadata is normal — many tokens are simply unregistered.
:::

## Architecture

Metadata enrichment is fully database-backed — there is no runtime HTTP call to any external registry from the API hot path.

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
     │   - ft_offchain_metadata   (CIP-26 fields)                       │
     │   - ft_offchain_logo       (CIP-26 logos)                        │
     │   - metadata_reference_nft (CIP-68 reference NFT datums)         │
     └────────────────────────────────┬─────────────────────────────────┘
                                      │
                                      ▼
                            ┌─────────────────────┐
                            │ Rosetta API         │
                            │ (TokenQueryService) │
                            │ reads DB at request │
                            │ time, merges CIP-68 │
                            │ over CIP-26         │
                            └─────────────────────┘
```

**Key properties:**
- The `yaci-store-assets-ext` module runs inside the `yaci-indexer` and populates the three tables above. It is **always enabled** in the bundled `yaci-indexer` — you do not need to install or configure a separate service.
- Initial CIP-26 sync from GitHub typically takes a few minutes after the indexer starts. CIP-68 data is indexed continuously as blocks are processed.
- The Rosetta API reads these tables in a constant number of batched queries per request (one CIP-26 metadata query, optionally one CIP-26 logo query, and one CIP-68 query only if the request contains CIP-68 fungible tokens).

## Enabling Token Metadata

Enrichment is controlled entirely from the API side with two environment variables.

### Step 1: Update the Rosetta API environment

Edit your environment file (e.g. `.env.docker-compose`, `.env.docker-compose-preprod`, or your Kubernetes values file):

```bash
# Enable metadata enrichment in API responses (default: false)
TOKEN_REGISTRY_ENABLED=true

# Optionally include logos in responses. Logos can be large (base64-encoded
# PNGs for CIP-26) and significantly increase payload size, so this is off
# by default. Enable only if your downstream consumer needs them.
TOKEN_REGISTRY_LOGO_FETCH=false
```

:::note Only these two flags
The legacy `TOKEN_REGISTRY_BASE_URL`, `TOKEN_REGISTRY_CACHE_TTL_HOURS`, and `TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS` variables are **no longer used** — the API no longer calls a remote registry at request time. If they are still set in your environment, they are simply ignored and can be removed.
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

The `yaci-indexer` does not need to be restarted — it is already indexing the assets-ext tables regardless of whether the API is configured to read from them.

### Step 3: Verify Enrichment

Query a block that is known to contain native tokens and inspect a `currency.metadata` object:

```bash
curl -X POST http://localhost:8082/block \
  -H "Content-Type: application/json" \
  -d '{
    "network_identifier": { "blockchain": "cardano", "network": "mainnet" },
    "block_identifier":   { "index": 10000259 }
  }' | jq '.block.transactions[].operations[]
           | select(.metadata.tokenBundle)
           | .metadata.tokenBundle[].tokens[0].currency.metadata'
```

For a registered token you should see something like:

```json
{
  "policyId":    "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc92843489",
  "subject":     "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc928434894e4d4b52",
  "name":        "NMKR",
  "description": "Utility Token for Tokenization & NFT Infrastructure by NMKR",
  "ticker":      "NMKR",
  "url":         "https://nmkr.io"
}
```

:::tip Testing Other Endpoints
All endpoints that return `currency` objects (`/account/balance`, `/account/coins`, `/search/transactions`, `/block/transaction`) will include the same enriched metadata.
:::

## Troubleshooting

### API Responses Don't Include Metadata

**Symptom:** `currency.metadata` contains only `policyId`.

**Check 1 — Flag is on:**
```bash
docker exec cardano-rosetta-java-api-1 \
  env | grep TOKEN_REGISTRY_ENABLED
```
Expected: `TOKEN_REGISTRY_ENABLED=true`. If `false` or unset, update the env file and restart the API.

**Check 2 — The token is actually registered:**
Tokens with no CIP-26 registry entry and no CIP-68 reference NFT will correctly return only `policyId`. Pick a well-known registered token (for example NMKR or HOSKY on mainnet) to confirm enrichment works end-to-end before investigating a specific unregistered token.

**Check 3 — Indexer has finished its initial CIP-26 sync:**
```bash
docker exec cardano-rosetta-java-db-1 \
  psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM ft_offchain_metadata;"
```
On a freshly-started indexer this may be `0` for a few minutes while the assets-ext module downloads and processes the GitHub registry snapshot. Wait for a non-zero count before assuming enrichment is broken.

### Logos Are Missing

**Symptom:** `currency.metadata.logo` is never populated.

- Confirm `TOKEN_REGISTRY_LOGO_FETCH=true` is set on the API and the API has been restarted.
- For CIP-26 tokens, also verify the logo table has rows:
  ```bash
  docker exec cardano-rosetta-java-db-1 \
    psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM ft_offchain_logo WHERE logo IS NOT NULL;"
  ```
- For CIP-68 tokens, the logo value is taken directly from the on-chain reference NFT datum and will be `null` whenever the issuer didn't include one.

### Slower Responses After Enabling

The extra DB queries are batched (1–3 round trips per API request, independent of batch size) and hit indexed columns, so the overhead is typically small. If you see a meaningful regression:

- Disable logo fetching (`TOKEN_REGISTRY_LOGO_FETCH=false`) — it saves one query and avoids shipping base64 blobs over the wire.
- Check the indexer's PostgreSQL is healthy (`pg_stat_activity`, `pg_stat_statements`) — a struggling indexer DB also slows the API's metadata lookups.

## Further Reading

- [CIP-26 — Cardano Off-Chain Metadata](https://cips.cardano.org/cip/CIP-0026)
- [CIP-68 — On-Chain Datum Metadata Standard](https://cips.cardano.org/cip/CIP-0068)
- [Cardano Token Registry (GitHub)](https://github.com/cardano-foundation/cardano-token-registry)
- [yaci-store assets-ext module](https://github.com/bloxbean/yaci-store)
