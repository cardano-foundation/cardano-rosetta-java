package org.cardanofoundation.rosetta.common.services;


import java.util.List;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.openapitools.client.model.Currency;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {

    GenesisBlock findGenesisBlock();

  /**
   * Returns a block by its number and hash. Including all populated Transactions.
   * @param number
   * @param hash
   * @return
   */
    Block findBlock(Long number, String hash);

    List<AddressBalance> findBalanceByAddressAndBlock(String address, Long number);

    List<Utxo> findUtxoByAddressAndCurrency(String address, List<Currency> currencies);
    List<StakeAddressBalance> findStakeAddressBalanceByAddressAndBlock(String address, Long number);

    Long findLatestBlockNumber();

    ProtocolParams findProtocolParametersFromIndexer();

    Block findLatestBlock();

  /**
   * Returns a list of all transactions within a block. The UTXO aren't populated yet. They contain only the hash and the index.
   * @param number
   * @param hash
   * @return
   */
  List<Transaction> findTransactionsByBlock(Long number, String hash);

    ProtocolParams findProtolParametersFromConfig();

    ProtocolParams findProtocolParametersFromIndexerAndConfig();

}
