package org.cardanofoundation.rosetta.api.service;

import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockResponse;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;

public interface BlockService {

  AccountBalanceResponse findBalanceDataByAddressAndBlock(String address,
      Long number,
      String hash);

  //  BlockUtxos findCoinsDataByAddress(String accountAddress, List<Currency> currenciesRequested);
//
//  BlockDto findBlock(Long number, String hash);


  BlockResponse getBlockByBlockRequest(BlockRequest blockRequest);

//  List<PopulatedTransaction> fillTransactions(List<TransactionDto> transactions);

  List<TransactionDto> findTransactionsByBlock(BlockDto block);

  BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest);
}
