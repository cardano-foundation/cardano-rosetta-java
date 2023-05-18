package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;

import org.cardanofoundation.rosetta.crawler.projection.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.GenesisBlockDto;


/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {
    BlockIdentifier getTip(final String networkId);

    GenesisBlockDto findGenesisBlock();

    BlockDto findBlock(Long number, String hash);

    Double findBalanceByAddressAndBlock(String address, String hash);

    Long findLatestBlockNumber();

}
