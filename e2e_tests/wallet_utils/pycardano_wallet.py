from typing import List, Dict, Optional, Union
from pycardano import (
    Address,
    PaymentVerificationKey,
    PaymentSigningKey,
    PaymentExtendedSigningKey,
    PaymentExtendedVerificationKey,
    StakeExtendedSigningKey,
    StakeExtendedVerificationKey,
    Network,
    HDWallet,
    Transaction,
    TransactionBody,
    TransactionWitnessSet,
    PlutusData,
    Redeemer,
    AuxiliaryData,
    NativeScript,
    VerificationKeyWitness,
    Metadata,
    StakeCredential,
    VerificationKey,
)
from mnemonic import Mnemonic
import cbor2
import logging

logger = logging.getLogger(__name__)


class PyCardanoWallet:
    """Wrapper for PyCardano wallet operations"""

    def __init__(self, network: str = "testnet"):
        # Recognize common testnet names
        testnet_names = ["testnet", "preprod", "preview"]
        if network.lower() in testnet_names:
            self.network = Network.TESTNET
        else:
            self.network = Network.MAINNET
            
        self.payment_signing_key: Optional[PaymentExtendedSigningKey] = None
        self.payment_verification_key: Optional[PaymentExtendedVerificationKey] = None
        self.stake_signing_key: Optional[StakeExtendedSigningKey] = None
        self.stake_verification_key: Optional[StakeExtendedVerificationKey] = None
        self.address: Optional[Address] = None
        self.hd_wallet: Optional[HDWallet] = None

    @classmethod
    def from_mnemonic(
        cls, mnemonic: str, network: str = "testnet", address_type: str = "base"
    ) -> "PyCardanoWallet":
        """Create wallet instance from mnemonic phrase

        Args:
            mnemonic: The mnemonic seed phrase
            network: Network name (e.g., "testnet", "preprod", "mainnet")
            address_type: Type of address to create ("base", "enterprise", or "stake")

        Returns:
            PyCardanoWallet instance
        """
        # Validate mnemonic
        if not Mnemonic("english").check(mnemonic):
            raise ValueError("Invalid mnemonic phrase")

        wallet = cls(network)
        # Create HD wallet from mnemonic
        wallet.hd_wallet = HDWallet.from_mnemonic(mnemonic)

        # Derive payment keys (using path 1852H/1815H/0H/0/0 for Cardano)
        payment_derived_wallet = wallet.hd_wallet.derive_from_path(
            "m/1852'/1815'/0'/0/0"
        )
        wallet.payment_signing_key = PaymentExtendedSigningKey.from_hdwallet(
            payment_derived_wallet
        )
        wallet.payment_verification_key = (
            wallet.payment_signing_key.to_verification_key()
        )

        # Derive stake keys (using path 1852H/1815H/0H/2/0 for Cardano)
        stake_derived_wallet = wallet.hd_wallet.derive_from_path("m/1852'/1815'/0'/2/0")
        wallet.stake_signing_key = StakeExtendedSigningKey.from_hdwallet(
            stake_derived_wallet
        )
        wallet.stake_verification_key = wallet.stake_signing_key.to_verification_key()

        # Create appropriate address type
        if address_type == "base":
            # Base address with both payment and staking capabilities
            wallet.address = Address(
                payment_part=wallet.payment_verification_key.hash(),
                staking_part=wallet.stake_verification_key.hash(),
                network=wallet.network,
            )
        elif address_type == "enterprise":
            # Enterprise address with only payment capabilities
            wallet.address = Address(
                payment_part=wallet.payment_verification_key.hash(),
                network=wallet.network,
            )
        elif address_type == "stake":
            # Stake address with only staking capabilities
            wallet.address = Address(
                staking_part=wallet.stake_verification_key.hash(),
                network=wallet.network,
            )
        else:
            raise ValueError(
                "Invalid address type. Must be one of: base, enterprise, stake"
            )

        return wallet

    def get_test_addresses(self) -> List[str]:
        """Get list of test addresses"""
        if not self.address:
            raise ValueError("Wallet not initialized")
        return [str(self.address)]

    def sign_transaction(self, tx_data: Dict) -> Dict:
        """
        Sign a transaction payload from Rosetta API

        Args:
            tx_data: Transaction data from Rosetta construction/payloads endpoint

        Returns:
            Dictionary containing the signature information required by the /construction/combine endpoint
        """
        if not self.payment_signing_key:
            raise ValueError("No signing key available")

        # Extract signing payload
        signing_payload = tx_data.get("payloads", [{}])[0]

        # Get the hex bytes to sign
        hex_bytes = signing_payload.get("hex_bytes")
        if not hex_bytes:
            raise ValueError("No hex_bytes found in signing payload")

        # Sign the payload
        signature = self.payment_signing_key.sign(bytes.fromhex(hex_bytes))

        # Get the raw verification key bytes (32 bytes)
        # The verification key is the first 32 bytes of the key data
        vkey_bytes = bytes(self.payment_verification_key)[:32]

        logger.debug("Verification key length: %d bytes", len(vkey_bytes))
        logger.debug("Verification key hex: %s", vkey_bytes.hex())

        # Return the signature in the format expected by /construction/combine
        return {
            "signing_payload": signing_payload,
            "public_key": {"hex_bytes": vkey_bytes.hex(), "curve_type": "edwards25519"},
            "signature_type": "ed25519",
            "hex_bytes": signature.hex(),
        }

    def sign_transaction_with_payment_key(self, signing_payload: Dict) -> Dict:
        """
        Sign a transaction payload with the payment key for UTXO spending

        Args:
            signing_payload: Signing payload from Rosetta construction/payloads endpoint

        Returns:
            Dictionary containing the signature information required by the /construction/combine endpoint
        """
        if not self.payment_signing_key:
            raise ValueError("No payment signing key available")

        # Get the hex bytes to sign
        hex_bytes = signing_payload.get("hex_bytes")
        if not hex_bytes:
            raise ValueError("No hex_bytes found in signing payload")

        # Sign the payload with the payment key
        signature = self.payment_signing_key.sign(bytes.fromhex(hex_bytes))

        # Get the raw verification key bytes (32 bytes)
        # The verification key is the first 32 bytes of the key data
        vkey_bytes = bytes(self.payment_verification_key)[:32]

        logger.debug("Payment verification key length: %d bytes", len(vkey_bytes))
        logger.debug("Payment verification key hex: %s", vkey_bytes.hex())

        # Return the signature in the format expected by /construction/combine
        return {
            "signing_payload": signing_payload,
            "public_key": {"hex_bytes": vkey_bytes.hex(), "curve_type": "edwards25519"},
            "signature_type": "ed25519",
            "hex_bytes": signature.hex(),
        }

    def sign_with_stake_key(self, signing_payload: Dict) -> Dict:
        """
        Sign a transaction payload with the stake key for stake operations

        Args:
            signing_payload: Signing payload from Rosetta construction/payloads endpoint

        Returns:
            Dictionary containing the signature information required by the /construction/combine endpoint
        """
        if not self.stake_signing_key:
            raise ValueError("No stake signing key available")

        # Get the hex bytes to sign
        hex_bytes = signing_payload.get("hex_bytes")
        if not hex_bytes:
            raise ValueError("No hex_bytes found in signing payload")

        # Sign the payload with the stake key
        signature = self.stake_signing_key.sign(bytes.fromhex(hex_bytes))

        # Get the raw verification key bytes (32 bytes)
        # The verification key is the first 32 bytes of the key data
        vkey_bytes = bytes(self.stake_verification_key)[:32]

        logger.debug("Stake verification key length: %d bytes", len(vkey_bytes))
        logger.debug("Stake verification key hex: %s", vkey_bytes.hex())

        # Return the signature in the format expected by /construction/combine
        return {
            "signing_payload": signing_payload,
            "public_key": {"hex_bytes": vkey_bytes.hex(), "curve_type": "edwards25519"},
            "signature_type": "ed25519",
            "hex_bytes": signature.hex(),
        }

    def get_address(self) -> str:
        """Get wallet's primary address"""
        if not self.address:
            raise ValueError("Wallet not initialized")
        return str(self.address)

    def get_public_key(self) -> str:
        """Get wallet's public key hex"""
        if not self.payment_verification_key:
            raise ValueError("Wallet not initialized")
        return self.payment_verification_key.to_cbor().hex()

    def get_payment_verification_key_hex(self) -> str:
        """Get the payment verification key as hex bytes.

        Returns:
            Hex string of the payment verification key

        Raises:
            ValueError: If wallet not initialized
        """
        if not self.payment_verification_key:
            raise ValueError("Wallet not initialized or payment key not available")

        # Get the raw verification key bytes (32 bytes)
        vkey_bytes = bytes(self.payment_verification_key)[:32]
        return vkey_bytes.hex()

    def get_stake_verification_key_hex(self) -> str:
        """Get the stake verification key as hex bytes.

        Returns:
            Hex string of the stake verification key

        Raises:
            ValueError: If wallet not initialized
        """
        if not self.stake_verification_key:
            raise ValueError("Wallet not initialized or stake key not available")

        # Get the raw verification key bytes (32 bytes)
        vkey_bytes = bytes(self.stake_verification_key)[:32]
        return vkey_bytes.hex()

    def get_stake_address(self) -> str:
        """Get the stake address (Bech32)."""
        if not self.stake_verification_key:
            raise ValueError("Wallet not initialized or stake key not available")
            
        # Create a stake-only address
        stake_address = Address(
            staking_part=self.stake_verification_key.hash(),
            network=self.network
        )
        
        return str(stake_address)

    def get_stake_key_hash(self) -> str:
        """Get the stake key hash (hex)."""
        if not self.stake_verification_key:
            raise ValueError("Wallet not initialized or stake key not available")
            
        # Get the hash of the stake verification key and convert to hex
        # VerificationKeyHash needs to be converted to bytes first, then to hex
        key_hash = self.stake_verification_key.hash()
        return bytes(key_hash).hex()

    def get_payment_vkey(self) -> VerificationKey:
        """Get the payment verification key."""
        if not self.payment_verification_key:
            raise ValueError("Wallet not initialized")
        return self.payment_verification_key

    def convert_pool_id_to_hex(self, pool_id: str) -> str:
        """Convert a pool ID from bech32 format to hex format.

        Args:
            pool_id: Pool ID in bech32 format (e.g., pool1...)

        Returns:
            Pool ID in hex format

        Raises:
            ValueError: If pool ID format is invalid
        """
        # If already in hex format, return as is
        if pool_id.startswith("0x") or all(
            c in "0123456789abcdefABCDEF" for c in pool_id
        ):
            return pool_id.lower().replace("0x", "")

        # If it's a bech32 pool ID, convert to hex using PyCardano
        if pool_id.startswith("pool1"):
            from pycardano import PoolId

            pool_hash = PoolId.from_bech32(pool_id)
            return bytes(pool_hash).hex()
        else:
            raise ValueError(f"Invalid pool ID format: {pool_id}") 