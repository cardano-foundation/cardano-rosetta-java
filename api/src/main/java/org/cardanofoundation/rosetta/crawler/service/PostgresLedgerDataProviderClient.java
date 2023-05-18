package org.cardanofoundation.rosetta.crawler.service;

import lombok.Builder;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;

@Builder
public class PostgresLedgerDataProviderClient {
    private final String networkId;

    public BlockIdentifier getTip() {
        return null;
    }
}
