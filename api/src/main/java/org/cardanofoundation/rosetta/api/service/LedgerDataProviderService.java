package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;

import org.cardanofoundation.rosetta.api.projection.BlockDto;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockDto;


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
