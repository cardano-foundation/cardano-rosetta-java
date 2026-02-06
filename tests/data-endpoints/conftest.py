"""
Shared fixtures for data endpoint tests.
"""

import os
import pytest
from pathlib import Path
from dotenv import load_dotenv
from client import RosettaClient


# Load .env file at module import time; explicit .env values override the parent env
load_dotenv(Path(__file__).parent / ".env", override=True)


@pytest.fixture(scope="session")
def rosetta_url():
    """Base URL for Rosetta API."""
    return os.environ.get("ROSETTA_URL", "http://localhost:8082")


@pytest.fixture(scope="session")
def network():
    """Configured Cardano network (preprod, mainnet, preview, etc)."""
    network = os.environ.get("CARDANO_NETWORK")
    if not network:
        raise ValueError(
            "CARDANO_NETWORK environment variable is required. "
            "Set it to 'preprod', 'mainnet', 'preview', etc."
        )
    return network


@pytest.fixture
def client(rosetta_url, network):
    """Rosetta API client instance with configured network."""
    with RosettaClient(base_url=rosetta_url, default_network=network) as client:
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
    with RosettaClient(base_url=rosetta_url, default_network=network) as client:
        return client.network_status().json()


@pytest.fixture(scope="session")
def pruning_enabled():
    """Read REMOVE_SPENT_UTXOS from environment."""
    return os.environ.get("REMOVE_SPENT_UTXOS", "false").lower() == "true"


@pytest.fixture(scope="session")
def grace_window():
    """Read pruning grace window from environment."""
    return int(os.environ.get("REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT", "2160"))


@pytest.fixture(scope="session")
def is_pruned_instance(pruning_enabled):
    """
    Check if running against a pruned instance.

    Reads from environment configuration instead of API detection.
    """
    return pruning_enabled


@pytest.fixture(scope="session")
def oldest_block_identifier(network_status, is_pruned_instance):
    """
    Get oldest fully queryable block if pruning is enabled.

    Returns None for non-pruned instances.
    Below this block index, blocks might have missing data due to pruning.

    NOTE: This reads from API response, not configuration.
    Use this to validate the API behavior, not to detect pruning.
    """
    if is_pruned_instance:
        return network_status.get("oldest_block_identifier", {}).get("index")
    return None


@pytest.fixture(scope="session")
def has_token_registry():
    """Read TOKEN_REGISTRY_ENABLED from environment."""
    return os.environ.get("TOKEN_REGISTRY_ENABLED", "false").lower() == "true"


@pytest.fixture(scope="session")
def has_peer_discovery():
    """Read PEER_DISCOVERY from environment."""
    return os.environ.get("PEER_DISCOVERY", "false").lower() == "true"


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



