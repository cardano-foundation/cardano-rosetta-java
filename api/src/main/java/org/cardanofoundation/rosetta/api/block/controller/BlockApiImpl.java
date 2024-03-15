package org.cardanofoundation.rosetta.api.block.controller;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import org.openapitools.client.api.BlockApi;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.service.BlockService;

@RestController
@RequiredArgsConstructor
public class BlockApiImpl implements BlockApi {

  private final BlockService blockService;

  private final BlockToBlockResponse mapper;

  @Override
  public ResponseEntity<BlockResponse> block(@RequestBody BlockRequest blockRequest) {

    PartialBlockIdentifier bid = blockRequest.getBlockIdentifier();
    String hash = bid.getHash();
    Long index = bid.getIndex();

    val block = blockService.findBlock(index, hash);

    return ResponseEntity.ok(mapper.toDto(block));
  }

  @Override
  public ResponseEntity<BlockTransactionResponse> blockTransaction(
      @RequestBody BlockTransactionRequest blockTransactionRequest) {
    return ResponseEntity.ok(blockService.getBlockTransaction(blockTransactionRequest));
  }

  @Override
  public Optional<NativeWebRequest> getRequest() {
    throw new UnsupportedOperationException("TODO implement");
  }
}

