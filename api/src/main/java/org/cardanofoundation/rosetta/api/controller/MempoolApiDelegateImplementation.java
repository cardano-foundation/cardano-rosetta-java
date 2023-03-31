package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MempoolApiDelegateImplementation implements MempoolApiDelegate {
    @Override
    public ResponseEntity<MempoolResponse> mempool(NetworkRequest networkRequest) {
        return MempoolApiDelegate.super.mempool(networkRequest);
    }

    @Override
    public ResponseEntity<MempoolTransactionResponse> mempoolTransaction(MempoolTransactionRequest mempoolTransactionRequest) {
        return MempoolApiDelegate.super.mempoolTransaction(mempoolTransactionRequest);
    }
}
