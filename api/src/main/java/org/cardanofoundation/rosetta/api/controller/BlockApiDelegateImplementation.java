package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockResponse;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionResponse;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class BlockApiDelegateImplementation implements BlockApiDelegate {
  @Autowired
  BlockService blockService;
    @Override
    public ResponseEntity<BlockResponse> block(@RequestBody BlockRequest blockRequest) {
      BlockResponse response = blockService.getBlockByBlockRequest(blockRequest);
      return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BlockTransactionResponse> blockTransaction(
        @RequestBody BlockTransactionRequest blockTransactionRequest) {
      BlockTransactionResponse response = blockService.getBlockTransaction(blockTransactionRequest);
      return ResponseEntity.ok(response);
    }
}

