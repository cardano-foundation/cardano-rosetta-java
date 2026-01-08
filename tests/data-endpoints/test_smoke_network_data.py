"""
Smoke tests for network test data validation.

These tests validate that the test data in network_test_data.yaml is still valid
for the configured network (preprod, mainnet, etc.).

They are marked as smoke tests and run FIRST to catch stale test data early.

Purpose:
- Alert when network test data becomes stale (addresses have no transactions)
- Validate that configured addresses/assets still have transactions
- Help maintain test data quality across networks

Run with: pytest -m smoke
Skip with: pytest -m "not smoke"
"""

import pytest
import allure


@pytest.mark.pr
@pytest.mark.smoke
@allure.feature("Smoke Tests")
@allure.story("Network Test Data Validation")
class TestNetworkDataValidity:
    """Validates that network_test_data.yaml entries are current and usable."""

    @pytest.mark.parametrize(
        "address_type",
        ["shelley_base", "shelley_enterprise", "byron", "with_large_utxos"],
    )
    @pytest.mark.pruning_compatible
    def test_configured_address_has_transactions(
        self, client, network, network_data, address_type):
        """Configured addresses should have transactions on the network."""
        address = network_data["addresses"][address_type]

        response = client.search_transactions(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) > 0, (
            f"Address '{address_type}' ({address}) has no transactions. "
            f"Network may have changed or address is wrong - update network_test_data.yaml"
        )

    @pytest.mark.pruning_compatible
    def test_configured_assets_exist_in_historical_balance(self, client, network, network_data):
        """Configured native assets should exist in historical balance at test_block."""
        assets = network_data["assets"]
        assert len(assets) > 0, f"No assets configured for network '{network}'"

        for asset in assets:
            # Use historical balance query at a known block - much faster than search
            test_address = asset["test_address"]
            test_block = asset["test_block"]

            response = client.account_balance(
                network=network,
                account_identifier={"address": test_address},
                block_identifier={"index": test_block},
            )
            assert response.status_code == 200, (
                f"Failed to query historical balance for asset '{asset['name']}' "
                f"at block {test_block}"
            )

            balances = response.json()["balances"]

            # Collect all currency symbols from balances
            balance_symbols = {b["currency"]["symbol"].lower() for b in balances}

            assert asset["symbol_hex"].lower() in balance_symbols, (
                f"Asset '{asset['name']}' with symbol {asset['symbol_hex']} not found "
                f"in historical balance at block {test_block}. "
                f"Found symbols: {balance_symbols}. "
                f"Update test_address/test_block in network_test_data.yaml."
            )

    def test_asset_policy_ids_are_valid(self, client, network, network_data):
        """Asset policy IDs should be valid hex strings."""
        assets = network_data["assets"]
        assert len(assets) > 0, f"No assets configured for network '{network}'"

        for asset in assets:
            policy_id = asset["policy_id"]

            # Policy ID should be 56 hex characters (28 bytes)
            assert len(policy_id) == 56, (
                f"Asset '{asset['name']}' policy_id should be 56 hex chars, "
                f"got {len(policy_id)}"
            )
            assert all(c in "0123456789abcdef" for c in policy_id.lower()), (
                f"Asset '{asset['name']}' policy_id must be hexadecimal"
            )

    @pytest.mark.parametrize(
        "address_type", ["shelley_base", "shelley_enterprise", "byron"]
    )
    def test_addresses_work_on_account_balance(
        self, client, network, network_data, address_type
    ):
        """Configured addresses should return valid balance data."""
        address = network_data["addresses"][address_type]

        response = client.account_balance(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200, (
            f"Address '{address_type}' ({address}) failed on /account/balance. "
            f"Update network_test_data.yaml"
        )

        balances = response.json()["balances"]
        assert len(balances) > 0, f"Address '{address_type}' has no balances"

    @pytest.mark.parametrize(
        "address_type", ["shelley_base", "shelley_enterprise", "byron"]
    )
    def test_addresses_work_on_account_coins(
        self, client, network, network_data, address_type
    ):
        """Configured addresses should return valid coins data."""
        address = network_data["addresses"][address_type]

        response = client.account_coins(
            network=network, account_identifier={"address": address}
        )
        assert response.status_code == 200, (
            f"Address '{address_type}' ({address}) failed on /account/coins. "
            f"Update network_test_data.yaml"
        )

        # Byron/enterprise addresses may have 0 UTXOs - just verify endpoint works
        coins = response.json()["coins"]
        assert isinstance(coins, list)
