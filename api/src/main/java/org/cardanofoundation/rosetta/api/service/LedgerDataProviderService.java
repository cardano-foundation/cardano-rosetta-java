package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.ProtocolParametersResponse;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;


import java.util.List;
import java.util.Map;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {
    BlockIdentifier getTip(final String networkId);

    GenesisBlockDto findGenesisBlock();

    BlockDto findBlock(Long number, String hash);

//    Long findBalanceByAddressAndBlock(String address, String hash);

//    List<Utxo> findUtxoByAddressAndBlock(String address, String hash, List<Currency> currencies);

    Long findLatestBlockNumber();

//    ProtocolParameters findProtocolParameters();

//    List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash);

    BlockDto findLatestBlock();

//  List<TransactionDto> findTransactionsByBlock(Long number, String hash);
//
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
