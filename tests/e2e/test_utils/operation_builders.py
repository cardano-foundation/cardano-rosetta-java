import logging
from typing import Dict, List, Optional

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
                    "curve_type": "edwards25519",
                }
            },
        }
        
    @staticmethod
    def build_stake_key_deregistration_operation(
        index: int, stake_address: str, stake_key_hex: str
    ) -> Dict:
        """
        Build a stake key deregistration operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex

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
                    "curve_type": "edwards25519",
                }
            },
        }
        
    @staticmethod
    def build_stake_delegation_operation(
        index: int, stake_address: str, stake_key_hex: str, pool_id: str
    ) -> Dict:
        """
        Build a stake delegation operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            pool_id: Pool ID to delegate to

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
            },
        }
        
    @staticmethod
    def build_drep_vote_delegation_operation(
        index: int,
        stake_address: str,
        stake_key_hex: str,
        drep_id: Optional[str] = None,
        drep_type: str = "key_hash",
    ) -> Dict:
        """
        Build a DRep vote delegation operation.
        
        Args:
            index: Operation index
            stake_address: Stake address
            stake_key_hex: Stake verification key as hex
            drep_id: DRep ID (not needed for abstain/no_confidence)
            drep_type: Type of DRep (key_hash, script_hash, abstain, no_confidence)

        Returns:
            DRep vote delegation operation dictionary
        """
        metadata = {
            "staking_credential": {
                "hex_bytes": stake_key_hex,
                "curve_type": "edwards25519"
            },
            "drep": {"type": drep_type},
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
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )

        # Build output operation (stake registration has 2 ADA deposit)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input - 2_000_000,  # Adjust for 2 ADA deposit
            )
        ]

        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_stake_key_registration_operation(
                index=len(input_ops) + len(output_ops),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
            )
        ]

        # Return operations in correct order: inputs, outputs, certificate operations
        return input_ops + output_ops + cert_ops

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
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )

        # Build output operation (no deposit required for delegation)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops), address=payment_address, amount=total_input
            )
        ]

        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_stake_delegation_operation(
                index=len(input_ops) + len(output_ops),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                pool_id=pool_id
            )
        ]

        # Return operations in correct order: inputs, outputs, certificate operations
        return input_ops + output_ops + cert_ops

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
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )

        # Build output operation (stake registration has 2 ADA deposit)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input - 2_000_000,  # Adjust for 2 ADA deposit
            )
        ]

        # Build certificate-requiring operations
        cert_ops = [
            OperationBuilder.build_stake_key_registration_operation(
                index=len(input_ops) + len(output_ops),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
            ),
            OperationBuilder.build_stake_delegation_operation(
                index=len(input_ops) + len(output_ops) + 1,
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                pool_id=pool_id,
            ),
        ]

        # Return operations in correct order: inputs, outputs, certificate operations
        return input_ops + output_ops + cert_ops

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
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"]
                )
            )

        # Build output operation (no deposit required for vote delegation)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input,  # No deposit required for vote delegation
            )
        ]

        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_drep_vote_delegation_operation(
                index=len(input_ops) + len(output_ops),
                stake_address=stake_address,
                stake_key_hex=stake_key_hex,
                drep_id=drep_id,
                drep_type=drep_type,
            )
        ]

        # Return operations in correct order: inputs, outputs, certificate operations
        return input_ops + output_ops + cert_ops

    @staticmethod
    def build_pool_registration_operation(
        index: int, pool_address: str, pool_registration_params: Dict
    ) -> Dict:
        """
        Build a pool registration operation.

        Args:
            index: Operation index
            pool_address: Pool cold key address (hex)
            pool_registration_params: Pool registration parameters

        Returns:
            Pool registration operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "poolRegistration",
            "status": "",
            "account": {"address": pool_address},
            "metadata": {"poolRegistrationParams": pool_registration_params},
        }

    @staticmethod
    def build_pool_registration_with_cert_operation(
        index: int, pool_address: str, pool_registration_cert: str
    ) -> Dict:
        """
        Build a pool registration with certificate operation.

        Args:
            index: Operation index
            pool_address: Pool cold key address (hex)
            pool_registration_cert: Pool registration certificate as hex string

        Returns:
            Pool registration with cert operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "poolRegistrationWithCert",
            "status": "",
            "account": {"address": pool_address},
            "metadata": {"poolRegistrationCert": pool_registration_cert},
        }

    @staticmethod
    def build_pool_governance_vote_operation(
        index: int,
        pool_address: str,
        pool_credential: Dict,
        governance_action_hash: str,
        vote: str,
        vote_rationale: Optional[Dict] = None,
    ) -> Dict:
        """
        Build a pool governance vote operation.

        Args:
            index: Operation index
            pool_address: Pool cold key address (hex)
            pool_credential: Pool credential with hex_bytes and curve_type
            governance_action_hash: Governance action hash string
            vote: Vote choice ("yes", "no", or "abstain")
            vote_rationale: Optional vote rationale with data_hash and url

        Returns:
            Pool governance vote operation dictionary
        """
        metadata = {
            "poolGovernanceVoteParams": {
                "pool_credential": pool_credential,
                "governance_action_hash": governance_action_hash,
                "vote": vote,
            }
        }

        if vote_rationale:
            metadata["vote_rationale"] = vote_rationale

        return {
            "operation_identifier": {"index": index},
            "type": "poolGovernanceVote",
            "status": "",
            "account": {"address": pool_address},
            "metadata": metadata,
        }

    @staticmethod
    def build_pool_retirement_operation(
        index: int, pool_address: str, epoch: int
    ) -> Dict:
        """
        Build a pool retirement operation.

        Args:
            index: Operation index
            pool_address: Pool cold key address (hex)
            epoch: Epoch in which the pool should be retired

        Returns:
            Pool retirement operation dictionary
        """
        return {
            "operation_identifier": {"index": index},
            "type": "poolRetirement",
            "status": "",
            "account": {"address": pool_address},
            "metadata": {"epoch": epoch},
        }

    @staticmethod
    def build_pool_registration_operations(
        payment_address: str,
        pool_address: str,
        pool_registration_params: Dict,
        input_utxos: List[Dict],
        min_output_amount: int = 1_000_000,
    ) -> List[Dict]:
        """
        Build operations for a pool registration transaction.

        Args:
            payment_address: Payment address for transaction funds
            pool_address: Pool cold key address (hex)
            pool_registration_params: Pool registration parameters
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output

        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)

        # Build input operations
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )
        # Build output operation (pool registration has 500 ADA deposit)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input - 500_000_000,  # Adjust for 500 ADA deposit
            )
        ]
        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_pool_registration_operation(
                index=len(input_ops) + len(output_ops),
                pool_address=pool_address,
                pool_registration_params=pool_registration_params,
            )
        ]
        # Withdrawals would go here if needed in the future
        return input_ops + output_ops + cert_ops

    @staticmethod
    def build_pool_governance_vote_operations(
        payment_address: str,
        pool_address: str,
        pool_credential: Dict,
        governance_action_hash: str,
        vote: str,
        input_utxos: List[Dict],
        vote_rationale: Optional[Dict] = None,
        min_output_amount: int = 1_000_000,
    ) -> List[Dict]:
        """
        Build operations for a pool governance vote transaction.

        Args:
            payment_address: Payment address for transaction funds
            pool_address: Pool cold key address (hex)
            pool_credential: Pool credential with hex_bytes and curve_type
            governance_action_hash: Governance action hash string
            vote: Vote choice ("yes", "no", or "abstain")
            input_utxos: List of UTXOs to use as inputs
            vote_rationale: Optional vote rationale with data_hash and url
            min_output_amount: Minimum amount for change output

        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)

        # Build input operations
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )
        # Build output operation (no deposit required for governance vote)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops), address=payment_address, amount=total_input
            )
        ]
        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_pool_governance_vote_operation(
                index=len(input_ops) + len(output_ops),
                pool_address=pool_address,
                pool_credential=pool_credential,
                governance_action_hash=governance_action_hash,
                vote=vote,
                vote_rationale=vote_rationale,
            )
        ]
        # Withdrawals would go here if needed in the future
        return input_ops + output_ops + cert_ops

    @staticmethod
    def build_pool_retirement_operations(
        payment_address: str,
        pool_address: str,
        epoch: int,
        input_utxos: List[Dict],
        min_output_amount: int = 1_000_000,
    ) -> List[Dict]:
        """
        Build operations for a pool retirement transaction.

        Args:
            payment_address: Payment address for transaction funds
            pool_address: Pool cold key address (hex)
            epoch: Epoch in which the pool should be retired
            input_utxos: List of UTXOs to use as inputs
            min_output_amount: Minimum amount for change output

        Returns:
            List of operations for the transaction
        """
        # Calculate total input
        total_input = sum(int(utxo["amount"]["value"]) for utxo in input_utxos)

        # Build input operations
        input_ops = []
        for i, utxo in enumerate(input_utxos):
            input_ops.append(
                OperationBuilder.build_input_operation(
                    index=i,
                    address=payment_address,
                    amount=int(utxo["amount"]["value"]),
                    utxo_id=utxo["coin_identifier"]["identifier"],
                )
            )

        # Build output operation (no refund for pool retirement)
        output_ops = [
            OperationBuilder.build_output_operation(
                index=len(input_ops),
                address=payment_address,
                amount=total_input,  # No refund for pool retirement
            )
        ]

        # Build certificate-requiring operation
        cert_ops = [
            OperationBuilder.build_pool_retirement_operation(
                index=len(input_ops) + len(output_ops),
                pool_address=pool_address,
                epoch=epoch,
            )
        ]

        # Return operations in correct order: inputs, outputs, certificate operations
        return input_ops + output_ops + cert_ops
