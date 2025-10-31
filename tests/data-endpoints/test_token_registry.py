"""
Tests for token registry metadata enrichment (v1.4.0).

These tests validate:
* Policy identifiers are surfaced for configured tokens
* Metadata enrichment is consistent across account, block, and search endpoints
* Logo formats (base64 vs URL) for CIP-26 vs CIP-68 tokens
* Rosetta metadata matches the upstream metadata server (weekly parity check)
"""

import os
from functools import lru_cache
from typing import Dict, List, Tuple

import allure
import pytest
import requests


TOKEN_REGISTRY_BASE_URL = os.environ.get("TOKEN_REGISTRY_BASE_URL")
TOKEN_REGISTRY_LOGO_FETCH = os.environ.get("TOKEN_REGISTRY_LOGO_FETCH", "false").lower() == "true"


@pytest.fixture
def tokens_config(network_data):
    """Extract and validate tokens_in_registry configuration from network test data."""
    tokens = network_data.get("tokens_in_registry")
    assert tokens, "network_test_data.yaml must define tokens_in_registry for the configured network"
    return tokens


@pytest.fixture
def token_registry_base_url():
    """Validate and return token registry base URL from environment."""
    assert TOKEN_REGISTRY_BASE_URL, "TOKEN_REGISTRY_BASE_URL environment variable must be set"
    return TOKEN_REGISTRY_BASE_URL.rstrip("/")


@lru_cache(maxsize=None)
def _registry_metadata(base_url: str, subject: str) -> Dict | None:
    """Fetch token metadata from upstream registry by subject ID.

    Registry v2 API returns: {"subject": {"metadata": {"name": {"value": "...", "source": "CIP_26"}, ...}}}
    Extracts metadata and unwraps {value, source} structure to just values.

    Returns: Simplified metadata dict with unwrapped values, or None if subject not found (404).
    Raises: HTTPError for other request failures (network issues, 500, etc).
    """
    response = requests.get(f"{base_url}/v2/subjects/{subject}", timeout=60)

    if response.status_code == 404:
        return None

    response.raise_for_status()
    raw = response.json()
    metadata = raw.get("subject", {}).get("metadata", {})

    simple: Dict[str, object] = {}
    for key, field_obj in metadata.items():
        if key in {"subject", "policy"}:
            continue
        if isinstance(field_obj, dict) and "value" in field_obj:
            simple[key] = field_obj["value"]
        elif not isinstance(field_obj, (dict, list)):
            simple[key] = field_obj

    return simple


def _fetch_token_from_account(client, network: str, token: Dict) -> Tuple[Dict | None, Dict | None]:
    """Fetch token currency and metadata from /account/balance at configured test block.

    Returns: (currency, metadata) tuple, or (None, None) if token not found or request fails.
    """
    response = client.account_balance(
        network=network,
        account_identifier={"address": token["test_address"]},
        block_identifier={"index": token["test_block"]},
    )

    if response.status_code != 200:
        return None, None

    for balance in response.json().get("balances", []):
        currency = balance.get("currency", {})
        metadata = currency.get("metadata", {})
        if metadata.get("policyId") == token["policy_id"]:
            return currency, metadata

    return None, None


def _verify_all_metadata_fields_match(currency: Dict, metadata: Dict, token: Dict) -> None:
    """Verify currency and enriched metadata fields match expected token configuration.

    Validates:
    - currency: symbol_hex, decimals
    - metadata: policyId, subject (if configured), name, description, ticker, url
    """
    assert metadata.get("policyId") == token["policy_id"]
    if token.get("subject"):
        subject = metadata.get("subject")
        if subject is None:
            policy_id = metadata.get("policyId", "")
            symbol = currency.get("symbol", "")
            if policy_id and symbol:
                subject = policy_id + symbol
        assert subject == token["subject"], "subject mismatch"

    # Currency symbol/decimals should match expectations
    if "symbol_hex" in token:
        assert currency.get("symbol") == token["symbol_hex"], "currency symbol must use hex form"
    if "decimals" in token:
        assert currency.get("decimals") == token["decimals"], "currency decimals mismatch"

    for field in ("name", "description", "ticker", "url"):
        expected = token.get(field)
        if expected is not None:
            assert metadata.get(field) == expected, f"Metadata field '{field}' mismatch"


def _block_operations_for_token(client, network: str, token: Dict) -> List[Tuple[Dict, Dict]]:
    """Find operations containing token in /block response at configured test block.

    Asserts: Response is 200.
    Returns: List of (transaction, operation) tuples (may be empty if token not in block).
    """
    response = client.block(network=network, block_identifier={"index": token["test_block"]})
    assert response.status_code == 200, (
        f"/block {token['test_block']} returned {response.status_code}"
    )

    block = response.json().get("block", {})
    matches: List[Tuple[Dict, Dict]] = []

    for tx in block.get("transactions", []):
        for op in tx.get("operations", []):
            # Strict: only consider bundled tokens inside operation metadata.tokenBundle
            bundle = op.get("metadata", {}).get("tokenBundle", [])
            for entry in bundle:
                if entry.get("policyId") != token["policy_id"]:
                    continue
                for t in entry.get("tokens", []):
                    t_currency = t.get("currency", {})
                    t_metadata = t_currency.get("metadata", {})
                    if t_metadata.get("policyId") == token["policy_id"]:
                        # Create a minimal pseudo-op so downstream checks read the token currency
                        pseudo_op = {"amount": {"currency": t_currency}}
                        matches.append((tx, pseudo_op))
                        break
                # Only one match per operation is needed
                if matches and matches[-1][0] is tx:
                    break

    return matches


def _search_operations_for_token(client, network: str, token: Dict) -> List[Tuple[Dict, Dict]]:
    """Find operations containing token in /search/transactions response using hex symbol.

    Returns: List of (transaction, operation) tuples from operation amounts or tokenBundle.
             Empty list if non-200 response.
    """
    # Use hex-encoded symbol in search request (canonical format in v1.4.1+)
    symbol = token["symbol_hex"]
    response = client.search_transactions(
        network=network,
        currency={
            "symbol": symbol,
            "decimals": token["decimals"],
            "metadata": {"policyId": token["policy_id"]},
        },
    )

    if response.status_code != 200:
        return []

    matches: List[Tuple[Dict, Dict]] = []
    for tx in response.json().get("transactions", []):
        for op in tx.get("transaction", {}).get("operations", []):
            # Prefer matched asset surfaced as operation amount (typical for search)
            currency = op.get("amount", {}).get("currency", {})
            metadata = currency.get("metadata", {})
            if metadata.get("policyId") == token["policy_id"]:
                matches.append((tx, op))
                continue

            # Also allow bundled tokens if present
            bundle = op.get("metadata", {}).get("tokenBundle", [])
            for entry in bundle:
                if entry.get("policyId") != token["policy_id"]:
                    continue
                for t in entry.get("tokens", []):
                    t_currency = t.get("currency", {})
                    t_metadata = t_currency.get("metadata", {})
                    if t_metadata.get("policyId") == token["policy_id"]:
                        pseudo_op = {"amount": {"currency": t_currency}}
                        matches.append((tx, pseudo_op))
                        break
                if matches and matches[-1][0] is tx:
                    break

    return matches


@allure.feature("Token Registry")
@allure.story("v1.4.0 Feature - policyId")
class TestPolicyIdMetadata:
    """Verify policy identifiers are exposed for configured tokens."""

    @pytest.mark.pr
    def test_tokens_expose_policy_id(self, client, network, tokens_config, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in tokens_config:
            currency, metadata = _fetch_token_from_account(client, network, token)

            assert currency is not None, f"Token {token['ticker']} not found"
            assert "policyId" in metadata, "Missing policyId in enriched metadata"
            if token.get("subject"):
                symbol = currency.get("symbol", "")
                assert metadata.get("policyId") + symbol == token["subject"]


@pytest.mark.smoke
@pytest.mark.requires_token_registry
@allure.feature("Smoke Tests")
@allure.story("Token Registry Health")
class TestTokenRegistryHealth:
    """Validate metadata enrichment for configured tokens using account balances."""

    def test_configured_tokens_have_enrichment(self, client, network, tokens_config, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in tokens_config:
            currency, metadata = _fetch_token_from_account(client, network, token)

            assert currency is not None, (
                f"Token {token['ticker']} not found in /account/balance for "
                f"{token['test_address']} at block {token['test_block']}"
            )
            _verify_all_metadata_fields_match(currency, metadata, token)


@allure.feature("Token Registry")
@allure.story("Enriched Metadata")
class TestTokenRegistryEnrichment:
    """Validate enrichment across block and search endpoints."""

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_enriched_metadata_in_block_operations(self, client, network, tokens_config, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in tokens_config:
            matches = _block_operations_for_token(client, network, token)
            assert matches, (
                f"No operations found with token {token['ticker']} in block {token['test_block']}"
            )
            for _, op in matches:
                currency = op.get("amount", {}).get("currency", {})
                metadata = currency.get("metadata", {})
                _verify_all_metadata_fields_match(currency, metadata, token)

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_enriched_metadata_in_search_results(self, client, network, tokens_config, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in tokens_config:
            matches = _search_operations_for_token(client, network, token)
            assert matches, (
                f"/search/transactions returned no results for token {token['ticker']} "
                f"(hex symbol used: {token['symbol_hex']})"
            )
            for _, op in matches:
                currency = op.get("amount", {}).get("currency", {})
                metadata = currency.get("metadata", {})
                _verify_all_metadata_fields_match(currency, metadata, token)


@allure.feature("Token Registry")
@allure.story("Logo Formats")
class TestTokenRegistryLogos:
    """Validate logo enrichment for CIP-26 (base64) and CIP-68 (URL) tokens."""

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    @pytest.mark.requires_logo_fetch
    def test_logo_formats_match_expected_standard(self, client, network, tokens_config, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"
        if not TOKEN_REGISTRY_LOGO_FETCH:
            pytest.skip("TOKEN_REGISTRY_LOGO_FETCH must be true to validate logo payloads")

        for token in tokens_config:
            currency, metadata = _fetch_token_from_account(client, network, token)
            logo = metadata.get("logo") if metadata else None
            expected_format = token.get("logo_format")
            prefix = token.get("logo_value_prefix")

            assert currency is not None, f"Token {token['ticker']} not found"
            assert isinstance(logo, dict), "Logo metadata missing or malformed"

            if expected_format:
                assert logo.get("format") == expected_format, (
                    f"Expected logo format '{expected_format}' for token {token['ticker']}"
                )
            if prefix:
                assert str(logo.get("value", "")).startswith(prefix), (
                    f"Logo value does not start with expected prefix '{prefix}' for token {token['ticker']}"
                )


@allure.feature("Token Registry")
@allure.story("Metadata Parity")
class TestTokenRegistryParity:
    """Weekly parity check between Rosetta enrichment and metadata registry source."""

    @pytest.mark.weekly
    @pytest.mark.requires_token_registry
    def test_rosetta_metadata_matches_registry(self, client, network, tokens_config, token_registry_base_url, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in tokens_config:
            currency, rosetta_metadata = _fetch_token_from_account(client, network, token)
            registry_metadata = _registry_metadata(token_registry_base_url, token["subject"])

            assert currency is not None, f"Token {token['ticker']} not found"
            assert registry_metadata is not None, (
                f"Registry missing subject {token['subject']} at {token_registry_base_url}"
            )

            for field in ("name", "description", "ticker", "url"):
                registry_value = registry_metadata.get(field)
                if registry_value is not None:
                    assert rosetta_metadata.get(field) == registry_value, (
                        f"Field '{field}' mismatch between Rosetta and registry for {token['ticker']}"
                    )

            registry_decimals = registry_metadata.get("decimals")
            if registry_decimals is not None:
                assert currency.get("decimals") == int(registry_decimals), (
                    f"Decimals mismatch between Rosetta and registry for {token['ticker']}: "
                    f"rosetta={currency.get('decimals')}, registry={registry_decimals}"
                )

            registry_logo = registry_metadata.get("logo")
            rosetta_logo = rosetta_metadata.get("logo")

            if TOKEN_REGISTRY_LOGO_FETCH:
                if registry_logo:
                    assert isinstance(rosetta_logo, dict), "Rosetta logo metadata missing or not a dict"
                    assert rosetta_logo.get("value") == registry_logo, (
                        f"Logo value mismatch between Rosetta and registry for {token['ticker']}: "
                        f"rosetta={rosetta_logo.get('value')[:50]}..., registry={registry_logo[:50] if isinstance(registry_logo, str) else registry_logo}..."
                    )
                else:
                    assert rosetta_logo is None or rosetta_logo == {}, (
                        f"Rosetta should have no logo when registry has none for {token['ticker']}"
                    )
            else:
                assert rosetta_logo is None or rosetta_logo == {}, (
                    "Logo metadata should be absent when TOKEN_REGISTRY_LOGO_FETCH is disabled"
                )
