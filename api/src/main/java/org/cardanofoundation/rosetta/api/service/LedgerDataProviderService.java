package org.cardanofoundation.rosetta.api.service;

import java.util.Map;
import org.cardanofoundation.rosetta.api.construction.data.ProtocolParametersResponse;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;


import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.FindPoolRetirements;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDelegations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDeregistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolOwners;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRegistrationsData;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRelays;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionRegistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionWithdrawals;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsInputs;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsOutputs;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.PopulatedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {
    BlockIdentifier getTip(final String networkId);

    GenesisBlockDto findGenesisBlock();

    BlockDto findBlock(Long number, String hash);

    Double findBalanceByAddressAndBlock(String address, String hash);

    List<Utxo> findUtxoByAddressAndBlock(String address, String hash, List<Currency> currencies);

    Long findLatestBlockNumber();

    ProtocolParameters findProtocolParameters();

    List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash);

    BlockDto findLatestBlock();

  List<TransactionDto> findTransactionsByBlock(Long number, String hash);

  List<PopulatedTransaction> fillTransaction(List<TransactionDto> transactions);

  List<PopulatedTransaction> populateTransactions(
      Map<String, PopulatedTransaction> transactionsMap);


  PopulatedTransaction findTransactionByHashAndBlock(String transactionHash, Long blockNumber, String blockHash);
}
