# Rosetta API Construction Integration Test Validator

A integration testing tool for Rosetta API construction endpoints. It validates live API responses against both:
- Snapshot expectations stored in JSON test files, and
- The OpenAPI schema (when available) for structural correctness.

## What is Snapshot Testing?

This validator implements snapshot testing - it compares actual API responses against previously captured "snapshots" of expected behavior. When the API behavior changes, tests fail, alerting you to potential regressions or the need to update snapshots.

## Quick Start

### Option 1: Basic Mode (No Environment Setup)
Run snapshot testing without schema validation - no dependencies needed:

```bash
# Test all files with default parallel execution (10 workers)
python3 test_construction_api.py

# Test specific endpoint with more workers for faster execution
python3 test_construction_api.py parse/ -j 20

# Note: Schema validation will be skipped in this mode
```

### Option 2: Full Mode with uv (Includes Schema Validation)
For full features including OpenAPI schema validation:

```bash
# Install uv if you don't have it
curl -LsSf https://astral.sh/uv/install.sh | sh

# Run the validator with full features (uv handles environment automatically)
uv run test_construction_api.py

# Run with specific options and increased parallelism
uv run test_construction_api.py parse/ -v -j 15
uv run test_construction_api.py -o results.txt -j 20
```

That's it! The `uv run` command automatically:
- Creates a virtual environment (if needed)
- Installs all dependencies (pyyaml, jsonschema)
- Runs the script with full schema validation enabled

## Features

- **Parallel execution**: Run tests in parallel with configurable workers (10x+ speedup)
- **Snapshot testing**: Detects API response changes against stored expectations
- **Schema validation**: Validates responses against OpenAPI spec (when using uv environment)
- **Two modes**: Basic (Python only) or Full (with uv for schema validation)
- **CLI interface**: Flexible path/pattern matching
- **Detailed error reporting**: Shows actual API error details
- **HTTP status validation**: Robust error detection using status codes
- **File output**: Save results to file with `-o` option
- **Dynamic field handling**: Ignores expected volatile fields (TTL, signatures)
- **Execution timing**: Total test suite execution time tracking

## Detailed Usage Examples

### With uv (Full Features)
```bash
# Test all files with schema validation
uv run test_construction_api.py

# Test specific endpoint
uv run test_construction_api.py parse/

# Test with wildcards
uv run test_construction_api.py "parse/native_assets/*.json"
uv run test_construction_api.py "*/withdrawals/*.json"

# Test specific file
uv run test_construction_api.py parse/native_assets/multiple_assets_different_policies.json

# Run with increased parallelism (20 workers instead of default 10)
uv run test_construction_api.py -j 20

# Run single-threaded (useful for debugging)
uv run test_construction_api.py -j 1

# Output to file
uv run test_construction_api.py -o results.txt

# Custom API URL
uv run test_construction_api.py -u http://localhost:8080

# Verbose output
uv run test_construction_api.py -v    # verbose prints schema details per test

# Combine options
uv run test_construction_api.py parse/ -v -u http://localhost:8080 -o results.txt -j 15
```

### Without uv (Basic Mode)
```bash
# Same commands work with python3 directly (no schema validation)
python3 test_construction_api.py
python3 test_construction_api.py parse/ -v
python3 test_construction_api.py --no-schema  # explicitly skip schema
python3 test_construction_api.py -j 20  # parallel execution with 20 workers
```

## CLI Options

- `[paths/patterns...]` - Test file paths or patterns (positional, optional)
- `-o <file>` - Output results to file instead of stdout
- `-u <url>` - Rosetta API URL (default: `http://localhost:8082`)
- `-v, --verbose` - Show detailed output for each test
- `-j, --workers <n>` - Number of parallel workers for test execution (default: 10)
- `--openapi <path>` - OpenAPI spec file (YAML or JSON). Defaults to `api.yaml` next to the script
- `--no-schema` - Disable schema validation (snapshot-only)
- `--schema-details` - Force schema summary (redundant with `-v`, which enables it)

## Requirements

- Python 3.6+ 
- Running Rosetta API (configure via `-u` option)
- Test files in JSON format
- For schema validation: `uv` (installs pyyaml & jsonschema automatically)

## Test File Structure

Each test file should contain:
- `request_body`: The API request payload
- Either `expected_response` (success) or `expected_error` (failure)
- Test metadata (name, description)

## Exit Codes

- `0`: All tests passed
- `1`: One or more tests failed or connection error

## What It Validates

The script validates all Rosetta construction flow endpoints:
- `/construction/derive`
- `/construction/preprocess`
- `/construction/metadata`
- `/construction/payloads`
- `/construction/parse`
- `/construction/combine`
- `/construction/hash`
- `/construction/submit`

For each test, it compares the actual API response against the expected response, accounting for dynamic fields like TTL and signatures.

If schema validation is enabled and the OpenAPI spec is available, it also validates the structure of the response (and error responses) against the schema. Schema failures are reported alongside snapshot diffs and will mark the test as failed.
