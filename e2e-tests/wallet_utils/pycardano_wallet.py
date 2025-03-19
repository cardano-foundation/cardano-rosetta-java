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
)
from mnemonic import Mnemonic
import cbor2
import logging

logger = logging.getLogger(__name__)


class PyCardanoWallet:
    """Wrapper for PyCardano wallet operations"""

    def __init__(self, network: str = "testnet"):
        self.network = Network.TESTNET if network == "testnet" else Network.MAINNET
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
            network: Network to use ("testnet" or "mainnet")
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

    # @classmethod
    # def create_test_wallet(cls) -> 'PyCardanoWallet':
    #     """Create a new wallet for testing purposes"""
    #     wallet = cls()
    #     wallet.payment_signing_key = PaymentSigningKey.generate()
    #     wallet.payment_verification_key = PaymentVerificationKey.from_signing_key(
    #         wallet.payment_signing_key
    #     )
    #     wallet.address = Address(
    #         payment_part=wallet.payment_verification_key.hash(),
    #         network=wallet.network
    #     )
    #     return wallet

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
        """Get the stake address in bech32 format.

        Returns:
            Stake address as string

        Raises:
            ValueError: If wallet not initialized
        """
        if not self.stake_verification_key:
            raise ValueError("Wallet not initialized or stake key not available")

        # Create a stake-only address
        stake_address = Address(
            staking_part=self.stake_verification_key.hash(), network=self.network
        )

        return str(stake_address)

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

    def select_utxos(
        self,
        rosetta_client,
        min_ada_required: Optional[int] = None,
        required_assets: Optional[List[Dict]] = None,
        exclude_utxos: Optional[List[str]] = None,
    ) -> List[Dict]:
        """
        Select UTXOs that satisfy the given requirements.

        Args:
            rosetta_client: RosettaClient instance to fetch UTXOs
            min_ada_required: Minimum ADA amount required (in lovelace)
            required_assets: List of assets that must be present in the UTXO(s)
                           Format: [{"policy_id": "...", "asset_name": "...", "amount": int}]
            exclude_utxos: List of UTXO identifiers to exclude (e.g. being used in another tx)

        Returns:
            List of UTXOs that satisfy the requirements

        Raises:
            ValueError: If no suitable UTXOs found or if wallet not initialized
        """
        if not self.address:
            raise ValueError("Wallet not initialized")

        # Get all UTXOs
        utxos = rosetta_client.get_utxos(str(self.address))
        if not utxos:
            raise ValueError(f"No UTXOs found for address {self.address}")

        # Filter out excluded UTXOs
        if exclude_utxos:
            utxos = [
                utxo
                for utxo in utxos
                if utxo["coin_identifier"]["identifier"] not in exclude_utxos
            ]

        # Filter UTXOs based on requirements
        suitable_utxos = []
        for utxo in utxos:
            # Check minimum ADA requirement
            ada_amount = int(utxo["amount"]["value"])
            if min_ada_required and ada_amount < min_ada_required:
                continue

            # Check for required assets if specified
            if required_assets:
                has_all_assets = True
                utxo_assets = utxo.get("metadata", {}).get("assets", [])

                for required in required_assets:
                    asset_found = False
                    for asset in utxo_assets:
                        if (
                            asset.get("policy_id") == required["policy_id"]
                            and asset.get("asset_name") == required["asset_name"]
                            and int(asset.get("amount", 0)) >= required["amount"]
                        ):
                            asset_found = True
                            break
                    if not asset_found:
                        has_all_assets = False
                        break

                if not has_all_assets:
                    continue

            suitable_utxos.append(utxo)

        if not suitable_utxos:
            requirements = []
            if min_ada_required:
                requirements.append(f"min {min_ada_required} lovelace")
            if required_assets:
                requirements.append(f"assets: {required_assets}")
            raise ValueError(
                f"No suitable UTXOs found matching requirements: {', '.join(requirements)}"
            )

        return suitable_utxos

    def select_ada_only_utxo(
        self, rosetta_client, min_amount: int, exclude_utxos: Optional[List[str]] = None
    ) -> Dict:
        """
        Select a single UTXO that contains only ADA (no native assets)
        with at least the specified amount.

        Args:
            rosetta_client: RosettaClient instance to fetch UTXOs
            min_amount: Minimum ADA amount required (in lovelace)
            exclude_utxos: List of UTXO identifiers to exclude

        Returns:
            A single UTXO that satisfies the requirements

        Raises:
            ValueError: If no suitable UTXO found
        """
        utxos = self.select_utxos(
            rosetta_client=rosetta_client,
            min_ada_required=min_amount,
            exclude_utxos=exclude_utxos,
        )

        # Further filter to find UTXOs with only ADA (no other assets)
        # Any UTXO with metadata likely contains native assets
        ada_only_utxos = [utxo for utxo in utxos if "metadata" not in utxo]

        if not ada_only_utxos:
            raise ValueError(
                f"No UTXOs found with only ADA and minimum {min_amount} lovelace"
            )

        # Return the UTXO with the smallest amount that meets the requirement
        # This helps prevent UTXO fragmentation
        return min(ada_only_utxos, key=lambda u: int(u["amount"]["value"]))

    def select_multiple_ada_utxos(
        self,
        rosetta_client,
        num_utxos: int,
        min_total_amount: int = 0,
        exclude_utxos: Optional[List[str]] = None,
    ) -> List[Dict]:
        """
        Select multiple ADA-only UTXOs (no native assets).

        Args:
            rosetta_client: RosettaClient instance to fetch UTXOs
            num_utxos: Number of UTXOs to select
            min_total_amount: Minimum total ADA amount required (in lovelace)
            exclude_utxos: List of UTXO identifiers to exclude

        Returns:
            List of UTXOs that satisfy the requirements

        Raises:
            ValueError: If not enough suitable UTXOs found
        """
        all_utxos = self.select_utxos(
            rosetta_client=rosetta_client,
            min_ada_required=None,  # We'll filter by total amount later
            exclude_utxos=exclude_utxos,
        )

        # Filter to ADA-only UTXOs - any UTXO with metadata likely contains native assets
        ada_only_utxos = [utxo for utxo in all_utxos if "metadata" not in utxo]

        if len(ada_only_utxos) == 0:
            raise ValueError("No ADA-only UTXOs available")

        # Now we have multiple selection strategies depending on requirements

        # 1. If we need exactly num_utxos and have enough funds with them:
        #    Sort smallest first and take exactly num_utxos
        # 2. If we need exactly num_utxos but they don't have enough funds:
        #    Sort largest first and take exactly num_utxos
        # 3. If we prioritize meeting the min_total_amount:
        #    Sort largest first and take as few UTXOs as needed to meet min_total_amount

        # Calculate the total amount available in all UTXOs
        total_available = sum(int(utxo["amount"]["value"]) for utxo in ada_only_utxos)
        logger.debug(f"Total available in all UTXOs: {total_available} lovelace")

        if total_available < min_total_amount:
            raise ValueError(
                f"Not enough funds. Total available {total_available} lovelace, need {min_total_amount}"
            )

        # If we have enough UTXOs, try the original method first (smallest UTXOs)
        if len(ada_only_utxos) >= num_utxos:
            # Sort by value to select smallest UTXOs first
            smallest_first = sorted(
                ada_only_utxos, key=lambda u: int(u["amount"]["value"])
            )

            # Select exactly num_utxos
            selected_utxos = smallest_first[:num_utxos]
            total_amount = sum(int(utxo["amount"]["value"]) for utxo in selected_utxos)

            # If the selected UTXOs satisfy the minimum amount, we're done
            if total_amount >= min_total_amount:
                logger.debug(
                    f"Selected {num_utxos} smallest UTXOs with total {total_amount} lovelace"
                )
                return selected_utxos

            # If we need exactly this many UTXOs but they don't have enough value,
            # try with the largest UTXOs instead
            largest_first = sorted(
                ada_only_utxos, key=lambda u: int(u["amount"]["value"]), reverse=True
            )
            selected_utxos = largest_first[:num_utxos]
            total_amount = sum(int(utxo["amount"]["value"]) for utxo in selected_utxos)

            if total_amount >= min_total_amount:
                logger.debug(
                    f"Selected {num_utxos} largest UTXOs with total {total_amount} lovelace"
                )
                return selected_utxos

            # If we still can't meet the requirements with exactly num_utxos,
            # we'll fall through to the alternative strategy below

        # Alternative strategy: Take as few UTXOs as possible to meet min_total_amount,
        # prioritizing meeting the amount requirement over the exact number of UTXOs
        logger.debug(
            f"Using alternative strategy to meet min_total_amount {min_total_amount}"
        )

        # Sort by value (largest first) for more efficient selection
        largest_first = sorted(
            ada_only_utxos, key=lambda u: int(u["amount"]["value"]), reverse=True
        )

        # Take UTXOs until we reach the minimum amount
        selected_utxos = []
        total_amount = 0

        for utxo in largest_first:
            selected_utxos.append(utxo)
            total_amount += int(utxo["amount"]["value"])

            # If we have enough funds and either we have exactly num_utxos
            # or we've exceeded num_utxos but have enough funds
            if total_amount >= min_total_amount:
                if len(selected_utxos) <= num_utxos or num_utxos == 0:
                    logger.debug(
                        f"Selected {len(selected_utxos)} UTXOs with total {total_amount} lovelace"
                    )
                    return selected_utxos
                else:
                    # We've selected more than num_utxos, but we needed them for the amount
                    logger.warning(
                        f"Selected {len(selected_utxos)} UTXOs instead of requested {num_utxos} "
                        f"to meet the minimum amount {min_total_amount}"
                    )
                    return selected_utxos

        # If we get here, we couldn't select UTXOs meeting the requirements
        raise ValueError(
            f"Could not select UTXOs meeting the requirements: "
            f"num_utxos={num_utxos}, min_total_amount={min_total_amount}"
        )
