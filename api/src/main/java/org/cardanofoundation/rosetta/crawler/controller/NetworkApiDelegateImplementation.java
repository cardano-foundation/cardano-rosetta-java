package org.cardanofoundation.rosetta.crawler.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.crawler.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.crawler.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class NetworkApiDelegateImplementation implements NetworkApiDelegate {

    @Autowired
    private NetworkService networkService;

    @Override
    public ResponseEntity<NetworkListResponse> networkList(MetadataRequest metadataRequest) {
        final NetworkListResponse networkListResponse = networkService.getNetworkList(metadataRequest);
        return ResponseEntity.ok(networkListResponse);
    }

    @Override
    public ResponseEntity<NetworkOptionsResponse> networkOptions(NetworkRequest networkRequest) {
        final NetworkOptionsResponse networkOptionsResponse = networkService.getNetworkOptions(networkRequest);
        return ResponseEntity.ok(networkOptionsResponse);
    }

    @Override
    public ResponseEntity<NetworkStatusResponse> networkStatus(NetworkRequest networkRequest) {
        final NetworkStatusResponse networkStatusResponse = networkService.getNetworkStatus(networkRequest);
        return ResponseEntity.ok(networkStatusResponse);
    }
}
