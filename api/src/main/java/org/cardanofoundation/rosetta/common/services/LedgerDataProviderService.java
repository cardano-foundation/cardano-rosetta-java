package org.cardanofoundation.rosetta.common.services;


import java.util.List;

import org.cardanofoundation.rosetta.api.account.model.dto.AddressBalanceDTO;
import org.cardanofoundation.rosetta.api.account.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.block.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.block.model.dto.StakeAddressBalanceDTO;
import org.cardanofoundation.rosetta.api.block.model.dto.TransactionDto;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
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

    ProtocolParams findProtocolParametersFromIndexer();

    BlockDto findLatestBlock();

  List<TransactionDto> findTransactionsByBlock(Long number, String hash);

    ProtocolParams findProtolParametersFromConfig();

    ProtocolParams findProtocolParametersFromIndexerAndConfig();

}
