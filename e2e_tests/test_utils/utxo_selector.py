import logging
from typing import Dict, List, Optional, Any
import random

from e2e_tests.rosetta_client.client import RosettaClient
from e2e_tests.rosetta_client.exceptions import ValidationError

logger = logging.getLogger(__name__)

class UtxoSelector:
    """
    Utility for selecting UTXOs for transactions.
    Provides different strategies for UTXO selection.
    """
    
    @staticmethod
    def select_utxos(
        client: RosettaClient,
        address: str,
        required_amount: int,
        exclude_utxos: Optional[List[str]] = None,
        strategy: str = "single",
        utxo_count: int = 1
    ) -> List[Dict]:
        """
        Select UTXOs from an address based on requirements.
        
        Args:
            client: RosettaClient instance
            address: Address to select UTXOs from
            required_amount: Minimum amount required (in lovelace)
            exclude_utxos: List of UTXO IDs to exclude
            strategy: Selection strategy ('single', 'multiple', 'random', 'consolidate')
            utxo_count: Number of UTXOs to select (for 'multiple' strategy)
            
        Returns:
            List of selected UTXOs
            
        Raises:
            ValidationError: If no suitable UTXOs found
        """
        # Get all UTXOs for the address
        all_utxos = client.get_utxos(address)
        
        if not all_utxos:
            raise ValidationError(f"No UTXOs found for address {address}")
            
        logger.debug(f"Found {len(all_utxos)} UTXOs for address {address}")
        
        # Filter out excluded UTXOs
        if exclude_utxos:
            all_utxos = [utxo for utxo in all_utxos if utxo["coin_identifier"]["identifier"] not in exclude_utxos]
            logger.debug(f"After exclusions: {len(all_utxos)} UTXOs")
            
        # Filter out UTXOs with assets (only use ADA-only UTXOs)
        ada_only_utxos = []
        for utxo in all_utxos:
            has_assets = False
            if "metadata" in utxo and "assets" in utxo["metadata"]:
                has_assets = len(utxo["metadata"]["assets"]) > 0
            if not has_assets:
                ada_only_utxos.append(utxo)
                
        logger.debug(f"Found {len(ada_only_utxos)} ADA-only UTXOs")
        
        if not ada_only_utxos:
            raise ValidationError(f"No ADA-only UTXOs found for address {address}")
            
        # Apply strategy-specific selection
        selected_utxos = []
        
        if strategy == "single":
            # Find a single UTXO with sufficient funds
            selected_utxo = None
            for utxo in sorted(ada_only_utxos, key=lambda u: int(u["amount"]["value"])):
                if int(utxo["amount"]["value"]) >= required_amount:
                    selected_utxo = utxo
                    break
                    
            if not selected_utxo:
                raise ValidationError(
                    f"No single UTXO with at least {required_amount} lovelace found"
                )
                
            selected_utxos = [selected_utxo]
            
        elif strategy == "multiple":
            # Select multiple UTXOs with sufficient total
            sorted_utxos = sorted(ada_only_utxos, key=lambda u: int(u["amount"]["value"]), reverse=True)
            
            # If we need exactly utxo_count UTXOs
            if len(sorted_utxos) >= utxo_count:
                selected_utxos = sorted_utxos[:utxo_count]
                total_amount = sum(int(utxo["amount"]["value"]) for utxo in selected_utxos)
                
                if total_amount < required_amount:
                    # If largest UTXOs don't have enough, try a different approach
                    logger.debug(f"Top {utxo_count} UTXOs have insufficient funds. Selecting by total amount.")
                    selected_utxos = []
                    total_amount = 0
                    
                    for utxo in sorted_utxos:
                        selected_utxos.append(utxo)
                        total_amount += int(utxo["amount"]["value"])
                        if total_amount >= required_amount and len(selected_utxos) >= utxo_count:
                            break
                            
            # If we still don't have enough UTXOs or enough funds
            if len(selected_utxos) < utxo_count or sum(int(utxo["amount"]["value"]) for utxo in selected_utxos) < required_amount:
                # Just get as many as we can to meet the amount
                selected_utxos = []
                total_amount = 0
                
                for utxo in sorted_utxos:
                    selected_utxos.append(utxo)
                    total_amount += int(utxo["amount"]["value"])
                    if total_amount >= required_amount:
                        break
                        
            if not selected_utxos or sum(int(utxo["amount"]["value"]) for utxo in selected_utxos) < required_amount:
                raise ValidationError(
                    f"Cannot find {utxo_count} UTXOs with total amount {required_amount} lovelace"
                )
                
        elif strategy == "random":
            # Randomly select UTXOs until we have enough
            random_utxos = list(ada_only_utxos)  # Create a copy for shuffling
            random.shuffle(random_utxos)
            
            selected_utxos = []
            total_amount = 0
            
            for utxo in random_utxos:
                selected_utxos.append(utxo)
                total_amount += int(utxo["amount"]["value"])
                if total_amount >= required_amount and len(selected_utxos) >= utxo_count:
                    break
                    
            if total_amount < required_amount:
                raise ValidationError(
                    f"Cannot find UTXOs with total amount {required_amount} lovelace"
                )
                
        elif strategy == "consolidate":
            # Select the smallest UTXOs until we have enough (for consolidation)
            sorted_utxos = sorted(ada_only_utxos, key=lambda u: int(u["amount"]["value"]))
            
            selected_utxos = []
            total_amount = 0
            
            # Take as many small UTXOs as possible, up to utxo_count
            for utxo in sorted_utxos:
                selected_utxos.append(utxo)
                total_amount += int(utxo["amount"]["value"])
                if len(selected_utxos) >= utxo_count:
                    break
                    
            if total_amount < required_amount:
                raise ValidationError(
                    f"Cannot find enough small UTXOs with total amount {required_amount} lovelace"
                )
                
        else:
            raise ValueError(f"Unknown UTXO selection strategy: {strategy}")
            
        # Log selected UTXOs
        total_selected = sum(int(utxo["amount"]["value"]) for utxo in selected_utxos)
        logger.debug(f"Selected {len(selected_utxos)} UTXOs with total {total_selected} lovelace")
        
        return selected_utxos 