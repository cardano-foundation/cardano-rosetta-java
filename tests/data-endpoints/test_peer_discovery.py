"""Peer discovery TTL validation (v1.4.0)."""
import time
import pytest
import allure


@allure.feature("Peer Discovery")
@allure.story("Dynamic Peer List")
class TestPeerDiscovery:
    """Validate dynamic peer discovery with TTL."""

    @pytest.mark.weekly
    @pytest.mark.requires_peer_discovery
    @pytest.mark.slow
    @pytest.mark.skip(reason="Peer discovery refresh takes 65+ minutes - reserved for weekly tests (#619)")
    def test_peer_list_changes_over_time(self, client, network, has_peer_discovery):
        """Verify peer discovery refreshes and peers change over time (60 min refresh cycle)."""
        if not has_peer_discovery:
            pytest.skip("Peer discovery not enabled")

        # Get initial peer list
        peers_t0 = client.network_status(network=network).json().get("peers", [])
        assert peers_t0, "Initial peer list should not be empty"

        # Wait for peer discovery refresh cycle (60 min + 5 min buffer)
        wait_minutes = 65
        print(f"Waiting {wait_minutes} minutes for peer discovery refresh...")
        time.sleep(wait_minutes * 60)

        # Get peer list after refresh
        peers_t1 = client.network_status(network=network).json().get("peers", [])
        assert peers_t1, "Peer list after refresh should not be empty"

        # Verify peers changed (dynamic discovery is working)
        assert peers_t0 != peers_t1, (
            f"Peers should change after {wait_minutes} minutes (discovery refresh cycle). "
            f"T0 peers: {len(peers_t0)}, T1 peers: {len(peers_t1)}"
        )
