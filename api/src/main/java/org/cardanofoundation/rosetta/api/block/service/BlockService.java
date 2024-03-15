package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;

import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.openapitools.client.model.*;

public interface BlockService {

  AccountBalanceResponse findBalanceDataByAddressAndBlock(String address,
                                                          Long number,
                                                          String hash);

  Block findBlock(Long number, String hash);


  BlockResponse getBlockByBlockRequest(BlockRequest blockRequest);

//  List<PopulatedTransaction> fillTransactions(List<TransactionDto> transactions);

  List<Transaction> findTransactionsByBlock(Block block);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);

}
