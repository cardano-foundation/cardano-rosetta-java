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

```bash
# Run all tests
uv run pytest

# Run with verbose output
uv run pytest -v

# Run specific test class
uv run pytest test_search_transactions.py::TestPagination -v

# Run specific test
uv run pytest test_search_transactions.py::TestPagination::test_valid_limits -v

# Run in parallel (faster)
uv run pytest -n auto

# Run with Allure reporting
uv run pytest --alluredir=./allure-results

# Generate and serve Allure report (requires allure CLI)
allure serve allure-results
```

## Environment Variables

- `ROSETTA_URL`: Base URL for Rosetta API (default: `http://localhost:8082`)

```bash
# Run against different environment
ROSETTA_URL=http://localhost:8082 uv run pytest
```

## Test Organization

All 61 tests are organized in `test_search_transactions.py`:

- **TestSanityChecks**: Basic endpoint availability (3 tests)
- **TestPaginationLimits**: Limit parameter tests (7 tests)
- **TestPaginationOffsets**: Offset parameter tests (6 tests)
- **TestTransactionIdentifier**: Transaction hash filtering (4 tests)
- **TestAccountIdentifier**: Address filtering (6 tests)
- **TestMaxBlock**: Block height filtering (9 tests)
- **TestStatusFiltering**: Status and success filtering (5 tests)
- **TestOperationTypeFiltering**: Operation type filtering (9 tests)
- **TestLogicalOperators**: AND/OR operator tests (7 tests)
- **TestCurrencyFiltering**: Currency filtering (5 tests)

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