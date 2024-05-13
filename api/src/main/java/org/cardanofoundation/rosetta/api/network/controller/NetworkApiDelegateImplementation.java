package org.cardanofoundation.rosetta.api.network.controller;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;

import org.cardanofoundation.rosetta.api.network.service.NetworkService;

@RestController
@RequiredArgsConstructor
public class NetworkApiDelegateImplementation implements NetworkApiDelegate {

  private final NetworkService networkService;

  @Override
  public ResponseEntity<NetworkListResponse> networkList(
      @RequestBody MetadataRequest metadataRequest) throws IOException {
    final NetworkListResponse networkListResponse = networkService.getNetworkList(metadataRequest);
    return ResponseEntity.ok(networkListResponse);
  }

  @Override
  public ResponseEntity<NetworkOptionsResponse> networkOptions(
      @RequestBody NetworkRequest networkRequest) throws IOException, InterruptedException {
    networkService.verifyNetworkRequest(networkRequest.getNetworkIdentifier());
    final NetworkOptionsResponse networkOptionsResponse = networkService.getNetworkOptions(
        networkRequest);
    return ResponseEntity.ok(networkOptionsResponse);
  }

  @Override
  public ResponseEntity<NetworkStatusResponse> networkStatus(
      @RequestBody NetworkRequest networkRequest) throws IOException {
    networkService.verifyNetworkRequest(networkRequest.getNetworkIdentifier());
    final NetworkStatusResponse networkStatusResponse = networkService.getNetworkStatus(
        networkRequest);
    return ResponseEntity.ok(networkStatusResponse);
  }
}
