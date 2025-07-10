import pytest
import logging
import os
from typing import Dict, List

from e2e_tests.test_utils.operation_builders import OperationBuilder
from e2e_tests.rosetta_client.exceptions import ValidationError
from e2e_tests.test_utils.validation_utils import (
    verify_address_derivation,
    verify_final_balance,
)

logger = logging.getLogger(__name__)


# --- Helper to fetch current epoch ---
def get_current_epoch(rosetta_client):
    """
    Fetch the current epoch number from the latest block's metadata via Rosetta API.
    """
    status = rosetta_client.network_status()
    latest_block_index = status["current_block_identifier"]["index"]
    block = rosetta_client.block({"index": latest_block_index})
    epoch_no = block["block"]["metadata"].get("epochNo")
    if epoch_no is None:
        raise RuntimeError("Could not determine current epoch from block metadata.")
    return int(epoch_no)


# --- Constants ---
POOL_DEPOSIT = 500_000_000  # 500 ADA pool registration deposit

# --- Pool Operations Tests ---


@pytest.mark.order(1)
def test_pool_registration(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
):
    """
    Test stake pool registration using the Rosetta Construction API.
    Pool Operations Step 1.
    """
    logger.info("⬧ Pool Operations Step 1: Pool Registration")

    # Set up test parameters
    payment_address = test_wallet.get_address()

    # Generate pool keys and get pool address
    test_wallet.generate_pool_keys()
    pool_address = test_wallet.get_pool_cold_address()
    pool_key_hex = test_wallet.get_pool_cold_verification_key_hex()

    # Generate pool registration parameters
    pool_registration_params = test_wallet.generate_pool_registration_params()

    try:
        # Verify address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Registration requires 500 ADA deposit + tx fee
        required_amount = (
            500_000_000 + 200_000 + 1_000_000
        )  # deposit + est. fee + min change

        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single",
        )

        # 3. Build operations
        operations = OperationBuilder.build_pool_registration_operations(
            payment_address=payment_address,
            pool_address=pool_address,
            pool_registration_params=pool_registration_params,
            input_utxos=input_utxos,
        )

        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = (
            transaction_orchestrator.build_transaction(operations=operations)
        )

        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # 5. Sign and submit transaction
        # Pool registration requires three signatures: stake key (owner), payment key (UTxO), and pool cold key (certificate).
        from e2e_tests.test_utils.signing_handler import KeyType

        # Inspect payload order for transparency
        logger.debug(f"Pool registration payload count: {len(payloads)}")
        for i, p in enumerate(payloads):
            addr = p.get("account_identifier", {}).get("address")
            logger.debug(f"  Payload {i} address: {addr}")

        # Define which key signs each payload index (based on Rosetta response)
        index_key_map = {
            0: KeyType.STAKE,
            1: KeyType.PAYMENT,
            2: KeyType.POOL_COLD,
        }

        # Pre-sign payloads and build a lookup by object id (so orchestrator can call with single arg)
        signed_map = {}
        for idx, payload in enumerate(payloads):
            key_type = index_key_map.get(idx)
            if key_type == KeyType.PAYMENT:
                sig = signing_handler.sign_with_payment_key(payload)
            elif key_type == KeyType.STAKE:
                sig = signing_handler.sign_with_stake_key(payload)
            elif key_type == KeyType.POOL_COLD:
                sig = signing_handler.sign_with_pool_cold_key(payload)
            else:
                raise ValueError(f"Unexpected key type for payload {idx}: {key_type}")
            signed_map[id(payload)] = sig

        # Single-arg signing function expected by TransactionOrchestrator
        def signing_function(payload_to_sign):
            try:
                return signed_map[id(payload_to_sign)]
            except KeyError:
                raise ValueError("Payload not found in pre-signed map")

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function,
            expected_operations=["poolRegistration"],
        )

        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,
            deposit=POOL_DEPOSIT,  # Pool registration includes deposit
        )

        logger.info(f"Pool registered successfully: {tx_hash} · Fee: {fee:,} lovelace!")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(2)
def test_pool_registration_with_cert(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    pool_registration_cert,
):
    """
    Test stake pool certificate update using the Rosetta Construction API.
    Pool Operations Step 2. Updates existing pool - no additional deposit required.
    """
    logger.info("⬧ Pool Operations Step 2: Pool Certificate Update")

    # Set up test parameters
    payment_address = test_wallet.get_address()

    # Use existing pool keys from previous test
    if not hasattr(test_wallet, "pool_cold_verification_key"):
        test_wallet.generate_pool_keys()
    pool_address = test_wallet.get_pool_cold_address()

    try:
        # Verify address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Certificate update only requires tx fee (no additional deposit)
        required_amount = 200_000 + 1_000_000  # est. fee + min change

        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single",
        )

        # 3. Build operations
        input_ops = []
        total_input = 0
        for i, utxo in enumerate(input_utxos):
            utxo_value = int(utxo["amount"]["value"])
            total_input += utxo_value
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=utxo_value,
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input,  # No deposit deduction for certificate update
            )
        ]
        cert_ops = [
            OperationBuilder.build_pool_registration_with_cert_operation(
                index=len(input_ops) + len(output_ops),
                pool_address=pool_address,
                pool_registration_cert=pool_registration_cert,
            )
        ]
        operations = input_ops + output_ops + cert_ops

        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = (
            transaction_orchestrator.build_transaction(operations=operations)
        )

        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # 5. Sign and submit transaction
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function,
            expected_operations=["poolRegistrationWithCert"],
        )

        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,  # Certificate update only requires fee (no deposit)
        )

        logger.info(
            f"Pool certificate updated successfully: {tx_hash} · Fee: {fee:,} lovelace!"
        )

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(3)
def test_pool_governance_vote(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    pool_governance_proposal_id,
):
    """
    Test stake pool governance vote using the Rosetta Construction API.
    Pool Operations Step 3.
    Requires POOL_GOVERNANCE_PROPOSAL_ID environment variable.
    """
    logger.info(
        f"⬧ Pool Operations Step 3: Pool Governance Vote » Proposal: {pool_governance_proposal_id}"
    )

    # Set up test parameters
    payment_address = test_wallet.get_address()

    # Use existing pool keys from previous tests
    if not hasattr(test_wallet, "pool_cold_verification_key"):
        test_wallet.generate_pool_keys()
    pool_address = test_wallet.get_pool_cold_address()
    pool_key_hex = test_wallet.get_pool_cold_verification_key_hex()

    # Parse governance proposal ID (now a hash string)
    governance_action_hash = pool_governance_proposal_id

    # Pool credential
    pool_credential = {"hex_bytes": pool_key_hex, "curve_type": "edwards25519"}

    # Vote choice (configurable via environment or default to "yes")
    vote = os.environ.get("POOL_VOTE_CHOICE", "yes")

    try:
        # Verify address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Governance vote only requires tx fee
        required_amount = 200_000 + 1_000_000  # est. fee + min change

        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single",
        )

        # 3. Build operations
        operations = OperationBuilder.build_pool_governance_vote_operations(
            payment_address=payment_address,
            pool_address=pool_address,
            pool_credential=pool_credential,
            governance_action_hash=governance_action_hash,
            vote=vote,
            input_utxos=input_utxos,
        )

        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = (
            transaction_orchestrator.build_transaction(operations=operations)
        )

        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # 5. Sign and submit transaction
        # Pool governance vote requires pool cold key signature
        from e2e_tests.test_utils.signing_handler import KeyType

        # Inspect payload order for transparency
        logger.debug(f"Pool governance vote payload count: {len(payloads)}")
        for i, p in enumerate(payloads):
            addr = p.get("account_identifier", {}).get("address")
            logger.debug(f"  Payload {i} address: {addr}")

        # Define which key signs each payload index (based on Rosetta response)
        # For pool governance vote: payment key for UTXO, pool cold key for the vote operation
        index_key_map = {
            0: KeyType.PAYMENT,
            1: KeyType.POOL_COLD,
        }

        # Pre-sign payloads and build a lookup by object id
        signed_map = {}
        for idx, payload in enumerate(payloads):
            key_type = index_key_map.get(idx, KeyType.PAYMENT)
            if key_type == KeyType.PAYMENT:
                sig = signing_handler.sign_with_payment_key(payload)
            elif key_type == KeyType.POOL_COLD:
                sig = signing_handler.sign_with_pool_cold_key(payload)
            else:
                raise ValueError(f"Unexpected key type for payload {idx}: {key_type}")
            signed_map[id(payload)] = sig

        # Single-arg signing function expected by TransactionOrchestrator
        def signing_function(payload_to_sign):
            try:
                return signed_map[id(payload_to_sign)]
            except KeyError:
                raise ValueError("Payload not found in pre-signed map")

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function,
            expected_operations=["poolGovernanceVote"],
        )

        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,  # No deposit required for governance vote
        )

        logger.info(
            f"Pool governance vote submitted: {tx_hash} · Fee: {fee:,} lovelace · Vote: {vote}!"
        )

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(4)
def test_pool_retirement(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
):
    """
    Test stake pool retirement using the Rosetta Construction API.
    Pool Operations Step 4. Cleans up pool from previous tests.
    """
    logger.info("⬧ Pool Operations Step 4: Pool Retirement")

    # Set up test parameters
    payment_address = test_wallet.get_address()

    # Use existing pool keys from previous tests
    if not hasattr(test_wallet, "pool_cold_verification_key"):
        test_wallet.generate_pool_keys()
    pool_address = test_wallet.get_pool_cold_address()

    # Set retirement epoch (current epoch + 2 for safety)
    # retirement_epoch = 300  # Example epoch, in practice would query current epoch
    current_epoch = get_current_epoch(rosetta_client)
    retirement_epoch = current_epoch + 1

    try:
        # Verify address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Retirement requires tx fee (no deposit refund in this transaction)
        required_amount = 200_000 + 1_000_000  # est. fee + min change

        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single",
        )

        # 3. Build operations
        operations = OperationBuilder.build_pool_retirement_operations(
            payment_address=payment_address,
            pool_address=pool_address,
            epoch=retirement_epoch,
            input_utxos=input_utxos,
        )

        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = (
            transaction_orchestrator.build_transaction(operations=operations)
        )

        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # 5. Sign and submit transaction
        # Pool retirement requires payment key and pool cold key signatures
        from e2e_tests.test_utils.signing_handler import KeyType

        # Inspect payload order for transparency
        logger.debug(f"Pool retirement payload count: {len(payloads)}")
        for i, p in enumerate(payloads):
            addr = p.get("account_identifier", {}).get("address")
            logger.debug(f"  Payload {i} address: {addr}")

        # Define which key signs each payload index (based on Rosetta response)
        # For pool retirement: payment key for UTXO, pool cold key for the retirement operation
        index_key_map = {
            0: KeyType.PAYMENT,
            1: KeyType.POOL_COLD,
        }

        # Pre-sign payloads and build a lookup by object id
        signed_map = {}
        for idx, payload in enumerate(payloads):
            key_type = index_key_map.get(idx, KeyType.PAYMENT)
            if key_type == KeyType.PAYMENT:
                sig = signing_handler.sign_with_payment_key(payload)
            elif key_type == KeyType.POOL_COLD:
                sig = signing_handler.sign_with_pool_cold_key(payload)
            else:
                raise ValueError(f"Unexpected key type for payload {idx}: {key_type}")
            signed_map[id(payload)] = sig

        # Single-arg signing function expected by TransactionOrchestrator
        def signing_function(payload_to_sign):
            try:
                return signed_map[id(payload_to_sign)]
            except KeyError:
                raise ValueError("Payload not found in pre-signed map")

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function,
            expected_operations=["poolRetirement"],
        )

        # 6. Verify balance
        verify_final_balance(
            rosetta_client, logger, payment_address, initial_balance, fee
        )

        logger.info(
            f"Pool retired successfully: {tx_hash} · Fee: {fee:,} lovelace · Epoch: {retirement_epoch}!"
        )

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise
