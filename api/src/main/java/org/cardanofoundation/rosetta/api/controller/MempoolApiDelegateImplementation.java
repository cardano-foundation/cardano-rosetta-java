package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class MempoolApiDelegateImplementation implements MempoolApiDelegate {
    @Override
    public ResponseEntity<MempoolResponse> mempool(NetworkRequest networkRequest) {
        return null;
    }

    @Override
    public ResponseEntity<MempoolTransactionResponse> mempoolTransaction(
        MempoolTransactionRequest mempoolTransactionRequest) {
        return null;
    }
}
