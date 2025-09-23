"""
Shared fixtures for data endpoint tests.
"""

import os
import pytest
from client import RosettaClient


@pytest.fixture
def rosetta_url():
    """Base URL for Rosetta API."""
    return os.environ.get("ROSETTA_URL", "http://localhost:8082")


@pytest.fixture
def client(rosetta_url):
    """Rosetta API client instance."""
    with RosettaClient(base_url=rosetta_url) as client:
        yield client
