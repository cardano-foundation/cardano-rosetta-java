package org.cardanofoundation.rosetta.crawler.service;

import java.util.List;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.Currency;
import org.cardanofoundation.rosetta.crawler.model.rest.MaBalance;
import org.cardanofoundation.rosetta.crawler.model.rest.Utxo;
import org.cardanofoundation.rosetta.crawler.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.dto.GenesisBlockDto;

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

  List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash);

  BlockDto findLatestBlock();
}
