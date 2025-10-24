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


def _tokens_config(network_data: Dict) -> List[Dict]:
    tokens = network_data.get("tokens_in_registry")
    assert tokens, "network_test_data.yaml must define tokens_in_registry for the configured network"
    return tokens


def _ensure_token_registry_base_url() -> str:
    assert TOKEN_REGISTRY_BASE_URL, "TOKEN_REGISTRY_BASE_URL environment variable must be set"
    return TOKEN_REGISTRY_BASE_URL.rstrip("/")


@lru_cache(maxsize=None)
def _registry_metadata(subject: str) -> Dict:
    base_url = _ensure_token_registry_base_url()
    # Use v2 endpoint shape: {base}/v2/subjects/{subject}
    response = requests.get(f"{base_url}/v2/subjects/{subject}", timeout=15)
    response.raise_for_status()
    return _simplify_registry_metadata(response.json())


def _simplify_registry_metadata(raw: Dict) -> Dict:
    """Simplify v2 registry payload: keep only scalar values as-is."""
    simple: Dict[str, object] = {}
    for key, value in raw.items():
        if key in {"subject", "policy"}:
            continue
        if not isinstance(value, (dict, list)):
            simple[key] = value
    return simple


def _account_token_metadata(client, network: str, token: Dict) -> Tuple[Dict, Dict]:
    response = client.account_balance(
        network=network,
        account_identifier={"address": token["test_address"]},
        block_identifier={"index": token["test_block"]},
    )
    assert response.status_code == 200, (
        f"/account/balance returned {response.status_code} for {token['test_address']} "
        f"at block {token['test_block']}"
    )

    for balance in response.json().get("balances", []):
        currency = balance.get("currency", {})
        metadata = currency.get("metadata", {})
        if metadata.get("policyId") == token["policy_id"]:
            return currency, metadata

    raise AssertionError(
        f"Token with policyId {token['policy_id']} not found in /account/balance response "
        f"for address {token['test_address']} at block {token['test_block']}"
    )


def _assert_basic_metadata(currency: Dict, metadata: Dict, token: Dict) -> None:
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
    # Deterministic selection: CIP-26 (base64 logo) uses ASCII; CIP-68 (url logo) uses HEX.
    use_hex = token.get("logo_format") == "url"
    symbol = token["symbol_hex"] if use_hex else token.get("symbol_ascii", "")
    response = client.search_transactions(
        network=network,
        currency={
            "symbol": symbol,
            "decimals": token["decimals"],
            "metadata": {"policyId": token["policy_id"]},
        },
        limit=10,
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
    def test_tokens_expose_policy_id(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in _tokens_config(network_data):
            currency, metadata = _account_token_metadata(client, network, token)
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

    def test_configured_tokens_have_enrichment(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in _tokens_config(network_data):
            currency, metadata = _account_token_metadata(client, network, token)
            _assert_basic_metadata(currency, metadata, token)


@allure.feature("Token Registry")
@allure.story("Enriched Metadata")
class TestTokenRegistryEnrichment:
    """Validate enrichment across block and search endpoints."""

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_enriched_metadata_in_block_operations(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in _tokens_config(network_data):
            matches = _block_operations_for_token(client, network, token)
            assert matches, (
                f"No operations found with token {token['ticker']} in block {token['test_block']}"
            )
            for tx, op in matches:
                currency = op.get("amount", {}).get("currency", {})
                metadata = currency.get("metadata", {})
                _assert_basic_metadata(currency, metadata, token)

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_enriched_metadata_in_search_results(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"

        for token in _tokens_config(network_data):
            matches = _search_operations_for_token(client, network, token)
            tried_symbol = token["symbol_hex"] if token.get("logo_format") == "url" else token.get("symbol_ascii", "")
            assert matches, (
                f"/search/transactions returned no results for token {token['ticker']} "
                f"(symbol tried: {tried_symbol})"
            )
            for tx, op in matches:
                currency = op.get("amount", {}).get("currency", {})
                metadata = currency.get("metadata", {})
                _assert_basic_metadata(currency, metadata, token)


@allure.feature("Token Registry")
@allure.story("Logo Formats")
class TestTokenRegistryLogos:
    """Validate logo enrichment for CIP-26 (base64) and CIP-68 (URL) tokens."""

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    @pytest.mark.requires_logo_fetch
    def test_logo_formats_match_expected_standard(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"
        if not TOKEN_REGISTRY_LOGO_FETCH:
            pytest.skip("TOKEN_REGISTRY_LOGO_FETCH must be true to validate logo payloads")

        for token in _tokens_config(network_data):
            _, metadata = _account_token_metadata(client, network, token)
            logo = metadata.get("logo")
            assert isinstance(logo, dict), "Logo metadata missing or malformed"
            expected_format = token.get("logo_format")
            if expected_format:
                assert logo.get("format") == expected_format, (
                    f"Expected logo format '{expected_format}' for token {token['ticker']}"
                )
            prefix = token.get("logo_value_prefix")
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
    def test_rosetta_metadata_matches_registry(self, client, network, network_data, has_token_registry):
        assert has_token_registry, "Token registry must be enabled for this test"
        _ensure_token_registry_base_url()

        for token in _tokens_config(network_data):
            currency, rosetta_metadata = _account_token_metadata(client, network, token)
            try:
                registry_metadata = _registry_metadata(token["subject"])
            except requests.HTTPError as e:
                if getattr(e.response, "status_code", None) == 404:
                    pytest.fail(
                        f"Registry missing subject {token['subject']} at {TOKEN_REGISTRY_BASE_URL}"
                    )
                raise

            _assert_basic_metadata(currency, rosetta_metadata, token)

            for field in ("name", "description", "ticker", "url"):
                registry_value = registry_metadata.get(field)
                if registry_value is not None:
                    assert rosetta_metadata.get(field) == registry_value, (
                        f"Field '{field}' mismatch between Rosetta and registry for {token['ticker']}"
                    )

            registry_decimals = registry_metadata.get("decimals")
            if registry_decimals is not None:
                assert int(registry_decimals) == token["decimals"], (
                    f"Decimals mismatch for {token['ticker']}: registry={registry_decimals}"
                )

            registry_logo = registry_metadata.get("logo")
            rosetta_logo = rosetta_metadata.get("logo")
            if registry_logo and TOKEN_REGISTRY_LOGO_FETCH:
                assert isinstance(rosetta_logo, dict), "Rosetta logo metadata missing"
                assert rosetta_logo.get("value") == registry_logo, (
                    f"Logo value mismatch between Rosetta and registry for {token['ticker']}"
                )
            elif not TOKEN_REGISTRY_LOGO_FETCH:
                assert rosetta_logo is None or rosetta_logo == {}, (
                    "Logo metadata should be absent when TOKEN_REGISTRY_LOGO_FETCH is disabled"
                )
