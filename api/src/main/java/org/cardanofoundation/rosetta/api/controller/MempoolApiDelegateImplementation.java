package org.cardanofoundation.rosetta.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class MempoolApiDelegateImplementation implements MempoolApiDelegate {

  private final MempoolMonitoringService mempoolMonitoringService;

  @Override
  public ResponseEntity<MempoolResponse> mempool(@RequestBody NetworkRequest networkRequest) {
    return ResponseEntity.ok(mempoolMonitoringService.getAllTransaction(networkRequest));
  }

  @Override
  public ResponseEntity<MempoolTransactionResponse> mempoolTransaction(
      MempoolTransactionRequest mempoolTransactionRequest) {
    return null;
  }
}
