# Data Endpoints Tests

Python test suite for Rosetta data endpoints, migrated from Postman collection.

## Setup

```bash
# Install uv if not already installed
curl -LsSf https://astral.sh/uv/install.sh | sh

# Sync dependencies
uv sync
```

## Running Tests

**Note**: Parallel execution (`-n auto`) is enabled by default in pytest.ini

```bash
# Recommended: Run smoke tests first, then behavioral tests
uv run pytest test_smoke_network_data.py && uv run pytest -m "not smoke"

# Run all tests (includes smoke tests, runs in parallel by default)
uv run pytest

# Run only behavioral tests (skip smoke tests - for CI)
uv run pytest -m "not smoke"

# Run only smoke tests (validate test data)
uv run pytest -m smoke

# Run with verbose output
uv run pytest -v

# Run sequentially (disable default parallelization)
uv run pytest -n 0

# Run specific test class
uv run pytest test_search_transactions.py::TestPagination -v

# Run specific test file
uv run pytest test_block_endpoints.py -v

# Run with Allure reporting
uv run pytest --alluredir=./allure-results
allure serve allure-results
```

## Environment Variables

### Test Execution Variables
- `ROSETTA_URL`: Base URL for Rosetta API (default: `http://localhost:8082`)
- `CARDANO_NETWORK`: Network to test against (default: `preprod`, options: `mainnet`, `preview`)

### Configuration Variables (Read from .env file)
Tests read the actual service configuration from `.env` file in this directory:
- `REMOVE_SPENT_UTXOS`: Whether pruning is enabled (`true`/`false`)
- `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT`: Grace window in blocks (default: `2160`)
- `TOKEN_REGISTRY_ENABLED`: Whether token metadata is enabled (`true`/`false`)

**How it works**:
- Workflows create `.env` by merging `.env.docker-compose-preprod` + `.env.docker-compose-profile-mid-level`
- Tests auto-load `.env` via `conftest.py` at import time
- Configuration detection is based on actual env vars, not API inference

```bash
# Run against mainnet (requires mainnet test data in network_test_data.yaml)
CARDANO_NETWORK=mainnet uv run pytest -m "not smoke"

# Run against different port
ROSETTA_URL=http://localhost:8083 uv run pytest

# Test with specific configuration (create .env file)
echo "REMOVE_SPENT_UTXOS=true" > .env
echo "TOKEN_REGISTRY_ENABLED=true" >> .env
uv run pytest -m nightly
```

## Test Organization

**130+ total tests** organized across 8 files:

### Core Behavioral Tests (113 tests, 19 properly skipped)
- `test_network_endpoints.py` - /network/* endpoints (3 tests)
- `test_search_transactions.py` - /search/transactions (72 tests including parametrized)
- `test_block_endpoints.py` - /block and /block/transaction (18 tests)
- `test_account_endpoints.py` - /account/balance and /account/coins (13 tests)
- `test_error_handling.py` - Cross-cutting error tests for all 7 endpoints (14 tests)

### v1.4.0 Feature Tests
- `test_pruning_behavior.py` - Validates UTXO-based pruning behavior (3 tests)
- `test_token_registry.py` - Token metadata enrichment tests (4 tests)

### Smoke Tests (12 tests)
- `test_smoke_network_data.py` - Validates network_test_data.yaml entries across endpoints

### Test Data
- `network_test_data.yaml` - Network-specific addresses and assets (extensible for mainnet)

## Pruning Compatibility

Tests automatically adapt to pruned instances (where `REMOVE_SPENT_UTXOS=true`):

- **Auto-detection**: Tests detect pruning via `oldest_block_identifier` in `/network/status`
- **Relaxed validation**: Schema validation makes `address` field optional in `account` objects for pruned responses
- **Conditional tests**: Tests marked `@pytest.mark.requires_full_history` are intended for full-history environments (PR runs); skip them manually with `-m "not requires_full_history"` when testing against a pruned instance

### Understanding Pruning

When pruning is enabled, **spent UTXOs** are removed to save disk space:

- **Inputs**: Always spent (by definition), so always show `account: {}` and `amount: "0"` in old blocks
- **Outputs**: Only pruned if spent later; unspent outputs preserve full data even in old blocks
- **Boundary**: `oldest_block_identifier` marks where ALL data is complete
- **Current state**: `/account/balance` and `/account/coins` work normally (unspent UTXOs preserved)

For detailed pruning behavior, see `test_pruning_behavior.py`.

## Test Markers

Tests are organized with markers for different execution contexts:

### Execution Tier Markers
```bash
# PR workflow - fast essential tests (minutes)
pytest -m pr

# Nightly workflow - comprehensive validation (hours)
pytest -m nightly

# Weekly workflow - sync validation (many hours)
pytest -m weekly
```

### Configuration Requirement Markers
```bash
# Tests requiring full historical data (skip on pruned instances)
pytest -m requires_full_history

# Tests compatible with both pruned and non-pruned instances
pytest -m pruning_compatible

# Tests requiring token registry enabled
pytest -m requires_token_registry

# Tests requiring peer discovery enabled
pytest -m requires_peer_discovery
```

### Special Markers
```bash
# Smoke tests that validate test data
pytest -m smoke

# Slow tests (>5 seconds)
pytest -m slow
```

## Features

- **Schema Validation**: Automatic validation against OpenAPI spec using Draft4Validator
- **Pruning Detection**: Auto-adapts to pruned instances with relaxed validation
- **Clear Test Names**: Each test function clearly describes what it tests
- **Parameterization**: Uses pytest parametrize for test variations
- **Allure Integration**: Beautiful HTML reports with test history
- **Parallel Execution**: Run tests in parallel with pytest-xdist

## Quick Test

```bash
# Run a quick sanity check
uv run pytest test_search_transactions.py::TestSanityChecks::test_default_pagination_returns_100_transactions -v
```
