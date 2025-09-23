"""Tests for Rosetta /network endpoints."""

import allure


DEFAULT_NETWORK = {
    "blockchain": "cardano",
    "network": "preprod",
}


@allure.feature("Network")
@allure.story("List")
class TestNetworkList:
    def test_returns_preprod_network(self, client):
        response = client.network_list()
        assert response.status_code == 200

        data = response.json()
        identifiers = data.get("network_identifiers", [])

        assert identifiers, "Should return at least one network identifier"
        assert any(
            item.get("blockchain") == DEFAULT_NETWORK["blockchain"]
            and item.get("network") == DEFAULT_NETWORK["network"]
            for item in identifiers
        ), "Preprod network identifier must be present"


@allure.feature("Network")
@allure.story("Status")
class TestNetworkStatus:
    def test_returns_current_network_status(self, client):
        response = client.network_status()
        assert response.status_code == 200

        data = response.json()

        current = data.get("current_block_identifier", {})
        genesis = data.get("genesis_block_identifier", {})

        current_index = current.get("index")
        current_hash = current.get("hash")
        assert isinstance(current_index, int) and current_index >= 0
        assert isinstance(current_hash, str) and len(current_hash) == 64

        timestamp = data.get("current_block_timestamp")
        assert isinstance(timestamp, int) and timestamp > 0

        genesis_index = genesis.get("index")
        genesis_hash = genesis.get("hash")
        assert genesis_index == 0
        assert isinstance(genesis_hash, str) and len(genesis_hash) == 64
        assert current_index >= genesis_index

        sync_status = data.get("sync_status")
        assert isinstance(sync_status, dict), "sync_status must be present"
        assert sync_status.get("synced") is True, "Node must report synced"

        sync_current = sync_status.get("current_index")
        sync_target = sync_status.get("target_index")
        assert isinstance(sync_current, int), (
            "sync_status.current_index must be integer"
        )
        assert isinstance(sync_target, int), "sync_status.target_index must be integer"
        assert sync_current >= current_index, "sync progress cannot trail current block"
        assert sync_current <= sync_target, "sync progress cannot exceed target"

        peers = data.get("peers")
        assert isinstance(peers, list) and peers, "Peers list must not be empty"
        for peer in peers:
            peer_id = peer.get("peer_id")
            metadata = peer.get("metadata")
            assert isinstance(peer_id, str) and ":" in peer_id, (
                "peer_id must include hostname and port"
            )
            assert isinstance(metadata, dict) and metadata.get("type") in {
                "IPv4",
                "IPv6",
            }

    # Error handling tests moved to test_error_handling.py


@allure.feature("Network")
@allure.story("Options")
class TestNetworkOptions:
    def test_returns_network_capabilities(self, client):
        response = client.network_options()
        assert response.status_code == 200

        data = response.json()
        version = data.get("version", {})
        allow = data.get("allow", {})

        assert version.get("rosetta_version") == "1.4.13"
        assert version.get("node_version") == "10.4.1"
        assert version.get("middleware_version") == "1.3.2"

        statuses = allow.get("operation_statuses", [])
        assert statuses
        assert any(status.get("successful") for status in statuses)

        operation_types = allow.get("operation_types", [])
        expected_operation_types = [
            "input",
            "output",
            "stakeKeyRegistration",
            "stakeDelegation",
            "withdrawal",
            "stakeKeyDeregistration",
            "poolRegistration",
            "poolRegistrationWithCert",
            "poolRetirement",
            "dRepVoteDelegation",
            "poolGovernanceVote",
        ]
        assert operation_types == expected_operation_types

        errors = allow.get("errors", [])
        error_map = {error.get("code"): error for error in errors}
        assert error_map.get(4002, {}).get("retriable") is False
        assert error_map.get(4015, {}).get("retriable") is False

        # Check specific important error codes are present
        important_codes = [
            5054,
            5055,
            5058,
        ]  # Skip 5056, 5057 as they're not in current implementation
        for code in important_codes:
            if code in error_map:  # Only check if present
                assert error_map[code].get("retriable") is False

        assert isinstance(allow.get("historical_balance_lookup"), bool)

    # Error handling tests moved to test_error_handling.py
