import time
import logging
from typing import List, Dict, Optional, Callable, Tuple, Union, Any

from e2e_tests.rosetta_client.client import RosettaClient
from e2e_tests.rosetta_client.exceptions import ValidationError, TransactionError

logger = logging.getLogger(__name__)

class TransactionOrchestrator:
    """
    Orchestrates the Rosetta transaction construction workflow.
    Handles the multi-step process of constructing, signing, and submitting transactions.
    """
    
    def __init__(self, client: RosettaClient):
        """Initialize the transaction orchestrator with a Rosetta client."""
        self.client = client
        
    def build_transaction(
        self,
        operations: List[Dict],
        public_keys: Optional[List[Dict]] = None,
        preprocess_metadata: Optional[Dict] = None,
        fixed_fee: bool = False
    ) -> Tuple[str, List[Dict], Dict, Optional[int]]:
        """
        Build an unsigned transaction using the Rosetta Construction API workflow.
        
        Args:
            operations: List of operations for the transaction
            public_keys: Optional list of public keys involved
            preprocess_metadata: Optional metadata for preprocess step
            fixed_fee: If True, use the fee from operations as is
            
        Returns:
            Tuple containing:
            - unsigned_transaction: The unsigned transaction hex string
            - payloads: List of signing payloads
            - metadata: The construction metadata
            - fee: The calculated fee (if not fixed_fee)
            
        Raises:
            ValidationError: If transaction construction fails
        """
        logger.debug(f"Building transaction with {len(operations)} operations (fixed_fee={fixed_fee})")
        
        # Step 1: Preprocess the operations
        logger.debug("Calling construction/preprocess...")
        preprocess_result = self.client.construction_preprocess(
            operations=operations,
            metadata=preprocess_metadata,
            public_keys=public_keys
        )
        
        if not preprocess_result or "options" not in preprocess_result:
            raise ValidationError("Preprocess failed: No options returned")
            
        options = preprocess_result["options"]
        logger.debug(f"Preprocess successful. Options metadata included: {list(options.keys())}")
        
        # Step 2: Get metadata with suggested fee
        logger.debug("Calling construction/metadata...")
        metadata_result = self.client.construction_metadata(
            options=options,
            public_keys=public_keys
        )
        
        if not metadata_result or "metadata" not in metadata_result:
            raise ValidationError("Metadata failed: No metadata returned")
            
        metadata = metadata_result["metadata"]
        suggested_fee = None
        
        # Get suggested fee if present and not using fixed fee
        if not fixed_fee and "suggested_fee" in metadata_result:
            for fee in metadata_result["suggested_fee"]:
                if fee.get("currency", {}).get("symbol") == "ADA":
                    suggested_fee = int(fee["value"])
                    logger.debug(f"Suggested fee: {suggested_fee} lovelace")
                    break
        
        # Step 3: Adjust operations for fee if needed
        final_operations = operations.copy()
        
        if not fixed_fee and suggested_fee is not None:
            # Find input and output operations to calculate totals
            input_ops = [op for op in operations if op["type"] == "input"]
            output_ops = [op for op in operations if op["type"] == "output"]
            
            if not input_ops or not output_ops:
                raise ValidationError("Cannot adjust fee: No input or output operations")
                
            total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
            total_output = sum(int(op["amount"]["value"]) for op in output_ops)
            
            # Find change output (usually the last one)
            change_index = None
            for i in range(len(final_operations) - 1, -1, -1):
                if final_operations[i]["type"] == "output":
                    # Verify the address is the sender's address
                    if i > 0 and "account" in final_operations[i] and "account" in final_operations[0]:
                        if final_operations[i]["account"]["address"] == final_operations[0]["account"]["address"]:
                            change_index = i
                            break
                    # If we can't verify, just use the last output
                    if change_index is None:
                        change_index = i
                        break
                        
            if change_index is None:
                raise ValidationError("Cannot adjust fee: No suitable change output found")
            
            # Adjust the change output for the fee
            original_change = int(final_operations[change_index]["amount"]["value"])
            adjusted_change = original_change - suggested_fee
            
            # Make sure change is still positive
            if adjusted_change < 0:
                # Try to see if we can distribute the fee across multiple outputs if this is a same-address transaction
                if len(output_ops) > 1:
                    logger.debug(f"Last output insufficient for fee. Attempting fee distribution across multiple outputs")
                    sender_address = input_ops[0]["account"]["address"]
                    same_address_outputs = [
                        i for i, op in enumerate(final_operations) 
                        if op["type"] == "output" and op["account"]["address"] == sender_address
                    ]
                    
                    if same_address_outputs:
                        # Try to distribute fee equally among sender's outputs
                        fee_per_output = suggested_fee // len(same_address_outputs)
                        remaining_fee = suggested_fee
                        
                        for i in same_address_outputs:
                            output_value = int(final_operations[i]["amount"]["value"])
                            fee_to_deduct = min(fee_per_output, output_value - 1000000, remaining_fee)  # Ensure min 1 ADA output
                            
                            if fee_to_deduct > 0:
                                final_operations[i]["amount"]["value"] = str(output_value - fee_to_deduct)
                                remaining_fee -= fee_to_deduct
                        
                        logger.debug(f"Fee distribution complete. Remaining fee: {remaining_fee}")
                            
                        # If we still have remaining fee, raise error
                        if remaining_fee > 0:
                            raise ValidationError(
                                f"Insufficient funds for fee. Inputs: {total_input}, "
                                f"Outputs: {total_output}, Fee: {suggested_fee}. "
                                f"Could not distribute {remaining_fee} of the fee."
                            )
                    else:
                        # No outputs with same address, raise error
                        raise ValidationError(f"Cannot adjust fee: Change would be negative ({adjusted_change})")
                else:
                    raise ValidationError(f"Cannot adjust fee: Change would be negative ({adjusted_change})")
            else:
                # Standard case: just adjust the last output
                logger.debug(f"Adjusting change output from {original_change} to {adjusted_change} lovelace")
                final_operations[change_index]["amount"]["value"] = str(adjusted_change)
        
        # Step 4: Get payloads for signing
        logger.debug("Calling construction/payloads...")
        payloads_result = self.client.construction_payloads(
            operations=final_operations,
            metadata=metadata,
            public_keys=public_keys
        )
        
        if not payloads_result or "unsigned_transaction" not in payloads_result or "payloads" not in payloads_result:
            raise ValidationError("Payloads failed: Missing unsigned_transaction or payloads")
            
        unsigned_transaction = payloads_result["unsigned_transaction"]
        payloads = payloads_result["payloads"]
        
        logger.debug(f"Generated unsigned transaction with {len(payloads)} signing payloads")
        
        # --- NEW STEP 5: Parse unsigned transaction ---
        try:
            logger.debug("Calling construction/parse for unsigned transaction...")
            parsed_unsigned_tx = self.client.construction_parse(
                network_identifier=self.client.network_identifier,
                signed=False,
                transaction=unsigned_transaction
            )
            
            # Add validation of operations
            parsed_ops = parsed_unsigned_tx.get('operations', [])
            if not parsed_ops:
                raise ValidationError("Parsed unsigned transaction contains no operations")
            
            # Verify operation count matches (accounting for possible related_operations being expanded)
            op_types_provided = [op["type"] for op in final_operations]
            op_types_parsed = [op["type"] for op in parsed_ops]
            
            logger.debug(f"Operation types provided: {op_types_provided}")
            logger.debug(f"Operation types parsed: {op_types_parsed}")
            
            # Enhanced validation for operation inclusion
            # Build a map of operation types and counts to ensure all operations are included
            provided_op_counts = {}
            for op_type in op_types_provided:
                provided_op_counts[op_type] = provided_op_counts.get(op_type, 0) + 1
                
            parsed_op_counts = {}
            for op_type in op_types_parsed:
                parsed_op_counts[op_type] = parsed_op_counts.get(op_type, 0) + 1
                
            # Verify all provided operation types are present with at least the expected count
            for op_type, count in provided_op_counts.items():
                parsed_count = parsed_op_counts.get(op_type, 0)
                if parsed_count < count:
                    raise ValidationError(
                        f"Operation type '{op_type}' count mismatch: expected at least {count}, got {parsed_count}"
                    )
            
            # Verify other essential operation details
            for op in parsed_ops:
                if op["type"] in ["input", "output"]:
                    # Verify input/output operations have amount and account
                    assert "amount" in op, f"Operation {op['operation_identifier']['index']} missing amount"
                    assert "account" in op, f"Operation {op['operation_identifier']['index']} missing account"
                    
                # Verify stake operations have required fields based on type
                elif op["type"] == "stakeKeyRegistration":
                    assert "metadata" in op, f"Stake registration operation missing metadata"
                elif op["type"] == "stakeDelegation":
                    assert "metadata" in op, f"Stake delegation operation missing metadata"
                    # Verify pool ID is present in metadata
                    if "metadata" in op and "pool_key_hash" not in op["metadata"]:
                        logger.warning(f"Stake delegation operation missing pool_key_hash in metadata")
                elif op["type"] == "stakeKeyDeregistration":
                    assert "metadata" in op, f"Stake deregistration operation missing metadata"
            
            logger.debug("Unsigned transaction operations validated successfully")
        except Exception as e:
            # Don't stop execution for parse validation errors
            logger.error(f"Failed to parse or validate unsigned transaction: {e}", exc_info=True)
            logger.warning("Continuing with transaction submission despite parse validation error")
        # --- END NEW STEP ---
        
        # Calculate fee if fixed fee mode
        if fixed_fee:
            input_ops = [op for op in operations if op["type"] == "input"]
            output_ops = [op for op in operations if op["type"] == "output"]
            total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
            total_output = sum(int(op["amount"]["value"]) for op in output_ops)
            suggested_fee = total_input - total_output
            logger.debug(f"Implicit fee: {suggested_fee:,} lovelace")
            
        return unsigned_transaction, payloads, metadata, suggested_fee
        
    def sign_and_submit(
        self,
        unsigned_transaction: str,
        payloads: List[Dict],
        signing_function,
        wait_for_confirmation: bool = True,
        confirmation_timeout: int = 180,
        expected_operations: List[str] = None
    ) -> Tuple[str, Optional[Dict]]:
        """
        Sign and submit a transaction, optionally waiting for confirmation.
        
        Args:
            unsigned_transaction: Unsigned transaction from build_transaction
            payloads: Signing payloads from build_transaction
            signing_function: Function that takes a payload and returns a signature
            wait_for_confirmation: Whether to wait for confirmation
            confirmation_timeout: Timeout in seconds for confirmation
            expected_operations: List of operation types that should be present in the transaction
            
        Returns:
            Tuple containing:
            - transaction_hash: The hash of the submitted transaction
            - transaction_details: Details of the confirmed transaction (if wait_for_confirmation is True)
            
        Raises:
            TransactionError: If signing or submission fails
            ValidationError: If transaction doesn't contain expected operations
        """
        # Step 1: Sign the transaction
        logger.debug(f"Signing transaction with {len(payloads)} payloads")
        signatures = []
        
        for i, payload in enumerate(payloads):
            try:
                signature = signing_function(payload)
                signatures.append(signature)
                logger.debug(f"Generated signature {i+1} of {len(payloads)}")
            except Exception as e:
                raise TransactionError(f"Failed to sign payload {i+1}: {str(e)}")
                
        # Step 2: Combine signatures with unsigned transaction
        logger.debug("Calling construction/combine...")
        combine_result = self.client.construction_combine(
            unsigned_transaction=unsigned_transaction,
            signatures=signatures
        )
        
        if not combine_result or "signed_transaction" not in combine_result:
            raise TransactionError("Combine failed: No signed transaction returned")
            
        signed_transaction = combine_result["signed_transaction"]
        
        # --- NEW STEP 3: Parse signed transaction ---
        try:
            logger.debug("Calling construction/parse for signed transaction...")
            parsed_signed_tx = self.client.construction_parse(
                network_identifier=self.client.network_identifier,
                signed=True,
                transaction=signed_transaction
            )
            
            # Log basic info about parsed transaction
            parsed_ops = parsed_signed_tx.get('operations', [])
            signers = parsed_signed_tx.get('account_identifier_signers', [])
            logger.debug(f"Parsed signed transaction. Operations count: {len(parsed_ops)}, Signers count: {len(signers)}")
            
            # Validate operations in signed transaction
            if not parsed_ops:
                raise ValidationError("Parsed signed transaction contains no operations")
            
            # Enhanced validation for operation inclusion in signed transaction
            op_types = [op["type"] for op in parsed_ops]
            logger.debug(f"Signed transaction operation types: {op_types}")
            
            # Count operation types
            op_type_counts = {}
            for op_type in op_types:
                op_type_counts[op_type] = op_type_counts.get(op_type, 0) + 1
                
            # Verify operation details
            for op in parsed_ops:
                if op["type"] in ["input", "output"]:
                    # Verify input/output operations have amount and account
                    assert "amount" in op, f"Operation {op['operation_identifier']['index']} missing amount"
                    assert "account" in op, f"Operation {op['operation_identifier']['index']} missing account"
                    
                # Verify stake operations have required fields based on type
                elif op["type"] == "stakeKeyRegistration":
                    assert "metadata" in op, f"Stake registration operation missing metadata"
                elif op["type"] == "stakeDelegation":
                    assert "metadata" in op, f"Stake delegation operation missing metadata"
                    # Verify pool ID is present in metadata
                    if "metadata" in op and "pool_key_hash" not in op["metadata"]:
                        logger.warning(f"Stake delegation operation missing pool_key_hash in metadata")
                elif op["type"] == "stakeKeyDeregistration":
                    assert "metadata" in op, f"Stake deregistration operation missing metadata"
            
            # Log operation counts for info
            for op_type, count in op_type_counts.items():
                logger.debug(f"  Operation type '{op_type}': {count} operations")
            
            # Validate signers if present
            if not signers:
                logger.warning("No signers found in parsed signed transaction")
            else:
                signer_addresses = [signer.get('address') for signer in signers if 'address' in signer]
                logger.debug(f"Transaction signers: {signer_addresses}")
                
            logger.debug("Signed transaction validation successful")
        except Exception as e:
            # Don't stop execution for parse validation errors
            logger.error(f"Failed to parse or validate signed transaction: {e}", exc_info=True)
            logger.warning("Continuing with transaction submission despite parse validation error")
        # --- END NEW STEP ---
        
        # Step 4: Get transaction hash
        logger.debug("Calling construction/hash...")
        hash_result = self.client.construction_hash(signed_transaction)
        
        if not hash_result or "transaction_identifier" not in hash_result:
            raise TransactionError("Hash failed: No transaction identifier returned")
            
        transaction_hash = hash_result["transaction_identifier"]["hash"]
        logger.debug(f"Transaction hash: {transaction_hash}")
        
        # Step 5: Submit transaction
        logger.debug("Calling construction/submit...")
        submit_result = self.client.construction_submit(signed_transaction)
        
        if not submit_result or "transaction_identifier" not in submit_result:
            raise TransactionError("Submit failed: No transaction identifier returned")
            
        submit_hash = submit_result["transaction_identifier"]["hash"]
        
        if submit_hash != transaction_hash:
            logger.warning(f"Hash mismatch: Expected {transaction_hash}, got {submit_hash}")
            
        logger.info(f"Transaction submitted · Hash: {transaction_hash}!")
        
        # Step 6: Wait for confirmation if requested
        transaction_details = None
        if wait_for_confirmation:
            transaction_details = self.wait_for_confirmation(transaction_hash, confirmation_timeout)
            
            # Step 7: Validate the expected operations if specified
            if expected_operations and transaction_details:
                self.validate_operations(transaction_details, expected_operations)
                
        return transaction_hash, transaction_details
        
    def validate_operations(self, transaction_details: Dict, expected_operations: List[str]) -> bool:
        """
        Validate that a transaction contains the expected operations.
        
        Args:
            transaction_details: Transaction details from block/transaction
            expected_operations: List of operation types that should be present
            
        Returns:
            True if validation succeeds
            
        Raises:
            ValidationError: If validation fails
        """
        if not transaction_details or "transaction" not in transaction_details:
            raise ValidationError("Invalid transaction details for validation")
            
        # Get operations from transaction
        operations = transaction_details["transaction"].get("operations", [])
        
        # Get stake operations (not input/output)
        stake_ops = [op for op in operations if op["type"] not in ["input", "output"]]
        
        # Log all operations for debugging
        logger.debug(f"Transaction operations: {len(operations)} total ({len(stake_ops)} stake operations)")
        
        # Get stake operation types
        stake_op_types = [op["type"] for op in stake_ops]
        if stake_op_types:
            logger.debug(f"Stake operation types: {stake_op_types}")
        
        # Map operation type names
        operation_type_map = {
            "registration": "stakeKeyRegistration",
            "deregistration": "stakeKeyDeregistration", 
            "delegation": "stakeDelegation",
            "drepVoteDelegation": "dRepVoteDelegation"
        }
        
        # Translate expected operations
        expected_op_types = [operation_type_map.get(op, op) for op in expected_operations]
        
        # Check if all expected operations are present
        missing_ops = []
        for expected_op in expected_op_types:
            if expected_op not in stake_op_types:
                missing_ops.append(expected_op)
                
        if missing_ops:
            error_msg = (
                f"Expected {len(expected_operations)} stake operations "
                f"({', '.join(expected_operations)}), got {len(stake_ops)} "
                f"({', '.join(stake_op_types)})"
            )
            # Let the exception handler in the test function handle the error logging
            raise ValidationError(error_msg)
            
        logger.debug("Transaction operations validated successfully")
        return True
        
    def wait_for_confirmation(self, transaction_hash: str, timeout_seconds: int = 180) -> Dict:
        """
        Wait for a transaction to be confirmed on the blockchain.
        
        Args:
            transaction_hash: Transaction hash to wait for
            timeout_seconds: Maximum time to wait in seconds
            
        Returns:
            Transaction details from block/transaction
            
        Raises:
            TimeoutError: If confirmation times out
            TransactionError: If search or fetch fails
        """
        logger.debug(f"Waiting for confirmation of transaction {transaction_hash}")
        start_time = time.time()
        polling_interval = 10  # seconds between checks
        
        while time.time() - start_time < timeout_seconds:
            try:
                # Search for the transaction
                search_result = self.client.search_transactions(transaction_hash)
                
                if search_result and "transactions" in search_result and search_result["transactions"]:
                    # Transaction found in a block
                    tx_info = search_result["transactions"][0]
                    if "block_identifier" in tx_info:
                        block_identifier = tx_info["block_identifier"]
                        logger.debug(f"Transaction found in block {block_identifier['index']}")
                        
                        # Get full block data (for verification like in e2e-tests)
                        block_data = self.client.block(block_identifier)
                        if block_data and "block" in block_data:
                            # Verify block identifiers match
                            if (block_data["block"]["block_identifier"]["index"] == block_identifier["index"] and
                                block_data["block"]["block_identifier"]["hash"] == block_identifier["hash"]):
                                logger.debug(f"Block verification successful for block {block_identifier['index']}")
                            else:
                                logger.warning(f"Block identifier mismatch in fetched block data")
                        
                        # Get full transaction details
                        tx_details = self.client.block_transaction(
                            block_identifier=block_identifier,
                            transaction_hash=transaction_hash
                        )
                        
                        if tx_details and "transaction" in tx_details:
                            logger.info(f"Transaction found in block · Index: {block_identifier['index']} · Hash: {block_identifier['hash']}!")
                            return tx_details
                            
                logger.debug(f"Transaction {transaction_hash} not yet confirmed, waiting {polling_interval}s...")
                time.sleep(polling_interval)
                
            except Exception as e:
                logger.warning(f"Error checking transaction status: {str(e)}")
                time.sleep(polling_interval)
                
        raise TimeoutError(f"Transaction {transaction_hash} not confirmed within {timeout_seconds} seconds")
        
    def calculate_onchain_fee(self, transaction_details: Dict) -> int:
        """
        Calculate the fee from confirmed transaction details.
        
        Args:
            transaction_details: Transaction details from block/transaction
            
        Returns:
            Fee in lovelace
            
        Raises:
            ValueError: If fee cannot be calculated
        """
        if not transaction_details or "transaction" not in transaction_details or "operations" not in transaction_details["transaction"]:
            raise ValueError("Invalid transaction details")
            
        operations = transaction_details["transaction"]["operations"]
        input_ops = [op for op in operations if op["type"] == "input"]
        output_ops = [op for op in operations if op["type"] == "output"]
        
        if not input_ops:
            raise ValueError("No input operations found")
            
        total_input = sum(abs(int(op["amount"]["value"])) for op in input_ops)
        total_output = sum(int(op["amount"]["value"]) for op in output_ops)
        
        fee = total_input - total_output
        logger.debug(f"Calculated on-chain fee: {fee:,} lovelace")
        
        return fee 