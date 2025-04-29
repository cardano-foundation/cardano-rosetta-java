import logging
from typing import Dict, List, Optional, Any

logger = logging.getLogger(__name__)

class OperationBuilder:
    """
    Utility class for building Rosetta API operations.
    Provides methods for creating common operation types.
    """
    
    @staticmethod
    def build_input_operation(index: int, address: str, amount: int, utxo_id: str) -> Dict:
        """
        Build an input operation (spending a UTXO).
        
        Args:
            index: Operation index
            address: Input address
            amount: Amount in lovelace
            utxo_id: UTXO identifier
            
        Returns:
            Input operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "input",
            "status": "",
            "account": {"address": address},
            "amount": {"value": f"-{amount}", "currency": {"symbol": "ADA", "decimals": 6}},
            "coin_change": {
                "coin_identifier": {"identifier": utxo_id},
                "coin_action": "coin_spent"
            }
        }
        
    @staticmethod
    def build_output_operation(index: int, address: str, amount: int) -> Dict:
        """
        Build an output operation (creating a new UTXO).
        
        Args:
            index: Operation index
            address: Output address
            amount: Amount in lovelace
            
        Returns:
            Output operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "output",
            "status": "",
            "account": {"address": address},
            "amount": {"value": str(amount), "currency": {"symbol": "ADA", "decimals": 6}}
        }
        
    @staticmethod
    def build_stake_key_registration_operation(index: int, stake_address: str, stake_key_hex: str, cert_index: int = 0) -> Dict:
        """
        Build a stake key registration operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            cert_index: Certificate index (for multiple certs in one tx)
            
        Returns:
            Stake key registration operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "stakeKeyRegistration",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519"
                },
                "certificate_index": cert_index
            }
        }
        
    @staticmethod
    def build_stake_key_deregistration_operation(index: int, stake_address: str, stake_key_hex: str, cert_index: int = 0) -> Dict:
        """
        Build a stake key deregistration operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            cert_index: Certificate index (for multiple certs in one tx)
            
        Returns:
            Stake key deregistration operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "stakeKeyDeregistration",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519"
                },
                "certificate_index": cert_index
            }
        }
        
    @staticmethod
    def build_stake_delegation_operation(
        index: int, 
        stake_address: str, 
        stake_key_hex: str,
        pool_id: str,
        cert_index: int = 0
    ) -> Dict:
        """
        Build a stake delegation operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            pool_id: Pool ID to delegate to
            cert_index: Certificate index (for multiple certs in one tx)
            
        Returns:
            Stake delegation operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "stakeDelegation",
            "status": "",
            "account": {"address": stake_address},
            "metadata": {
                "staking_credential": {
                    "hex_bytes": stake_key_hex,
                    "curve_type": "edwards25519"
                },
                "pool_key_hash": pool_id,
                "certificate_index": cert_index
            }
        }
        
    @staticmethod
    def build_drep_vote_delegation_operation(
        index: int,
        stake_address: str,
        stake_key_hex: str,
        drep_id: Optional[str] = None,
        drep_type: str = "key_hash",
        cert_index: int = 0
    ) -> Dict:
        """
        Build a DRep vote delegation operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            drep_id: DRep ID (not needed for abstain/no_confidence)
            drep_type: Type of DRep (key_hash, script_hash, abstain, no_confidence)
            cert_index: Certificate index (for multiple certs in one tx)
            
        Returns:
            DRep vote delegation operation dictionary
        """
        metadata = {
            "staking_credential": {
                "hex_bytes": stake_key_hex,
                "curve_type": "edwards25519"
            },
            "certificate_index": cert_index,
            "drep": {
                "type": drep_type
            }
        }
        
        # Add drep_id for key_hash and script_hash types
        if drep_type in ["key_hash", "script_hash"] and drep_id:
            metadata["drep"]["id"] = drep_id
            
        return {
            "operation_identifier": {"index": index},
            "type": "dRepVoteDelegation",
            "status": "",
            "account": {"address": stake_address},
            "metadata": metadata
        }
        
    @staticmethod
    def build_basic_transfer_operations(
        sender_address: str,
        recipient_address: str,
        input_utxos: List[Dict],
        transfer_amount: int
    ) -> List[Dict]:
        """
        Build operations for a basic transfer transaction.
        
        Args:
            sender_address: Sender address
            recipient_address: Recipient address
            input_utxos: List of UTXOs to use as inputs
            transfer_amount: Amount to transfer in lovelace
            
        Returns:
            List of operations for the transaction
            
        Raises:
            ValueError: If inputs are insufficient
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)
        
        # Check if inputs are sufficient
        if total_input <= transfer_amount:
            raise ValueError(
                f"Insufficient inputs: {total_input} <= {transfer_amount}"
            )
            
        # Build input operations
        operations = []
        for i, utxo in enumerate(input_utxos):
            operations.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=sender_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
            
        # Build output operations
        # 1. Recipient output
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=recipient_address,
                amount=transfer_amount
            )
        )
        
        # 2. Change output
        change_amount = total_input - transfer_amount
        if change_amount > 0:
            operations.append(
                OperationBuilder.build_output_operation(
                    index=len(operations),
                    address=sender_address,
                    amount=change_amount
                )
            )
            
        return operations
        
    @staticmethod
    def build_stake_key_registration_operations(
        payment_address: str,
        stake_address: str,
        stake_key_hex: str,
        input_utxos: List[Dict],
        min_output_amount: int = 1_000_000
    ) -> List[Dict]:
        """
        Build operations for a stake key registration transaction.
        
        Args:
            payment_address: Payment address
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output
            
        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)
        
        # Build input operations
        operations = []
        for i, utxo in enumerate(input_utxos):
            operations.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
            
        # Add stake key registration operation
        operations.append(
            OperationBuilder.build_stake_key_registration_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex
            )
        )
        
        # Add change output
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input - 2_000_000  # Adjust for 2 ADA deposit
            )
        )
        
        return operations
        
    @staticmethod
    def build_stake_delegation_operations(
        payment_address: str,
        stake_address: str,
        stake_key_hex: str,
        pool_id: str,
        input_utxos: List[Dict],
        min_output_amount: int = 1_000_000
    ) -> List[Dict]:
        """
        Build operations for a stake delegation transaction.
        
        Args:
            payment_address: Payment address
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            pool_id: Pool ID to delegate to
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output
            
        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)
        
        # Build input operations
        operations = []
        for i, utxo in enumerate(input_utxos):
            operations.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
            
        # Add stake delegation operation
        operations.append(
            OperationBuilder.build_stake_delegation_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                pool_id=pool_id
            )
        )
        
        # Add change output
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input
            )
        )
        
        return operations
        
    @staticmethod
    def build_combined_registration_delegation_operations(
        payment_address: str,
        stake_address: str,
        stake_key_hex: str,
        pool_id: str,
        input_utxos: List[Dict],
        min_output_amount: int = 1_000_000
    ) -> List[Dict]:
        """
        Build operations for a combined stake key registration and delegation transaction.
        
        Args:
            payment_address: Payment address
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            pool_id: Pool ID to delegate to
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output
            
        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)
        
        # Build input operations
        operations = []
        for i, utxo in enumerate(input_utxos):
            operations.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
            
        # Add stake key registration operation
        operations.append(
            OperationBuilder.build_stake_key_registration_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex
            )
        )
        
        # Add stake delegation operation
        operations.append(
            OperationBuilder.build_stake_delegation_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                pool_id=pool_id
            )
        )
        
        # Add change output
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input - 2_000_000  # Adjust for 2 ADA deposit
            )
        )
        
        return operations
        
    @staticmethod
    def build_drep_vote_delegation_operations(
        payment_address: str,
        stake_address: str,
        stake_key_hex: str,
        drep_type: str = "key_hash",
        drep_id: Optional[str] = None,
        input_utxos: List[Dict] = None,
        min_output_amount: int = 1_000_000
    ) -> List[Dict]:
        """
        Build operations for a DRep vote delegation transaction.
        
        Args:
            payment_address: Payment address
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            drep_type: Type of DRep (key_hash, script_hash, abstain, no_confidence)
            drep_id: DRep ID (for key_hash/script_hash types)
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output
            
        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)
        
        # Build input operations
        operations = []
        for i, utxo in enumerate(input_utxos):
            operations.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )
            
        # Add DRep vote delegation operation
        operations.append(
            OperationBuilder.build_drep_vote_delegation_operation(
                index=len(operations),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                drep_id=drep_id,
                drep_type=drep_type
            )
        )
        
        # Add change output
        operations.append(
            OperationBuilder.build_output_operation(
                index=len(operations),
                address=payment_address,
                amount=total_input  # No deposit required for vote delegation
            )
        )
        
        return operations 