package org.cardanofoundation.rosetta.api.network.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.NetworkApi;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@RestController
@RequiredArgsConstructor
public class NetworkApiImpl implements NetworkApi {

  private final NetworkService networkService;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public ResponseEntity<NetworkListResponse> networkList(
          @RequestBody MetadataRequest metadataRequest) {
    final NetworkListResponse networkListResponse = networkService.getNetworkList(metadataRequest);
    return ResponseEntity.ok(networkListResponse);
  }

  @Override
  public ResponseEntity<NetworkOptionsResponse> networkOptions(
          @RequestBody NetworkRequest networkRequest) {
    networkService.verifyNetworkRequest(networkRequest.getNetworkIdentifier());
    final NetworkOptionsResponse networkOptionsResponse = networkService.getNetworkOptions(
            networkRequest);

    return ResponseEntity.ok(networkOptionsResponse);
  }

  @Override
  public ResponseEntity<NetworkStatusResponse> networkStatus(
          @RequestBody NetworkRequest networkRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }

    networkService.verifyNetworkRequest(networkRequest.getNetworkIdentifier());

    final NetworkStatusResponse networkStatusResponse = networkService.getNetworkStatus(
            networkRequest);

    return ResponseEntity.ok(networkStatusResponse);
  }

}
