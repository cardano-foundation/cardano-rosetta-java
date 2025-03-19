import pytest
import logging
import time
import os
import traceback
from typing import Dict, List, Optional

# Import the execution function from the utility module
from tests.stake_operations import execute_stake_operation_test

logger = logging.getLogger("test")


@pytest.mark.order(1)
def test_scenario1_stake_key_registration(rosetta_client, test_wallet):
    """
    Scenario 1, Step 1: Registration

    Test stake key registration using the Rosetta Construction API.
    """
    logger.info("⬧ Scenario 1.1: Stake Key Registration")

    # Bypassing stake key registration check and assuming it's unregistered by default
    logger.debug("Skipping registration check and assuming stake key is unregistered")

    try:
        logger.info("▹ Executing registration operation")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="stake key registration",
            required_funds={
                "deposit": 2_000_000,  # 2 ADA deposit for stake key registration
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["registration"],
            pool_id=None,
        )
        logger.info("✓ Stake key registered successfully")
    except Exception as e:
        logger.error(f"✗ Registration failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(2)
def test_scenario1_stake_delegation(rosetta_client, test_wallet):
    """
    Scenario 1, Step 2: Delegation

    Test stake delegation using the Rosetta Construction API.

    This test assumes that the stake key is already registered (test_stake_key_registration
    should be run first).
    """
    # Check if STAKE_POOL_HASH is set
    pool_hash = os.getenv("STAKE_POOL_HASH")
    if not pool_hash:
        pytest.skip(
            "STAKE_POOL_HASH environment variable is required for delegation tests"
        )

    logger.info(f"⬧ Scenario 1.2: Delegation » {pool_hash[:8]}...")

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
        pytest.skip(
            "Stake key must be registered before delegation. Run test_stake_key_registration first."
        )

    try:
        logger.info("▹ Executing delegation operation")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="stake delegation",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["delegation"],
            pool_id=pool_hash,
        )
        logger.info(f"✓ Stake key delegated to pool {pool_hash[:8]}...")
    except Exception as e:
        logger.error(f"✗ Delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(3)
def test_scenario1_stake_key_deregistration(rosetta_client, test_wallet):
    """
    Scenario 1, Step 3: Deregistration

    Test stake key deregistration using the Rosetta Construction API.

    This test assumes that the stake key is already registered
    (test_stake_key_registration should be run first).
    """
    logger.info("⬧ Scenario 1.3: Stake Key Deregistration")

    # Check if stake key is registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.info("▹ Checking registration status")

    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")

        # If we get a successful response, the stake key is registered
        # Proceed with deregistration test
        pass
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Cannot check stake key: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        pytest.skip(
            "Stake key is not registered. Run test_stake_key_registration first."
        )

    try:
        logger.info("▹ Executing deregistration operation")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="stake key deregistration",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection only
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["deregistration"],
            pool_id=None,
        )
        logger.info("✓ Stake key deregistered successfully")
    except Exception as e:
        logger.error(f"✗ Deregistration failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(4)
def test_scenario2_combined_registration_delegation(rosetta_client, test_wallet):
    """
    Scenario 2, Step 1: Combined Registration and Delegation

    Test combined stake key registration and delegation using the Rosetta Construction API.

    This test does not assume that the stake key is already registered, it will
    register and delegate in a single transaction.
    """
    # Check if STAKE_POOL_HASH is set
    pool_hash = os.getenv("STAKE_POOL_HASH")
    if not pool_hash:
        pytest.skip(
            "STAKE_POOL_HASH environment variable is required for delegation tests"
        )

    logger.info(f"⬧ Scenario 2.1: Register+Delegate » {pool_hash[:8]}...")

    try:
        logger.info("▹ Executing combined operation")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="combined stake registration and delegation",
            required_funds={
                "deposit": 2_000_000,  # 2 ADA deposit
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["registration", "delegation"],
            pool_id=pool_hash,
        )
        logger.info(f"✓ Stake key registered and delegated to pool {pool_hash[:8]}...")
    except Exception as e:
        logger.error(f"✗ Combined operation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(5)
def test_scenario2_drep_vote_delegation_abstain(rosetta_client, test_wallet):
    """
    Scenario 2, Step 2: DRep Vote Delegation - Abstain

    Test DRep vote delegation to Abstain using the Rosetta Construction API.
    This test assumes that the stake key is already registered from the previous step.
    """
    logger.info("⬧ Scenario 2.2: DRep Vote Delegation » Type: abstain")

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
        pytest.skip(
            "Stake key must be registered before vote delegation. Run test_scenario2_combined_registration_delegation first."
        )

    try:
        logger.info("▹ Executing DRep vote delegation operation - Abstain")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="DRep vote delegation",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["drepVoteDelegation"],
            pool_id=None,  # Not used for DRep vote delegation
            drep_id=None,  # Not needed for abstain
            drep_type="abstain",
        )
        logger.info("✓ Vote delegated to DRep type: abstain")
    except Exception as e:
        logger.error(f"✗ DRep vote delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(6)
def test_scenario2_drep_vote_delegation_no_confidence(rosetta_client, test_wallet):
    """
    Scenario 2, Step 3: DRep Vote Delegation - No Confidence

    Test DRep vote delegation to No Confidence using the Rosetta Construction API.
    This test assumes that the stake key is already registered from the previous steps.
    """
    logger.info("⬧ Scenario 2.3: DRep Vote Delegation » Type: no_confidence")

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
        pytest.skip(
            "Stake key must be registered before vote delegation. Run test_scenario2_combined_registration_delegation first."
        )

    try:
        logger.info("▹ Executing DRep vote delegation operation - No Confidence")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="DRep vote delegation",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["drepVoteDelegation"],
            pool_id=None,  # Not used for DRep vote delegation
            drep_id=None,  # Not needed for no_confidence
            drep_type="no_confidence",
        )
        logger.info("✓ Vote delegated to DRep type: no_confidence")
    except Exception as e:
        logger.error(f"✗ DRep vote delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(7)
def test_scenario2_drep_vote_delegation_key_hash(rosetta_client, test_wallet):
    """
    Scenario 2, Step 4: DRep Vote Delegation - Key Hash

    Test DRep vote delegation to a specific DRep (key hash) using the Rosetta Construction API.
    This test assumes that the stake key is already registered from the previous steps.
    """
    # Check if DREP_KEY_HASH_ID is set
    drep_id = os.getenv("DREP_KEY_HASH_ID")
    if not drep_id:
        pytest.skip(
            "DREP_KEY_HASH_ID environment variable is required for DRep vote delegation tests with type key_hash"
        )

    logger.info(
        f"⬧ Scenario 2.4: DRep Vote Delegation » Type: key_hash, ID: {drep_id[:8]}..."
    )

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
        pytest.skip(
            "Stake key must be registered before vote delegation. Run test_scenario2_combined_registration_delegation first."
        )

    try:
        logger.info("▹ Executing DRep vote delegation operation - Key Hash")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="DRep vote delegation",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["drepVoteDelegation"],
            pool_id=None,  # Not used for DRep vote delegation
            drep_id=drep_id,
            drep_type="key_hash",
        )
        logger.info(f"✓ Vote delegated to DRep type: key_hash, ID: {drep_id[:8]}...")
    except Exception as e:
        logger.error(f"✗ DRep vote delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(8)
def test_scenario2_drep_vote_delegation_script_hash(rosetta_client, test_wallet):
    """
    Scenario 2, Step 5: DRep Vote Delegation - Script Hash

    Test DRep vote delegation to a specific DRep (script hash) using the Rosetta Construction API.
    This test assumes that the stake key is already registered from the previous steps.
    """
    # Check if DREP_SCRIPT_HASH_ID is set
    drep_id = os.getenv("DREP_SCRIPT_HASH_ID")
    if not drep_id:
        pytest.skip(
            "DREP_SCRIPT_HASH_ID environment variable is required for DRep vote delegation tests with type script_hash"
        )

    logger.info(
        f"⬧ Scenario 2.5: DRep Vote Delegation » Type: script_hash, ID: {drep_id[:8]}..."
    )

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
        pytest.skip(
            "Stake key must be registered before vote delegation. Run test_scenario2_combined_registration_delegation first."
        )

    try:
        logger.info("▹ Executing DRep vote delegation operation - Script Hash")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="DRep vote delegation",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["drepVoteDelegation"],
            pool_id=None,  # Not used for DRep vote delegation
            drep_id=drep_id,
            drep_type="script_hash",
        )
        logger.info(f"✓ Vote delegated to DRep type: script_hash, ID: {drep_id[:8]}...")
    except Exception as e:
        logger.error(f"✗ DRep vote delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


@pytest.mark.order(9)
def test_scenario2_stake_key_deregistration(rosetta_client, test_wallet):
    """
    Scenario 2, Step 6: Deregistration

    Test stake key deregistration using the Rosetta Construction API.

    This test should be run after the combined registration and delegation test.
    """
    logger.info("⬧ Scenario 2.6: Stake Key Deregistration")

    # Check if stake key is registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.info("▹ Checking registration status")

    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered as expected: {account_info}")

        # If we get a successful response, the stake key is registered
        # Proceed with deregistration test
        pass
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Cannot check stake key: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        pytest.skip(
            "Stake key is not registered. Run combined registration and delegation test first."
        )

    try:
        logger.info("▹ Executing deregistration operation")
        execute_stake_operation_test(
            rosetta_client=rosetta_client,
            test_wallet=test_wallet,
            operation_type="stake key deregistration",
            required_funds={
                "fee": 200_000,  # 0.2 ADA initial estimate for UTXO selection only
                "min_output": 1_000_000,  # 1 ADA minimum for outputs
            },
            operation_types=["deregistration"],
            pool_id=None,
        )
        logger.info("✓ Stake key deregistered successfully")
    except Exception as e:
        logger.error(f"✗ Deregistration failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise
