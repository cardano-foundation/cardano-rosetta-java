import pytest
import logging
import time
from typing import List, Dict

from e2e.test_utils.operation_builders import OperationBuilder
from e2e.rosetta_client.exceptions import ValidationError, TransactionError
from e2e.test_utils.validation_utils import verify_address_derivation, verify_final_balance, extract_operations_from_details

# Define logger for the module
logger = logging.getLogger(__name__)

@pytest.mark.parametrize(
    "num_inputs,num_outputs,scenario_name",
    [
        (1, 2, "basic"),  # Basic transaction (1 input, 2 outputs - transfer + change)
        (1, 10, "fan-out"),  # Single input, multiple outputs (fan-out)
        (
            10,
            1,
            "consolidation",
        ),  # Multiple inputs, single output (consolidating UTXOs)
        (10, 10, "complex"),  # Multiple inputs, multiple outputs (complex)
    ],
)
def test_multi_io_transaction(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
    num_inputs: int, 
    num_outputs: int, 
    scenario_name: str
):
    """
    Test multi-input/output transactions with different scenarios:

    1. Consolidation: Multiple inputs, single output (consolidating UTXOs)
    2. Fan-out: Single input, multiple outputs (sending to multiple recipients)
    3. Complex: Multiple inputs, multiple outputs (combination of both)
    4. Basic: Single input, two outputs (simple transfer with change)

    The test follows these steps:
    1. Select appropriate UTXOs based on the scenario
    2. Construct transaction with the specified number of inputs and outputs
    3. Sign and submit the transaction
    4. Validate the transaction on-chain
    """
    logger.info(
        f"Starting test · Scenario: {scenario_name} · Inputs: {num_inputs} · Outputs: {num_outputs}"
    )

    # Get sender address from test wallet
    sender_address = test_wallet.get_address()
    
    try:
        # Verify Base address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # Get initial ADA balance
        initial_balance = rosetta_client.get_ada_balance(sender_address)
        logger.debug(f"Initial ADA balance: {initial_balance:,} lovelace")
        assert initial_balance > 0, "Initial balance must be greater than zero"

        # Constants for the test
        transfer_amount_per_output = 1_500_000  # 1.5 ADA per output
        min_output_value = 1_000_000  # 1 ADA minimum required for outputs
        
        # Calculate total output amount needed (excluding fee which will be calculated later)
        total_output_amount = transfer_amount_per_output * num_outputs
        # Rough estimation of fee for selecting UTXOs
        estimated_fee = 180_000 + (50_000 * (num_inputs + num_outputs - 2))
        
        # Total required amount including estimated fee
        total_required_amount = total_output_amount + estimated_fee

        # Step 1: Select UTXOs based on the scenario
        if num_inputs == 1:
            # For basic or fan-out scenario, select a single UTXO with sufficient funds
            input_utxos = utxo_selector.select_utxos(
                client=rosetta_client,
                address=sender_address,
                required_amount=total_required_amount,
                strategy="single",
                utxo_count=1
            )
            logger.debug(f"Selected single UTXO for {scenario_name} scenario")
        else:
            # For consolidation or complex scenarios, select multiple UTXOs
            input_utxos = utxo_selector.select_utxos(
                client=rosetta_client,
                address=sender_address,
                required_amount=total_required_amount,
                strategy="multiple",
                utxo_count=num_inputs
            )
            logger.debug(f"Selected {len(input_utxos)} UTXOs for {scenario_name} scenario")
        
        # Verify we have the correct number of inputs
        assert len(input_utxos) == num_inputs, f"Failed to select {num_inputs} UTXOs, got {len(input_utxos)}"
        
        # Step 2: Build operations for the transaction
        operations = []
        total_input_value = 0
        
        # Build input operations
        for i, utxo in enumerate(input_utxos):
            input_value = int(utxo["amount"]["value"])
            total_input_value += input_value
            operations.append(
                OperationBuilder.build_input_operation(
                    index=len(operations),
                    address=sender_address,
                    amount=input_value,
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
        
        # Let the orchestrator handle fee calculation instead of reserving a fee amount
        available_for_outputs = total_input_value
        
        # If it's a single output scenario (consolidation), just create one output
        if num_outputs == 1:
            operations.append(
                OperationBuilder.build_output_operation(
                    index=len(operations),
                    address=sender_address,
                    amount=available_for_outputs  # Will be adjusted for actual fee
                )
            )
        else:
            # For multi-output scenarios, create equal outputs but ensure the last one can be adjusted for fees
            # The orchestrator will adjust the last output for the fee
            amount_per_regular_output = available_for_outputs // num_outputs
            
            # Ensure each output has at least the minimum value
            min_output_value = 1_000_000  # 1 ADA
            if amount_per_regular_output < min_output_value:
                logger.warning(f"Insufficient funds to create {num_outputs} outputs with minimum value. Creating fewer outputs.")
                max_possible_outputs = available_for_outputs // min_output_value
                if max_possible_outputs < 1:
                    max_possible_outputs = 1
                amount_per_regular_output = available_for_outputs // max_possible_outputs
                num_outputs = max_possible_outputs
            
            # Create regular outputs with equal values
            amount_used = 0
            for i in range(num_outputs - 1):  # All but the last output
                operations.append(
                    OperationBuilder.build_output_operation(
                        index=len(operations),
                        address=sender_address,
                        amount=amount_per_regular_output
                    )
                )
                amount_used += amount_per_regular_output
            
            # Last output gets remaining funds and will be adjusted for fees
            remaining = available_for_outputs - amount_used
            operations.append(
                OperationBuilder.build_output_operation(
                    index=len(operations),
                    address=sender_address,
                    amount=remaining  # This will be adjusted for fees
                )
            )
        
        logger.debug(f"Built {len(operations)} operations ({len([op for op in operations if op['type'] == 'input'])} inputs, {len([op for op in operations if op['type'] == 'output'])} outputs)")
        
        # Step 3: Build transaction
        unsigned_tx, payloads, metadata, calculated_fee = transaction_orchestrator.build_transaction(
            operations=operations,
            fixed_fee=False  # Allow proper fee calculation based on transaction size
        )
        
        logger.debug(f"Transaction built. Calculated fee: {calculated_fee:,} lovelace")
        
        # If we got a calculated fee different from our estimate, log it
        if calculated_fee and calculated_fee != estimated_fee:
            logger.debug(f"Fee estimate ({estimated_fee:,}) differs from calculated fee ({calculated_fee:,})")
        
        # Step 4: Sign and submit transaction
        signing_function = signing_handler.get_signing_function()
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function
        )
        
        # Step 5: Verify transaction details
        # Add debug logging to inspect tx_details structure
        logger.debug(f"Transaction details structure: {tx_details.keys() if isinstance(tx_details, dict) else 'not a dict'}")
        
        # Try to extract operations using the utility function
        onchain_ops = extract_operations_from_details(tx_details, logger)
        
        # If operations couldn't be extracted, fetch from block
        if onchain_ops is None:
            logger.warning("Operations not found in tx_details, fetching from block transaction")
            # Use the transaction orchestrator's wait_for_confirmation method which should return parsed details
            try:
                # Assuming wait_for_confirmation returns details including the operations list
                tx_details_from_block = transaction_orchestrator.wait_for_confirmation(tx_hash, timeout_seconds=180)
                onchain_ops = extract_operations_from_details(tx_details_from_block, logger)
                if onchain_ops is None:
                     raise AssertionError(f"Could not extract operations even after fetching tx {tx_hash} from block.")
            except TimeoutError as e:
                raise AssertionError(f"Transaction {tx_hash} not found on-chain: {str(e)}")
            except Exception as e:
                raise AssertionError(f"Error fetching/parsing transaction details for {tx_hash}: {str(e)}")
        
        # Now proceed with the verification using onchain_ops
        input_ops = [op for op in onchain_ops if op["type"] == "input"]
        output_ops = [op for op in onchain_ops if op["type"] == "output"]
        
        logger.debug(f"Transaction has {len(input_ops)} inputs and {len(output_ops)} outputs")
        
        # Verify input count matches expected
        assert len(input_ops) == num_inputs, f"Expected {num_inputs} input operations, got {len(input_ops)}"
        
        # Verify output count matches expected
        assert len(output_ops) == num_outputs, f"Expected {num_outputs} output operations, got {len(output_ops)}"
        
        # Step 6: Verify fee and balance change
        try:
            # Try to use the orchestrator's method first
            onchain_fee = transaction_orchestrator.calculate_onchain_fee(tx_details)
        except (ValueError, KeyError):
            # If that fails, calculate it from operations we have
            total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
            total_output = sum(int(op["amount"]["value"]) for op in output_ops)
            onchain_fee = total_input - total_output
            
        logger.debug(f"On-chain fee: {onchain_fee:,} lovelace")
        
        # Verify the fee matches what was calculated - Cardano fees are deterministic
        if calculated_fee:
            assert onchain_fee == calculated_fee, \
                f"Fee mismatch: calculated fee was {calculated_fee:,}, but on-chain fee is {onchain_fee:,}"
        
        # Use the utility function to verify the final balance
        verify_final_balance(
            rosetta_client,
            logger,
            sender_address,
            initial_balance,
            onchain_fee # In this test, deposit/refund are 0
        )
        
        logger.info(f"Validation successful · Initial balance: {initial_balance:,} · Fee: {onchain_fee:,}!")
        
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        # Add HTTP request debugging if available
        if hasattr(rosetta_client, "request_debugger"):
            rosetta_client.request_debugger.print_summary_report()
        raise

def test_fixed_fee_transaction(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector
):
    """
    Test transaction with fixed fee handling, bypassing the fee calculation.
    
    This test simulates a client that:
    1. Doesn't use the standard fee calculation flow (preprocess/metadata)
    2. Instead, uses a fixed predetermined fee (e.g., 4 ADA)
    3. The fee is expressed implicitly as the difference between inputs and outputs.
    4. Construct and submit the transaction.
    5. Validate the transaction on-chain and check if the fee matches the fixed fee.
    """
    fixed_fee = 4_000_000  # 4 ADA fixed fee
    logger.info(f"Starting test · Scenario: fixed_fee · Fee: {fixed_fee:,} lovelace")

    sender_address = test_wallet.get_address()
    recipient_address = sender_address # Send to self

    try:
        # Verify Base address derivation first
        verify_address_derivation(rosetta_client, test_wallet, logger, "Base")

        # 1. Get initial balance
        initial_balance = rosetta_client.get_ada_balance(sender_address)
        logger.debug(f"Initial balance: {initial_balance:,} lovelace")

        # Constants for the test
        transfer_amount = 1_500_000  # 1.5 ADA transfer (arbitrary)
        min_output_value = 1_000_000  # 1 ADA minimum required for outputs
        
        # Calculate total amount needed for the transaction outputs
        # We need enough input to cover the transfer amount AND the fixed fee
        required_input_amount = transfer_amount + fixed_fee
        # Also need enough for the change output to be >= min_output_value
        # input = transfer + fee + change >= transfer + fee + min_output
        required_input_amount_with_min_change = transfer_amount + fixed_fee + min_output_value

        # Step 2: Select UTXO(s) with sufficient funds
        input_utxos = utxo_selector.select_utxos(
            client=rosetta_client,
            address=sender_address,
            required_amount=required_input_amount_with_min_change,
            strategy="multiple",  # Use valid strategy name
            utxo_count=2  # We need at least a couple of UTXOs for this test
        )
        logger.debug(f"Selected {len(input_utxos)} UTXO(s) for fixed fee transaction.")

        # 3. Build operations manually to enforce fixed fee
        operations = []
        total_input_value = 0
        # Input operations
        for i, utxo in enumerate(input_utxos):
            input_value = int(utxo["amount"]["value"])
            total_input_value += input_value
            operations.append(
                OperationBuilder.build_input_operation(
                    index=len(operations),
                    address=sender_address,
                    amount=input_value,
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
        
        # Calculate required output values based on fixed fee
        # Total output value must be exactly total_input_value - fixed_fee
        total_output_value_required = total_input_value - fixed_fee

        # Output operations: 1 transfer, 1 change
        transfer_output_value = transfer_amount
        change_output_value = total_output_value_required - transfer_output_value

        # Basic validation
        if change_output_value < min_output_value:
             logger.error(
                f"Calculated change ({change_output_value:,}) is below minimum required ({min_output_value:,}). "
                f"Input ({total_input_value:,}) likely insufficient for transfer ({transfer_amount:,}) + fixed fee ({fixed_fee:,}) + min change."
            )
             raise ValueError("Insufficient funds to meet minimum UTXO requirements for change with fixed fee.")

        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=recipient_address,
                amount=transfer_output_value
            )
        )
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=sender_address, # Change back to self
                amount=change_output_value
            )
        )
        
        # 4. Build transaction - CRUCIALLY, do NOT pass fixed_fee=True to orchestrator
        # The orchestrator's build_transaction assumes dynamic fee calculation unless told otherwise
        # Here, the fee is *implicitly* defined by the input/output difference.
        # We rely on the underlying rosetta-client/cardano-node to accept this.
        unsigned_tx, payloads, metadata, calculated_fee = transaction_orchestrator.build_transaction(
            operations=operations,
            # We are passing fixed_fee=True to tell the orchestrator to use our fee calculation
            # instead of recalculating it based on the transaction size
            fixed_fee=True
        )
        
        logger.debug(f"Transaction built. Orchestrator suggested fee (ignored): {calculated_fee:,} lovelace. Implicit fee: {fixed_fee:,}")
        
        # 5. Sign and submit transaction
        signing_function = signing_handler.get_signing_function()
        tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
            unsigned_transaction=unsigned_tx,
            payloads=payloads,
            signing_function=signing_function
        )
        
        # 6. Verify fee ON-CHAIN
        # Add debug logging to inspect tx_details structure
        logger.debug(f"Transaction details structure: {tx_details.keys() if isinstance(tx_details, dict) else 'not a dict'}")
        
        # Try to extract operations using the utility function
        onchain_ops = extract_operations_from_details(tx_details, logger)
        
        # If operations couldn't be extracted, fetch from block
        if onchain_ops is None:
            logger.warning("Operations not found in tx_details, fetching from block transaction")
            # Use the transaction orchestrator's wait_for_confirmation method which should return parsed details
            try:
                # Assuming wait_for_confirmation returns details including the operations list
                tx_details_from_block = transaction_orchestrator.wait_for_confirmation(tx_hash, timeout_seconds=180)
                onchain_ops = extract_operations_from_details(tx_details_from_block, logger)
                if onchain_ops is None:
                     raise AssertionError(f"Could not extract operations even after fetching tx {tx_hash} from block.")
            except TimeoutError as e:
                raise AssertionError(f"Transaction {tx_hash} not found on-chain: {str(e)}")
            except Exception as e:
                raise AssertionError(f"Error fetching/parsing transaction details for {tx_hash}: {str(e)}")
        
        # Calculate fee from operations
        input_ops = [op for op in onchain_ops if op["type"] == "input"]
        output_ops = [op for op in onchain_ops if op["type"] == "output"]
        
        # Verify input and output operations
        logger.debug(f"Transaction has {len(input_ops)} inputs and {len(output_ops)} outputs")
        assert len(input_ops) > 0, "Expected at least one input operation"
        assert len(output_ops) > 0, "Expected at least one output operation"
        
        try:
            # Try to use the orchestrator's method first if we have the full transaction details
            if 'transaction' in tx_details:
                onchain_fee = transaction_orchestrator.calculate_onchain_fee(tx_details)
            else:
                # Calculate it directly from operations
                total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
                total_output = sum(int(op["amount"]["value"]) for op in output_ops)
                onchain_fee = total_input - total_output
        except (ValueError, KeyError):
            # If that fails, calculate it directly from operations
            total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
            total_output = sum(int(op["amount"]["value"]) for op in output_ops)
            onchain_fee = total_input - total_output
        
        assert onchain_fee == fixed_fee, f"Fee mismatch: expected fixed fee {fixed_fee:,}, but on-chain fee was {onchain_fee:,}"
        
        # 7. Verify balance change
        # Use the utility function to verify the final balance
        verify_final_balance(
            rosetta_client,
            logger,
            sender_address,
            initial_balance,
            onchain_fee # In this test, deposit/refund are 0
        )
        
        logger.info(f"Validation successful · Initial balance: {initial_balance:,} · Fee: {onchain_fee:,}!")
        
    except Exception as e:
        logger.error(f"‼ Test failed: {str(e)}")
        # Add HTTP request debugging if available
        if hasattr(rosetta_client, "request_debugger"):
            rosetta_client.request_debugger.print_summary_report()
        raise 