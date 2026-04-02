"""
Cross-cutting error handling tests for all Rosetta endpoints.

Tests:
- network_identifier validation errors across all endpoints
- Currency input validation (policyId, symbol, token name)
- Address validation
- Coin identifier validation
"""

import pytest
import allure

pytestmark = pytest.mark.pr


# Endpoints that require network_identifier with their extra required params
ENDPOINTS = [
    ("network_status", "/network/status", {}),
    ("network_options", "/network/options", {}),
    ("search_transactions", "/search/transactions", {}),
    ("block", "/block", {"block_identifier": {"index": 100}}),
    (
        "block_transaction",
        "/block/transaction",
        {
            "block_identifier": {"index": 100, "hash": "a" * 64},
            "transaction_identifier": {"hash": "b" * 64},
        },
    ),
    (
        "account_balance",
        "/account/balance",
        {"account_identifier": {"address": "addr_test1qz..."}},
    ),
    (
        "account_coins",
        "/account/coins",
        {"account_identifier": {"address": "addr_test1qz..."}},
    ),
]


@allure.feature("Error Handling")
@allure.story("Missing Network Identifier")
class TestMissingNetworkIdentifier:
    """Test that all endpoints return error when network_identifier is missing entirely."""

    @pytest.mark.parametrize(
        "endpoint_name,path,extra_body",
        [pytest.param(name, path, body, id=name) for name, path, body in ENDPOINTS],
    )
    def test_missing_network_identifier_returns_error(
        self, client, endpoint_name, path, extra_body
    ):
        """Missing network_identifier should return 400 validation error."""
        response = client._post(path, extra_body)

        assert response.status_code == 400, (
            f"{endpoint_name} should return 400 for missing network_identifier"
        )

        error = response.json()
        error_message = error.get("details", {}).get("message", "").lower()
        assert "network" in error_message or "identifier" in error_message, (
            f"{endpoint_name} error should mention network_identifier"
        )


@allure.feature("Error Handling")
@allure.story("Empty Network Identifier")
class TestEmptyNetworkIdentifier:
    """Test that all endpoints return error when network_identifier is empty object."""

    @pytest.mark.parametrize(
        "endpoint_name,path,extra_body",
        [pytest.param(name, path, body, id=name) for name, path, body in ENDPOINTS],
    )
    def test_empty_network_identifier_returns_error(
        self, client, endpoint_name, path, extra_body
    ):
        """Empty network_identifier object should return error about missing inner fields."""
        body = {"network_identifier": {}, **extra_body}
        response = client._post(path, body)

        # Validation can happen at controller (400) or service (500) level
        assert response.status_code in [400, 500], (
            f"{endpoint_name} should return 400 or 500 for empty network_identifier"
        )

        error = response.json()
        error_message = error.get("details", {}).get("message", "")
        # Empty object means inner fields (blockchain, network) are missing
        assert "networkIdentifier.network" in error_message or "networkIdentifier.blockchain" in error_message, (
            f"{endpoint_name} should indicate networkIdentifier inner fields are missing. Got: {error_message}"
        )


@allure.feature("Error Handling")
@allure.story("Empty Network String")
class TestEmptyNetworkString:
    """Test that all endpoints return error when network string is empty."""

    @pytest.mark.parametrize(
        "endpoint_name,path,extra_body",
        [pytest.param(name, path, body, id=name) for name, path, body in ENDPOINTS],
    )
    def test_empty_network_returns_error_4002(
        self, client, endpoint_name, path, extra_body
    ):
        """Empty network string should return error 4002 - Network not found."""
        body = {
            "network_identifier": {"blockchain": "cardano", "network": ""},
            **extra_body,
        }
        response = client._post(path, body)

        assert response.status_code == 400, (
            f"{endpoint_name} should return 400 for empty network"
        )

        error = response.json()
        assert error.get("code") == 4002, (
            f"{endpoint_name} should return error code 4002"
        )
        assert error.get("message") == "Network not found", (
            f"{endpoint_name} error message mismatch"
        )
        assert error.get("retriable") is False, (
            f"{endpoint_name} should not be retriable"
        )


@allure.feature("Error Handling")
@allure.story("Invalid Network Identifier")
class TestInvalidNetworkIdentifier:
    """Test that all endpoints return error when network value is invalid."""

    @pytest.mark.parametrize(
        "endpoint_name,path,extra_body",
        [pytest.param(name, path, body, id=name) for name, path, body in ENDPOINTS],
    )
    def test_invalid_network_returns_error_4002(
        self, client, endpoint_name, path, extra_body
    ):
        """Invalid network name should return error 4002 - Network not found."""
        body = {
            "network_identifier": {"blockchain": "cardano", "network": "invalid_network"},
            **extra_body,
        }
        response = client._post(path, body)

        assert response.status_code == 400, (
            f"{endpoint_name} should return 400 for invalid network"
        )

        error = response.json()
        assert error.get("code") == 4002, (
            f"{endpoint_name} should return error code 4002"
        )
        assert error.get("message") == "Network not found", (
            f"{endpoint_name} error message mismatch"
        )
        assert error.get("retriable") is False, (
            f"{endpoint_name} should not be retriable"
        )


@allure.feature("Error Handling")
@allure.story("Currency Input Validation")
class TestSearchCurrencyValidation:
    """Validate that /search/transactions rejects malformed currency inputs (PR #726)."""

    @pytest.mark.parametrize("payload", [
        pytest.param("'}]') OR 1=1 --", id="jsonb_injection"),
        pytest.param("'; DROP TABLE transaction; --", id="sql_drop_table"),
        pytest.param("abcdef1234", id="too_short"),
        pytest.param("g" * 56, id="non_hex_correct_length"),
        pytest.param("a" * 57, id="too_long"),
    ])
    def test_search_rejects_malformed_policy_id(self, client, payload):
        """Malformed policyId should return error 4023."""
        response = client.search_transactions(
            currency={"symbol": "ADA", "decimals": 6, "metadata": {"policyId": payload}}
        )

        assert response.status_code == 400, (
            f"Expected 400 for policyId '{payload[:30]}...', got {response.status_code}"
        )
        error = response.json()
        assert error["code"] == 4023
        assert error["message"] == "Invalid policy id"

    @pytest.mark.parametrize("payload", [
        pytest.param("'; DROP TABLE transaction; --", id="sql_injection"),
        pytest.param("' OR 1=1 --", id="sql_or_injection"),
        pytest.param("not-hex!", id="special_chars"),
        pytest.param("abc def", id="spaces"),
    ])
    def test_search_rejects_malformed_symbol(self, client, payload):
        """Non-hex currency symbol should return error 5059."""
        response = client.search_transactions(
            currency={"symbol": payload, "decimals": 6}
        )

        assert response.status_code == 400, (
            f"Expected 400 for symbol '{payload[:30]}', got {response.status_code}"
        )
        error = response.json()
        assert error["code"] == 5059

    @pytest.mark.slow
    def test_search_accepts_valid_policy_id(self, client, network_data):
        """Valid hex policyId with matching symbol should return 200 (positive control)."""
        asset = network_data["assets"][0]

        response = client.search_transactions(
            currency={
                "symbol": asset["symbol_hex"],
                "decimals": asset["decimals"],
                "metadata": {"policyId": asset["policy_id"]},
            },
            limit=1,
        )

        assert response.status_code == 200


@allure.feature("Error Handling")
@allure.story("Account Currency Validation")
class TestAccountCurrencyValidation:
    """Validate that /account/balance rejects malformed currency inputs."""

    @pytest.mark.parametrize("payload", [
        pytest.param("'; DROP TABLE transaction; --", id="sql_injection"),
        pytest.param("abcdef", id="too_short"),
    ])
    def test_account_balance_rejects_malformed_policy_id(self, client, network_data, payload):
        """Malformed policyId in currencies should return error 4023."""
        response = client.account_balance(
            account_identifier={"address": network_data["addresses"]["shelley_base"]},
            currencies=[{"symbol": "aabbcc", "decimals": 0, "metadata": {"policyId": payload}}],
        )

        assert response.status_code == 400, (
            f"Expected 400 for policyId '{payload[:30]}', got {response.status_code}"
        )
        assert response.json()["code"] == 4023

    @pytest.mark.parametrize("payload", [
        pytest.param("not-hex!", id="special_chars"),
        pytest.param("Diamond", id="ascii_letters"),
    ])
    def test_account_balance_rejects_invalid_token_name(self, client, network_data, payload):
        """Invalid token name (non-hex) should return error 4024."""
        response = client.account_balance(
            account_identifier={"address": network_data["addresses"]["shelley_base"]},
            currencies=[{"symbol": payload, "decimals": 0}],
        )

        assert response.status_code == 400, (
            f"Expected 400 for token name '{payload}', got {response.status_code}"
        )
        assert response.json()["code"] == 4024


@allure.feature("Error Handling")
@allure.story("Address Validation")
class TestAddressValidation:
    """Validate that account endpoints reject invalid addresses."""

    def test_account_balance_rejects_invalid_address(self, client):
        """Invalid bech32 address should return error 4015."""
        response = client.account_balance(
            account_identifier={"address": "not_a_valid_bech32_address"}
        )

        assert response.status_code == 500, (
            f"Expected 500 (retriable) for invalid address, got {response.status_code}"
        )
        error = response.json()
        assert error["code"] == 4015
        assert error["retriable"] is True

    def test_account_coins_rejects_invalid_address(self, client):
        """Invalid bech32 address should return error 4015."""
        response = client.account_coins(
            account_identifier={"address": "not_a_valid_bech32_address"}
        )

        assert response.status_code == 500, (
            f"Expected 500 (retriable) for invalid address, got {response.status_code}"
        )
        error = response.json()
        assert error["code"] == 4015
        assert error["retriable"] is True


@allure.feature("Error Handling")
@allure.story("Coin Identifier Validation")
class TestCoinIdentifierValidation:
    """Validate that /search/transactions handles malformed coin identifiers."""

    @pytest.mark.skip(reason="Bug: coin_identifier without ':' causes ArrayIndexOutOfBoundsException (no ticket yet)")
    def test_search_malformed_coin_identifier_no_separator(self, client):
        """coin_identifier without ':' separator should return 400 with structured error."""
        response = client.search_transactions(
            coin_identifier={"identifier": "no_colon_here"}
        )

        assert response.status_code == 400
        error = response.json()
        assert "code" in error
