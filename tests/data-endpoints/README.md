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

- `ROSETTA_URL`: Base URL for Rosetta API (default: `http://localhost:8082`)
- `CARDANO_NETWORK`: Network to test against (default: `preprod`, options: `mainnet`, `preview`)

```bash
# Run against mainnet (requires mainnet test data in network_test_data.yaml)
CARDANO_NETWORK=mainnet uv run pytest -m "not smoke"

# Run against different port
ROSETTA_URL=http://localhost:8083 uv run pytest
```

## Test Organization

**125 total tests** organized across 6 files:

### Behavioral Tests (113 tests, 19 properly skipped)
- `test_network_endpoints.py` - /network/* endpoints (3 tests)
- `test_search_transactions.py` - /search/transactions (72 tests including parametrized)
- `test_block_endpoints.py` - /block and /block/transaction (18 tests)
- `test_account_endpoints.py` - /account/balance and /account/coins (13 tests)
- `test_error_handling.py` - Cross-cutting error tests for all 7 endpoints (14 tests)

### Smoke Tests (12 tests)
- `test_smoke_network_data.py` - Validates network_test_data.yaml entries across endpoints

### Test Data
- `network_test_data.yaml` - Network-specific addresses and assets (extensible for mainnet)

## Features

- **Schema Validation**: Automatic validation against OpenAPI spec using Draft4Validator
- **Clear Test Names**: Each test function clearly describes what it tests
- **Parameterization**: Uses pytest parametrize for test variations
- **Allure Integration**: Beautiful HTML reports with test history
- **Parallel Execution**: Run tests in parallel with pytest-xdist

## Quick Test

```bash
# Run a quick sanity check
uv run pytest test_search_transactions.py::TestSanityChecks::test_default_pagination_returns_100_transactions -v
```