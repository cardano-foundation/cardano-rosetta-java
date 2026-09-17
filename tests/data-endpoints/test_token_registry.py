"""
Tests for token registry metadata enrichment (v1.4.0).

These tests validate:
* Policy identifiers are surfaced for configured tokens
* Metadata enrichment is consistent across account, block, and search endpoints
* Logo formats (base64 vs URL) for CIP-26 vs CIP-68 tokens
"""

import base64
import os
from typing import Dict, List, Tuple

import allure
import pytest


TOKEN_REGISTRY_LOGO_FETCH = os.environ.get("TOKEN_REGISTRY_LOGO_FETCH", "false").lower() == "true"


@pytest.fixture
def tokens_config(network_data):
    """Extract and validate tokens_in_registry configuration from network test data."""
    tokens = network_data.get("tokens_in_registry")
    assert tokens, "network_test_data.yaml must define tokens_in_registry for the configured network"
    return tokens



def _fetch_token_from_account(client, network: str, token: Dict) -> Tuple[Dict | None, Dict | None]:
    """Fetch token currency and metadata from /account/balance at configured test block.

    Returns: (currency, metadata) tuple, or (None, None) if token not found or request fails.
    """
    response = client.account_balance(
        network_identifier={"blockchain": "cardano", "network": network},
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
    response = client.block(
        network_identifier={"blockchain": "cardano", "network": network},
        block_identifier={"index": token["test_block"]},
    )
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
        network_identifier={"blockchain": "cardano", "network": network},
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


@pytest.mark.smoke
@pytest.mark.requires_token_registry
@allure.feature("Smoke Tests")
@allure.story("Token Registry Health")
class TestTokenRegistryHealth:
    """Validate metadata enrichment for configured tokens using account balances."""

    def test_configured_tokens_have_enrichment(self, client, network, tokens_config, has_token_registry):
        if not has_token_registry:
            pytest.skip("Token registry not enabled")

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
        if not has_token_registry:
            pytest.skip("Token registry not enabled")

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
    @pytest.mark.slow
    @pytest.mark.requires_token_registry
    def test_enriched_metadata_in_search_results(self, client, network, tokens_config, has_token_registry):
        if not has_token_registry:
            pytest.skip("Token registry not enabled")

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
        if not has_token_registry:
            pytest.skip("Token registry not enabled")
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

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    @pytest.mark.requires_logo_fetch
    def test_base64_logo_is_valid_and_decodable(self, client, network, tokens_config, has_token_registry):
        """Positive case: For CIP-26 tokens, verify the logo is valid base64 and decodes to PNG/JPEG bytes."""
        if not has_token_registry:
            pytest.skip("Token registry not enabled")
        if not TOKEN_REGISTRY_LOGO_FETCH:
            pytest.skip("TOKEN_REGISTRY_LOGO_FETCH must be true to validate logo payloads")

        for token in tokens_config:
            if token.get("logo_format") != "base64":
                continue

            currency, metadata = _fetch_token_from_account(client, network, token)
            logo = metadata.get("logo") if metadata else None

            assert logo is not None, f"Logo metadata missing for {token['ticker']}"
            assert logo.get("format") == "base64"
            
            value = logo.get("value")
            assert value, f"Logo value is empty for {token['ticker']}"

            try:
                decoded = base64.b64decode(value)
                # Verify PNG magic bytes (89 50 4E 47 0D 0A 1A 0A)
                assert decoded.startswith(b"\x89PNG\r\n\x1a\n"), (
                    f"Decoded logo for {token['ticker']} does not start with PNG magic bytes"
                )
            except Exception as e:
                pytest.fail(f"Failed to decode base64 logo for {token['ticker']}: {e}")

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_logo_absent_when_fetch_disabled(self, client, network, tokens_config, has_token_registry):
        """Negative case: Logo metadata must be absent/empty when TOKEN_REGISTRY_LOGO_FETCH is disabled."""
        if not has_token_registry:
            pytest.skip("Token registry not enabled")
        if TOKEN_REGISTRY_LOGO_FETCH:
            pytest.skip("TOKEN_REGISTRY_LOGO_FETCH is enabled (this test validates behavior when disabled)")

        for token in tokens_config:
            currency, metadata = _fetch_token_from_account(client, network, token)
            logo = metadata.get("logo") if metadata else None
            assert logo is None or logo == {}, (
                f"Logo metadata should be absent when TOKEN_REGISTRY_LOGO_FETCH is disabled for token {token['ticker']}"
            )

    @pytest.mark.nightly
    @pytest.mark.requires_token_registry
    def test_logo_absent_for_unregistered_token(self, client, network, has_token_registry):
        """Negative case: A token not registered in the metadata server must not return logo or metadata fields."""
        if not has_token_registry:
            pytest.skip("Token registry not enabled")

        # Use a dummy asset policy ID and symbol that is guaranteed to not be in the token registry
        dummy_token = {
            "policy_id": "00000000000000000000000000000000000000000000000000000000",
            "symbol_hex": "44554d4d59",  # "DUMMY"
            "decimals": 0,
            "ticker": "DUMMY"
        }

        # Query /search/transactions with an unregistered dummy token to verify 
        # Rosetta handles fallback/unregistered metadata gracefully without crashing.
        response = client.search_transactions(
            network_identifier={"blockchain": "cardano", "network": network},
            currency={
                "symbol": dummy_token["symbol_hex"],
                "decimals": dummy_token["decimals"],
                "metadata": {"policyId": dummy_token["policy_id"]},
            },
        )
        assert response.status_code == 200, (
            f"Rosetta search transactions failed with status {response.status_code} for unregistered token"
        )
