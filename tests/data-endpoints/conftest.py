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
def blockchain_height(rosetta_url, network):
    """
    Get current blockchain height once per test module.

    Cached to avoid repeated network_status calls.
    Fails loudly if blockchain is too young for integration testing.
    """
    with RosettaClient(base_url=rosetta_url) as client:
        status = client.network_status(network=network).json()
        height = status["current_block_identifier"]["index"]

        if height < 100:
            raise AssertionError(
                f"Blockchain too young ({height} blocks). "
                f"Need at least 100 blocks for integration testing."
            )

        return height


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
