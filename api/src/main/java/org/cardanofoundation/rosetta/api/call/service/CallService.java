package org.cardanofoundation.rosetta.api.call.service;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;

import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

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
     * Get a list of supported method names for the /call endpoint
     */
    List<String> getSupportedMethods();

}
