package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.Operation;

import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for retrieving token metadata from the token registry.
 * Provides normalized token registry currency data for use in the domain layer.
 * Designed to encourage bulk operations for efficiency.
 */
public interface TokenRegistryService {

    /**
     * Get token metadata for multiple assets using batch request
     * @param assetFingerprints Set of Asset objects containing policyId and optional assetName
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata (always returns at least policyId)
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> getTokenMetadataBatch(@NotNull Set<AssetFingerprint> assetFingerprints);

    /**
     * Extract all native token assets from a BlockTx (inputs and outputs)
     * @param blockTx The block transaction to extract assets from
     * @return Set of unique Asset objects found in the transaction
     */
    Set<AssetFingerprint> extractAssetsFromBlockTx(@NonNull BlockTx blockTx);

    /**
     * Extract all native token assets from a list of BlockTransaction objects
     * @param transactions List of BlockTransaction objects from search results
     * @return Set of unique Asset objects found across all transactions
     */
    Set<AssetFingerprint> extractAssetsFromBlockTransactions(@NotNull List<BlockTransaction> transactions);
    
    /**
     * Extract assets from a list of Amt objects (utility method)
     * @param amounts List of amounts potentially containing native tokens
     * @return Set of unique Asset objects (excludes ADA/lovelace)
     */
    Set<AssetFingerprint> extractAssetsFromAmounts(@NonNull List<Amt> amounts);
    
    /**
     * Extract assets from a list of Operation objects
     * @param operations List of operations potentially containing native tokens
     * @return Set of unique Asset objects found in operation amounts
     */
    Set<AssetFingerprint> extractAssetsFromOperations(@NotNull List<Operation> operations);
    
    /**
     * Convenience method to extract assets and fetch metadata for a BlockTx in one call
     * @param blockTx The block transaction to process
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTx(@NotNull BlockTx blockTx);

    /**
     * Convenience method to extract assets and fetch metadata for BlockTransactions in one call
     * @param transactions List of BlockTransaction objects from search results
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTransactions(@NotNull List<BlockTransaction> transactions);

    /**
     * Convenience method to extract assets and fetch metadata for a list of BlockTx objects in one call.
     * This method handles the common pattern of processing multiple transactions and fetching their metadata
     * in a single batch call for optimal performance.
     *
     * @param blockTxList List of BlockTx objects to process
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata (empty map if no native tokens)
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTxList(@NotNull List<BlockTx> blockTxList);

    /**
     * Extract all native token assets from AddressBalance list and fetch metadata in a single batch call
     * @param balances List of address balances potentially containing native tokens
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForAddressBalances(@NotNull List<AddressBalance> balances);

    /**
     * Extract all native token assets from UTXO list and fetch metadata in a single batch call
     * @param utxos List of UTXOs potentially containing native tokens
     * @return Map of Asset -> TokenRegistryCurrencyData with metadata
     */
    Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForUtxos(@NotNull List<Utxo> utxos);

}
