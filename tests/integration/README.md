# Rosetta API Golden Tests

Validate live API responses against JSON snapshot expectations and the OpenAPI response schema. Run with `uv`; schema validation is required.

## What is Snapshot Testing?

This validator implements snapshot testing - it compares actual API responses against previously captured "snapshots" of expected behavior. When the API behavior changes, tests fail, alerting you to potential regressions or the need to update snapshots.

## Quick Start

From `tests/integration/`:

```bash
# Run all fixtures with 10 workers
uv run test_golden_examples.py

# Select an endpoint, with detailed output
uv run test_golden_examples.py construction/parse/ -v -j 15

# Save the report
uv run test_golden_examples.py -o results.txt -j 20
```

`uv` installs the script's declared dependencies: PyYAML, jsonschema and Rich. Missing dependencies do not trigger a reduced-functionality mode. An unreadable or invalid OpenAPI document, missing response schema or broken local reference is an error, not a reason to skip validation.

## Features

- **Parallel execution**: Run tests in parallel with configurable workers (10x+ speedup)
- **Snapshot testing**: Detects API response changes against stored expectations
- **Network flexibility**: Support for different networks via `{{networkId}}` placeholders and `--network-id` parameter
- **Schema validation**: Validates responses against the OpenAPI spec on every response
- **CLI interface**: Flexible path/pattern matching
- **Detailed error reporting**: Shows actual API error details
- **HTTP status validation**: Checks HTTP 200 for success and HTTP 400/500 according to the fixture's declared `retriable` expectation
- **File output**: Save results to file with `-o` option
- **Dynamic field handling**: Ignores expected volatile fields (TTL, signatures)
- **Execution timing**: Total test suite execution time tracking
- **CI/CD integration**: Proper exit codes for continuous integration pipelines

## Detailed Usage Examples

```bash
# Test all files with schema validation
uv run test_golden_examples.py

# Test specific endpoint
uv run test_golden_examples.py construction/parse/

# Test with wildcards
uv run test_golden_examples.py "construction/parse/native_assets/*.json"
uv run test_golden_examples.py "construction/*/withdrawals/*.json"

# Test specific file
uv run test_golden_examples.py construction/parse/native_assets/multiple_assets_different_policies.json

# Use specific network (for CI/CD with different networks)
uv run test_golden_examples.py --network-id devkit
uv run test_golden_examples.py -n preprod

# Run with increased parallelism (20 workers instead of default 10)
uv run test_golden_examples.py -j 20

# Run single-threaded (useful for debugging)
uv run test_golden_examples.py -j 1

# Output to file
uv run test_golden_examples.py -o results.txt

# Custom API URL
uv run test_golden_examples.py -u http://localhost:8080

# Verbose output
uv run test_golden_examples.py -v    # verbose prints schema details per test

# Combine options for CI/CD environment
uv run test_golden_examples.py --network-id devkit -v -u http://localhost:8080 -o results.txt -j 15
```

## CLI Options

- `[paths/patterns...]` - Test file paths or patterns (positional, optional)
- `-o <file>` - Output results to file instead of stdout
- `-u <url>` - Rosetta API URL (default: `http://localhost:8082`)
- `-n, --network-id <id>` - Network ID to replace {{networkId}} placeholders (e.g., 'devkit', 'preprod', 'mainnet'). Defaults to 'preprod' when placeholders are found.
- `-v, --verbose` - Show detailed output for each test
- `-j, --workers <n>` - Number of parallel workers for test execution (default: 10)
- `--base-dir <path>` - Fixture root, defaulting to `golden_examples/rosetta_java` beside the script
- `--openapi <path>` - OpenAPI spec file (YAML or JSON), defaulting to `api/src/main/resources/rosetta-specifications-1.4.15/api.yaml` in the repository
- `--schema-details` - Force schema summary (redundant with `-v`, which enables it)

## Requirements

- `uv` and Python 3.10+
- Running Rosetta API (configure via `-u`)
- Test files in JSON format
- The repository's OpenAPI document, or another supplied with `--openapi`

## Test File Structure

Each test file should contain:
- `request_body`: The API request payload
- Either `expected_response` (success) or `expected_error` (failure)
- Test metadata (name, description)

Existing fixtures are unchanged. For errors, compare the declared `code`, `message` and `retriable` fields. When a fixture declares `retriable`, the expected HTTP status follows `GlobalExceptionHandler`: `false` means 400 and `true` means 500. This uses the fixture's expectation, not the received retry flag. Fixtures that omit `retriable` keep their partial expectations; no new field or HTTP value is invented. Schema validation remains mandatory.

### Network-specific tests

Most goldens are network-independent: they use `{{networkId}}` in `request_body` (and in `expected_error`) and run unchanged on every network, for example on the devkit chain in CI and on preprod. The CIP-113 goldens work this way too, because `.env.IntegrationTest` configures the same CIP-113 base script hash as preprod (`CIP113_BASE_SCRIPT_HASH`), so the expected smart-wallet addresses are identical on both.

A test file that depends on real chain data may carry a top-level `"network"` field (for example `"preprod"`). When `--network-id` differs from that value (the default is `preprod`), the file is skipped and reported as `Requires network 'X', running on 'Y'`. Use it only for such tests, for example the `cip113_search_*`, `cip113_block_transaction_*` and `cip113_resolve_onchain_*` goldens, which are pinned to fixed preprod transactions and so do not drift as the chain advances.

## Exit Codes

- `0`: All tests passed
- `1`: Test failure, transport error, schema configuration error or interruption
- `2`: Invalid CLI arguments

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

For each test, it compares the actual API response against the expected response. ADA decimals and all construction-response decimals are compared. Non-ADA token decimals on data endpoints retain the registry-enrichment exception. Existing volatile metadata and per-endpoint exclusions remain unchanged.

Both success and error responses are checked against the schema. Repeated references retain their constraints at every occurrence. Schema failures are reported alongside snapshot differences. The Rosetta spec declares the common error schema under HTTP 500; other error statuses use that schema when no exact or `default` response is declared. The HTTP check uses the existing fixture's `retriable` expectation when declared.
