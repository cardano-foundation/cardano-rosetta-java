package org.cardanofoundation.rosetta.api.block.controller;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BlockApiDelegateImplementation implements BlockApiDelegate {

  private final BlockService blockService;

  @Override
  public ResponseEntity<BlockResponse> block(
      @RequestBody BlockRequest blockRequest) {
    return ResponseEntity.ok(blockService.getBlockByBlockRequest(blockRequest));
  }

  @Override
  public ResponseEntity<BlockTransactionResponse> blockTransaction(
      @RequestBody BlockTransactionRequest blockTransactionRequest) {
    return ResponseEntity.ok(blockService.getBlockTransaction(blockTransactionRequest));
  }
}

