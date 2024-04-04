package org.cardanofoundation.rosetta.api.mempool.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.cardanofoundation.rosetta.api.mempool.service.MempoolService;
import org.openapitools.client.api.MempoolApi;
import org.openapitools.client.model.MempoolResponse;
import org.openapitools.client.model.MempoolTransactionRequest;
import org.openapitools.client.model.MempoolTransactionResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.TransactionIdentifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("mempool")
public class MempoolApiImplementation implements MempoolApi {

  private final MempoolService mempoolService;

  @Override
  public ResponseEntity<MempoolResponse> mempool(NetworkRequest networkRequest) {
    MempoolResponse mempoolResponse = MempoolResponse.builder()
        .transactionIdentifiers(mempoolService.getCurrentTransactionIdentifiers(networkRequest.getNetworkIdentifier().getNetwork()))
        .build();
    return ResponseEntity.ok(mempoolResponse);
  }

  @Override
  public ResponseEntity<MempoolTransactionResponse> mempoolTransaction(
      MempoolTransactionRequest mempoolTransactionRequest) {
    throw new NotImplementedException("Not implemented yet");
  }
}
