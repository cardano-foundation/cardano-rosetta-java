"""
Cross-cutting error handling tests for all Rosetta endpoints.

Tests common error scenarios across all endpoints that require network_identifier.
Uses lambda pattern to handle different parameter requirements per endpoint.
"""

import pytest
import allure

pytestmark = pytest.mark.pr


@allure.feature("Error Handling")
@allure.story("Missing Network Identifier")
class TestMissingNetworkIdentifier:
    """Test that all endpoints properly handle missing/empty network identifier."""

    @pytest.mark.parametrize(
        "endpoint_name,make_request",
        [
            pytest.param(
                "network_status",
                lambda c: c.network_status(network=""),
                id="network_status",
            ),
            pytest.param(
                "network_options",
                lambda c: c.network_options(network=""),
                id="network_options",
            ),
            pytest.param(
                "search_transactions",
                lambda c: c.search_transactions(network=""),
                id="search_transactions",
            ),
            pytest.param(
                "block",
                lambda c: c.block(network="", block_identifier={"index": 100}),
                id="block",
            ),
            pytest.param(
                "block_transaction",
                lambda c: c.block_transaction(
                    network="",
                    block_identifier={"index": 100, "hash": "a" * 64},
                    transaction_identifier={"hash": "b" * 64},
                ),
                id="block_transaction",
            ),
            pytest.param(
                "account_balance",
                lambda c: c.account_balance(
                    network="", account_identifier={"address": "addr_test1..."}
                ),
                id="account_balance",
            ),
            pytest.param(
                "account_coins",
                lambda c: c.account_coins(
                    network="", account_identifier={"address": "addr_test1..."}
                ),
                id="account_coins",
            ),
        ],
    )
    def test_empty_network_returns_error_4002(
        self, client, endpoint_name, make_request
    ):
        """Empty network string should return error 4002 - Network not found."""
        response = make_request(client)

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
    """Test that all endpoints properly handle invalid network identifiers."""

    @pytest.mark.parametrize(
        "endpoint_name,make_request",
        [
            pytest.param(
                "network_status",
                lambda c: c.network_status(network="invalid_network"),
                id="network_status",
            ),
            pytest.param(
                "network_options",
                lambda c: c.network_options(network="invalid_network"),
                id="network_options",
            ),
            pytest.param(
                "search_transactions",
                lambda c: c.search_transactions(network="invalid_network"),
                id="search_transactions",
            ),
            pytest.param(
                "block",
                lambda c: c.block(
                    network="invalid_network", block_identifier={"index": 100}
                ),
                id="block",
            ),
            pytest.param(
                "block_transaction",
                lambda c: c.block_transaction(
                    network="invalid_network",
                    block_identifier={"index": 100, "hash": "a" * 64},
                    transaction_identifier={"hash": "b" * 64},
                ),
                id="block_transaction",
            ),
            pytest.param(
                "account_balance",
                lambda c: c.account_balance(
                    network="invalid_network",
                    account_identifier={"address": "addr_test1..."},
                ),
                id="account_balance",
            ),
            pytest.param(
                "account_coins",
                lambda c: c.account_coins(
                    network="invalid_network",
                    account_identifier={"address": "addr_test1..."},
                ),
                id="account_coins",
            ),
        ],
    )
    def test_invalid_network_returns_error_4002(
        self, client, endpoint_name, make_request
    ):
        """Invalid network name should return error 4002 - Network not found."""
        response = make_request(client)

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
