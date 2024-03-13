package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;

import org.cardanofoundation.rosetta.api.block.model.dto.TransactionDto;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.openapitools.client.model.*;

public interface BlockService {

  BlockDto findBlock(Long number, String hash);

  BlockResponse getBlockByBlockRequest(BlockRequest blockRequest);

  List<TransactionDto> findTransactionsByBlock(BlockDto block);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);

}
