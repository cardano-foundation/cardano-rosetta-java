package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockResponse;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class BlockApiDelegateImplementation implements BlockApiDelegate {
    @Override
    public ResponseEntity<BlockResponse> block(BlockRequest blockRequest) {
        return null;
    }

    @Override
    public ResponseEntity<BlockTransactionResponse> blockTransaction(BlockTransactionRequest blockTransactionRequest) {
        return null;
    }
}

