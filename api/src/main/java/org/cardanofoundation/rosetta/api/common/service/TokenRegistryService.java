package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.openapitools.client.model.CurrencyMetadataResponse;

import java.util.Map;
import java.util.Set;

/**
 * Service for retrieving token metadata from the token registry.
 * Provides normalized currency metadata responses for use in the API layer.
 * Designed to encourage bulk operations for efficiency.
 */
public interface TokenRegistryService {

    /**
     * Get token metadata for multiple assets using batch request
     * @param assets Set of Asset objects containing policyId and optional assetName
     * @return Map of Asset -> CurrencyMetadataResponse with metadata (always returns at least policyId)
     */
    Map<Asset, CurrencyMetadataResponse> getTokenMetadataBatch(Set<Asset> assets);

}
