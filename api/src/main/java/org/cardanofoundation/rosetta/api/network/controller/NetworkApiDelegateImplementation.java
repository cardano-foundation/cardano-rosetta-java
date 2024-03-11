package org.cardanofoundation.rosetta.api.network.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.openapitools.client.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NetworkApiDelegateImplementation implements NetworkApiDelegate {

    private final NetworkService networkService;

    @Override
    public ResponseEntity<NetworkListResponse> networkList(@RequestBody MetadataRequest metadataRequest)
        throws IOException {
        final NetworkListResponse networkListResponse = networkService.getNetworkList(metadataRequest);
        return ResponseEntity.ok(networkListResponse);
    }

    @Override
    public ResponseEntity<NetworkOptionsResponse> networkOptions(@RequestBody NetworkRequest networkRequest)
        throws IOException, InterruptedException {
        final NetworkOptionsResponse networkOptionsResponse = networkService.getNetworkOptions(networkRequest);
        return ResponseEntity.ok(networkOptionsResponse);
    }

    @Override
    public ResponseEntity<NetworkStatusResponse> networkStatus(@RequestBody NetworkRequest networkRequest)
        throws  IOException {
        final NetworkStatusResponse networkStatusResponse = networkService.getNetworkStatus(networkRequest);
        return ResponseEntity.ok(networkStatusResponse);
    }
}
