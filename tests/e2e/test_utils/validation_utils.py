import logging
from typing import Literal, Dict, Optional, List

def verify_address_derivation(
    rosetta_client, 
    test_wallet, 
    logger: logging.Logger, 
    address_type: Literal["Base", "Reward"]
):
    """
    Verifies address derivation using the /construction/derive endpoint.

    Args:
        rosetta_client: The Rosetta API client instance.
        test_wallet: The test wallet instance.
        logger: The logger instance.
        address_type: The type of address to derive ("Base" or "Reward").
    """
    logger.debug(f"Verifying /construction/derive for {address_type} address...")
    
    key_obj = None
    metadata = {"address_type": address_type}
    expected_address = None

    try:
        if address_type == "Base":
            payment_key = test_wallet.get_payment_verification_key_hex()
            stake_key = test_wallet.get_stake_verification_key_hex()
            key_obj = {
                "hex_bytes": payment_key, 
                "curve_type": "edwards25519"
            }
            metadata["staking_credential"] = {
                "hex_bytes": stake_key,
                "curve_type": "edwards25519"
            }
            expected_address = test_wallet.get_address()
        elif address_type == "Reward":
            stake_key = test_wallet.get_stake_verification_key_hex()
            key_obj = {
                "hex_bytes": stake_key, 
                "curve_type": "edwards25519"
            }
            # Metadata only needs address_type for Reward address
            expected_address = test_wallet.get_stake_address()
        else:
            logger.error(f"Unsupported address_type for derivation check: {address_type}")
            return # Or raise an error

        if not key_obj or not expected_address:
             logger.error(f"Could not prepare data for {address_type} address derivation check.")
             return

        derive_response = rosetta_client.construction_derive(key_obj, metadata=metadata)
        derived_address = derive_response["account_identifier"]["address"]

        if derived_address != expected_address:
            logger.warning(
                f"Derived {address_type} address {derived_address} doesn't match expected wallet address {expected_address}"
            )
        else:
            logger.debug(f"{address_type} address derivation verified successfully")

    except Exception as e:
        logger.error(f"Error during {address_type} address derivation check: {e}")
        # Optionally re-raise or handle specific exceptions if needed 

def verify_final_balance(
    rosetta_client,
    logger: logging.Logger,
    address: str,
    initial_balance: int,
    actual_fee: int,
    deposit: int = 0,
    refund: int = 0
):
    """
    Verifies the final ADA balance after a transaction.

    Calculates the expected balance based on initial balance, fee, deposit, and refund,
    fetches the current balance via the Rosetta API, and asserts they match.

    Args:
        rosetta_client: The Rosetta API client instance.
        logger: The logger instance.
        address: The Cardano address (payment address) to check balance for.
        initial_balance: The ADA balance (in lovelace) before the transaction.
        actual_fee: The actual transaction fee paid (in lovelace).
        deposit: Any deposit made (e.g., stake registration, in lovelace). Defaults to 0.
        refund: Any refund received (e.g., stake deregistration, in lovelace). Defaults to 0.
    """
    logger.debug("Verifying final balance...")
    try:
        final_balance = rosetta_client.get_ada_balance(address)
        expected_balance = initial_balance - actual_fee - deposit + refund
        
        logger.debug(
            f"Initial: {initial_balance:,}, Fee: {actual_fee:,}, Deposit: {deposit:,}, Refund: {refund:,} -> Expected: {expected_balance:,}, Actual: {final_balance:,}"
        )

        assert final_balance == expected_balance, \
            f"Final balance mismatch: expected {expected_balance:,}, got {final_balance:,}"
            
        logger.debug("Final balance verified successfully.")
        
    except AssertionError as e:
        logger.error(f"‼ Balance assertion failed: {e}")
        raise # Re-raise assertion error to fail the test
    except Exception as e:
        logger.error(f"Error during final balance verification: {e}")
        # Re-raise other exceptions as well, as they indicate problems
        raise

def extract_operations_from_details(tx_details: Dict, logger: logging.Logger) -> Optional[List[Dict]]:
    """
    Extracts the list of operations from a transaction details dictionary,
    assuming the standard Rosetta /block/transaction structure.

    Args:
        tx_details: The transaction details dictionary.
        logger: The logger instance.

    Returns:
        The list of operations, or None if the structure is unexpected.
    """ 
    if not isinstance(tx_details, dict):
        logger.warning("tx_details is not a dictionary, cannot extract operations.")
        return None

    # Expect operations directly within tx_details['transaction']['operations']
    transaction_data = tx_details.get('transaction')
    if isinstance(transaction_data, dict):
        operations = transaction_data.get('operations')
        if isinstance(operations, list):
            logger.debug("Found operations in tx_details['transaction']['operations']")
            return operations
            
    logger.warning("Could not find 'operations' list in tx_details['transaction']['operations']. Structure might be unexpected.")
    return None 