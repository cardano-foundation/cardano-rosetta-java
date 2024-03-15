package org.cardanofoundation.rosetta.api.block.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.openapitools.client.api.BlockApi;
import org.openapitools.client.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

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

    BlockDto block = blockService.findBlock(index, hash);
    
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

