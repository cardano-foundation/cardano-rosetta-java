package org.cardanofoundation.rosetta.api.call.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.call.service.CallService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.api.CallApi;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CallApiImplementation implements CallApi {

    private final CallService callService;
    private final NetworkService networkService;

    @Value("${cardano.rosetta.OFFLINE_MODE}")
    private boolean offlineMode;

    @Override
    public ResponseEntity<CallResponse> call(CallRequest callRequest) {
        if (offlineMode) {
            throw ExceptionFactory.notSupportedInOfflineMode();
        }

        networkService.verifyNetworkRequest(callRequest.getNetworkIdentifier());

        log.info("Received call request for method: {}", callRequest.getMethod());

        CallResponse response = callService.processCallRequest(callRequest);

        return ResponseEntity.ok(response);
    }

}