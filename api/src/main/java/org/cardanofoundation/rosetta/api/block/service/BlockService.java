package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;

import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;

public interface BlockService {

  Block findBlock(Long index, String hash);


  List<Transaction> findTransactionsByBlock(Block block);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);

}
