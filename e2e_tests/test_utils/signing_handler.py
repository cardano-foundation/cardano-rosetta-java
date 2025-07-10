import logging
from typing import Dict, List, Optional, Any, Callable, Tuple, Union
import enum

from e2e_tests.wallet_utils.pycardano_wallet import PyCardanoWallet

logger = logging.getLogger(__name__)

class KeyType(enum.Enum):
    """Enum for key types used in Cardano transactions."""
    PAYMENT = "payment"
    STAKE = "stake"
    POOL_COLD = "pool_cold"

class SigningHandler:
    """
    Handles the signing of Rosetta transaction payloads.
    Provides different signing strategies for payment and stake keys.
    """
    
    def __init__(self, wallet: PyCardanoWallet):
        """
        Initialize the signing handler with a wallet.
        
        Args:
            wallet: PyCardano wallet instance for signing
        """
        self.wallet = wallet
        
    def sign_with_payment_key(self, payload: Dict) -> Dict:
        """
        Sign a payload with the payment key.
        
        Args:
            payload: Signing payload from Rosetta API
            
        Returns:
            Signature dictionary for the Rosetta API
            
        Raises:
            ValueError: If signing fails
        """
        logger.debug("Signing with payment key")
        try:
            return self.wallet.sign_transaction_with_payment_key(payload)
        except Exception as e:
            logger.error(f"Failed to sign with payment key: {str(e)}")
            raise ValueError(f"Payment key signing failed: {str(e)}")
            
    def sign_with_stake_key(self, payload: Dict) -> Dict:
        """
        Sign a payload with the stake key.
        
        Args:
            payload: Signing payload from Rosetta API
            
        Returns:
            Signature dictionary for the Rosetta API
            
        Raises:
            ValueError: If signing fails
        """
        logger.debug("Signing with stake key")
        try:
            return self.wallet.sign_with_stake_key(payload)
        except Exception as e:
            logger.error(f"Failed to sign with stake key: {str(e)}")
            raise ValueError(f"Stake key signing failed: {str(e)}")
            
    def sign_with_pool_cold_key(self, payload: Dict) -> Dict:
        """
        Sign a payload with the pool cold key.
        
        Args:
            payload: Signing payload from Rosetta API
            
        Returns:
            Signature dictionary for the Rosetta API
            
        Raises:
            ValueError: If signing fails
        """
        logger.debug("Signing with pool cold key")
        try:
            return self.wallet.sign_with_pool_cold_key(payload)
        except Exception as e:
            logger.error(f"Failed to sign with pool cold key: {str(e)}")
            raise ValueError(f"Pool cold key signing failed: {str(e)}")
            
    def get_signing_function(self, key_type: KeyType = KeyType.PAYMENT) -> Callable:
        """
        Get a function that signs payloads with the specified key type.
        
        Args:
            key_type: Type of key to use for signing
            
        Returns:
            Function that takes a payload and returns a signature
        """
        if key_type == KeyType.PAYMENT:
            return self.sign_with_payment_key
        elif key_type == KeyType.STAKE:
            return self.sign_with_stake_key
        elif key_type == KeyType.POOL_COLD:
            return self.sign_with_pool_cold_key
        else:
            raise ValueError(f"Unsupported key type: {key_type}")
            
    def multi_key_signing_function(self, payload_to_key_map: Dict[int, KeyType]) -> Callable:
        """
        Get a function that signs payloads with different keys based on their index.
        
        Args:
            payload_to_key_map: Mapping from payload index to key type
            
        Returns:
            Function that takes a payload and its index and returns a signature
            
        Example:
            >>> signer = SigningHandler(wallet)
            >>> # Map payload 0 to payment key, payload 1 to stake key
            >>> signing_fn = signer.multi_key_signing_function({0: KeyType.PAYMENT, 1: KeyType.STAKE})
            >>> # Use in transaction orchestrator
            >>> signatures = [signing_fn(payload, i) for i, payload in enumerate(payloads)]
        """
        def sign_payload(payload: Dict, index: int) -> Dict:
            key_type = payload_to_key_map.get(index, KeyType.PAYMENT)
            if key_type == KeyType.PAYMENT:
                return self.sign_with_payment_key(payload)
            elif key_type == KeyType.STAKE:
                return self.sign_with_stake_key(payload)
            elif key_type == KeyType.POOL_COLD:
                return self.sign_with_pool_cold_key(payload)
            else:
                raise ValueError(f"Unsupported key type for payload {index}: {key_type}")
                
        return sign_payload 

    def create_combined_signing_function(self, payloads: List[Dict]) -> Callable[[Dict], Dict]:
        """
        Creates a signing function that handles common payment/stake key signing.
        Assumes payload[0] uses the payment key, and payload[1] (if present)
        uses the stake key.

        Args:
            payloads: The list of signing payloads from /construction/payloads.

        Returns:
            A function that takes a payload and returns the corresponding signature.
        """
        if not payloads:
            raise ValueError("Payloads list cannot be empty")

        # Pre-sign the payloads to build a map
        sig_map = {}
        try:
            # Payment key signs the first payload
            payment_sig = self.sign_with_payment_key(payloads[0])
            # Using payload content as key might be unstable if dict order changes
            # Use the object itself (or its ID) if possible, or rely on exact match.
            # For simplicity here, we'll assume the exact payload object is passed back.
            sig_map[id(payloads[0])] = payment_sig 

            # Stake key signs the second payload, if present
            if len(payloads) > 1:
                stake_sig = self.sign_with_stake_key(payloads[1])
                sig_map[id(payloads[1])] = stake_sig
        except Exception as e:
            logger.error(f"Failed during pre-signing for combined function: {e}")
            raise
            
        # The returned function looks up the pre-computed signature
        def signing_function(payload_to_sign: Dict) -> Dict:
            payload_id = id(payload_to_sign)
            signature = sig_map.get(payload_id)
            if signature:
                return signature
            else:
                # Fallback or error if unexpected payload is passed
                logger.error(f"Received unexpected payload to sign: {payload_to_sign}")
                raise ValueError("Payload not found in pre-signed map")

        return signing_function 