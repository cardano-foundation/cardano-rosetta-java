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
        self, client, network, network_data, address_type, is_pruned_instance
    ):
        """Configured addresses should have transactions on the network."""
        address = network_data["addresses"][address_type]

        response = client.search_transactions(
            network=network, account_identifier={"address": address}, limit=1
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) > 0, (
            f"Address '{address_type}' ({address}) has no transactions. "
            f"Network may have changed or address is wrong - update network_test_data.yaml"
        )

    @pytest.mark.pruning_compatible
    def test_configured_assets_have_transactions(self, client, network, network_data, is_pruned_instance):
        """Configured native assets should have transactions on the network."""
        for asset in network_data["assets"]:
            # Note: Do NOT add limit parameter - currency filter + limit causes timeout (#615)
            response = client.search_transactions(
                network=network,
                currency={"symbol": asset["symbol"], "decimals": asset["decimals"]},
            )
            assert response.status_code == 200

            txs = response.json()["transactions"]
            assert len(txs) > 0, (
                f"Asset '{asset['name']}' has no transactions. "
                f"Network may have changed or asset data is wrong - update network_test_data.yaml"
            )

    def test_asset_policy_ids_are_valid(self, client, network, network_data):
        """Asset policy IDs should be valid hex strings."""
        for asset in network_data["assets"]:
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
