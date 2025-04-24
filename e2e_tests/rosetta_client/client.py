import logging
from typing import Dict, List, Optional, Any, Tuple
import json

from .exceptions import NetworkError, ValidationError
from .request_debugger import RequestDebugger

logger = logging.getLogger(__name__)

class RosettaClient:
    """
    Client for interacting with Cardano Rosetta API.
    This client directly maps to Rosetta API endpoints.
    """
    def __init__(self, endpoint: str, network: str = "testnet"):
        """
        Initialize the Rosetta client.
        
        Args:
            endpoint: The Rosetta API endpoint URL
            network: The network identifier (e.g., "testnet", "mainnet")
        """
        self.endpoint = endpoint.rstrip("/")
        self.network = network
        self.headers = {"Content-Type": "application/json"}
        self.request_debugger = RequestDebugger()
        
        # Standard network identifier object used in most requests
        self.network_identifier = {"blockchain": "cardano", "network": self.network}
        
        logger.debug(f"Initialized Rosetta client for {self.endpoint} ({self.network})")

    def _handle_request_error(self, err, context="API request"):
        """Handle request errors with appropriate exception types."""
        import requests
        
        if isinstance(err, requests.exceptions.HTTPError):
            status_code = err.response.status_code
            error_response = None
            try:
                error_response = err.response.json()
            except:
                pass

            logger.error(
                "HTTP Error %d: %s\nResponse: %s", 
                status_code, 
                str(err),
                json.dumps(error_response, indent=2) if error_response else "No response body"
            )
            
            if 400 <= status_code < 500:
                raise ValidationError(f"{context} validation error: {str(err)}")
            else:
                raise NetworkError(f"{context} server error: {str(err)}")
        else:
            logger.error("Request Error: %s", str(err))
            raise NetworkError(f"{context} network error: {str(err)}")

    # ==================
    # Network Endpoints
    # ==================
    
    def network_list(self) -> Dict:
        """
        Call the /network/list endpoint to get supported networks.
        
        Returns:
            Dictionary containing supported networks
        """
        url = f"{self.endpoint}/network/list"
        payload = {"metadata": {}}
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "network/list")
            
    def network_options(self) -> Dict:
        """
        Call the /network/options endpoint to get network options.
        
        Returns:
            Dictionary containing network options
        """
        url = f"{self.endpoint}/network/options"
        payload = {
            "network_identifier": self.network_identifier,
            "metadata": {}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "network/options")
            
    def network_status(self) -> Dict:
        """
        Call the /network/status endpoint to check network status.
        
        Returns:
            Dictionary containing network status information
        """
        url = f"{self.endpoint}/network/status"
        payload = {
            "network_identifier": self.network_identifier,
            "metadata": {}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "network/status")
            
    # ==================
    # Account Endpoints
    # ==================
    
    def account_balance(self, address: str) -> Dict:
        """
        Call the /account/balance endpoint to get account balance.
        
        Args:
            address: The address to get balance for
            
        Returns:
            Dictionary containing account balance information
        """
        url = f"{self.endpoint}/account/balance"
        payload = {
            "network_identifier": self.network_identifier,
            "account_identifier": {"address": address}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, f"account/balance for {address}")
            
    def account_coins(self, address: str) -> Dict:
        """
        Call the /account/coins endpoint to get UTXOs for an account.
        
        Args:
            address: The address to get UTXOs for
            
        Returns:
            Dictionary containing account UTXOs
        """
        url = f"{self.endpoint}/account/coins"
        payload = {
            "network_identifier": self.network_identifier,
            "account_identifier": {"address": address},
            "include_mempool": False
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, f"account/coins for {address}")
            
    # ==================
    # Block Endpoints
    # ==================
    
    def block(self, block_identifier: Optional[Dict] = None) -> Dict:
        """
        Call the /block endpoint to get block information.
        
        Args:
            block_identifier: Optional block identifier.
                If None, gets the latest block.
                
        Returns:
            Dictionary containing block information
        """
        url = f"{self.endpoint}/block"
        payload = {
            "network_identifier": self.network_identifier,
            "block_identifier": block_identifier or {}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            block_id = block_identifier or "latest"
            self._handle_request_error(e, f"block for {block_id}")
            
    def block_transaction(self, block_identifier: Dict, transaction_hash: str) -> Dict:
        """
        Call the /block/transaction endpoint to get transaction details.
        
        Args:
            block_identifier: Block identifier
            transaction_hash: Transaction hash
            
        Returns:
            Dictionary containing transaction details
        """
        url = f"{self.endpoint}/block/transaction"
        payload = {
            "network_identifier": self.network_identifier,
            "block_identifier": block_identifier,
            "transaction_identifier": {"hash": transaction_hash}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, f"block/transaction for {transaction_hash}")
            
    # ==================
    # Mempool Endpoints
    # ==================
    
    def mempool(self) -> Dict:
        """
        Call the /mempool endpoint to get mempool information.
        
        Returns:
            Dictionary containing mempool information
        """
        url = f"{self.endpoint}/mempool"
        payload = {"network_identifier": self.network_identifier}
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "mempool")
            
    def mempool_transaction(self, transaction_hash: str) -> Dict:
        """
        Call the /mempool/transaction endpoint to get mempool transaction details.
        
        Args:
            transaction_hash: Transaction hash
            
        Returns:
            Dictionary containing mempool transaction details
        """
        url = f"{self.endpoint}/mempool/transaction"
        payload = {
            "network_identifier": self.network_identifier,
            "transaction_identifier": {"hash": transaction_hash}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, f"mempool/transaction for {transaction_hash}")
            
    # ======================
    # Construction Endpoints
    # ======================
    
    def construction_derive(self, public_key: Dict, metadata: Optional[Dict] = None) -> Dict:
        """
        Call the /construction/derive endpoint to derive an address.
        
        Args:
            public_key: Public key to derive address from
            metadata: Optional metadata for derivation
            
        Returns:
            Dictionary containing derived address
        """
        url = f"{self.endpoint}/construction/derive"
        payload = {
            "network_identifier": self.network_identifier,
            "public_key": public_key,
            "metadata": metadata or {}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/derive")
            
    def construction_preprocess(
        self, 
        operations: List[Dict], 
        metadata: Optional[Dict] = None,
        public_keys: Optional[List[Dict]] = None
    ) -> Dict:
        """
        Call the /construction/preprocess endpoint.
        
        Args:
            operations: List of operations for the transaction
            metadata: Optional metadata for preprocessing
            public_keys: Optional list of public keys involved
            
        Returns:
            Dictionary containing options for metadata endpoint
        """
        url = f"{self.endpoint}/construction/preprocess"
        payload = {
            "network_identifier": self.network_identifier,
            "operations": operations,
            "metadata": metadata or {}
        }
        
        if public_keys:
            payload["public_keys"] = public_keys
            
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/preprocess")
            
    def construction_metadata(
        self, 
        options: Dict, 
        public_keys: Optional[List[Dict]] = None
    ) -> Dict:
        """
        Call the /construction/metadata endpoint.
        
        Args:
            options: Options from preprocess response
            public_keys: Optional list of public keys involved
            
        Returns:
            Dictionary containing metadata for transaction construction
        """
        url = f"{self.endpoint}/construction/metadata"
        payload = {
            "network_identifier": self.network_identifier,
            "options": options
        }
        
        if public_keys:
            payload["public_keys"] = public_keys
            
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/metadata")
            
    def construction_payloads(
        self, 
        operations: List[Dict], 
        metadata: Optional[Dict] = None,
        public_keys: Optional[List[Dict]] = None
    ) -> Dict:
        """
        Call the /construction/payloads endpoint.
        
        Args:
            operations: List of operations for the transaction
            metadata: Optional metadata from metadata endpoint
            public_keys: Optional list of public keys involved
            
        Returns:
            Dictionary containing unsigned transaction and payloads
        """
        url = f"{self.endpoint}/construction/payloads"
        payload = {
            "network_identifier": self.network_identifier,
            "operations": operations
        }
        
        if metadata:
            payload["metadata"] = metadata
            
        if public_keys:
            payload["public_keys"] = public_keys
            
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/payloads")
            
    def construction_combine(self, unsigned_transaction: str, signatures: List[Dict]) -> Dict:
        """
        Call the /construction/combine endpoint.
        
        Args:
            unsigned_transaction: Unsigned transaction from payloads endpoint
            signatures: List of signatures
            
        Returns:
            Dictionary containing signed transaction
        """
        url = f"{self.endpoint}/construction/combine"
        payload = {
            "network_identifier": self.network_identifier,
            "unsigned_transaction": unsigned_transaction,
            "signatures": signatures
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/combine")
            
    def construction_parse(self, network_identifier: Dict, signed: bool, transaction: str) -> Dict:
        """
        Parse a transaction blob.

        Args:
            network_identifier: Network identifier dictionary.
            signed: Whether the transaction blob is signed.
            transaction: Hex-encoded transaction blob.

        Returns:
            Parsed transaction details (operations, signers).
        """
        data = {
            "network_identifier": network_identifier,
            "signed": signed,
            "transaction": transaction,
        }
        return self.request_debugger.post(f"{self.endpoint}/construction/parse", json=data, headers=self.headers).json()
            
    def construction_hash(self, signed_transaction: str) -> Dict:
        """
        Call the /construction/hash endpoint.
        
        Args:
            signed_transaction: Signed transaction from combine endpoint
            
        Returns:
            Dictionary containing transaction hash
        """
        url = f"{self.endpoint}/construction/hash"
        payload = {
            "network_identifier": self.network_identifier,
            "signed_transaction": signed_transaction
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/hash")
            
    def construction_submit(self, signed_transaction: str) -> Dict:
        """
        Call the /construction/submit endpoint.
        
        Args:
            signed_transaction: Signed transaction from combine endpoint
            
        Returns:
            Dictionary containing submission result
        """
        url = f"{self.endpoint}/construction/submit"
        payload = {
            "network_identifier": self.network_identifier,
            "signed_transaction": signed_transaction
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, "construction/submit")
            
    # =================
    # Search Endpoints
    # =================
    
    def search_transactions(self, transaction_hash: str) -> Dict:
        """
        Call the /search/transactions endpoint.
        
        Args:
            transaction_hash: Transaction hash to search for
            
        Returns:
            Dictionary containing search results
        """
        url = f"{self.endpoint}/search/transactions"
        payload = {
            "network_identifier": self.network_identifier,
            "transaction_identifier": {"hash": transaction_hash}
        }
        
        try:
            response = self.request_debugger.post(url, json=payload, headers=self.headers)
            return response.json()
        except Exception as e:
            self._handle_request_error(e, f"search/transactions for {transaction_hash}")

    # =================
    # Utility Methods
    # =================
    
    def get_utxos(self, address: str) -> List[Dict]:
        """
        Get all UTXOs for an address.
        
        Args:
            address: Address to get UTXOs for
            
        Returns:
            List of UTXO dictionaries
        """
        try:
            result = self.account_coins(address)
            return result.get("coins", [])
        except Exception as e:
            logger.error(f"Error getting UTXOs for {address}: {str(e)}")
            raise
            
    def get_ada_balance(self, address: str) -> int:
        """
        Get the ADA balance in lovelace.
        
        Args:
            address: Address to get balance for
            
        Returns:
            Balance in lovelace (int)
            
        Raises:
            ValueError: If balance cannot be determined
        """
        try:
            balance_data = self.account_balance(address)
            for balance in balance_data.get("balances", []):
                currency = balance.get("currency", {})
                if currency.get("symbol") == "ADA" and "value" in balance:
                    return int(balance["value"])
                    
            # If we get here, we didn't find the ADA balance
            raise ValueError(f"ADA balance not found in response for {address}")
        except Exception as e:
            logger.error(f"Error getting ADA balance for {address}: {str(e)}")
            raise 