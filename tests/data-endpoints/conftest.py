"""
Shared fixtures for data endpoint tests.
"""

import os
import pytest
from client import RosettaClient


@pytest.fixture(scope="session")
def rosetta_url():
    """Base URL for Rosetta API."""
    return os.environ.get("ROSETTA_URL", "http://localhost:8082")


@pytest.fixture(scope="session")
def network():
    """Configured Cardano network (preprod, mainnet, preview, etc)."""
    return os.environ.get("CARDANO_NETWORK", "preprod")


@pytest.fixture
def client(rosetta_url):
    """Rosetta API client instance."""
    with RosettaClient(base_url=rosetta_url) as client:
        yield client


@pytest.fixture(scope="module")
def blockchain_height(network_status):
    """
    Get current blockchain height once per test module.

    Cached to avoid repeated network_status calls.
    Fails loudly if blockchain is too young for integration testing.
    """
    height = network_status["current_block_identifier"]["index"]

    if height < 100:
        raise AssertionError(
            f"Blockchain too young ({height} blocks). "
            f"Need at least 100 blocks for integration testing."
        )

    return height


@pytest.fixture(scope="session")
def network_status(rosetta_url, network):
    """
    Get network status once per test session.

    Cached to avoid repeated calls and used for configuration detection.
    """
    with RosettaClient(base_url=rosetta_url) as client:
        return client.network_status(network=network).json()


@pytest.fixture(scope="session")
def is_pruned_instance(network_status):
    """
    Detect if running against a pruned instance.

    Pruned instances return oldest_block_identifier in network status.
    """
    return "oldest_block_identifier" in network_status


@pytest.fixture(scope="session")
def oldest_block_identifier(network_status, is_pruned_instance):
    """
    Get oldest fully queryable block if pruning is enabled.

    Returns None for non-pruned instances.
    Below this block index, blocks might have missing data due to pruning.
    """
    if is_pruned_instance:
        return network_status["oldest_block_identifier"]["index"]
    return None


@pytest.fixture(scope="session")
def has_token_registry(rosetta_url, network):
    """
    Detect if token registry is enabled.

    Checks for token metadata in a known token transaction.
    This is a best-effort detection - returns False if unsure.
    """
    # This would need a known token transaction to check
    # For now, we'll default to assuming it's enabled in v1.4.0
    # TODO: Implement actual detection logic
    return True


@pytest.fixture(scope="session")
def has_peer_discovery(network_status, network):
    """
    Detect if peer discovery is enabled.

    When peer discovery is DISABLED: API returns only static bootstrap peers from topology
    When peer discovery is ENABLED: API returns dynamic peers discovered via P2P

    We detect this by checking if returned peers differ from known bootstrap peers.
    """
    if "peers" not in network_status:
        return False

    # Known bootstrap peers per network
    bootstrap_peers = {
        "mainnet": [
            "backbone.cardano.iog.io",
            "backbone.mainnet.cardanofoundation.org",
            "backbone.mainnet.emurgornd.com"
        ],
        "preprod": [
            "preprod-node.play.dev.cardano.org"
        ],
        "preview": [
            "preview-node.play.dev.cardano.org"
        ]
    }

    known_bootstraps = bootstrap_peers.get(network, [])
    peers = network_status.get("peers", [])

    # Check if any peer is NOT a bootstrap peer (indicating dynamic discovery)
    for peer in peers:
        peer_address = peer.get("metadata", {}).get("address", "")
        # If we find a peer that's not in bootstrap list, discovery is enabled
        if peer_address and not any(bootstrap in peer_address for bootstrap in known_bootstraps):
            return True

    # If all peers are bootstrap peers, discovery is disabled
    return False


@pytest.fixture(scope="session")
def network_data(network):
    """
    Load network-specific test data from YAML.

    Returns test addresses, assets, and other network-specific data.
    Fails loudly if network has no test data configured.
    """
    import yaml
    from pathlib import Path

    config_file = Path(__file__).parent / "network_test_data.yaml"

    if not config_file.exists():
        raise FileNotFoundError(
            f"Network test data file not found: {config_file}\n"
            f"Create this file with test data for network '{network}'"
        )

    with open(config_file) as f:
        all_data = yaml.safe_load(f)

    if network not in all_data:
        available = ", ".join(all_data.keys())
        raise ValueError(
            f"No test data configured for network '{network}'.\n"
            f"Available networks: {available}\n"
            f"Add '{network}' section to {config_file}"
        )

    return all_data[network]


def get_error_message(error_response):
    """
    Extract error message from API error response.

    Handles multiple error response formats:
    - {"message": "..."}
    - {"message": "...", "details": {"message": "..."}}
    - {"details": {"message": "..."}}

    Returns combined message from all available fields.
    """
    message = error_response.get("message", "")
    details_message = error_response.get("details", {}).get("message", "")
    return (message + " " + details_message).strip()


def pytest_configure(config):
    """Configure pytest hooks for handling pruning markers."""
    config.addinivalue_line(
        "markers",
        "requires_full_history: mark test as requiring complete historical data"
    )


def pytest_runtest_setup(item):
    """Skip tests based on pruning configuration."""
    # Skip tests marked with requires_full_history when running against pruned instance
    if item.get_closest_marker("requires_full_history"):
        # Check if we're running against a pruned instance
        network_status = item.funcargs.get("network_status")
        if network_status and "oldest_block_identifier" in network_status:
            pytest.skip("Test requires full history - skipping on pruned instance")
