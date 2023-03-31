package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerDataProviderService {
    BlockIdentifier getTip(final String networkId);
}
