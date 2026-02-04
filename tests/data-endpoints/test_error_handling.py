"""
Cross-cutting error handling tests for all Rosetta endpoints.

Tests network_identifier validation errors across all endpoints:
- Missing network_identifier entirely
- Empty network_identifier object
- Invalid network value
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

        assert response.status_code == 500, (
            f"{endpoint_name} should return 500 for empty network"
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

        assert response.status_code == 500, (
            f"{endpoint_name} should return 500 for invalid network"
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
