package org.cardanofoundation.rosetta.api.call.service;

import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;

import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface CallService {

    /**
     * Process a call request and return the appropriate response
     */
    CallResponse processCallRequest(CallRequest callRequest);
    
    /**
     * Get parse error blocks with optional status filter
     */
    CallResponse getParseErrorBlocks(@Nullable ReviewStatus status);
    
    /**
     * Mark all parse error blocks for a specific block number as checked
     */
    CallResponse markParseErrorBlockChecked(Map<String, Object> params);

    /**
     * Resolve a supported Cardano address to its configured CIP-113 smart-wallet address
     */
    @NotNull CallResponse resolveSmartWalletAddress(@NotNull CallRequest callRequest);

    /**
     * Get a list of supported method names for the /call endpoint
     */
    List<String> getSupportedMethods();

}
