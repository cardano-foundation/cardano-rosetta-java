package org.cardanofoundation.rosetta.api.controller;

import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.exception.ServerException;
import org.cardanofoundation.rosetta.api.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.api.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class NetworkApiDelegateImplementation implements NetworkApiDelegate {

    @Autowired
    private NetworkService networkService;

    @Override
    public ResponseEntity<NetworkListResponse> networkList( @RequestBody MetadataRequest metadataRequest)
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
        throws ServerException, IOException {
        final NetworkStatusResponse networkStatusResponse = networkService.getNetworkStatus(networkRequest);
        return ResponseEntity.ok(networkStatusResponse);
    }
}
