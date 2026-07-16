package org.cardanofoundation.rosetta.api.block.controller;

import lombok.RequiredArgsConstructor;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.BlockApi;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BlockApiImpl implements BlockApi {

  private final BlockService blockService;
  private final NetworkService networkService;
  private final TokenRegistryService tokenRegistryService;

  private final BlockMapper mapper;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public ResponseEntity<BlockResponse> block(@RequestBody BlockRequest blockRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }

    if (blockRequest.getBlockIdentifier().getIndex() != null && blockRequest.getBlockIdentifier().getIndex() < 0) {
      throw ExceptionFactory.invalidBlockIdentifier(blockRequest.getBlockIdentifier().getIndex());
    }

    networkService.verifyNetworkRequest(blockRequest.getNetworkIdentifier());

    PartialBlockIdentifier bid = blockRequest.getBlockIdentifier();
    String hash = bid.getHash();
    Long index = bid.getIndex();

    Block block = blockService.findBlock(index, hash);

    // Make single batch call to fetch all token metadata for all transactions in this block (empty map if no native tokens)
    Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = tokenRegistryService.fetchMetadataForBlockTxList(block.getTransactions());

    // Always use metadata version - downstream code won't lookup from empty map if no native tokens
    return ResponseEntity.ok(mapper.mapToBlockResponseWithMetadata(block, metadataMap));
  }

  @Override
  public ResponseEntity<BlockTransactionResponse> blockTransaction(
          @RequestBody BlockTransactionRequest blockReq) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    if (blockReq.getBlockIdentifier().getIndex() != null && blockReq.getBlockIdentifier().getIndex() < 0) {
      throw ExceptionFactory.invalidBlockIdentifier(blockReq.getBlockIdentifier().getIndex());
    }

    networkService.verifyNetworkRequest(blockReq.getNetworkIdentifier());

    Long blockId = blockReq.getBlockIdentifier().getIndex();
    String blockHash = blockReq.getBlockIdentifier().getHash();
    String txHash = blockReq.getTransactionIdentifier().getHash();

    BlockTx blockTx = blockService.getBlockTransaction(blockId, blockHash, txHash);

    // Make single batch call to fetch all token metadata for this transaction (empty map if no native tokens)
    Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = tokenRegistryService.fetchMetadataForBlockTx(blockTx);

    // Always use metadata version - downstream code won't lookup from empty map if no native tokens
    return ResponseEntity.ok(mapper.mapToBlockTransactionResponseWithMetadata(blockTx, metadataMap));
  }

}
