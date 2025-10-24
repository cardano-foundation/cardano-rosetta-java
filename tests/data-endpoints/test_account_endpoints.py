"""
Tests for Rosetta /account endpoints.

Tests both /account/balance and /account/coins endpoints with behavioral assertions.
"""

import pytest
import allure
from conftest import get_error_message

pytestmark = pytest.mark.pr


@allure.feature("Account")
@allure.story("Account Balance")
class TestAccountBalance:
    """Test /account/balance endpoint."""

    @pytest.mark.pr
    def test_get_current_balance(self, client, network, network_data):
        """Get current account balance."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        data = response.json()
        balances = data["balances"]

        # Should have at least ADA balance
        assert len(balances) > 0, "Account should have at least one balance"

        # ADA balance should be first
        assert balances[0]["currency"]["symbol"] == "ADA", (
            "ADA balance must be first in array"
        )

        # Balance should be non-negative
        ada_value = int(balances[0]["value"])
        assert ada_value >= 0, "Balance cannot be negative"

    def test_multiple_balances_for_multi_asset_account(
        self, client, network, network_data
    ):
        """Account with native assets should have multiple balances."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        balances = response.json()["balances"]

        # This address is known to have native assets (from smoke tests)
        # Should have ADA + native assets
        assert len(balances) > 1, "Multi-asset account should have multiple balances"

        # All balances should have required fields
        for balance in balances:
            assert "value" in balance
            assert "currency" in balance
            assert "symbol" in balance["currency"]
            assert "decimals" in balance["currency"]

    def test_historical_balance_query(self, client, network, network_data):
        """Historical balance queries should work for payment addresses."""
        address = network_data["addresses"]["shelley_base"]

        # Get current balance
        current_response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        current_block = current_response.json()["block_identifier"]["index"]
        current_balance = int(current_response.json()["balances"][0]["value"])

        # Get historical balance
        historical_block = current_block - 1000000
        if historical_block < 0:
            raise AssertionError(
                f"Blockchain too young ({current_block} blocks). "
                f"Need at least 1M blocks for historical query test."
            )

        historical_response = client.account_balance(
            network=network,
            account_identifier={"address": address},
            block_identifier={"index": historical_block},
        )
        assert historical_response.status_code == 200

        returned_block = historical_response.json()["block_identifier"]["index"]
        assert returned_block == historical_block, (
            "Should return balance at requested block"
        )

        # Balances should differ (account has transactions between blocks)
        historical_balance = int(historical_response.json()["balances"][0]["value"])
        assert historical_balance != current_balance, (
            "Balance should change across 1M blocks for active address"
        )

    @pytest.mark.skip(
        reason="Known bug: stake addresses ignore block_identifier, return current balance (#590)"
    )
    def test_stake_address_historical_balance_bug(self, client, network, network_data):
        """Stake addresses with block_identifier should return error or historical balance."""
        stake_addr = network_data["addresses"]["stake"]

        # This SHOULD work but is known to be buggy
        response = client.account_balance(
            network=network,
            account_identifier={"address": stake_addr},
            block_identifier={"index": 3500000},
        )
        assert response.status_code == 200

        # Should return balance AT requested block, not current
        assert response.json()["block_identifier"]["index"] == 3500000

    def test_account_balance_structure(self, client, network, network_data):
        """Test balance response structure."""
        address = network_data["addresses"]["shelley_enterprise"]

        response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        data = response.json()

        # Response must have block_identifier
        assert "block_identifier" in data
        assert data["block_identifier"]["index"] >= 0

        # Response must have balances array
        assert "balances" in data
        assert isinstance(data["balances"], list)

        # At minimum, should have ADA balance
        assert len(data["balances"]) >= 1
        assert data["balances"][0]["currency"]["symbol"] == "ADA"

    def test_currencies_filter_for_ada_only(self, client, network, network_data):
        """currencies parameter should filter to only requested currencies."""
        address = network_data["addresses"]["shelley_base"]

        # Get all balances
        all_response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        all_balances = all_response.json()["balances"]

        # This address has multiple assets (verified in smoke tests)
        assert len(all_balances) > 1, "Test address should have multiple balances"

        # Filter to ADA only
        filtered_response = client.account_balance(
            network=network,
            account_identifier={"address": address},
            currencies=[{"symbol": "ADA", "decimals": 6}],
        )
        assert filtered_response.status_code == 200

        filtered_balances = filtered_response.json()["balances"]

        # Should return only ADA balance
        assert len(filtered_balances) == 1, "currencies filter should return only ADA"
        assert filtered_balances[0]["currency"]["symbol"] == "ADA"

        # Verify it's less than total balances
        assert len(filtered_balances) < len(all_balances), (
            "Filtered results should be subset of all balances"
        )

    def test_invalid_address_returns_error(self, client, network):
        """Invalid address format should return error."""
        response = client.account_balance(
            network=network, account_identifier={"address": "invalid_address"}
        )
        assert response.status_code == 500


@allure.feature("Account")
@allure.story("Account Coins")
class TestAccountCoins:
    """Test /account/coins endpoint (unspent UTXOs)."""

    def test_get_unspent_utxos(self, client, network, network_data):
        """Get unspent UTXOs for account."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        data = response.json()
        coins = data["coins"]

        # Address is known to have UTXOs (from smoke tests)
        assert len(coins) > 0, "Account should have unspent UTXOs"

        # Validate first UTXO structure
        coin = coins[0]
        assert "coin_identifier" in coin
        assert "amount" in coin

        # UTXO identifier format: "tx_hash:output_index"
        utxo_id = coin["coin_identifier"]["identifier"]
        parts = utxo_id.split(":")
        assert len(parts) == 2, "UTXO identifier must be tx_hash:output_index"

        tx_hash, output_index = parts
        assert len(tx_hash) == 64, "Transaction hash must be 64 hex chars"
        assert all(c in "0123456789abcdef" for c in tx_hash.lower())
        assert output_index.isdigit(), "Output index must be numeric"

    @pytest.mark.requires_full_history
    def test_utxos_match_search_transactions_outputs(
        self, client, network, network_data
    ):
        """UTXOs from /account/coins should match coin_created in /search/transactions."""
        address = network_data["addresses"]["shelley_base"]

        # Get UTXOs
        coins_response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        utxo_id = coins_response.json()["coins"][0]["coin_identifier"]["identifier"]

        # Search for this UTXO in transactions
        search_response = client.search_transactions(
            network=network, coin_identifier={"identifier": utxo_id}
        )
        assert search_response.status_code == 200

        txs = search_response.json()["transactions"]
        assert len(txs) == 1, "Coin search should return exactly one transaction"

        # Verify the transaction created this UTXO
        tx_hash = txs[0]["transaction"]["transaction_identifier"]["hash"]
        assert utxo_id.startswith(tx_hash), (
            "UTXO should reference its creation transaction"
        )

    def test_coins_response_structure(self, client, network, network_data):
        """Test coins response structure."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        data = response.json()

        # Response must have block_identifier
        assert "block_identifier" in data
        assert data["block_identifier"]["index"] >= 0

        # Response must have coins array
        assert "coins" in data
        assert isinstance(data["coins"], list)

    @pytest.mark.requires_full_history
    def test_all_coins_are_unspent(self, client, network, network_data):
        """All returned coins should be unspent (not consumed in later transactions)."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        coins = response.json()["coins"]

        # Check first few coins to ensure they're unspent
        for coin in coins[:5]:
            utxo_id = coin["coin_identifier"]["identifier"]

            # Search for this UTXO being spent
            search_response = client.search_transactions(
                network=network, coin_identifier={"identifier": utxo_id}
            )

            # Transaction should have coin_created, not coin_spent
            tx = search_response.json()["transactions"][0]
            for op in tx["transaction"]["operations"]:
                if "coin_change" in op:
                    coin_change = op["coin_change"]
                    if coin_change["coin_identifier"]["identifier"] == utxo_id:
                        assert coin_change["coin_action"] == "coin_created", (
                            "UTXO in /account/coins must be unspent (coin_created, not coin_spent)"
                        )

    @pytest.mark.skip(
        reason="currencies filter not implemented on /account/coins (#614)"
    )
    def test_currencies_filter_for_ada_utxos_only(self, client, network, network_data):
        """currencies parameter should filter to only UTXOs with requested currencies."""
        address = network_data["addresses"]["shelley_base"]

        # Get all UTXOs
        all_response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        all_coins = all_response.json()["coins"]

        # Filter to ADA-only UTXOs
        filtered_response = client.account_coins(
            network=network,
            account_identifier={"address": address},
            currencies=[{"symbol": "ADA", "decimals": 6}],
        )
        assert filtered_response.status_code == 200

        filtered_coins = filtered_response.json()["coins"]

        # Should return fewer coins (only pure ADA UTXOs, not multi-asset UTXOs)
        assert len(filtered_coins) < len(all_coins), (
            "currencies filter should exclude multi-asset UTXOs"
        )

        # All returned UTXOs should only have ADA
        for coin in filtered_coins:
            assert coin["amount"]["currency"]["symbol"] == "ADA"
            # Should not have native assets in metadata
            assert not coin.get("metadata"), (
                "ADA-only filter should not return UTXOs with native assets"
            )


@allure.feature("Account")
@allure.story("Account Errors")
class TestAccountErrors:
    """Test error handling for account endpoints."""

    def test_missing_account_identifier_returns_error(self, client, network):
        """Missing account_identifier should return error for /account/balance."""
        response = client.account_balance(network=network)
        assert response.status_code == 400, "Missing required parameter should return 400"

        error = response.json()
        error_message = get_error_message(error).lower()
        assert "account" in error_message or "identifier" in error_message

    def test_invalid_address_format_returns_error(self, client, network):
        """Invalid address format should return error."""
        response = client.account_balance(
            network=network, account_identifier={"address": "not_an_address"}
        )
        assert response.status_code == 500

    def test_invalid_block_index_in_historical_query(
        self, client, network, network_data
    ):
        """Negative block index in historical query."""
        address = network_data["addresses"]["shelley_base"]

        response = client.account_balance(
            network=network,
            account_identifier={"address": address},
            block_identifier={
                "index": -2
            },  # -1 might be valid (genesis parent), use -2
        )
        # May return error or may treat as genesis parent
        assert response.status_code in [200, 400, 500]
