package org.cardanofoundation.rosetta.api.block.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.BlockApi;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@RestController
@RequiredArgsConstructor
public class BlockApiImpl implements BlockApi {

  private final BlockService blockService;
  private final NetworkService networkService;

  private final BlockMapper mapper;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public ResponseEntity<BlockResponse> block(@RequestBody BlockRequest blockRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    networkService.verifyNetworkRequest(blockRequest.getNetworkIdentifier());

    PartialBlockIdentifier bid = blockRequest.getBlockIdentifier();
    String hash = bid.getHash();
    Long index = bid.getIndex();

    Block block = blockService.findBlock(index, hash);

    return ResponseEntity.ok(mapper.mapToBlockResponse(block));
  }

  @Override
  public ResponseEntity<BlockTransactionResponse> blockTransaction(
      @RequestBody BlockTransactionRequest blockReq) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    networkService.verifyNetworkRequest(blockReq.getNetworkIdentifier());

    Long blockId = blockReq.getBlockIdentifier().getIndex();
    String blockHash = blockReq.getBlockIdentifier().getHash();
    String txHash = blockReq.getTransactionIdentifier().getHash();

    BlockTx blockTx = blockService.getBlockTransaction(blockId, blockHash, txHash);

    return ResponseEntity.ok(mapper.mapToBlockTransactionResponse(blockTx));
  }

}
