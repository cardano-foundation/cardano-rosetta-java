# Testing Guidelines

Hard-earned lessons from writing integration tests for Rosetta API.

## Test Structure

### AAA Pattern (Arrange-Act-Assert)

Separate phases visually with a blank line. No phase comments - structure should be obvious.

```python
def test_currency_filter_returns_matching_transactions(...):
    # Data gathering
    asset = network_data["assets"][0]
    response = client.search_transactions(currency={...})
    txs = response.json()["transactions"]

    # Validation
    assert len(txs) > 0
    for tx in txs:
        assert asset["symbol_hex"] in currencies_from_tx
```

### Filter Semantics

When testing filters, validate **ALL** results match the filter criteria, not "at least one".

```python
# WRONG - only checks first match
found = False
for item in results:
    if matches_filter(item):
        found = True
        break
assert found

# RIGHT - validates every result
for item in results:
    assert matches_filter(item), "Filter should return only matching items"
```

### Collection Pattern

Use "collect then assert membership" instead of "find first and break".

```python
# Cleaner, no nested breaks
currencies_in_tx = [
    token["currency"]["symbol"].lower()
    for op in tx["operations"]
    if "tokenBundle" in op.get("metadata", {})
    for bundle in op["metadata"]["tokenBundle"]
    for token in bundle.get("tokens", [])
]
assert expected_symbol in currencies_in_tx
```

## Cardano-Specific

### Native Assets Live in TokenBundle

In `/search/transactions` responses, native assets are ALWAYS in `operations[].metadata.tokenBundle[].tokens[]`, never in `operations[].amount.currency` (which is always ADA).

```python
# Native assets sit in UTXOs alongside ADA
for op in tx["operations"]:
    if "metadata" in op and "tokenBundle" in op["metadata"]:
        for bundle in op["metadata"]["tokenBundle"]:
            for token in bundle.get("tokens", []):
                currencies.append(token["currency"]["symbol"])
```

### Currency Symbols Must Be Hex

After issue #610, all currency symbols for native assets must be hex-encoded.

```python
# network_test_data.yaml
assets:
  - symbol: "tTEURO"        # Human-readable
    symbol_hex: "74544555524f"  # Use this in tests
```

## Helper Functions

### Helpers Return Data, Tests Assert

Data-fetching helpers should return `None` on failure and let tests decide if that's an error.

```python
# GOOD - returns data or None
def _fetch_token_from_account(...) -> Tuple[Dict | None, Dict | None]:
    response = client.account_balance(...)
    if response.status_code != 200:
        return None, None
    # ... find and return
    return currency, metadata

# In test - explicit assertion
currency, metadata = _fetch_token_from_account(...)
assert currency is not None, f"Token {token['ticker']} not found"
```

### Universal Preconditions → Fixtures

Setup requirements shared across all tests should be pytest fixtures, not helper functions.

```python
# GOOD - pytest fixture for universal setup
@pytest.fixture
def tokens_config(network_data):
    """Extract and validate tokens_in_registry configuration."""
    tokens = network_data.get("tokens_in_registry")
    assert tokens, "network_test_data.yaml must define tokens_in_registry"
    return tokens

# Test signature declares dependencies
def test_enrichment(client, network, tokens_config, has_token_registry):
    for token in tokens_config:  # Fixture already validated
        ...
```

### Domain Assertion Helpers

Grouping related assertions is OK if the name is explicit about what it validates.

```python
# Acceptable - domain-specific, clear name
def _verify_all_metadata_fields_match(currency, metadata, token):
    """Verify currency and enriched metadata fields match expected token configuration.

    Validates:
    - currency: symbol_hex, decimals
    - metadata: policyId, subject, name, description, ticker, url
    """
    assert metadata.get("policyId") == token["policy_id"]
    # ... 6 more assertions
```

## Test Documentation

### Tests Are Documentation

Tests should be self-contained and readable in isolation. Favor inline clarity over DRY.

When debugging a failed test, you want to understand:
- What was being checked?
- How was it being checked?
- Why did it fail?

Abstractions that hide this information make debugging harder.

### Docstrings for Helpers

Document what fails and why, not just what the function does.

```python
def _fetch_peers_with_retry(...):
    """Fetch peers from /network/status, retrying for peer discovery 5-min initial delay.

    Returns: List of peers, or empty list if still not populated after 5 minutes.
    """
```

## Control Flow

### if = Business Rule, assert = Validation

```python
# if - conditional logic (field is optional)
if registry_value is not None:
    assert rosetta_value == registry_value  # Validation: IF present, must match

# assert - requirement (field is mandatory)
assert rosetta_value is not None  # Fails if missing
```

### Avoid Nested Breaks

Use collection patterns or early returns instead of multiple break statements.

## What NOT to Do

- ❌ Don't hide assertions in helper functions (Mystery Guest anti-pattern)
- ❌ Don't mix data gathering and validation phases
- ❌ Don't validate "at least one" when testing filters (validate ALL)
- ❌ Don't add phase comments (`# Arrange`, `# Assert`) - structure should be obvious
- ❌ Don't check operation amounts for native assets - check tokenBundle
- ❌ Don't use ASCII symbols for currency filters - use hex encoding

## When in Doubt

Ask: "If this test fails, can I immediately see WHAT failed by reading the test code?"

If the answer requires jumping to helper functions, refactor to make failure points visible.
