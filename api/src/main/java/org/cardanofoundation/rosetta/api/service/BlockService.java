package org.cardanofoundation.rosetta.api.service;

import java.util.List;

import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;

public interface BlockService {

  AccountBalanceResponse findBalanceDataByAddressAndBlock(String address,
      Long number,
      String hash);

  BlockDto findBlock(Long number, String hash);


  BlockResponse getBlockByBlockRequest(BlockRequest blockRequest);

//  List<PopulatedTransaction> fillTransactions(List<TransactionDto> transactions);

  List<TransactionDto> findTransactionsByBlock(BlockDto block);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);
}
