"""Peer discovery TTL validation (v1.4.0)."""
import os
import pytest
import allure
from pathlib import Path

import yaml


@allure.feature("Peer Discovery")
@allure.story("Dynamic Peer List")
class TestPeerDiscovery:
    """Validate dynamic peer discovery with TTL."""

    @pytest.mark.nightly
    @pytest.mark.requires_peer_discovery
    @pytest.mark.slow
    @pytest.mark.skip(reason="Peer discovery takes too long to populate dynamic peers (#619)")
    def test_peer_list_contains_dynamic_entries(self, client, network, has_peer_discovery):
        """Verify peer discovery returns peers beyond the static bootstrap list."""
        if not has_peer_discovery:
            pytest.skip("Peer discovery not enabled")

        repo_root = Path(__file__).resolve().parents[2]
        topology_file = repo_root / "config" / "node" / network / "topology.json"
        assert topology_file.exists(), f"Bootstrap topology file not found: {topology_file}"

        bootstrap_hosts = set()
        try:
            topology = yaml.safe_load(topology_file.read_text(encoding="utf-8"))
            for peer in topology.get("bootstrapPeers", []):
                host = peer.get("address")
                if host:
                    bootstrap_hosts.add(host)
        except Exception as exc:
            pytest.fail(f"Unable to parse bootstrap topology {topology_file}: {exc}")
        assert bootstrap_hosts, f"Bootstrap topology {topology_file} does not define any bootstrap peers"

        peers = client.network_status(network=network).json().get("peers", [])
        peer_hosts = set()
        for peer in peers:
            peer_id = peer.get("peer_id", "")
            if ":" in peer_id:
                host = peer_id.split(":", 1)[0]
                if host:
                    peer_hosts.add(host)

        assert peer_hosts, "Peer discovery should return peers with peer_id host values"

        dynamic_hosts = peer_hosts - bootstrap_hosts
        assert dynamic_hosts, (
            "Peer discovery did not return any dynamic peers beyond bootstrap list. "
            f"Bootstrap hosts: {sorted(bootstrap_hosts)}; Reported peers: {sorted(peer_hosts)}"
        )
