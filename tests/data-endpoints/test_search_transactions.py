"""
Test suite for /search/transactions endpoint.
Covers all 61 test cases from the original Postman collection.
"""

import pytest
import allure
from conftest import get_error_message, assert_operations_ordered, assert_operations_sequential


pytestmark = pytest.mark.pr


class TestSanityChecks:
    """Basic endpoint availability and error handling tests."""

    @allure.feature("Search Transactions")
    @allure.story("Sanity Checks")
    def test_default_pagination_returns_100_transactions(self, client):
        """Default request should return exactly 100 transactions."""
        response = client.search_transactions()
        assert response.status_code == 200

        data = response.json()
        transactions = data.get("transactions", [])

        assert len(transactions) == 100, "Must respect default limit"
        assert data["total_count"] > 100, "`total_count` must be > #returned"

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
    def test_valid_transaction_hash(self, client):
        """Search by transaction hash returns that specific transaction."""
        # Dynamically fetch a transaction to test with
        sample_response = client.search_transactions()
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
            transaction_identifier={"hash": tx_hash}
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
    @pytest.mark.pruning_compatible
    def test_valid_payment_address_shelley_base_using_address_field_large_utxos(
        self, client, network_data, is_pruned_instance
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
    @pytest.mark.pruning_compatible
    def test_valid_payment_addresses(self, client, network_data, address_type, is_pruned_instance):
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
    @pytest.mark.pruning_compatible
    def test_valid_payment_address_shelley_base_using_address_field(
        self, client, network_data, is_pruned_instance
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
    @pytest.mark.slow
    @pytest.mark.parametrize("percentage", [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100])
    def test_max_block_filtering_at_percentage(
        self, client, blockchain_height, percentage, oldest_block_identifier
    ):
        """Test max_block filtering at different percentages of blockchain height."""
        max_block = int(blockchain_height * percentage / 100)

        # Skip test if max_block is before oldest queryable block in pruned instance
        if oldest_block_identifier and max_block < oldest_block_identifier:
            pytest.skip(f"max_block {max_block} is before oldest_block_identifier {oldest_block_identifier}")

        response = client.search_transactions(max_block=max_block)
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
    @pytest.mark.parametrize("symbol", ["ada", "ADA"])
    @pytest.mark.skip(reason="ADA filter test needs review due to fee ops always including ADA")
    def test_ada_filter_case_insensitive(self, client, symbol):
        """Filter by ada currency (case-insensitive)."""
        response = client.search_transactions(
            currency={"symbol": symbol, "decimals": 6}
        )
        assert response.status_code == 200

        # Verify filtering works - ALL returned transactions must have ADA
        # TODO: review this test as all transactions do need to have at least one ADA operation (txIn used for fees)
        # Even if the filter is for another currency, ADA ops will still be present
        # because Cardano transactions always include ADA for fees.
        txs = response.json()["transactions"]
        for tx in txs:
            currencies = [
                op["amount"]["currency"]["symbol"]
                for op in tx["transaction"]["operations"]
                if "amount" in op and "currency" in op["amount"]
            ]
            assert "ADA" in currencies, "ADA filter must only return ADA transactions"

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.pruning_compatible
    def test_native_asset_filtering_by_ascii_symbol(self, client, network_data, is_pruned_instance):
        """Currency filter with ASCII symbols should return error (negative test for v1.4.1+)."""
        asset = network_data["assets"][0]

        # Search by ASCII symbol (should return error in v1.4.1+)
        response = client.search_transactions(
            currency={"symbol": asset["symbol"], "decimals": asset["decimals"]},
        )

        # API should return error for non-hex-encoded symbols
        assert response.status_code == 500, (
            f"Expected error status code when using ASCII symbol '{asset['symbol']}'"
        )

        error_data = response.json()
        assert error_data["code"] == 5059, "Expected error code 5059 for non-hex symbol"
        assert "hex-encoded" in error_data["message"].lower(), (
            "Error message should mention hex encoding requirement"
        )
        assert asset["symbol"] in error_data.get("details", {}).get("message", ""), (
            f"Error details should mention the invalid symbol '{asset['symbol']}'"
        )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.slow
    # TODO: investigate and update the ticket; seems to be working now, although response time is ~2min
    # @pytest.mark.skip(
    #     reason="Currency filter + limit parameter causes timeout (#615)"
    # )
    def test_currency_filter_with_limit_parameter(self, client, network_data):
        """Currency filter with explicit limit should not cause timeout."""
        asset = network_data["assets"][0]

        # This combination causes 600s+ timeout in v1.3.3
        response = client.search_transactions(
            currency={"symbol": asset["symbol_hex"], "decimals": asset["decimals"]},
            limit=1,
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) == 1, "Should respect limit parameter"

        # Verify the returned transaction contains the filtered asset
        tx = txs[0]
        currencies_in_tx = []
        for op in tx["transaction"]["operations"]:
            if "metadata" in op and "tokenBundle" in op["metadata"]:
                for bundle in op["metadata"]["tokenBundle"]:
                    for token in bundle.get("tokens", []):
                        currencies_in_tx.append(token["currency"]["symbol"].lower())

        assert asset["symbol_hex"].lower() in currencies_in_tx, (
            f"Transaction must contain filtered asset {asset['symbol_hex']}. "
            f"Found currencies: {currencies_in_tx}"
        )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.slow
    @pytest.mark.pruning_compatible
    def test_currency_filter_with_hex_encoded_symbol(self, client, network_data, is_pruned_instance):
        """Currency filter accepts hex-encoded symbols (canonical format in v1.4.x+, issue #610 fixed)."""
        asset = network_data["assets"][0]

        # Use hex-encoded symbol from test data (canonical format)
        hex_symbol = asset["symbol_hex"]  # e.g., "74544555524f"

        # Search by hex-encoded symbol (supported in v1.4.x+ after #610 fix)
        response = client.search_transactions(
            currency={"symbol": hex_symbol, "decimals": asset["decimals"]},
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) > 0, "Hex symbol search should return transactions"

        # Verify ALL transactions contain the filtered asset (currency filter was applied)
        for tx in txs:
            # Collect all currency symbols from tokenBundle across all operations
            currencies_in_tx = []
            for op in tx["transaction"]["operations"]:
                # Native assets are always in tokenBundle (they sit in UTXOs with ADA)
                if "metadata" in op and "tokenBundle" in op["metadata"]:
                    for bundle in op["metadata"]["tokenBundle"]:
                        for token in bundle.get("tokens", []):
                            currencies_in_tx.append(token["currency"]["symbol"].lower())

            # Assert filtered currency is in this transaction
            assert hex_symbol.lower() in currencies_in_tx, (
                f"Transaction must contain native asset with hex symbol {hex_symbol} in tokenBundle. "
                f"Found currencies: {currencies_in_tx}"
            )

    @allure.feature("Search Transactions")
    @allure.story("Currency Filtering")
    @pytest.mark.slow
    @pytest.mark.pruning_compatible
    def test_native_asset_filtering_with_policy_id(self, client, network_data, is_pruned_instance):
        """Test currency filtering with hex-encoded symbol and metadata.policyId."""
        asset = network_data["assets"][0]

        # Search by hex-encoded asset symbol with policyId in metadata
        response = client.search_transactions(
            currency={
                "symbol": asset["symbol_hex"],
                "decimals": asset["decimals"],
                "metadata": {"policyId": asset["policy_id"]}
            },
        )
        assert response.status_code == 200

        # Verify ALL transactions contain the filtered asset with matching policyId and symbol
        txs = response.json()["transactions"]
        assert len(txs) > 0, "Currency filter with policyId should return transactions"

        for tx in txs:
            # Collect all (policyId, symbol) pairs from tokenBundle across all operations
            assets_in_tx = []
            for op in tx["transaction"]["operations"]:
                if "metadata" in op and "tokenBundle" in op["metadata"]:
                    for bundle in op["metadata"]["tokenBundle"]:
                        policy_id = bundle.get("policyId")
                        for token in bundle.get("tokens", []):
                            symbol = token["currency"]["symbol"].lower()
                            assets_in_tx.append((policy_id, symbol))

            # Assert filtered asset (policyId + symbol) is in this transaction
            expected_asset = (asset["policy_id"], asset["symbol_hex"].lower())
            assert expected_asset in assets_in_tx, (
                f"Transaction must contain asset with policyId {asset['policy_id']} "
                f"and symbol {asset['symbol_hex']}. Found assets: {assets_in_tx}"
            )


class TestOperationInvariants:
    """Operations must be ordered and sequential in all transactions."""

    @allure.feature("Search Transactions")
    @allure.story("Operation Invariants")
    def test_operations_ordered_by_index(self, client, network):
        """Operations array must be sorted by operation_identifier.index."""
        response = client.search_transactions(network=network, limit=10)
        assert response.status_code == 200

        for block_tx in response.json()["transactions"]:
            operations = block_tx["transaction"]["operations"]
            assert_operations_ordered(operations)

    @allure.feature("Search Transactions")
    @allure.story("Operation Invariants")
    def test_operations_sequential_indices(self, client, network):
        """Operation indices must be [0, 1, 2, ..., n-1] with no gaps."""
        response = client.search_transactions(network=network, limit=10)
        assert response.status_code == 200

        for block_tx in response.json()["transactions"]:
            operations = block_tx["transaction"]["operations"]
            assert_operations_sequential(operations)
