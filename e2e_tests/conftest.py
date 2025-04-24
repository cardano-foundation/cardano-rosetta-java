import pytest
import os

@pytest.fixture(scope="session")
def drep_key_hash_id():
    """Get the DRep key hash ID from environment variables."""
    drep_id = os.environ.get("DREP_KEY_HASH_ID")
    if not drep_id:
        pytest.skip("DREP_KEY_HASH_ID environment variable is required for DRep key hash tests")
    return drep_id

@pytest.fixture(scope="session")
def drep_script_hash_id():
    """Get the DRep script hash ID from environment variables."""
    drep_id = os.environ.get("DREP_SCRIPT_HASH_ID")
    if not drep_id:
        pytest.skip("DREP_SCRIPT_HASH_ID environment variable is required for DRep script hash tests")
    return drep_id 