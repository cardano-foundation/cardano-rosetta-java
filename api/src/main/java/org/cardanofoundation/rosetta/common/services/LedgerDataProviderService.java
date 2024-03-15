package org.cardanofoundation.rosetta.common.services;


import java.util.List;

import org.openapitools.client.model.Currency;

import org.cardanofoundation.rosetta.api.account.model.dto.AddressBalanceDTO;
import org.cardanofoundation.rosetta.api.account.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {

    GenesisBlock findGenesisBlock();

    Block findBlock(Long number, String hash);

    List<AddressBalanceDTO> findBalanceByAddressAndBlock(String address, Long number);

    List<UtxoDto> findUtxoByAddressAndCurrency(String address, List<Currency> currencies);
    List<StakeAddressBalance> findStakeAddressBalanceByAddressAndBlock(String address, Long number);

    Long findLatestBlockNumber();

    ProtocolParams findProtocolParametersFromIndexer();

    Block findLatestBlock();

  List<Transaction> findTransactionsByBlock(Long number, String hash);

    ProtocolParams findProtolParametersFromConfig();

    ProtocolParams findProtocolParametersFromIndexerAndConfig();

}
