---
sidebar_position: 4
title: Enabling Token Metadata
description: Configure token registry integration for native asset metadata
---

# Enabling Token Metadata

## Overview

Cardano native tokens (also called multi-assets) can have associated metadata that provides human-readable information such as token names, tickers, descriptions, and logos. This metadata helps exchanges display accurate token information to users and improves the overall user experience when working with Cardano native assets.

Cardano Rosetta Java supports integration with the **Cardano Token Metadata Registry**, which provides a unified API for retrieving token metadata from two complementary standards:

- **CIP-26**: Off-chain metadata registry for static token information
- **CIP-68**: On-chain metadata standard for dynamic token information

### Why Exchanges Need This

Without token metadata integration, native assets in API responses appear only as hex-encoded policy IDs and asset names. With the token registry enabled:

- **User Experience**: Display readable token names instead of hex strings
- **Accurate Information**: Show correct decimals, tickers, and descriptions for tokens
- **Trust**: Present verified token metadata from the official Cardano registry
- **Compliance**: Provide proper token identification for regulatory requirements

### What This Integration Provides

When enabled, the following endpoints will include enriched metadata for native tokens in their `currency` objects:

- `/block` - Block retrieval with transaction details
- `/block/transaction` - Individual transaction details
- `/account/balance` - Account balance information
- `/account/coins` - UTXO details for accounts
- `/search/transactions` - Transaction search results

#### Metadata Fields Added

The integration adds the following optional fields to the `currency.metadata` object:

| Field | Description | Example |
|-------|-------------|---------|
| `subject` | Base16-encoded combination of policyId + assetName | `"5dac8536...4e4d4b52"` |
| `name` | Human-readable token name | `"MKT coin"` |
| `description` | Token description | `"Utility Token for..."` |
| `ticker` | Token ticker/symbol | `"MKT"` |
| `url` | Project website URL | `"https://example.com"` |
| `decimals` | Number of decimal places | `6` |

#### Before and After Examples

**Before** (without token metadata):
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

**After** (with token metadata enabled):
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
All metadata fields are optional. If a token has not been registered in the Cardano Token Registry, only the basic `policyId` will be present. Missing metadata does not indicate an error - many tokens simply don't have registered metadata.
:::

## Installation Steps

### Step 1: Clone the Token Metadata Registry

Clone the official Cardano Foundation token metadata registry repository:

```bash
git clone https://github.com/cardano-foundation/cf-token-metadata-registry.git
cd cf-token-metadata-registry
```

Check out the latest release tag:

```bash
# List available tags
git tag -l

# Checkout the latest stable release (replace with actual latest version)
git checkout tags/v1.x.x  # Use the latest version from the tag list
```

### Step 2: Configure Ports

Since Cardano Rosetta Java uses PostgreSQL on port 5432, configure the token registry to use different ports to avoid conflicts.

Edit the `.env` file:

```bash
# Open the .env file
nano .env  # or use your preferred editor
```

Modify the port configuration:

```bash
# Database Port - Change from default 5432 to avoid conflict
# This line:
# DB_PORT='5432'

# To:
DB_PORT='5434'

# Ensure DB_URL uses the internal port (5432 inside the container):
DB_URL=jdbc:postgresql://db:5432/cf_token_metadata_registry

# API Port - Change if port 8080 is already in use
# Default:
# API_LOCAL_BIND_PORT='8080'

# If you need a different port (e.g., if 8080 is in use):
API_LOCAL_BIND_PORT='8088'
```

### Step 3: Start the Token Registry

Start the token metadata registry service using Docker Compose:

```bash
docker compose up -d
```

This will:
- Start a PostgreSQL database container for token metadata
- Start the token registry API container
- Begin syncing token metadata from the Cardano blockchain. 
- Synchronize the official token registry from GitHub

:::tip Initial Sync Time
The first sync can take 10-12 hours as the service indexes on-chain metadata and downloads the GitHub registry. Monitor progress with `docker compose logs -f api`.
:::

### Step 4: Verify Registry Health

Wait for the token registry to complete its initial sync and become healthy:

```bash
# Check service health (use your configured API_LOCAL_BIND_PORT)
curl http://localhost:8080/health
```

Expected response when ready:
```json
{"synced":true,"syncStatus":"Sync done"}
```

You can also check the logs to monitor sync progress:

```bash
# View logs
docker compose logs -f api

# The service is ready when you see:
# "Started TokenMetadataRegistryApplication"
```

### Step 5: Get the Gateway IP Address

To allow Cardano Rosetta Java to communicate with the token registry, you need the Docker gateway IP address.

Run this command to get the gateway IP for the token registry API container:

```bash
docker inspect cf-token-metadata-registry-api-1 --format '{{range .NetworkSettings.Networks}}{{.Gateway}} {{end}}'
```

This will output something like:
```
172.20.0.1
```

### Step 6: Configure Rosetta API

Navigate to your Cardano Rosetta Java directory and edit the `.env.docker-compose` file:

```bash
cd /path/to/cardano-rosetta-java
nano .env.docker-compose
```

Add or update the following environment variables:

```bash
# Enable token registry integration
TOKEN_REGISTRY_ENABLED=true

# Set the base URL using the gateway IP from Step 5
# Replace <gateway-ip> with the actual IP from the previous step
TOKEN_REGISTRY_BASE_URL=http://<gateway-ip>:8080/api

# Optional: Configure cache TTL (in hours)
TOKEN_REGISTRY_CACHE_TTL_HOURS=12

# Optional: Disable logo fetching to reduce response size
TOKEN_REGISTRY_LOGO_FETCH=false

# Optional: Set request timeout (in seconds)
TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS=2
```

Example configuration:
```bash
TOKEN_REGISTRY_ENABLED=true
TOKEN_REGISTRY_BASE_URL=http://172.20.0.1:8080/api
TOKEN_REGISTRY_CACHE_TTL_HOURS=12
TOKEN_REGISTRY_LOGO_FETCH=false
TOKEN_REGISTRY_REQUEST_TIMEOUT_SECONDS=2
```

### Step 7: Restart Rosetta API

Apply the configuration changes by restarting the Rosetta API service:

```bash
# Stop the API
docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-mid-level \
  -f docker-compose.yaml \
  down api

# Start the API with new configuration
docker compose --env-file .env.docker-compose \
  --env-file .env.docker-compose-profile-mid-level \
  -f docker-compose.yaml \
  up -d api
```

:::tip Hardware Profiles
The command above uses the `mid-level` profile. Adjust the profile file (`entry-level`, `mid-level`, or `advanced-level`) based on your deployment configuration. See [Hardware Profiles](../install-and-deploy/hardware-profiles) for details.
:::

### Step 8: Verify Integration

Test that the token metadata integration is working correctly:

```bash
# Check API startup logs
docker logs cardano-rosetta-java-api-1 | grep TokenRegistry
```

You should see a line like:
```
TokenRegistryHttpGatewayImpl initialized with enabled: true, batchEndpointUrl: http://172.20.0.1:8080/api/v2/subjects/query
```

To verify metadata is being returned in API responses, query a block containing native tokens:

```bash
curl -X POST http://localhost:8082/block \
  -H "Content-Type: application/json" \
  -d '{
    "network_identifier": {
      "blockchain": "cardano",
      "network": "mainnet"
    },
    "block_identifier": {
      "index": 10000259
    }
  }' | jq '.block.transactions[].operations[] | select(.metadata.tokenBundle) | .metadata.tokenBundle[].tokens[0].currency.metadata'
```

You should see metadata fields populated with token information for the tokens:
```json
{
  "policyId": "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc92843489",
  "subject": "5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc928434894e4d4b52",
  "name": "NMKR",
  "description": "Utility Token for Tokenization & NFT Infrastructure by NMKR",
  "ticker": "NMKR",
  "url": "https://nmkr.io"
}
```

:::tip Testing Multiple Endpoints
You can test token metadata on other endpoints like `/account/balance`, `/account/coins`, and `/search/transactions`. All endpoints that return `currency` objects will include the enriched metadata.
:::

## Troubleshooting 

### Rosetta API Not Fetching Metadata

**Symptom**: API responses don't include token metadata

**Solutions**:

1. **Verify registry is healthy**:
   ```bash
   curl http://localhost:8080/health
   ```

2. **Check API logs for connection errors**:
   ```bash
   docker logs cardano-rosetta-java-api-1 | grep -i "token registry"
   ```

3. **Verify gateway IP is correct**:
   ```bash
   # Re-check the gateway IP
   docker inspect cf-token-metadata-registry-api-1 \
     --format '{{range .NetworkSettings.Networks}}{{.Gateway}} {{end}}'

   # Ensure it matches TOKEN_REGISTRY_BASE_URL in .env.docker-compose
   ```

4. **Test registry connectivity from API container**:
   ```bash
   docker exec cardano-rosetta-java-api-1 \
     curl -s http://<gateway-ip>:8080/health
   ```

### Slow API Response Times

**Symptom**: API requests are slower after enabling token metadata

**Solutions**:

1. **Verify Docker connectivity** between Rosetta API and token registry:
   ```bash
   # Test connectivity from API container to registry
   docker exec cardano-rosetta-java-api-1 \
     curl -s -w "\nTime: %{time_total}s\n" \
     http://<gateway-ip>:8080/health

   # Should respond quickly (< 1 second)
   ```

2. **Increase cache TTL** to reduce registry API calls:
   ```bash
   TOKEN_REGISTRY_CACHE_TTL_HOURS=24
   ```

3. **Disable logo fetching** if not needed:
   ```bash
   TOKEN_REGISTRY_LOGO_FETCH=false
   ```

## Further Reading

- [Cardano Token Registry CIP-26](https://developers.cardano.org/docs/native-tokens/token-registry/cardano-token-registry-cip26)
- [Cardano Token Registry CIP-68](https://developers.cardano.org/docs/native-tokens/token-registry/cardano-token-registry-cip68)
- [Token Registry Server Documentation](https://developers.cardano.org/docs/native-tokens/token-registry/cardano-token-registry-server)