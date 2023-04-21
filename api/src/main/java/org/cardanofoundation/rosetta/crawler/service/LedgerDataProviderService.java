package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {
    BlockIdentifier getTip(final String networkId);
}
