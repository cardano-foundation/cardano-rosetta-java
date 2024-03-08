package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.dto.*;


import java.util.List;

import org.cardanofoundation.rosetta.api.model.entity.Amt;
import org.openapitools.client.model.Currency;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {

    GenesisBlockDto findGenesisBlock();

    BlockDto findBlock(Long number, String hash);

    List<AddressBalanceDTO> findBalanceByAddressAndBlock(String address, Long number);

    List<UtxoDto> findUtxoByAddressAndCurrency(String address, List<Currency> currencies);
    List<StakeAddressBalanceDTO> findStakeAddressBalanceByAddressAndBlock(String address, Long number);

    Long findLatestBlockNumber();

//    ProtocolParameters findProtocolParameters();

//    List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash);

    BlockDto findLatestBlock();

  List<TransactionDto> findTransactionsByBlock(Long number, String hash);

//  List<PopulatedTransaction> fillTransaction(List<TransactionDto> transactions);
//
//  List<PopulatedTransaction> populateTransactions(
//      Map<String, PopulatedTransaction> transactionsMap);
//
//
//  PopulatedTransaction findTransactionByHashAndBlock(String transactionHash, Long blockNumber, String blockHash);
//
//  List<FindTransactionsInputs> getFindTransactionsInputs(List<String> transactionsHashes);
//
//  List<FindPoolRetirements> getFindPoolRetirements(List<String> transactionsHashes);
//
//  List<FindTransactionPoolRelays> getFindTransactionPoolRelays(
//      List<String> transactionsHashes);
//
//  List<FindTransactionPoolOwners> getFindTransactionPoolOwners(
//      List<String> transactionsHashes);
//
//  List<FindTransactionPoolRegistrationsData> getTransactionPoolRegistrationsData(
//      List<String> transactionsHashes);
//
//  List<FindTransactionPoolRegistrationsData> getFindTransactionPoolRegistrationsData(
//      List<String> transactionsHashes);
//
//  List<TransactionMetadataDto> getTransactionMetadataDtos(List<String> transactionsHashes);
//
//  List<FindTransactionDelegations> getFindTransactionDelegations(
//      List<String> transactionsHashes);
//
//  List<FindTransactionDeregistrations> getFindTransactionDeregistrations(
//      List<String> transactionsHashes);
//
//  List<FindTransactionRegistrations> getFindTransactionRegistrations(
//      List<String> transactionsHashes);
//
//  List<FindTransactionWithdrawals> getFindTransactionWithdrawals(
//      List<String> transactionsHashes);
//
//  List<FindTransactionsOutputs> getFindTransactionsOutputs(
//      List<String> transactionsHashes);
}
