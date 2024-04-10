package org.cardanofoundation.rosetta.api.block.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.BlockApi;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.mapper.BlockTxToBlockTxResponse;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

@RestController
@RequiredArgsConstructor
public class BlockApiImpl implements BlockApi {

  private final BlockService blockService;
  private final ProtocolParamService protocolParamService;

  private final BlockToBlockResponse mapperToBlockResponse;
  private final BlockTxToBlockTxResponse mapperToBlockTxResponse;

  @Override
  public ResponseEntity<BlockResponse> block(@RequestBody BlockRequest blockRequest) {

    PartialBlockIdentifier bid = blockRequest.getBlockIdentifier();
    String hash = bid.getHash();
    Long index = bid.getIndex();

    Block block = blockService.findBlock(index, hash);

    return ResponseEntity.ok(mapperToBlockResponse.toDto(block));
  }

  @Override
  public ResponseEntity<BlockTransactionResponse> blockTransaction(
      @RequestBody BlockTransactionRequest blockReq) {

    Long blockId = blockReq.getBlockIdentifier().getIndex();
    String blockHash = blockReq.getBlockIdentifier().getHash();
    String txHash = blockReq.getTransactionIdentifier().getHash();

    BlockTx tx = blockService.getBlockTransaction(blockId, blockHash, txHash);
    String poolDeposit = String.valueOf(protocolParamService.getProtocolParameters().getPoolDeposit());

    return ResponseEntity.ok(mapperToBlockTxResponse.toDto(tx, poolDeposit));

  }
}

