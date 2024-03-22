package org.cardanofoundation.rosetta.api.block.service;

import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;

public interface BlockService {

  Block findBlock(Long index, String hash);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);

}
