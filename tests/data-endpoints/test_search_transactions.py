"""
Test suite for /search/transactions endpoint.
Covers all 61 test cases from the original Postman collection.
"""

import os
import pytest
import allure
from conftest import get_error_message


# Network configuration from environment - works on ANY network
NETWORK = os.environ.get("CARDANO_NETWORK", "preprod")


class TestSanityChecks:
    """Basic endpoint availability and error handling tests."""

    @allure.feature("Search Transactions")
    @allure.story("Sanity Checks")
    def test_default_pagination_returns_100_transactions(self, client, network):
        """Default request should return exactly 100 transactions."""
        response = client.search_transactions(network=network)
        assert response.status_code == 200

        data = response.json()
        transactions = data.get("transactions", [])

        assert len(transactions) == 100, "Must respect default limit"
        assert data["total_count"] > 100, "`total_count` must be > #returned"

    @allure.feature("Search Transactions")
    @allure.story("Sanity Checks")
    def test_missing_network_identifier_returns_error(self, client):
        """Missing network_identifier should return appropriate error."""
        response = client.search_transactions(network=None)
        assert response.status_code == 500

        error = response.json()
        error_message = get_error_message(error).lower()
        assert "network" in error_message or "identifier" in error_message, (
            "Error message should indicate missing network_identifier"
        )

    @allure.feature("Search Transactions")
    @allure.story("Sanity Checks")
    def test_invalid_network_returns_error_4002(self, client):
        """Invalid network should return error code 4002."""
        response = client.search_transactions(network="invalid_network")
        assert response.status_code == 500

        error = response.json()
        assert error["code"] == 4002, "Error code should be 4002"
        assert error["message"] == "Network not found", (
            "Error message should indicate network not found"
        )
        assert error["retriable"] is False, "`retriable` must be false"


class TestPaginationLimits:
    """Test pagination limit parameter."""

    @allure.feature("Search Transactions")
    @allure.story("Pagination - Limits")
    @pytest.mark.parametrize("limit", [0, 1, 9, 20, 100])
    def test_valid_limits(self, client, limit):
        """Test pagination with valid limit values."""
        response = client.search_transactions(limit=limit)
        assert response.status_code == 200

        data = response.json()
        transactions = data.get("transactions", [])

        if limit == 0:
            assert len(transactions) == 0, (
                f"Limit {limit} should return no transactions"
            )
        else:
            assert len(transactions) <= limit, (
                f"Should return at most {limit} transactions"
            )
            assert data["total_count"] >= len(transactions), (
                "total_count should be >= returned count"
            )

    @allure.feature("Search Transactions")
    @allure.story("Pagination - Limits")
    def test_limit_200_returns_error(self, client):
        """Limit 200 (above max) should return error."""
        response = client.search_transactions(limit=200)
        assert response.status_code == 500

        error = response.json()
        assert error["code"] == 5053, "Should return error code 5053 for invalid limit"

    @allure.feature("Search Transactions")
    @allure.story("Pagination - Limits")
    def test_limit_negative_returns_error(self, client):
        """Negative limit should return error."""
        response = client.search_transactions(limit=-1)
        # TODO: Standardize error codes for client validation errors
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )

        error = response.json()
        error_message = get_error_message(error).lower()
        assert any(
            word in error_message for word in ["negative", "invalid", "limit"]
        ), f"Error should mention negative/invalid/limit, got: {error}"


class TestPaginationOffsets:
    """Test pagination offset parameter."""

    @allure.feature("Search Transactions")
    @allure.story("Pagination - Offsets")
    @pytest.mark.parametrize("offset", [0, 1, 9, 20, 100])
    def test_valid_offsets(self, client, offset):
        """Test pagination with valid offset values."""
        response = client.search_transactions(offset=offset, limit=10)
        assert response.status_code == 200

        data = response.json()
        assert "transactions" in data
        assert "total_count" in data

    @allure.feature("Search Transactions")
    @allure.story("Pagination - Offsets")
    def test_offset_negative_returns_error(self, client):
        """Negative offset should return error."""
        response = client.search_transactions(offset=-1)
        # TODO: Standardize error codes for client validation errors
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )

        error = response.json()
        error_message = get_error_message(error).lower()
        assert any(
            word in error_message for word in ["negative", "invalid", "offset"]
        ), f"Error should mention negative/invalid/offset, got: {error}"


class TestTransactionIdentifier:
    """Test transaction identifier filtering."""

    @allure.feature("Search Transactions")
    @allure.story("Transaction Identifier")
    def test_valid_transaction_hash(self, client, network):
        """Search by transaction hash returns that specific transaction."""
        # Dynamically fetch a transaction to test with
        sample_response = client.search_transactions(network=network, limit=1)
        assert sample_response.status_code == 200
        sample_tx = sample_response.json()["transactions"][0]
        tx_hash = sample_tx["transaction"]["transaction_identifier"]["hash"]

        # Verify hash format (Cardano invariant - Blake2b-256)
        assert len(tx_hash) == 64, "Cardano tx hash must be 64 hex chars"
        assert all(c in "0123456789abcdef" for c in tx_hash.lower()), (
            "Hash must be hexadecimal"
        )

        # Search for it by hash
        response = client.search_transactions(
            network=network, transaction_identifier={"hash": tx_hash}
        )
        assert response.status_code == 200

        data = response.json()
        assert len(data["transactions"]) == 1, "Hash search must return exactly one tx"
        assert (
            data["transactions"][0]["transaction"]["transaction_identifier"]["hash"]
            == tx_hash
        )

    @allure.feature("Search Transactions")
    @allure.story("Transaction Identifier")
    def test_non_existent_transaction_hash_returns_empty(self, client):
        """Non-existent transaction hash returns empty results."""
        response = client.search_transactions(
            transaction_identifier={
                "hash": "0000000000000000000000000000000000000000000000000000000000000000"
            }
        )
        assert response.status_code == 200

        data = response.json()
        assert len(data["transactions"]) == 0, "Non-existent hash should return empty"

    @allure.feature("Search Transactions")
    @allure.story("Transaction Identifier")
    def test_invalid_transaction_hash_format_returns_empty(self, client):
        """Invalid hash format returns empty results."""
        response = client.search_transactions(
            transaction_identifier={"hash": "invalid_hash"}
        )
        assert response.status_code == 200

        data = response.json()
        assert len(data["transactions"]) == 0, "Invalid hash format should return empty"

    @allure.feature("Search Transactions")
    @allure.story("Transaction Identifier")
    def test_valid_payment_address_shelley_base_using_address_field_large_utxos(
        self, client, network_data
    ):
        """Test with address that has large UTXOs."""
        address = network_data["addresses"]["with_large_utxos"]
        response = client.search_transactions(account_identifier={"address": address})
        assert response.status_code == 200

        data = response.json()
        assert "transactions" in data


class TestAccountIdentifier:
    """Test account identifier filtering."""

    @allure.feature("Search Transactions")
    @allure.story("Account Identifier")
    @pytest.mark.parametrize(
        "address_type", ["shelley_base", "shelley_enterprise", "byron"]
    )
    def test_valid_payment_addresses(self, client, network_data, address_type):
        """Test filtering by different address types."""
        address = network_data["addresses"][address_type]

        response = client.search_transactions(account_identifier={"address": address})
        assert response.status_code == 200

        data = response.json()
        transactions = data.get("transactions", [])

        # All transactions should involve this address
        for tx in transactions:
            addresses_in_tx = []
            for op in tx.get("operations", []):
                if "account" in op and "address" in op["account"]:
                    addresses_in_tx.append(op["account"]["address"])

            if addresses_in_tx:  # Only assert if transaction has addresses
                assert address in addresses_in_tx, (
                    f"Transaction should involve {address_type} address"
                )

    @allure.feature("Search Transactions")
    @allure.story("Account Identifier")
    def test_valid_payment_address_shelley_base_using_address_field(
        self, client, network_data
    ):
        """Test filtering by shelley base address using address field."""
        address = network_data["addresses"]["with_large_utxos"]
        response = client.search_transactions(
            address=address  # Using address field directly
        )
        assert response.status_code == 200

        data = response.json()
        assert "transactions" in data

    @allure.feature("Search Transactions")
    @allure.story("Account Identifier")
    @pytest.mark.skip(
        reason="Future feature: stake address filtering not yet implemented (#575)"
    )
    def test_future_feature_stake_address(self, client, network_data):
        """Stake address filtering (marked as future feature)."""
        stake_addr = network_data["addresses"]["stake"]
        response = client.search_transactions(
            account_identifier={"address": stake_addr}
        )
        # Could be 200 (implemented) or 501 (not implemented)
        assert response.status_code in [200, 501]

    @allure.feature("Search Transactions")
    @allure.story("Account Identifier")
    def test_invalid_account_identifier_returns_empty(self, client):
        """Invalid address should return empty results."""
        response = client.search_transactions(
            account_identifier={"address": "invalid_address"}
        )
        assert response.status_code == 200

        data = response.json()
        assert len(data["transactions"]) == 0, "Invalid address should return empty"


class TestMaxBlock:
    """Test max_block filtering."""

    @allure.feature("Search Transactions")
    @allure.story("Max Block")
    @pytest.mark.parametrize("percentage", [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    def test_max_block_filtering_at_percentage(
        self, client, network, blockchain_height, percentage
    ):
        """Test max_block filtering at different percentages of blockchain height."""
        max_block = int(blockchain_height * percentage / 100)

        response = client.search_transactions(
            network=network, max_block=max_block, limit=25
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        # Core invariant: all returned transactions must respect max_block
        for tx in txs:
            block_index = tx["block_identifier"]["index"]
            assert block_index <= max_block, (
                f"Transaction at block {block_index} exceeds max_block {max_block}"
            )

    @allure.feature("Search Transactions")
    @allure.story("Max Block")
    def test_invalid_max_block_returns_error(self, client):
        """Invalid max_block should return error."""
        response = client.search_transactions(max_block=-1)
        # TODO: Standardize error codes for client validation errors
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )

        error = response.json()
        assert "code" in error or "message" in error, (
            "Error response should have code or message"
        )


class TestStatusFiltering:
    """Test status and success filtering."""

    @allure.feature("Search Transactions")
    @allure.story("Status Filtering")
    def test_status_invalid(self, client):
        """Filter by status='invalid'."""
        response = client.search_transactions(status="invalid")
        assert response.status_code == 200

        data = response.json()
        # All operations should have status "invalid"
        for tx in data.get("transactions", []):
            for op in tx.get("operations", []):
                assert op["status"] == "invalid"

    @allure.feature("Search Transactions")
    @allure.story("Status Filtering")
    def test_status_success(self, client):
        """Filter by status='success'."""
        response = client.search_transactions(status="success")
        assert response.status_code == 200

        data = response.json()
        # All operations should have status "success"
        for tx in data.get("transactions", []):
            for op in tx.get("operations", []):
                assert op["status"] == "success"

    @allure.feature("Search Transactions")
    @allure.story("Status Filtering")
    def test_success_true(self, client):
        """Filter by success=true."""
        response = client.search_transactions(success=True)
        assert response.status_code == 200

        data = response.json()
        # All operations should have status "success"
        for tx in data.get("transactions", []):
            for op in tx.get("operations", []):
                assert op["status"] == "success"

    @allure.feature("Search Transactions")
    @allure.story("Status Filtering")
    def test_success_false(self, client):
        """Filter by success=false."""
        response = client.search_transactions(success=False)
        assert response.status_code == 200

        data = response.json()
        # All operations should have status "invalid"
        for tx in data.get("transactions", []):
            for op in tx.get("operations", []):
                assert op["status"] == "invalid"

    @allure.feature("Search Transactions")
    @allure.story("Status Filtering")
    def test_status_unknown_returns_error(self, client):
        """Unknown status should return error."""
        response = client.search_transactions(status="UNKNOWN_STATUS")
        # TODO: Standardize error codes for invalid enum values
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )


class TestOperationTypeFiltering:
    """Test operation type filtering."""

    OPERATION_TYPES = [
        "withdrawal",
        "stakeKeyRegistration",
        "stakeKeyDeregistration",
        "stakeDelegation",
        "poolRetirement",
        "poolRegistration",
        "dRepVoteDelegation",
        "poolGovernanceVote",
    ]

    @allure.feature("Search Transactions")
    @allure.story("Operation Type Filtering")
    @pytest.mark.parametrize("op_type", OPERATION_TYPES)
    @pytest.mark.skip(reason="Operation type filtering not implemented (#540)")
    def test_filter_by_operation_type(self, client, op_type):
        """Filter transactions by operation type."""
        response = client.search_transactions(type=op_type)
        assert response.status_code == 200

        data = response.json()
        # All transactions should contain this operation type
        for tx in data.get("transactions", []):
            op_types = [op["type"] for op in tx.get("operations", [])]
            assert op_type in op_types, (
                f"Transaction should contain {op_type} operation"
            )

    @allure.feature("Search Transactions")
    @allure.story("Operation Type Filtering")
    @pytest.mark.skip(reason="Operation type filtering not implemented (#540)")
    def test_invalid_operation_type_returns_error(self, client):
        """Invalid operation type returns error code 5053."""
        response = client.search_transactions(type="INVALID_OPERATION")
        # TODO: Standardize error codes for invalid enum values
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )

        error = response.json()
        # API returns 5058 instead of expected 5053
        if "code" in error:
            assert error["code"] in [5053, 5058], (
                f"Expected error code 5053 or 5058, got {error.get('code')}"
            )


class TestLogicalOperators:
    """Test logical operator combinations."""

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    @pytest.mark.skip(reason="Operation type filtering not implemented (#540)")
    def test_account_identifier_and_withdrawal_explicit(self, client, network_data):
        """AND operator with account and withdrawal."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="and",
            account_identifier={"address": address},
            type="withdrawal",
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            # Must have the address
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            assert address in addresses, "Transaction should contain the account"

            # Must have withdrawal operation
            op_types = [op["type"] for op in tx["operations"]]
            assert "withdrawal" in op_types, (
                "Transaction should contain withdrawal operation"
            )

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    def test_account_identifier_and_invalid_explicit(self, client, network_data):
        """AND operator with account and invalid status."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="and",
            account_identifier={"address": address},
            status="invalid",
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            # Must have the address
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            assert address in addresses

            # Must have invalid status
            statuses = [op["status"] for op in tx["operations"]]
            assert "invalid" in statuses

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    @pytest.mark.skip(reason="Operation type filtering not implemented (#540)")
    def test_account_identifier_or_stakedelegation_explicit(self, client, network_data):
        """OR operator with account and stakeDelegation."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="or",
            account_identifier={"address": address},
            type="stakeDelegation",
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            op_types = [op["type"] for op in tx["operations"]]

            # Must have either the address OR stakeDelegation
            assert address in addresses or "stakeDelegation" in op_types

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    def test_account_identifier_or_invalid_explicit(self, client, network_data):
        """OR operator with account and invalid status."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="or",
            account_identifier={"address": address},
            status="invalid",
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            statuses = [op["status"] for op in tx["operations"]]

            # Must have either the address OR invalid status
            assert address in addresses or "invalid" in statuses

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    @pytest.mark.skip(reason="Operation type filtering not implemented (#540)")
    def test_address_and_withdrawal_explicit(self, client, network_data):
        """AND operator with address field and withdrawal."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="and", address=address, type="withdrawal"
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            op_types = [op["type"] for op in tx["operations"]]

            assert address in addresses
            assert "withdrawal" in op_types

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    def test_address_and_invalid_explicit(self, client, network_data):
        """AND operator with address field and invalid status."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="and", address=address, status="invalid"
        )
        assert response.status_code == 200

        data = response.json()
        for tx in data.get("transactions", []):
            addresses = [
                op.get("account", {}).get("address")
                for op in tx["operations"]
                if op.get("account")
            ]
            statuses = [op["status"] for op in tx["operations"]]

            assert address in addresses
            assert "invalid" in statuses

    @allure.feature("Search Transactions")
    @allure.story("Logical Operators")
    def test_invalid_operator_returns_error(self, client, network_data):
        """Invalid operator returns error."""
        address = network_data["addresses"]["shelley_base"]

        response = client.search_transactions(
            operator="xor",
            account_identifier={"address": address},
            type="withdrawal",
        )
        # TODO: Standardize error codes for invalid enum values
        assert response.status_code in [400, 500], (
            f"Expected 400 or 500, got {response.status_code}"
        )


class TestCurrencyFiltering:
    """Test currency filtering."""

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.skip(
        reason="Currency filtering not implemented in search/transactions (#610, #542)"
    )
    def test_ada_filter(self, client, network):
        """Filter by ada currency."""
        response = client.search_transactions(
            network=network, currency={"symbol": "ada", "decimals": 6}, limit=5
        )
        assert response.status_code == 200

        # Verify filtering works - ALL returned transactions must have ADA
        txs = response.json()["transactions"]
        for tx in txs:
            currencies = [
                op["amount"]["currency"]["symbol"]
                for op in tx.get("operations", [])
                if "amount" in op and "currency" in op["amount"]
            ]
            assert currencies, (
                "ADA filter must not return transactions without currency operations"
            )
            assert "ADA" in currencies, (
                "ADA filter must only return transactions with ADA"
            )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.skip(
        reason="Currency filtering not implemented in search/transactions (#610, #542)"
    )
    def test_ada_uppercase_filter(self, client, network):
        """Filter by ADA currency (uppercase) - case insensitive."""
        response = client.search_transactions(
            network=network, currency={"symbol": "ADA", "decimals": 6}, limit=5
        )
        assert response.status_code == 200

        # Verify case-insensitive filtering works
        txs = response.json()["transactions"]
        for tx in txs:
            currencies = [
                op["amount"]["currency"]["symbol"]
                for op in tx.get("operations", [])
                if "amount" in op and "currency" in op["amount"]
            ]
            assert currencies, (
                "ADA filter must not return transactions without currency operations"
            )
            assert "ADA" in currencies, (
                "ADA filter must only return transactions with ADA"
            )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.skip(
        reason="Currency filtering not implemented in search/transactions (#610, #542)"
    )
    def test_lovelace_filter(self, client, network):
        """Filter by lovelace currency."""
        response = client.search_transactions(
            network=network, currency={"symbol": "lovelace", "decimals": 0}, limit=5
        )
        assert response.status_code == 200

        # Verify filtering works - ALL returned transactions must have lovelace/ADA
        txs = response.json()["transactions"]
        for tx in txs:
            currencies = [
                op["amount"]["currency"]["symbol"]
                for op in tx.get("operations", [])
                if "amount" in op and "currency" in op["amount"]
            ]
            assert currencies, (
                "lovelace filter must not return transactions without currency operations"
            )
            # Lovelace and ADA are the same, API might return either
            assert any(c in ["lovelace", "ADA"] for c in currencies), (
                "lovelace filter must only return transactions with lovelace/ADA"
            )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.skip(
        reason="Currency filtering not implemented in search/transactions (#610, #542)"
    )
    def test_native_asset_filtering_by_symbol(self, client, network, network_data):
        """Test that currency filtering works for native assets."""
        # Use known native asset from test data
        asset = network_data["assets"][0]

        # Search by this asset
        response = client.search_transactions(
            network=network,
            currency={"symbol": asset["symbol"], "decimals": asset["decimals"]},
            limit=5,
        )
        assert response.status_code == 200

        # Verify all returned transactions contain this asset
        txs = response.json()["transactions"]
        assert len(txs) > 0, "Currency filter should return transactions"

        for tx in txs:
            currencies = [
                op["amount"]["currency"]["symbol"]
                for op in tx.get("operations", [])
                if "amount" in op and "currency" in op["amount"]
            ]
            assert asset["symbol"] in currencies, (
                "Currency filter must only return transactions with specified asset"
            )
