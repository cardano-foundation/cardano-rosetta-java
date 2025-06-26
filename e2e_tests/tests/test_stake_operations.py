import pytest
import logging
import os
from typing import Dict, List

from e2e_tests.test_utils.operation_builders import OperationBuilder
from e2e_tests.rosetta_client.exceptions import ValidationError
from e2e_tests.test_utils.validation_utils import verify_address_derivation, verify_final_balance

logger = logging.getLogger(__name__)

# --- Constants ---
STAKE_KEY_DEPOSIT = 2_000_000

# --- Scenario A: Separate Operations --- 

@pytest.mark.order(1)
def test_stake_key_registration(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Test stake key registration using the Rosetta Construction API.
    Scenario A, Step 1.
    """
    logger.info("⬧ Scenario A.1: Stake Key Registration")
    
    # Set up test parameters
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    
    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")
        
        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")
        
        # Registration requires 2 ADA deposit + tx fee
        required_amount = 2_000_000 + 200_000 + 1_000_000  # deposit + est. fee + min change
        
        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )
        
        # 3. Build operations
        operations = OperationBuilder.build_stake_key_registration_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            input_utxos=input_utxos
        )
        
        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")
        
        # 5. Sign and submit transaction
        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)
        
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["registration"]
        )
        
        
        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,
            deposit=STAKE_KEY_DEPOSIT # Registration includes deposit
        )
            
        logger.info(f"Stake key registered successfully: {tx_hash} · Fee: {fee:,} lovelace!")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(9)
def test_reward_withdrawal_zero(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Withdraw 0 ADA rewards after stake delegation.
    Scenario A, Step 2b.
    """
    logger.info("⬧ Scenario A.2b: Reward Withdrawal (0 ADA)")

    # Set up addresses and keys
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    stake_key_hex = test_wallet.get_stake_verification_key_hex()

    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Withdrawal only requires tx fee
        required_amount = 200_000 + 1_000_000  # est. fee + min change

        # 2. Select a single UTXO
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )

        # 3. Build operations (1 input, 1 withdrawal, 1 output)
        operations = []
        total_input = 0
        for utxo in input_utxos:
            utxo_value = int(utxo["amount"]["value"])
            total_input += utxo_value
            operations.append(
                OperationBuilder.build_input_operation(
                    index=len(operations),
                    address=payment_address,
                    amount=utxo_value,
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )

        # Withdrawal (0 ADA) operation
        operations.append({
            "operation_identifier": {"index": len(operations)},
            "type": "withdrawal",
            "status": "",
            "account": {"address": stake_address},
            "amount": {"value": "0", "currency": {"symbol": "ADA", "decimals": 6}},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519"
                }
            }
        })

        # Change output back to self (fee will be deducted automatically)
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input  # fee adjustment handled by orchestrator
            )
        )

        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # 5. Sign and submit transaction
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function,
            expected_operations=["withdrawal"]
        )

        # 6. Verify balance (no deposit or refund)
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee
        )

        logger.info(f"Reward withdrawal (0 ADA) successful: {tx_hash} · Fee: {fee:,} lovelace")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(2)
def test_stake_delegation(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    stake_pool_hash
):
    """
    Test stake delegation using the Rosetta Construction API.
    Scenario A, Step 2. Assumes stake key is registered from Step 1.
    """
    logger.info(f"⬧ Scenario A.2: Stake Delegation » Pool: {stake_pool_hash}")
    
    # Set up test parameters
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    
    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")
        
        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")
        
        # Delegation only requires tx fee
        required_amount = 200_000 + 1_000_000  # est. fee + min change
        
        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )
        
        # 3. Build operations
        operations = OperationBuilder.build_stake_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            pool_id=stake_pool_hash,
            input_utxos=input_utxos
        )
        
        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")
        
        # 5. Sign and submit transaction
        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)
        
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["delegation"]
        )
        
        
        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee # Delegation has no deposit/refund
        )
            
        logger.info(f"Stake delegated successfully: {tx_hash} · Fee: {fee:,} lovelace · Pool: {stake_pool_hash}!")
        
    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise


@pytest.mark.order(3) 
def test_scenario_A_deregistration(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Test stake key deregistration using the Rosetta Construction API.
    Scenario A, Step 3. Assumes stake key is registered from previous steps.
    Cleans up Scenario A.
    """
    logger.info("⬧ Scenario A.3: Stake Key Deregistration")
    
    # Set up test parameters
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    
    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")
        
        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")
        
        # Deregistration refunds 2 ADA but requires tx fee
        required_amount = 200_000 + 1_000_000  # est. fee + min change
        
        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )
        
        # 3. Build operations for deregistration
        operations = []
        total_input = 0
        for i, utxo in enumerate(input_utxos):
            utxo_value = int(utxo["amount"]["value"])
            total_input += utxo_value
            operations.append(
                OperationBuilder.build_input_operation(
                    index=len(operations),
                    address=payment_address,
                    amount=utxo_value,
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
        
        operations.append(
            OperationBuilder.build_stake_key_deregistration_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex
            )
        )
        
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input + 2_000_000  # Add refund to change calculation implicitly
            )
        )
        
        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")
        
        # 5. Sign and submit transaction
        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)
        
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["deregistration"]
        )
        
        
        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,
            refund=STAKE_KEY_DEPOSIT # Deregistration includes refund
        )
            
        logger.info(f"Stake key deregistered successfully: {tx_hash} · Fee: {fee:,} lovelace · Refund: 2 ADA!")
        
    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

# --- Scenario B: Combined Operations + Votes --- 

@pytest.mark.order(4) # Reordered
def test_combined_registration_delegation(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    stake_pool_hash
):
    """
    Test combined stake key registration and delegation in a single transaction.
    Scenario B, Step 1.
    """
    logger.info(f"⬧ Scenario B.1: Combined Registration and Delegation » Pool: {stake_pool_hash}")
    
    # Set up test parameters
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    
    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")
        
        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")
        
        # Registration + delegation requires 2 ADA deposit + tx fee
        required_amount = 2_000_000 + 200_000 + 1_000_000  # deposit + est. fee + min change
        
        # 2. Select UTXOs
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )
        
        # 3. Build operations
        operations = OperationBuilder.build_combined_registration_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            pool_id=stake_pool_hash,
            input_utxos=input_utxos
        )
        
        # 4. Build transaction
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")
        
        # 5. Sign and submit transaction
        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)
        
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["registration", "delegation"]
        )
        
        
        # 6. Verify balance
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,
            deposit=STAKE_KEY_DEPOSIT # Combined registration includes deposit
        )
            
        logger.info(f"Stake key registered and delegated: {tx_hash} · Fee: {fee:,} lovelace · Pool: {stake_pool_hash}")
        
    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

# Existing vote tests, reordered 
@pytest.mark.order(5)
def test_drep_vote_delegation_abstain(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Test DRep vote delegation to Abstain using the Rosetta Construction API.
    Scenario B, Step 2. Assumes stake key is registered from Step 1.
    """
    logger.info("⬧ Scenario B.2: DRep Vote Delegation » Type: abstain")

    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()

    # Verify stake key is registered
    try:
        _ = rosetta_client.account_balance(stake_address)
        logger.debug("Stake key is registered (verified)")
    except Exception as e:
        pytest.skip(f"Stake key not registered: {str(e)}. Run registration test first.")

    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        required_amount = 200_000 + 1_000_000  # est. fee + min change
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )

        operations = OperationBuilder.build_drep_vote_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            drep_type="abstain",
            input_utxos=input_utxos
        )

        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["drepVoteDelegation"]
        )
        

        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee # DRep vote delegation has no deposit/refund
        )
            
        logger.info(f"Vote delegation (abstain) successful: {tx_hash} · Fee: {fee:,} lovelace")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

@pytest.mark.order(6)
def test_drep_vote_delegation_no_confidence(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Test DRep vote delegation to No Confidence using the Rosetta Construction API.
    Scenario B, Step 3. Assumes stake key is registered.
    """
    logger.info("⬧ Scenario B.3: DRep Vote Delegation » Type: no_confidence")

    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()

    # Verify stake key is registered
    try:
        _ = rosetta_client.account_balance(stake_address)
        logger.debug("Stake key is registered (verified)")
    except Exception as e:
        pytest.skip(f"Stake key not registered: {str(e)}. Run registration test first.")

    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        required_amount = 200_000 + 1_000_000  # est. fee + min change
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )

        operations = OperationBuilder.build_drep_vote_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            drep_type="no_confidence",
            input_utxos=input_utxos
        )

        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["drepVoteDelegation"]
        )
        

        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee # DRep vote delegation has no deposit/refund
        )
            
        logger.info(f"Vote delegation (no confidence) successful: {tx_hash} · Fee: {fee:,} lovelace")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

@pytest.mark.order(7)
def test_drep_vote_delegation_key_hash(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    drep_key_hash_id # Fixture providing the DRep key hash
):
    """
    Test DRep vote delegation to a specific DRep (key hash).
    Scenario B, Step 4. Assumes stake key is registered.
    """
    logger.info(f"⬧ Scenario B.4: DRep Vote Delegation » Type: key_hash, ID: {drep_key_hash_id}")

    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()

    # Verify stake key is registered
    try:
        _ = rosetta_client.account_balance(stake_address)
        logger.debug("Stake key is registered (verified)")
    except Exception as e:
        pytest.skip(f"Stake key not registered: {str(e)}. Run registration test first.")

    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        required_amount = 200_000 + 1_000_000  # est. fee + min change
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )

        operations = OperationBuilder.build_drep_vote_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            drep_id=drep_key_hash_id,
            drep_type="key_hash",
            input_utxos=input_utxos
        )

        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["drepVoteDelegation"]
        )
        

        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee # DRep vote delegation has no deposit/refund
        )
            
        logger.info(f"Vote delegation (key hash) successful: {tx_hash} · Fee: {fee:,} lovelace · DRep: {drep_key_hash_id}")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

@pytest.mark.order(8)
def test_drep_vote_delegation_script_hash(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    drep_script_hash_id # Fixture providing the DRep script hash
):
    """
    Test DRep vote delegation to a specific DRep (script hash).
    Scenario B, Step 5. Assumes stake key is registered.
    """
    logger.info(f"⬧ Scenario B.5: DRep Vote Delegation » Type: script_hash, ID: {drep_script_hash_id}")

    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()

    # Verify stake key is registered
    try:
        _ = rosetta_client.account_balance(stake_address)
        logger.debug("Stake key is registered (verified)")
    except Exception as e:
        pytest.skip(f"Stake key not registered: {str(e)}. Run registration test first.")

    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        required_amount = 200_000 + 1_000_000  # est. fee + min change
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )

        operations = OperationBuilder.build_drep_vote_delegation_operations(
            payment_address=payment_address,
            stake_address=stake_address,
            stake_key_hex=stake_key_hex,
            drep_id=drep_script_hash_id,
            drep_type="script_hash",
            input_utxos=input_utxos
        )

        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")

        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)

        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["drepVoteDelegation"]
        )
        

        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee # DRep vote delegation has no deposit/refund
        )
            
        logger.info(f"Vote delegation (script hash) successful: {tx_hash} · Fee: {fee:,} lovelace · DRep: {drep_script_hash_id}")

    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise

# New test function for final deregistration
@pytest.mark.order(10) 
def test_scenario_B_final_deregistration(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Final stake key deregistration after Scenario B.
    Scenario B, Step 6. Ensures the stake key is deregistered at the end.
    """
    logger.info("⬧ Scenario B.6: Final Stake Key Deregistration")
    
    payment_address = test_wallet.get_address()
    stake_address = test_wallet.get_stake_address()
    
    # Use the verification key bytes, not the key hash
    stake_key_hex = test_wallet.get_stake_verification_key_hex()
    
    # Verify stake key is registered before attempting deregistration
    try:
        _ = rosetta_client.account_balance(stake_address)
        logger.debug("Stake key is registered (verified), proceeding with deregistration.")
    except Exception as e:
        pytest.skip(f"Stake key not registered: {str(e)}. Cannot run final deregistration.")
    
    # Re-use the logic from the original deregistration test
    try:
        # Verify Reward address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Reward")

        initial_balance = rosetta_client.get_ada_balance(payment_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")
        
        required_amount = 200_000 + 1_000_000  # est. fee + min change
        
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=payment_address,
            required_amount=required_amount,
            strategy="single"
        )
        
        operations = []
        total_input = 0
        for i, utxo in enumerate(input_utxos):
            utxo_value = int(utxo["amount"]["value"])
            total_input += utxo_value
            operations.append(
                OperationBuilder.build_input_operation(
                    index=len(operations),
                    address=payment_address,
                    amount=utxo_value,
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
        
        operations.append(
            OperationBuilder.build_stake_key_deregistration_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex
            )
        )
        
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input + 2_000_000  # Add refund implicitly
            )
        )
        
        unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(
            operations=operations
        )
        logger.debug(f"Transaction built with fee: {fee:,} lovelace")
        
        # Create the signing function using the handler
        signing_function = signing_handler.create_combined_signing_function(payloads)
        
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function, # Use the generated function
            expected_operations=["deregistration"]
        )
        
        
        verify_final_balance(
            rosetta_client,
            logger,
            payment_address,
            initial_balance,
            fee,
            refund=STAKE_KEY_DEPOSIT # Final deregistration includes refund
        )
            
        logger.info(f"Final stake key deregistered successfully: {tx_hash} · Fee: {fee:,} lovelace · Refund: 2 ADA!")
        
    except ValidationError as e:
        logger.error(f"Transaction validation failed · Error: {str(e)}!!")
        raise
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        raise 