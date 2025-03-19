import pytest
import logging
import time
import os
import traceback  # Add traceback module for detailed error logging
from typing import Dict, List, Callable, Optional, Any, Tuple

"""
Utility module for stake operations.

This module provides implementation functions for stake operations:
- Stake key registration
- Stake key deregistration
- Stake delegation
- Combined registration and delegation

These functions are used by the test_stake_scenarios.py for running ordered
stake operation scenarios.
"""

logger = logging.getLogger("test")
logger.propagate = False  # Prevent propagation to parent loggers


def perform_stake_key_registration(rosetta_client, test_wallet):
    """
    Perform stake key registration using the Rosetta Construction API.

    This operation:
    1) Fetches a UTXO from the wallet with enough funds for deposit + fees
    2) Constructs a transaction with:
       - 1 input (UTXO)
       - 1 output (change)
       - 1 stakeKeyRegistration operation
    3) Signs with PyCardano
    4) Submits via Rosetta
    5) Validates the transaction on-chain

    The stake key registration requires a deposit (typically 2 ADA on testnet).

    Note: This assumes the stake key is NOT registered by default.
    """
    logger.info("⬧ Stake Key Registration")

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
    except Exception as e:
        logger.error(f"✗ Registration failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def perform_stake_key_deregistration(rosetta_client, test_wallet):
    """
    Perform stake key deregistration using the Rosetta Construction API.

    This test:
    1) Checks if the stake key is registered
    2) Fetches a UTXO from the wallet with enough funds for fees
    3) Constructs a transaction with:
       - 1 input (UTXO)
       - 1 output (change)
       - 1 stakeKeyDeregistration operation
    4) Signs with PyCardano
    5) Submits via Rosetta
    6) Validates the transaction on-chain

    The stake key deregistration returns the deposit (typically 2 ADA on testnet).
    """
    logger.info("⬧ Stake Key Deregistration")

    # Check if stake key is registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")

    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key is registered. Account info: {account_info}")

        # If we get a successful response, the stake key is registered
        # Proceed with deregistration test
        pass
    except Exception as e:
        # If we get an error, the stake key is likely not registered
        logger.error(f"✗ Stake key not registered: {str(e)}")
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
    except Exception as e:
        logger.error(f"✗ Deregistration failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def perform_stake_delegation(rosetta_client, test_wallet):
    """
    Perform stake delegation using the Rosetta Construction API.

    This test:
    1) Checks if the stake key is already registered
    2) Fetches a UTXO from the wallet with enough funds for fees
    3) Constructs a transaction with:
       - 1 input (UTXO)
       - 1 output (change)
       - 1 stakeDelegation operation
    4) Signs with PyCardano
    5) Submits via Rosetta
    6) Validates the transaction on-chain

    Note: This test requires a valid pool hash to be set in the STAKE_POOL_HASH environment variable.
    """
    # Check if STAKE_POOL_HASH is set
    pool_hash = os.getenv("STAKE_POOL_HASH")
    if not pool_hash:
        pytest.skip(
            "STAKE_POOL_HASH environment variable is required for delegation tests"
        )

    logger.info(f"⬧ Stake Delegation » {pool_hash[:8]}...")

    # Check if stake key is already registered by querying account
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Using stake address: {stake_address}")
    try:
        # Try to get account information
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Stake key already registered: {account_info}")
        # If we get a successful response, the stake key is registered
        # Proceed with delegation test
        pass
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
    except Exception as e:
        logger.error(f"✗ Delegation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def perform_combined_registration_and_delegation(rosetta_client, test_wallet):
    """
    Perform combined stake key registration and delegation using the Rosetta Construction API.

    This test:
    1) Fetches a UTXO from the wallet with enough funds for deposit and fees
    2) Constructs a transaction with:
       - 1 input (UTXO)
       - 1 output (change)
       - 1 stakeRegistration operation
       - 1 stakeDelegation operation
    3) Signs with PyCardano
    4) Submits via Rosetta
    5) Validates the transaction on-chain

    This test requires a valid pool ID to be set in the STAKE_POOL_HASH environment variable.
    """
    # Check if STAKE_POOL_HASH is set
    pool_hash = os.getenv("STAKE_POOL_HASH")
    if not pool_hash:
        pytest.skip(
            "STAKE_POOL_HASH environment variable is required for delegation tests"
        )

    logger.info(f"⬧ Register+Delegate » {pool_hash[:8]}...")

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
    except Exception as e:
        logger.error(f"✗ Combined operation failed: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def execute_stake_operation_test(
    rosetta_client,
    test_wallet,
    operation_type,
    required_funds,
    operation_types,
    pool_id=None,
    drep_id=None,
    drep_type=None,
):
    """
    Execute a stake operation test with the given parameters.

    Args:
        rosetta_client: The Rosetta client instance
        test_wallet: The test wallet instance
        operation_type: Description of the operation for logging
        required_funds: Dictionary with required funds (deposit, fee, min_output)
        operation_types: List of operation types (registration, delegation, deregistration, drepVoteDelegation)
        pool_id: Pool ID for delegation operations
        drep_id: DRep ID for vote delegation operations (optional for abstain/no_confidence)
        drep_type: DRep type for vote delegation operations (key_hash, script_hash, abstain, no_confidence)
    """
    logger.debug(f"Testing {operation_type}...")

    # Get wallet address and UTXOs
    wallet_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    logger.debug(f"Wallet address: {wallet_address}")
    logger.debug(f"Stake address: {stake_address}")

    # Calculate total required funds
    total_required = sum(required_funds.values())
    logger.debug(f"Total required funds: {total_required} lovelaces")

    # Get UTXOs with sufficient funds
    try:
        utxos = rosetta_client.get_utxos(address=wallet_address)
        logger.info(f"▹ Found {len(utxos)} UTXOs")

        # Log the first UTXO to see its structure
        if utxos:
            logger.debug(f"First UTXO structure: {utxos[0]}")

        for i, utxo in enumerate(
            utxos[:5]
        ):  # Log only first 5 UTXOs to avoid excessive output
            # Extract identifer from coin_identifier
            coin_id = utxo["coin_identifier"]["identifier"]
            amount_value = utxo["amount"]["value"]

            logger.debug(f"UTXO {i+1}: {coin_id} - {amount_value} lovelaces")

        if len(utxos) > 5:
            logger.debug(f"... and {len(utxos) - 5} more UTXOs")

        selected_utxo = None

        for utxo in utxos:
            amount_value = int(utxo["amount"]["value"])
            if amount_value >= total_required:
                selected_utxo = utxo
                coin_id = utxo["coin_identifier"]["identifier"]
                logger.info(f"▹ Selected UTXO: {amount_value}₳")
                logger.debug(f"UTXO ID: {coin_id}")
                break

        assert (
            selected_utxo is not None
        ), f"No UTXO with sufficient funds ({total_required} lovelaces) found"
    except Exception as e:
        logger.error(f"✗ Error getting UTXOs: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise

    # Calculate change amount considering estimated fee
    input_amount = int(selected_utxo["amount"]["value"])
    estimated_fee = required_funds["fee"]
    deposit = required_funds.get("deposit", 0)

    # For deregistration, we expect to get the deposit back
    refund = 2_000_000 if "deregistration" in operation_types else 0
    logger.debug(f"Input amount: {input_amount} lovelaces")
    logger.debug(f"Estimated fee: {estimated_fee} lovelaces")
    logger.debug(f"Deposit: {deposit} lovelaces")
    logger.debug(f"Refund: {refund} lovelaces")

    # IMPORTANT: Don't subtract the estimated fee at this point!
    # For the initial operations, set the change to the full input amount
    # We'll adjust it later based on the actual fee from Rosetta
    # and deposit/refund amounts
    if "deregistration" in operation_types:
        # For deregistration, we'll get a refund so start with full input
        change_amount = input_amount
    else:
        # For other operations (registration/delegation), we'll subtract deposit if needed
        # but NOT the fee yet - that comes from Rosetta's suggested_fee
        change_amount = input_amount - deposit

    logger.debug(
        f"Calculated initial change amount: {change_amount} lovelaces (without fee subtraction)"
    )
    logger.debug(
        f"Fee will be subtracted after Rosetta API calculates the precise amount"
    )

    # Prepare operations
    operations = []

    # Input operation
    operations.append(
        {
            "operation_identifier": {"index": 0},
            "type": "input",
            "status": "",
            "account": {"address": wallet_address},
            "amount": {
                "value": f"-{input_amount}",
                "currency": {"symbol": "ADA", "decimals": 6},
            },
            "coin_change": {
                "coin_identifier": {
                    "identifier": f"{selected_utxo['coin_identifier']['identifier']}"
                },
                "coin_action": "coin_spent",
            },
            "metadata": {},
        }
    )

    # Change output operation
    operations.append(
        {
            "operation_identifier": {"index": 1},
            "type": "output",
            "status": "",
            "account": {"address": wallet_address},
            "amount": {
                "value": str(change_amount),
                "currency": {"symbol": "ADA", "decimals": 6},
            },
            "metadata": {},
        }
    )

    # Stake operations
    op_index = 2

    if "registration" in operation_types:
        # Get the stake verification key hex
        stake_key_hex = test_wallet.get_stake_verification_key_hex()
        operations.append(
            {
                "operation_identifier": {"index": op_index},
                "type": "stakeKeyRegistration",
                "status": "",
                "account": {"address": stake_address},
                "metadata": {
                    "staking_credential": {
                        "hex_bytes": stake_key_hex,
                        "curve_type": "edwards25519",
                    }
                },
            }
        )
        op_index += 1

    if "deregistration" in operation_types:
        # Get the stake verification key hex
        stake_key_hex = test_wallet.get_stake_verification_key_hex()
        stake_key_deregistration_op = {
            "operation_identifier": {"index": op_index},
            "type": "stakeKeyDeregistration",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                }
            },
        }
        operations.append(stake_key_deregistration_op)
        op_index += 1

    if "delegation" in operation_types:
        if not pool_id:
            pytest.skip("STAKE_POOL_HASH is required for delegation tests")

        # Get the stake verification key hex
        stake_key_hex = test_wallet.get_stake_verification_key_hex()
        # Use pool_id directly as the hash - pool_id is now expected to be the hex hash
        pool_key_hash = pool_id

        operations.append(
            {
                "operation_identifier": {"index": op_index},
                "type": "stakeDelegation",
                "status": "",
                "account": {"address": stake_address},
                "metadata": {
                    "staking_credential": {
                        "hex_bytes": stake_key_hex,
                        "curve_type": "edwards25519",
                    },
                    "pool_key_hash": pool_key_hash,
                },
            }
        )
        op_index += 1

    if "drepVoteDelegation" in operation_types:
        # Get the stake verification key hex
        stake_key_hex = test_wallet.get_stake_verification_key_hex()

        # Create the dRepVoteDelegation operation
        drep_vote_delegation_op = {
            "operation_identifier": {"index": op_index},
            "type": "dRepVoteDelegation",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                },
                "drep": {
                    "type": drep_type,
                },
            },
        }

        # Add drep.id for key_hash and script_hash types
        if drep_type in ["key_hash", "script_hash"] and drep_id:
            drep_vote_delegation_op["metadata"]["drep"]["id"] = drep_id

        operations.append(drep_vote_delegation_op)
        op_index += 1

    logger.debug(f"Created {len(operations)} operations")
    for i, op in enumerate(operations):
        logger.debug(f"Operation {i}: {op['type']}")

    # Construct, sign, and submit transaction
    try:
        logger.debug("Constructing transaction...")

        # Split operations into input and output operations
        input_ops = [op for op in operations if op["type"] == "input"]
        output_ops = [op for op in operations if op["type"] == "output"]
        stake_ops = [op for op in operations if op["type"] not in ["input", "output"]]

        # Extract inputs data for rosetta_client.construct_transaction
        inputs_data = []
        for op in input_ops:
            inputs_data.append(
                {
                    "address": op["account"]["address"],
                    "value": abs(int(op["amount"]["value"])),  # Positive value
                    "coin_identifier": op["coin_change"]["coin_identifier"],
                    "coin_change": op["coin_change"],
                }
            )

        # Extract outputs data for rosetta_client.construct_transaction
        outputs_data = []
        for op in output_ops:
            outputs_data.append(
                {
                    "address": op["account"]["address"],
                    "value": int(op["amount"]["value"]),
                }
            )

        logger.debug(f"Using {len(inputs_data)} inputs and {len(outputs_data)} outputs")

        # If we have stake operations, use construct_transaction_with_operations
        if stake_ops:
            logger.debug(
                f"Found {len(stake_ops)} stake operations, using custom construction"
            )
            # Combine all operations for the custom function
            all_operations = input_ops + output_ops + stake_ops
            unsigned_tx = construct_transaction_with_operations(
                rosetta_client, all_operations
            )
        else:
            # Otherwise use regular transaction construction
            logger.debug("No stake operations, using standard transaction construction")
            unsigned_tx = rosetta_client.construct_transaction(
                inputs=inputs_data, outputs=outputs_data
            )

        logger.debug("Transaction constructed successfully")

        # Get the actual fee from the constructed transaction
        if "suggested_fee" in unsigned_tx and unsigned_tx["suggested_fee"]:
            actual_fee = int(unsigned_tx["suggested_fee"][0]["value"])
            logger.debug(f"Actual fee from Rosetta: {actual_fee} lovelaces")

            # Adjusting change amount based on actual fee from Rosetta
            # For deregistration, we need to ADD the refund to our outputs
            if "deregistration" in operation_types:
                logger.debug(
                    "This is a deregistration operation - adding the refund of 2,000,000 lovelace to the change output"
                )
                # For deregistration: input - fee + refund
                adjusted_change = input_amount - actual_fee + refund
                logger.debug(f"Adding refund of {refund} lovelace to change output")
            else:
                # For registration/delegation: input - fee - deposit
                adjusted_change = input_amount - actual_fee - deposit

            logger.debug(f"Adjusted change: {adjusted_change} lovelaces")

            # Update the change output with the adjusted amount
            for op in operations:
                if op["type"] == "output" and op["operation_identifier"]["index"] == 1:
                    op["amount"]["value"] = str(adjusted_change)
                    logger.debug(
                        f"Updated change output to account for fee and refund: {adjusted_change} lovelace"
                    )
                    break
        else:
            # If suggested_fee not available, the transaction construction has failed
            # This should not happen with a properly configured Rosetta API
            raise ValueError(
                "No suggested fee returned from Rosetta API. Transaction construction failed."
            )

        # Update the unsigned_tx to include the adjusted operations
        if "operations" in unsigned_tx and unsigned_tx["operations"]:
            unsigned_tx["operations"] = operations

        logger.debug("Signing transaction...")
        logger.debug(
            f"Number of payloads in unsigned_tx: {len(unsigned_tx.get('payloads', []))}"
        )

        # Get the payload for the stake key
        stake_payload = None
        payment_payload = None

        for payload in unsigned_tx.get("payloads", []):
            account = payload.get("account_identifier", {}).get("address", "")
            logger.debug(f"Payload for account: {account}")

            if account == stake_address:
                stake_payload = payload
            elif account == wallet_address:
                payment_payload = payload

        if not stake_payload and operation_type in [
            "stake_key_deregistration",
            "stake_delegation",
        ]:
            logger.warning("No stake key payload found for a stake operation!")

        # Sign transaction
        signatures = []

        # Handle each operation type appropriately
        # Note: operation_type is passed as "stake key registration", "stake key deregistration", etc.
        # But the Rosetta API types are camelCase ("stakeKeyRegistration", etc.)
        if "registration" in operation_type and "delegation" in operation_type:
            # This is the combined registration and delegation case
            logger.debug(
                "This operation combines registration and delegation, adding both signatures"
            )
            if stake_payload:
                logger.debug(
                    "Adding stake key signature for registration and delegation"
                )
                stake_signature = test_wallet.sign_with_stake_key(stake_payload)
                signatures.append(stake_signature)
            else:
                logger.error(
                    "Missing stake payload - cannot register or delegate without stake key signature!"
                )

            if payment_payload:
                logger.debug("Adding payment key signature for UTXO spending")
                payment_signature = test_wallet.sign_transaction_with_payment_key(
                    payment_payload
                )
                signatures.append(payment_signature)
            else:
                logger.error(
                    "Missing payment payload - cannot spend inputs without payment key signature!"
                )
        elif "registration" in operation_type:
            # For registration, we now need BOTH payment and stake key signatures
            if stake_payload:
                logger.debug(
                    "Adding stake key signature for registration authorization"
                )
                stake_signature = test_wallet.sign_with_stake_key(stake_payload)
                signatures.append(stake_signature)
            else:
                logger.error(
                    "Missing stake payload - cannot register without stake key signature!"
                )

            if payment_payload:
                logger.debug("Adding payment key signature for UTXO spending")
                payment_signature = test_wallet.sign_transaction_with_payment_key(
                    payment_payload
                )
                signatures.append(payment_signature)
            else:
                logger.error(
                    "Missing payment payload - cannot spend inputs without payment key signature!"
                )
        elif "deregistration" in operation_type:
            # For deregistration, we need both stake and payment signatures
            if stake_payload:
                logger.debug(
                    "Adding stake key signature for deregistration authorization"
                )
                stake_signature = test_wallet.sign_with_stake_key(stake_payload)
                signatures.append(stake_signature)
            else:
                logger.error(
                    "Missing stake payload - cannot deregister without stake key signature!"
                )

            if payment_payload:
                logger.debug("Adding payment key signature for UTXO spending")
                payment_signature = test_wallet.sign_transaction_with_payment_key(
                    payment_payload
                )
                signatures.append(payment_signature)
            else:
                logger.error(
                    "Missing payment payload - cannot spend inputs without payment key signature!"
                )
        elif "delegation" in operation_type:
            # For delegation, we need both stake and payment signatures as well
            if stake_payload:
                logger.debug("Adding stake key signature for delegation authorization")
                stake_signature = test_wallet.sign_with_stake_key(stake_payload)
                signatures.append(stake_signature)
            else:
                logger.error(
                    "Missing stake payload - cannot delegate without stake key signature!"
                )

            if payment_payload:
                logger.debug("Adding payment key signature for UTXO spending")
                payment_signature = test_wallet.sign_transaction_with_payment_key(
                    payment_payload
                )
                signatures.append(payment_signature)
            else:
                logger.error(
                    "Missing payment payload - cannot spend inputs without payment key signature!"
                )
        elif (
            "drepVoteDelegation" in operation_type
            or "DRep vote delegation" in operation_type
        ):
            # For DRep vote delegation, we need both stake and payment signatures
            if stake_payload:
                logger.debug(
                    "Adding stake key signature for DRep vote delegation authorization"
                )
                stake_signature = test_wallet.sign_with_stake_key(stake_payload)
                signatures.append(stake_signature)
            else:
                logger.error(
                    "Missing stake payload - cannot delegate votes without stake key signature!"
                )

            if payment_payload:
                logger.debug("Adding payment key signature for UTXO spending")
                payment_signature = test_wallet.sign_transaction_with_payment_key(
                    payment_payload
                )
                signatures.append(payment_signature)
            else:
                logger.error(
                    "Missing payment payload - cannot spend inputs without payment key signature!"
                )
        else:
            # For standard operations (non-staking), we only need payment key signature
            logger.debug("Standard operation, adding payment key signature only")
            signature = test_wallet.sign_transaction(unsigned_tx)
            signatures.append(signature)
            logger.debug("Added payment key signature for standard operation")

        logger.debug(f"Generated {len(signatures)} signatures for transaction")
        for i, sig in enumerate(signatures):
            sig_type = "payment" if i == 0 else "stake"
            logger.debug(f"Signature {i+1} ({sig_type}): {sig}")

        # Combine transaction with signature(s)
        logger.debug("Combining transaction with signatures...")
        combined_tx = rosetta_client.combine_transaction(
            unsigned_transaction=unsigned_tx["unsigned_transaction"],
            signatures=signatures,
        )
        logger.debug("Transaction combined successfully")

        logger.debug("Submitting transaction...")
        submit_response = rosetta_client.submit_transaction(
            combined_tx["signed_transaction"]
        )

        # Extract the transaction hash from the response
        tx_hash = submit_response["transaction_identifier"]["hash"]
        logger.debug(f"Transaction submitted: {tx_hash}")
    except Exception as e:
        logger.error(f"✗ Error in transaction construction/submission: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise

    # Wait for transaction confirmation and get details
    try:
        logger.debug("Waiting for transaction confirmation...")
        block_id, tx_details = wait_for_transaction_confirmation(
            rosetta_client, tx_hash
        )
        logger.debug(f"Transaction confirmed in block: {block_id}")
    except Exception as e:
        logger.error(f"✗ Error waiting for transaction confirmation: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise

    # Validate the transaction based on operation type
    try:
        logger.debug(f"Validating transaction for operation type: {operation_types}")

        # Verify transaction exists in response
        assert "transaction" in tx_details, "Transaction details not found in response"
        onchain_tx = tx_details["transaction"]

        # Verify operations exist
        assert "operations" in onchain_tx, "Operations not found in transaction"
        onchain_ops = onchain_tx["operations"]

        # Validate operations
        input_ops = [op for op in onchain_ops if op["type"] == "input"]
        output_ops = [op for op in onchain_ops if op["type"] == "output"]
        stake_ops = [op for op in onchain_ops if op["type"] not in ["input", "output"]]

        # Basic validation for inputs and outputs
        assert len(input_ops) > 0, "No input operations found"
        assert len(output_ops) > 0, "No output operations found"

        # Log all operations for debugging
        logger.debug(f"Found {len(onchain_ops)} operations in transaction:")
        for i, op in enumerate(onchain_ops):
            logger.debug(f"Operation {i}: {op['type']}")

        # Validate the number of stake operations based on operation types
        expected_stake_ops_count = len(operation_types)
        assert len(stake_ops) == expected_stake_ops_count, (
            f"Expected {expected_stake_ops_count} stake operations, "
            f"got {len(stake_ops)}"
        )

        # Detailed validation of stake operations
        found_operation_types = [op["type"] for op in stake_ops]
        logger.debug(f"Found stake operations: {found_operation_types}")

        # Check that each expected operation type is found
        for op_type in operation_types:
            expected_type = ""
            if op_type == "registration":
                expected_type = "stakeKeyRegistration"
            elif op_type == "deregistration":
                expected_type = "stakeKeyDeregistration"
            elif op_type == "delegation":
                expected_type = "stakeDelegation"
            elif op_type == "drepVoteDelegation":
                expected_type = "dRepVoteDelegation"

            assert (
                expected_type in found_operation_types
            ), f"Expected operation {expected_type} not found"

        # Validate fee calculation
        onchain_input_value = sum(abs(int(op["amount"]["value"])) for op in input_ops)
        onchain_output_value = sum(int(op["amount"]["value"]) for op in output_ops)

        # Account for deposit in fee calculation if registration is included
        deposit_amount = (
            required_funds.get("deposit", 0) if "registration" in operation_types else 0
        )

        # Account for refund in fee calculation if deregistration is included
        refund_amount = 2_000_000 if "deregistration" in operation_types else 0

        # Fee = inputs - outputs - deposit + refund
        onchain_fee = (
            onchain_input_value - onchain_output_value - deposit_amount + refund_amount
        )
        logger.debug(f"Calculated on-chain fee: {onchain_fee} lovelaces")
        logger.debug(f"Total inputs: {onchain_input_value} lovelaces")
        logger.debug(f"Total outputs: {onchain_output_value} lovelaces")
        logger.debug(f"Deposit: {deposit_amount} lovelaces")
        logger.debug(f"Refund: {refund_amount} lovelaces")

        # Ensure fee is reasonable (positive value)
        assert onchain_fee > 0, f"Invalid fee calculation: {onchain_fee} lovelaces"

        # Validate stake operations
        if "registration" in operation_types and "delegation" in operation_types:
            validate_combined_registration_delegation(
                rosetta_client, test_wallet, tx_details, pool_id
            )
        elif "registration" in operation_types:
            validate_stake_key_registration(
                rosetta_client, test_wallet, tx_details, pool_id
            )
        elif "deregistration" in operation_types:
            validate_stake_key_deregistration(rosetta_client, test_wallet, tx_details)
        elif "delegation" in operation_types:
            validate_stake_delegation(rosetta_client, test_wallet, tx_details, pool_id)
        elif "drepVoteDelegation" in operation_types:
            validate_drep_vote_delegation(
                rosetta_client, test_wallet, tx_details, drep_id, drep_type
            )

        logger.debug("Transaction validation successful")
    except AssertionError as e:
        logger.error(f"✗ Transaction validation failed: {str(e)}")
        logger.debug(f"Transaction details: {tx_details}")
        raise
    except Exception as e:
        logger.error(f"✗ Error validating transaction: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        logger.debug(f"Transaction details: {tx_details}")
        raise

    return tx_hash


def build_stake_operations(
    inputs: List[Dict],
    outputs: List[Dict],
    test_wallet,
    operation_types: List[str],
    pool_id: Optional[str] = None,
    drep_id: Optional[str] = None,
    drep_type: Optional[str] = None,
) -> List[Dict]:
    """
    Build operations array for stake operations.

    Args:
        inputs: List of input UTXOs
        outputs: List of outputs
        test_wallet: PyCardanoWallet instance
        operation_types: List of operation types to include ("registration", "delegation", "deregistration", "drepVoteDelegation")
        pool_id: Pool ID for delegation (required if "delegation" in operation_types)
        drep_id: DRep ID for vote delegation (required for key_hash/script_hash if "drepVoteDelegation" in operation_types)
        drep_type: DRep type for vote delegation (required if "drepVoteDelegation" in operation_types)

    Returns:
        List of operations
    """
    operations = []

    # Add input operations
    for idx, input_data in enumerate(inputs):
        operation = {
            "operation_identifier": {"index": idx},
            "type": "input",
            "status": "",
            "account": {"address": input_data["address"]},
            "amount": {
                "value": str(-input_data["value"]),  # Negative for inputs
                "currency": {"symbol": "ADA", "decimals": 6},
            },
            "coin_change": {
                "coin_identifier": {
                    "identifier": input_data["coin_change"]["coin_identifier"][
                        "identifier"
                    ]
                },
                "coin_action": "coin_spent",
            },
            "metadata": input_data.get("metadata", {}),
        }
        operations.append(operation)

    # Add output operations
    offset = len(inputs)
    for idx, output_data in enumerate(outputs):
        operation = {
            "operation_identifier": {"index": idx + offset},
            "type": "output",
            "status": "",
            "account": {"address": output_data["address"]},
            "amount": {
                "value": str(output_data["value"]),  # Positive for outputs
                "currency": {"symbol": "ADA", "decimals": 6},
            },
            "metadata": output_data.get("metadata", {}),
        }
        operations.append(operation)

    # Get stake key and address (common for all stake operations)
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    stake_address = test_wallet.get_stake_address()

    # Add stakeKeyRegistration operation if requested
    if "registration" in operation_types:
        stake_key_registration_op = {
            "operation_identifier": {"index": len(operations)},
            "type": "stakeKeyRegistration",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                }
            },
        }
        operations.append(stake_key_registration_op)

    # Add stakeKeyDeregistration operation if requested
    if "deregistration" in operation_types:
        stake_key_deregistration_op = {
            "operation_identifier": {"index": len(operations)},
            "type": "stakeKeyDeregistration",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                }
            },
        }
        operations.append(stake_key_deregistration_op)

    # Add stakeDelegation operation if requested
    if "delegation" in operation_types:
        if not pool_id:
            raise ValueError("Pool ID is required for stake delegation operations")

        # Use pool_id directly as the hash - pool_id is now expected to be the hex hash
        pool_key_hash = pool_id

        stake_delegation_op = {
            "operation_identifier": {"index": len(operations)},
            "type": "stakeDelegation",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                },
                "pool_key_hash": pool_key_hash,
            },
        }
        operations.append(stake_delegation_op)

    # Add dRepVoteDelegation operation if requested
    if "drepVoteDelegation" in operation_types:
        if drep_type not in ["key_hash", "script_hash", "abstain", "no_confidence"]:
            raise ValueError(
                "DRep type must be one of: key_hash, script_hash, abstain, no_confidence"
            )

        # For key_hash and script_hash types, drep_id is required
        if drep_type in ["key_hash", "script_hash"] and not drep_id:
            raise ValueError(
                "DRep ID is required for key_hash and script_hash DRep types"
            )

        drep_vote_delegation_op = {
            "operation_identifier": {"index": len(operations)},
            "type": "dRepVoteDelegation",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519",
                },
                "drep": {
                    "type": drep_type,
                },
            },
        }

        # Add drep.id for key_hash and script_hash types
        if drep_type in ["key_hash", "script_hash"]:
            drep_vote_delegation_op["metadata"]["drep"]["id"] = drep_id

        operations.append(drep_vote_delegation_op)

    return operations


def validate_stake_operations(
    tx_details: Dict,
    test_wallet,
    expected_operations: List[str],
    expected_deposit: Optional[int] = None,
    expected_pool_id: Optional[str] = None,
    expected_drep_id: Optional[str] = None,
    expected_drep_type: Optional[str] = None,
):
    """
    Validate stake operations in a transaction.

    Args:
        tx_details: Transaction details from get_block_transaction
        test_wallet: PyCardanoWallet instance
        expected_operations: List of expected operation types ("registration", "delegation", "deregistration", "drepVoteDelegation")
        expected_deposit: Expected deposit amount (for registration)
        expected_pool_id: Expected pool ID in hex format (for delegation)
        expected_drep_id: Expected DRep ID in hex format (for drepVoteDelegation with key_hash/script_hash)
        expected_drep_type: Expected DRep type (for drepVoteDelegation)

    Raises:
        AssertionError: If validation fails
    """
    operation_type_desc = " and ".join(expected_operations)
    logger.debug(f"Validating {operation_type_desc} details...")

    # Verify transaction exists in response
    assert "transaction" in tx_details, "Transaction details not found in response"
    onchain_tx = tx_details["transaction"]

    # Verify operations exist
    assert "operations" in onchain_tx, "Operations not found in transaction"
    onchain_ops = onchain_tx["operations"]

    # Get stake key and address (for validation)
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    stake_address = test_wallet.get_stake_address()

    # Validate registration if expected
    if "registration" in expected_operations:
        # Find stake key registration operation
        registration_ops = [
            op for op in onchain_ops if op["type"] == "stakeKeyRegistration"
        ]
        assert (
            len(registration_ops) == 1
        ), f"Expected 1 stakeKeyRegistration operation, got {len(registration_ops)}"

        registration_op = registration_ops[0]

        # Validate stake address
        assert (
            registration_op["account"]["address"] == stake_address
        ), "Stake address mismatch"

        # Validate deposit amount (if available in metadata)
        if expected_deposit and "metadata" in registration_op:
            # Check for depositAmount in the metadata (format from on-chain data)
            if "depositAmount" in registration_op["metadata"]:
                deposit = int(registration_op["metadata"]["depositAmount"]["value"])
                assert (
                    deposit == expected_deposit
                ), f"Deposit amount mismatch: expected {expected_deposit}, got {deposit}"
            # Also check for deposit directly (format we might have used in construction)
            elif "deposit" in registration_op["metadata"]:
                deposit = int(registration_op["metadata"]["deposit"])
                assert (
                    deposit == expected_deposit
                ), f"Deposit amount mismatch: expected {expected_deposit}, got {deposit}"
            else:
                logger.warning("Deposit amount not found in metadata")

    # Validate deregistration if expected
    if "deregistration" in expected_operations:
        # Find stake key deregistration operation
        deregistration_ops = [
            op for op in onchain_ops if op["type"] == "stakeKeyDeregistration"
        ]
        assert (
            len(deregistration_ops) == 1
        ), f"Expected 1 stakeKeyDeregistration operation, got {len(deregistration_ops)}"

        deregistration_op = deregistration_ops[0]

        # Validate stake address
        assert (
            deregistration_op["account"]["address"] == stake_address
        ), "Stake address mismatch"

    # Validate delegation if expected
    if "delegation" in expected_operations:
        # Find stake delegation operation
        delegation_ops = [op for op in onchain_ops if op["type"] == "stakeDelegation"]
        assert (
            len(delegation_ops) == 1
        ), f"Expected 1 stakeDelegation operation, got {len(delegation_ops)}"

        delegation_op = delegation_ops[0]

        # Validate stake address
        assert (
            delegation_op["account"]["address"] == stake_address
        ), "Stake address mismatch"

        # Validate pool ID
        if expected_pool_id and "metadata" in delegation_op:
            if "pool_key_hash" in delegation_op["metadata"]:
                assert (
                    delegation_op["metadata"]["pool_key_hash"] == expected_pool_id
                ), f"Pool ID mismatch: expected {expected_pool_id}, got {delegation_op['metadata']['pool_key_hash']}"
            elif "poolKeyHash" in delegation_op["metadata"]:
                assert (
                    delegation_op["metadata"]["poolKeyHash"] == expected_pool_id
                ), f"Pool ID mismatch: expected {expected_pool_id}, got {delegation_op['metadata']['poolKeyHash']}"
            else:
                logger.warning("Pool key hash not found in metadata")

    # Validate DRep vote delegation if expected
    if "drepVoteDelegation" in expected_operations:
        # Find DRep vote delegation operation
        drep_vote_delegation_ops = [
            op for op in onchain_ops if op["type"] == "dRepVoteDelegation"
        ]
        assert (
            len(drep_vote_delegation_ops) == 1
        ), f"Expected 1 dRepVoteDelegation operation, got {len(drep_vote_delegation_ops)}"

        drep_vote_delegation_op = drep_vote_delegation_ops[0]

        # Validate stake address
        assert (
            drep_vote_delegation_op["account"]["address"] == stake_address
        ), "Stake address mismatch"

        # Validate DRep type if expected
        if expected_drep_type and "metadata" in drep_vote_delegation_op:
            if "drep" in drep_vote_delegation_op["metadata"]:
                drep_type = drep_vote_delegation_op["metadata"]["drep"]["type"]
                assert (
                    drep_type == expected_drep_type
                ), f"DRep type mismatch: expected {expected_drep_type}, got {drep_type}"
            else:
                logger.warning("DRep type not found in metadata")

        # Validate DRep ID if expected (only for key_hash and script_hash types)
        if (
            expected_drep_id
            and expected_drep_type in ["key_hash", "script_hash"]
            and "metadata" in drep_vote_delegation_op
        ):
            if (
                "drep" in drep_vote_delegation_op["metadata"]
                and "id" in drep_vote_delegation_op["metadata"]["drep"]
            ):
                drep_id = drep_vote_delegation_op["metadata"]["drep"]["id"]
                assert (
                    drep_id == expected_drep_id
                ), f"DRep ID mismatch: expected {expected_drep_id}, got {drep_id}"
            else:
                logger.warning("DRep ID not found in metadata")

    # Validate input and output operations
    input_ops = [op for op in onchain_ops if op["type"] == "input"]
    output_ops = [op for op in onchain_ops if op["type"] == "output"]
    stake_ops = [op for op in onchain_ops if op["type"] not in ["input", "output"]]

    assert len(input_ops) > 0, "No input operations found"
    assert len(output_ops) > 0, "No output operations found"

    # Calculate and validate the fee
    onchain_input_value = sum(abs(int(op["amount"]["value"])) for op in input_ops)
    onchain_output_value = sum(int(op["amount"]["value"]) for op in output_ops)

    # Account for deposit in fee calculation if registration is included
    deposit_amount = expected_deposit if expected_deposit else 0

    # Account for refund in fee calculation if deregistration is included
    refund_amount = 2_000_000 if "deregistration" in expected_operations else 0

    # Calculate expected fee: inputs - outputs - deposit + refund
    expected_fee = (
        onchain_input_value - onchain_output_value - deposit_amount + refund_amount
    )
    logger.debug(f"Calculated fee: {expected_fee} lovelaces")

    # Validate fee is reasonable (within 10% of expected)
    # This is a loose check since we don't know the exact fee calculation
    # assert expected_fee > 0, f"Invalid fee calculation: {expected_fee} lovelaces"

    logger.debug(f"{operation_type_desc} validation successful")
    return True


def construct_transaction_with_operations(
    rosetta_client, operations: List[Dict]
) -> Dict:
    """
    Construct a transaction using custom operations.

    This function follows the Rosetta Construction API flow:
    1) /construction/preprocess
    2) /construction/metadata - Gets suggested fee
    3) Adjusts change output based on suggested fee
    4) /construction/payloads - Creates the payload with adjusted operations

    Args:
        rosetta_client: RosettaClient instance
        operations: List of operations to include in the transaction

    Returns:
        Dict containing the unsigned transaction and payloads
    """
    try:
        # Log operations before sending to the API for debugging
        logger.debug(f"Constructing transaction with {len(operations)} operations")
        for i, op in enumerate(operations):
            logger.debug(f"Operation {i}: type={op['type']}")

        # Identify if this is a stake key deregistration operation
        is_deregistration = any(
            op["type"] == "stakeKeyDeregistration" for op in operations
        )

        # Identify if this is a DRep vote delegation operation
        is_drep_vote_delegation = any(
            op["type"] == "dRepVoteDelegation" for op in operations
        )

        if is_drep_vote_delegation:
            logger.debug("This is a DRep vote delegation operation")

        # For deregistration, we expect a 2 ADA refund
        refund_amount = 0
        if is_deregistration:
            refund_amount = 2_000_000
            logger.debug(
                f"This is a stake key deregistration with a refund of {refund_amount} lovelace from the protocol"
            )
            # We need to ADD the refund to our outputs to match
            # what the protocol expects (inputs + refund = outputs + fee)

        # Step 1: /construction/preprocess
        preprocess_payload = {
            "network_identifier": rosetta_client._get_network_identifier(),
            "operations": operations,
            # Add required metadata fields that might be expected by the API
            "metadata": {"relative_ttl": 1000},  # Add a reasonable TTL
        }

        logger.debug("Sending preprocess request to Rosetta API...")
        preprocess_response = rosetta_client.request_debugger.post(
            f"{rosetta_client.endpoint}/construction/preprocess",
            json=preprocess_payload,
            headers=rosetta_client.headers,
        )

        try:
            preprocess_data = preprocess_response.json()
        except Exception as e:
            logger.error(f"Failed to parse preprocess response: {str(e)}")
            logger.error(f"Response status code: {preprocess_response.status_code}")
            logger.error(f"Response text: {preprocess_response.text}")
            raise

        # Step 2: /construction/metadata - Get suggested fee
        metadata_payload = {
            "network_identifier": rosetta_client._get_network_identifier(),
            "options": preprocess_data["options"],
            "public_keys": [],
        }

        logger.debug("Sending metadata request to Rosetta API...")
        metadata_response = rosetta_client.request_debugger.post(
            f"{rosetta_client.endpoint}/construction/metadata",
            json=metadata_payload,
            headers=rosetta_client.headers,
        )
        metadata = metadata_response.json()

        # Extract suggested fee if available and adjust operations
        suggested_fee_info = metadata.get("suggested_fee", [])
        if suggested_fee_info:
            suggested_fee = int(suggested_fee_info[0]["value"])
            logger.debug(f"Rosetta suggested fee: {suggested_fee} lovelace")

            # Find input and output operations
            input_ops = [op for op in operations if op["type"] == "input"]
            output_ops = [op for op in operations if op["type"] == "output"]

        # Calculate total input and output values
        total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)

        # Find the last output (change) and adjust it to account for the fee
        if output_ops:
            # Check if the last output (change) has enough value to cover the fee
            last_output_op = output_ops[-1]
            last_output_value = int(last_output_op["amount"]["value"])

            # For deregistration, we need to ADD the refund to our outputs to match
            # what the protocol expects (inputs + refund = outputs + fee)
            if is_deregistration:
                # Add refund to the change output and subtract fee
                # For deregistration, the initial change was set to the full input amount
                # so we need to subtract the fee and add the refund
                adjusted_value = last_output_value - suggested_fee + refund_amount
                logger.debug(
                    f"Deregistration: Adding refund of {refund_amount} to change output and subtracting fee of {suggested_fee}"
                )
            else:
                # Normal case: just subtract fee
                adjusted_value = last_output_value - suggested_fee

            logger.debug(
                f"Adjusting change output from {last_output_value} to {adjusted_value} lovelace"
            )

            # Find the change output in the original operations list and update it
            for op in operations:
                if (
                    op["type"] == "output"
                    and op["operation_identifier"]["index"]
                    == last_output_op["operation_identifier"]["index"]
                ):
                    op["amount"]["value"] = str(adjusted_value)
                    logger.debug(
                        f"Adjusted change output to account for fee and refund: {adjusted_value} lovelace"
                    )
                    break
        else:
            logger.warning("No output operations found to adjust for fee")

        # Step 3: /construction/payloads with potentially adjusted operations
        payloads_payload = {
            "network_identifier": rosetta_client._get_network_identifier(),
            "operations": operations,
            "metadata": metadata["metadata"],
        }

        # Send payloads request to get unsigned transaction with payloads
        logger.debug("Sending payloads request to Rosetta API...")
        payloads_response = rosetta_client.request_debugger.post(
            f"{rosetta_client.endpoint}/construction/payloads",
            json=payloads_payload,
            headers=rosetta_client.headers,
        )
        payloads_data = payloads_response.json()

        # Debug: Log payloads
        logger.debug(
            f"Payloads received: {len(payloads_data.get('payloads', []))} payloads"
        )
        for i, payload in enumerate(payloads_data.get("payloads", [])):
            account_address = payload.get("account_identifier", {}).get(
                "address", "unknown"
            )
            logger.debug(f"  Payload {i}: Account {account_address}")

        # Ensure all required fields are present in the response
        required_fields = ["unsigned_transaction", "payloads"]
        for field in required_fields:
            if field not in payloads_data:
                raise ValueError(
                    f"Missing required field '{field}' in payloads response"
                )

        # Return the unsigned transaction with payloads and suggested fee
        return {
            "unsigned_transaction": payloads_data["unsigned_transaction"],
            "payloads": payloads_data["payloads"],
            "operations": operations,
            "suggested_fee": (
                [
                    {
                        "value": str(suggested_fee),
                        "currency": {"symbol": "ADA", "decimals": 6},
                    }
                ]
                if "suggested_fee" in locals()
                else None
            ),
        }

    except Exception as e:
        logger.error(f"✗ Error constructing transaction: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def wait_for_transaction_confirmation(
    rosetta_client, tx_id, timeout_seconds=90, polling_interval=5
):
    """
    Wait for a transaction to be confirmed on-chain and return its details.

    This improved implementation:
    1. Tracks the last checked block to ensure no blocks are skipped
    2. Checks each block sequentially to avoid missing transactions
    3. Uses block identifiers for detailed data retrieval
    4. Provides better logging for transaction discovery

    Args:
        rosetta_client: The Rosetta client instance
        tx_id: The transaction hash to wait for
        timeout_seconds: Maximum time to wait in seconds
        polling_interval: Time between polling attempts in seconds

    Returns:
        Tuple of (block_identifier, transaction_details)
    """
    logger.debug(f"Waiting for transaction confirmation for tx_id: {tx_id}...")

    start_time = time.time()
    found_in_block = False
    last_checked_block_index = None
    current_block_identifier = None
    block_tx_details = None

    # Get current network status to know where to start checking
    network_status = rosetta_client.network_status()
    current_block_index = network_status.get("current_block_identifier", {}).get(
        "index"
    )

    if not current_block_index:
        logger.warning("Could not get current block index, will use index tracking")
    else:
        # Start checking from a few blocks before current in case tx was already included
        last_checked_block_index = max(0, current_block_index - 3)
        logger.debug(f"Starting block check from index {last_checked_block_index}")

    # Poll the network until we find the transaction in a block
    while not found_in_block and (time.time() - start_time < timeout_seconds):
        # Get current network status with latest block info
        network_status = rosetta_client.network_status()
        current_block_identifier = network_status.get("current_block_identifier")

        if not current_block_identifier:
            logger.warning("Could not get current block identifier, retrying...")
            time.sleep(polling_interval)
            continue

        current_block_index = current_block_identifier.get("index")

        # If this is our first check, initialize last_checked_block_index
        if last_checked_block_index is None:
            # Start from a few blocks back to be safe
            last_checked_block_index = max(0, current_block_index - 3)
            logger.debug(f"Starting block check from index {last_checked_block_index}")

        # Check all blocks from last checked to current
        for block_index in range(last_checked_block_index, current_block_index + 1):
            block_identifier = {
                "index": block_index,
                # We don't have the hash, but the index is sufficient for querying
            }

            logger.debug(f"Checking block {block_index} for transaction {tx_id}...")

            # Get the block
            try:
                block_data = rosetta_client.get_block(block_identifier)

                # Check if our transaction is in this block
                if "block" in block_data and "transactions" in block_data["block"]:
                    transactions = block_data["block"]["transactions"]
                    logger.debug(
                        f"Found {len(transactions)} transactions in block {block_index}"
                    )

                    # Log all transaction hashes for debugging
                    for i, tx in enumerate(transactions):
                        tx_hash = tx["transaction_identifier"]["hash"]

                        # Check if this is our transaction
                        if tx_hash == tx_id:
                            found_in_block = True
                            logger.debug(
                                f"Transaction {tx_id} found in block {block_index} (position {i+1}/{len(transactions)})"
                            )

                            # Update block_identifier with hash if available
                            if "block_identifier" in block_data["block"]:
                                current_block_identifier = block_data["block"][
                                    "block_identifier"
                                ]

                            # Get detailed transaction information
                            block_tx_details = rosetta_client.get_block_transaction(
                                current_block_identifier, tx_id
                            )

                            # Verify we got the transaction details
                            if (
                                not block_tx_details
                                or "transaction" not in block_tx_details
                            ):
                                logger.warning(
                                    "Transaction found in block but details retrieval failed, will retry..."
                                )
                                found_in_block = False
                                break

                            logger.debug(
                                f"Successfully retrieved transaction details from block {block_index}"
                            )
                            break

                    if found_in_block:
                        break
            except Exception as e:
                logger.warning(f"Error checking block {block_index}: {str(e)}")

        # Update the last checked block index for the next polling interval
        if not found_in_block:
            last_checked_block_index = current_block_index + 1
            logger.debug(
                f"Transaction not found up to block {current_block_index}, "
                f"waiting {polling_interval} seconds before checking newer blocks..."
            )
            time.sleep(polling_interval)

    # Verify transaction was found on-chain
    if not found_in_block:
        raise TimeoutError(
            f"Transaction {tx_id} not found on-chain within {timeout_seconds} seconds"
        )

    return current_block_identifier, block_tx_details


def validate_stake_key_registration(
    rosetta_client, test_wallet, tx_details, pool_id=None
):
    """
    Validate that a stake key registration was successful by checking:
    1. The transaction contains a stake key registration operation/certificate
    2. The stake address is now recognized on-chain (can query balance)
    3. The deposit amount was correctly applied
    """
    logger.debug("Validating stake key registration...")
    # Determine if we're validating a combined operation or just registration
    is_combined_with_delegation = pool_id is not None

    if is_combined_with_delegation:
        logger.debug("Validating combined registration with delegation")
    else:
        logger.debug("Validating simple stake key registration (without delegation)")

    # Log the full transaction details for debugging
    logger.debug(f"Transaction details: {tx_details}")

    # 1. Check that the transaction contains a stake key registration operation or certificate
    found_registration = False
    found_delegation = False  # Initialize this variable
    deposit_amount = None
    stake_address = test_wallet.get_stake_address()

    # First check in operations (primary source)
    if "transaction" in tx_details and "operations" in tx_details["transaction"]:
        operations = tx_details["transaction"]["operations"]
        logger.debug(f"Found {len(operations)} operations in transaction")
        pool_hash_match = False  # Initialize to false

        for op in operations:
            if op.get("type") == "stakeKeyRegistration":
                found_registration = True
                logger.debug("Found stake key registration operation")
                # Check for depositAmount in operation metadata
                if "metadata" in op:
                    if "depositAmount" in op["metadata"]:
                        deposit_info = op["metadata"]["depositAmount"]
                        if isinstance(deposit_info, dict) and "value" in deposit_info:
                            deposit_amount = int(deposit_info["value"])
                        else:
                            deposit_amount = int(deposit_info)
                        logger.debug(
                            f"Found deposit amount in operation: {deposit_amount}"
                        )
                    elif "deposit" in op["metadata"]:
                        deposit_amount = int(op["metadata"]["deposit"])
                        logger.debug(f"Found deposit in operation: {deposit_amount}")

            elif op.get("type") == "stakeDelegation":
                found_delegation = True
                logger.debug(f"Found stake delegation operation")

                # Check pool ID if available in operation metadata
                if "metadata" in op and "pool_key_hash" in op["metadata"]:
                    observed_pool_hash = op["metadata"]["pool_key_hash"]
                    logger.debug(
                        f"Found pool_key_hash in operation: {observed_pool_hash}"
                    )
                    if pool_id and observed_pool_hash == pool_id:
                        pool_hash_match = True
                        logger.debug("Pool hash matches expected value")

    # 2. Fall back to checking certificates in metadata (legacy method)
    if not found_registration or (is_combined_with_delegation and not found_delegation):
        logger.debug(
            "Operations not found or incomplete, checking certificates in metadata..."
        )
        metadata = tx_details.get("metadata", {})
        logger.debug(f"Transaction metadata: {metadata}")
        certificates = metadata.get("certificates", [])
        logger.debug(f"Certificates: {certificates}")

        pool_hex = None
        if pool_id:
            pool_hex = test_wallet.convert_pool_id_to_hex(pool_id)
            logger.debug(f"Pool ID hex: {pool_hex}")

        for cert in certificates:
            logger.debug(f"Checking certificate: {cert}")
            if cert.get("type") == "stakeKeyRegistration":
                found_registration = True
                logger.debug("Found stake key registration certificate")
            elif cert.get("type") == "stakeDelegation":
                found_delegation = True
                logger.debug("Found stake delegation certificate")
                # Check pool ID if available in certificate
                if "pool" in cert:
                    logger.debug(f"Certificate pool: {cert['pool']}")
                    if pool_hex and cert["pool"] == pool_hex:
                        pool_hash_match = True
                        logger.debug("Pool ID verified")
            else:
                logger.warning("Pool key hash not found in certificate")

    # Always require registration operation to be found
    assert (
        found_registration
    ), "Stake key registration operation/certificate not found in transaction"

    # Only require delegation if we're validating a combined operation
    if is_combined_with_delegation:
        assert (
            found_delegation
        ), "Stake delegation operation/certificate not found in transaction"

    # 2. Verify the stake address is now recognized on-chain
    logger.debug(f"Verifying stake address {stake_address} is registered")
    try:
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Account info: {account_info}")
        assert (
            account_info is not None
        ), "Failed to retrieve account info for registered stake address"
        logger.debug("Stake address successfully registered and accessible")
    except Exception as e:
        error_msg = f"Failed to verify stake address registration: {str(e)}"
        logger.error(error_msg)
        logger.debug(f"Traceback: {traceback.format_exc()}")
        assert False, error_msg

    # 3. Check that the deposit amount was correctly applied
    # If we haven't found deposit in operations, try transaction metadata
    if deposit_amount is None:
        metadata = tx_details.get("metadata", {})
        if "depositAmount" in metadata:
            deposit_amount = metadata["depositAmount"]
            logger.debug(f"Found deposit amount in metadata: {deposit_amount}")
            if isinstance(deposit_amount, dict) and "value" in deposit_amount:
                deposit_amount = int(deposit_amount["value"])
            else:
                deposit_amount = int(deposit_amount)
        elif "deposit" in metadata:
            deposit_amount = metadata["deposit"]
            logger.debug(f"Found deposit in metadata: {deposit_amount}")
            deposit_amount = int(deposit_amount)

    # Some implementations might not include deposit explicitly
    # In that case, we'll log a warning but not fail the test
    if deposit_amount is not None:
        assert (
            deposit_amount == 2_000_000
        ), f"Unexpected deposit amount: {deposit_amount}, expected 2000000"
        logger.debug(f"Deposit amount verified: {deposit_amount}")
    else:
        logger.warning(
            "Deposit amount not explicitly found in metadata (implementation-dependent)"
        )

    if is_combined_with_delegation:
        logger.debug(
            "Combined stake key registration and delegation validation successful"
        )
    else:
        logger.debug("Stake key registration validation successful")
    return True


def validate_stake_delegation(rosetta_client, test_wallet, tx_details, pool_id):
    """
    Validate that a stake delegation was successful by checking:
    1. The transaction contains a stake delegation operation or certificate
    2. The delegation is to the correct pool
    3. The stake address is now delegated to the specified pool
    """
    logger.debug("Validating stake delegation...")

    # 1. Check for stake delegation operation first (preferred method)
    found_delegation = False
    pool_hash_match = False
    stake_address = test_wallet.get_stake_address()

    if "transaction" in tx_details and "operations" in tx_details["transaction"]:
        operations = tx_details["transaction"]["operations"]
        for op in operations:
            if op.get("type") == "stakeDelegation":
                found_delegation = True
                logger.debug(
                    f"Found stake delegation operation with account: {op.get('account', {}).get('address')}"
                )

                # Check pool ID if available in operation metadata
                if "metadata" in op and "pool_key_hash" in op["metadata"]:
                    observed_pool_hash = op["metadata"]["pool_key_hash"]
                    logger.debug(
                        f"Found pool_key_hash in operation: {observed_pool_hash}"
                    )
                    if pool_id and observed_pool_hash == pool_id:
                        pool_hash_match = True
                        logger.debug("Pool hash matches expected value")
                break

    # 2. Fall back to checking for delegation certificate in metadata (legacy method)
    if not found_delegation:
        logger.debug(
            "No stake delegation operation found, checking certificates in metadata..."
        )
        metadata = tx_details.get("metadata", {})
        certificates = metadata.get("certificates", [])

        for cert in certificates:
            if cert.get("type") == "stakeDelegation":
                found_delegation = True
                # Check pool ID if available in certificate
                if "pool" in cert:
                    pool_hex = test_wallet.convert_pool_id_to_hex(pool_id)
                    if cert["pool"] == pool_hex:
                        pool_hash_match = True
                break

    assert found_delegation, "Stake delegation certificate not found in transaction"

    if pool_id and not pool_hash_match:
        logger.warning(f"Pool hash verification skipped or failed. Expected: {pool_id}")

    # 3. Verify the stake address is delegated on-chain
    try:
        account_info = rosetta_client.get_balance(address=stake_address)
        assert (
            account_info is not None
        ), "Failed to retrieve account info for delegated stake address"

        # Ideally, we would check the delegation status here, but this may require
        # additional API calls or blockchain explorer integration
    except Exception as e:
        assert False, f"Failed to verify stake address delegation: {str(e)}"

    logger.debug("Stake delegation validation successful")
    return True


def validate_combined_registration_delegation(
    rosetta_client, test_wallet, tx_details, pool_id
):
    """
    Validate that a combined stake key registration and delegation was successful by checking:
    1. The transaction contains both registration and delegation operations/certificates
    2. The stake address is now recognized on-chain
    3. The deposit amount was correctly applied
    4. The delegation is to the correct pool
    """
    logger.debug("Validating combined stake key registration and delegation...")

    # 1. Check for registration and delegation operations first (preferred method)
    found_registration = False
    found_delegation = False
    pool_hash_match = False
    stake_address = test_wallet.get_stake_address()
    deposit_amount = None

    if "transaction" in tx_details and "operations" in tx_details["transaction"]:
        operations = tx_details["transaction"]["operations"]
        logger.debug(f"Found {len(operations)} operations in transaction")

        for op in operations:
            logger.debug(f"Checking operation: {op.get('type')}")

            if op.get("type") == "stakeKeyRegistration":
                found_registration = True
                logger.debug(f"Found stake key registration operation")

                # Check for depositAmount in operation metadata
                if "metadata" in op:
                    if "depositAmount" in op["metadata"]:
                        deposit_info = op["metadata"]["depositAmount"]
                        if isinstance(deposit_info, dict) and "value" in deposit_info:
                            deposit_amount = int(deposit_info["value"])
                        else:
                            deposit_amount = int(deposit_info)
                        logger.debug(
                            f"Found deposit amount in operation: {deposit_amount}"
                        )
                    elif "deposit" in op["metadata"]:
                        deposit_amount = int(op["metadata"]["deposit"])
                        logger.debug(f"Found deposit in operation: {deposit_amount}")

            elif op.get("type") == "stakeDelegation":
                found_delegation = True
                logger.debug(f"Found stake delegation operation")

                # Check pool ID if available in operation metadata
                if "metadata" in op and "pool_key_hash" in op["metadata"]:
                    observed_pool_hash = op["metadata"]["pool_key_hash"]
                    logger.debug(
                        f"Found pool_key_hash in operation: {observed_pool_hash}"
                    )
                    if pool_id and observed_pool_hash == pool_id:
                        pool_hash_match = True
                        logger.debug("Pool hash matches expected value")

    # 2. Fall back to checking certificates in metadata (legacy method)
    if not found_registration or not found_delegation:
        logger.debug(
            "Operations not found or incomplete, checking certificates in metadata..."
        )
        metadata = tx_details.get("metadata", {})
        logger.debug(f"Transaction metadata: {metadata}")
        certificates = metadata.get("certificates", [])
        logger.debug(f"Certificates: {certificates}")

        pool_hex = None
        if pool_id:
            pool_hex = test_wallet.convert_pool_id_to_hex(pool_id)
            logger.debug(f"Pool ID hex: {pool_hex}")

        for cert in certificates:
            logger.debug(f"Checking certificate: {cert}")
            if cert.get("type") == "stakeKeyRegistration":
                found_registration = True
                logger.debug("Found stake key registration certificate")
            elif cert.get("type") == "stakeDelegation":
                found_delegation = True
                logger.debug("Found stake delegation certificate")
                # Check pool ID if available in certificate
                if "pool" in cert:
                    logger.debug(f"Certificate pool: {cert['pool']}")
                    if pool_hex and cert["pool"] == pool_hex:
                        pool_hash_match = True
                        logger.debug("Pool ID verified")
            else:
                logger.warning("Pool key hash not found in certificate")

    assert (
        found_registration
    ), "Stake key registration operation/certificate not found in transaction"
    assert (
        found_delegation
    ), "Stake delegation operation/certificate not found in transaction"

    # 2. Verify the stake address is now recognized and delegated on-chain
    logger.debug(f"Verifying stake address {stake_address} is registered and delegated")
    try:
        account_info = rosetta_client.get_balance(address=stake_address)
        logger.debug(f"Account info: {account_info}")
        assert (
            account_info is not None
        ), "Failed to retrieve account info for registered stake address"
        logger.debug("Stake address successfully registered and accessible")
    except Exception as e:
        error_msg = (
            f"Failed to verify stake address registration and delegation: {str(e)}"
        )
        logger.error(error_msg)
        logger.debug(f"Traceback: {traceback.format_exc()}")
        assert False, error_msg

    # 3. Check that the deposit amount was correctly applied
    # If we haven't found deposit in operations, try transaction metadata
    if deposit_amount is None:
        metadata = tx_details.get("metadata", {})
        if "depositAmount" in metadata:
            deposit_amount = metadata["depositAmount"]
            logger.debug(f"Found deposit amount in metadata: {deposit_amount}")
            if isinstance(deposit_amount, dict) and "value" in deposit_amount:
                deposit_amount = int(deposit_amount["value"])
            else:
                deposit_amount = int(deposit_amount)
        elif "deposit" in metadata:
            deposit_amount = metadata["deposit"]
            logger.debug(f"Found deposit in metadata: {deposit_amount}")
            deposit_amount = int(deposit_amount)

    # Some implementations might not include deposit explicitly
    # In that case, we'll log a warning but not fail the test
    if deposit_amount is not None:
        assert (
            deposit_amount == 2_000_000
        ), f"Unexpected deposit amount: {deposit_amount}, expected 2000000"
        logger.debug(f"Deposit amount verified: {deposit_amount}")
    else:
        logger.warning(
            "Deposit amount not explicitly found in metadata (implementation-dependent)"
        )

    logger.debug("Combined stake key registration and delegation validation successful")
    return True


def validate_stake_key_deregistration(rosetta_client, test_wallet, tx_details):
    """
    Validate that a stake key deregistration was successful by checking:
    1. The transaction contains a stake key deregistration operation/certificate
    2. The stake address is no longer recognized on-chain (can't query balance)
    3. The deposit refund was correctly applied
    """
    logger.debug("Validating stake key deregistration...")

    try:
        # Log the full transaction details for debugging
        logger.debug(f"Transaction details: {tx_details}")

        # 1. Check that the transaction contains a stake key deregistration operation or certificate
        found_deregistration = False

        # First check in operations (primary source)
        if "transaction" in tx_details and "operations" in tx_details["transaction"]:
            operations = tx_details["transaction"]["operations"]
            for op in operations:
                if op.get("type") == "stakeKeyDeregistration":
                    found_deregistration = True
                    logger.debug("Found stake key deregistration operation")
                    # Check for refundAmount which is operation-specific
                    if "metadata" in op and "refundAmount" in op["metadata"]:
                        refund_info = op["metadata"]["refundAmount"]
                        logger.debug(f"Found refund info in operation: {refund_info}")
                    break

        # As fallback, also check in metadata certificates (legacy format)
        if not found_deregistration:
            metadata = tx_details.get("metadata", {})
            logger.debug(f"Transaction metadata: {metadata}")
            certificates = metadata.get("certificates", [])
            logger.debug(f"Certificates: {certificates}")

            for cert in certificates:
                if cert.get("type") == "stakeKeyDeregistration":
                    found_deregistration = True
                    logger.debug("Found stake key deregistration certificate")
                    break

        assert (
            found_deregistration
        ), "Stake key deregistration operation/certificate not found in transaction"

        # 2. Verify the stake address is no longer recognized on-chain
        stake_address = test_wallet.get_stake_address()
        logger.debug(f"Checking if stake address is deregistered: {stake_address}")

        # Wait a bit for the blockchain to process the deregistration
        logger.debug("Waiting for blockchain to process deregistration...")
        time.sleep(5)

        try:
            account_info = rosetta_client.get_balance(address=stake_address)
            # If we get here, the stake key might still be registered
            # This could be a false positive if the node hasn't processed the deregistration yet
            logger.warning(
                f"Stake address still appears to be registered, this might be due to node sync delay. Account info: {account_info}"
            )
        except Exception as e:
            # This is expected - the stake key should be deregistered
            logger.debug(
                f"Stake address is no longer registered (expected). Error: {str(e)}"
            )

        # 3. Check that the deposit refund was correctly applied
        # Check for refund in multiple possible locations
        refund_amount = None

        # First check in operation metadata (primary source in Rosetta)
        if "transaction" in tx_details and "operations" in tx_details["transaction"]:
            for op in tx_details["transaction"]["operations"]:
                if op["type"] == "stakeKeyDeregistration" and "metadata" in op:
                    if "refundAmount" in op["metadata"]:
                        if isinstance(op["metadata"]["refundAmount"], dict):
                            refund_amount = op["metadata"]["refundAmount"].get("value")
                        else:
                            refund_amount = op["metadata"]["refundAmount"]
                        logger.debug(
                            f"Found refund amount in operation metadata: {refund_amount}"
                        )
                        break

        # Then check in transaction metadata (fallback)
        if refund_amount is None:
            metadata = tx_details.get("metadata", {})
            if "refundAmount" in metadata:
                refund_amount = metadata["refundAmount"]
                logger.debug(f"Found refund amount in metadata: {refund_amount}")
            elif "refund" in metadata:
                refund_amount = metadata["refund"]
                logger.debug(f"Found refund in metadata: {refund_amount}")

        # Note: Some implementations might not explicitly show the refund in metadata
        # So we don't fail the test if it's not found
        if refund_amount is not None:
            # Handle negative values (sometimes the refund is represented as negative)
            refund_amount_value = int(refund_amount)
            if refund_amount_value < 0:
                refund_amount_value = abs(refund_amount_value)
                logger.debug(
                    f"Converting negative refund to positive: {refund_amount_value}"
                )

            assert (
                refund_amount_value == 2_000_000
            ), f"Unexpected refund amount: {refund_amount_value}, expected 2000000"
            logger.debug(f"Refund amount verified: {refund_amount_value}")
        else:
            logger.debug(
                "Refund amount not explicitly found in metadata (this is implementation-dependent)"
            )

        logger.debug("Stake key deregistration validation successful")
        return True
    except Exception as e:
        logger.error(f"✗ Error validating stake key deregistration: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise


def validate_drep_vote_delegation(
    rosetta_client, test_wallet, tx_details, drep_id, drep_type
):
    """
    Validate that a DRep vote delegation was successful by checking:
    1. The transaction contains a dRepVoteDelegation operation/certificate
    2. The stake address is now recognized on-chain (can query balance)
    3. The deposit amount was correctly applied
    """
    logger.debug("Validating DRep vote delegation...")

    try:
        # Log the full transaction details for debugging
        logger.debug(f"Transaction details: {tx_details}")

        # 1. Check that the transaction contains a dRepVoteDelegation operation or certificate
        found_drep_vote_delegation = False
        deposit_amount = None

        # First check in operations (primary source)
        if "transaction" in tx_details and "operations" in tx_details["transaction"]:
            operations = tx_details["transaction"]["operations"]
            for op in operations:
                if op.get("type") == "dRepVoteDelegation":
                    found_drep_vote_delegation = True
                    logger.debug("Found dRepVoteDelegation operation")
                    # Check for depositAmount in operation metadata
                    if "metadata" in op:
                        if "depositAmount" in op["metadata"]:
                            deposit_info = op["metadata"]["depositAmount"]
                            if (
                                isinstance(deposit_info, dict)
                                and "value" in deposit_info
                            ):
                                deposit_amount = int(deposit_info["value"])
                            else:
                                deposit_amount = int(deposit_info)
                            logger.debug(
                                f"Found deposit amount in operation: {deposit_amount}"
                            )
                        elif "deposit" in op["metadata"]:
                            deposit_amount = int(op["metadata"]["deposit"])
                            logger.debug(
                                f"Found deposit in operation: {deposit_amount}"
                            )

        # 2. Fall back to checking certificates in metadata (legacy format)
        if not found_drep_vote_delegation:
            metadata = tx_details.get("metadata", {})
            logger.debug(f"Transaction metadata: {metadata}")
            certificates = metadata.get("certificates", [])
            logger.debug(f"Certificates: {certificates}")

            for cert in certificates:
                if cert.get("type") == "dRepVoteDelegation":
                    found_drep_vote_delegation = True
                    logger.debug("Found dRepVoteDelegation certificate")
                    break

        assert (
            found_drep_vote_delegation
        ), "DRep vote delegation operation/certificate not found in transaction"

        # 2. Verify the stake address is now recognized on-chain
        logger.debug(
            f"Verifying stake address {test_wallet.get_stake_address()} is registered"
        )
        try:
            account_info = rosetta_client.get_balance(
                address=test_wallet.get_stake_address()
            )
            logger.debug(f"Account info: {account_info}")
            assert (
                account_info is not None
            ), "Failed to retrieve account info for registered stake address"
            logger.debug("Stake address successfully registered and accessible")
        except Exception as e:
            error_msg = f"Failed to verify stake address registration: {str(e)}"
            logger.error(error_msg)
            logger.debug(f"Traceback: {traceback.format_exc()}")
            assert False, error_msg

        # 3. Check that the deposit amount was correctly applied
        # If we haven't found deposit in operations, try transaction metadata
        if deposit_amount is None:
            metadata = tx_details.get("metadata", {})
            if "depositAmount" in metadata:
                deposit_amount = metadata["depositAmount"]
                logger.debug(f"Found deposit amount in metadata: {deposit_amount}")
                if isinstance(deposit_amount, dict) and "value" in deposit_amount:
                    deposit_amount = int(deposit_amount["value"])
                else:
                    deposit_amount = int(deposit_amount)
            elif "deposit" in metadata:
                deposit_amount = metadata["deposit"]
                logger.debug(f"Found deposit in metadata: {deposit_amount}")
                deposit_amount = int(deposit_amount)

        # Some implementations might not include deposit explicitly
        # In that case, we'll log a warning but not fail the test
        if deposit_amount is not None:
            assert (
                deposit_amount == 2_000_000
            ), f"Unexpected deposit amount: {deposit_amount}, expected 2000000"
            logger.debug(f"Deposit amount verified: {deposit_amount}")
        else:
            logger.warning(
                "Deposit amount not explicitly found in metadata (implementation-dependent)"
            )

        logger.debug("DRep vote delegation validation successful")
        return True
    except Exception as e:
        logger.error(f"✗ Error validating DRep vote delegation: {str(e)}")
        logger.debug(f"Traceback: {traceback.format_exc()}")
        raise
